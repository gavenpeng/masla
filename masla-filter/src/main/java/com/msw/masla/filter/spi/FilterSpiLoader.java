/*
 * Copyright 2025 msw
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.msw.masla.filter.spi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: Gavin.peng
 * Date: 2024/4/13
 * Description: filter loader by spi support customer identify
 */
public class FilterSpiLoader<I> {

    // Default path for the folder of Provider configuration file
    protected static final Logger LOG = LoggerFactory.getLogger(FilterSpiLoader.class);

    private static final String SPI_FILE_PREFIX = "META-INF/services/";

    // Cache the MaslaSpiLoader instances, key: classname of Service, value: MaslaSpiLoader instance
    private static final ConcurrentHashMap<String, FilterSpiLoader> SPI_LOADER_MAP = new ConcurrentHashMap<>();

    // Cache the classes of Provider
    private final List<Class<? extends I>> classList = Collections.synchronizedList(new ArrayList<Class<? extends I>>());

    // Cache the sorted classes of Provider
    private final List<Class<? extends I>> sortedClassList = Collections.synchronizedList(new ArrayList<Class<? extends I>>());

    /**
     * Cache the classes of Provider, key: aliasName, value: class of Provider.
     * Note: aliasName is the value of {@link MaslaSpi} when the Provider class has {@link MaslaSpi} annotation and value is not empty,
     * otherwise use classname of the Provider.
     */
    private final ConcurrentHashMap<String, Class<? extends I>> classMap = new ConcurrentHashMap<>();

    // Cache the singleton instance of Provider, key: classname of Provider, value: Provider instance
    private final ConcurrentHashMap<String, I> singletonMap = new ConcurrentHashMap<>();

    // Whether this MaslaSpiLoader has been loaded, that is, loaded the Provider configuration file
    private final AtomicBoolean loaded = new AtomicBoolean(false);

    // Default provider class
    private Class<? extends I> defaultClass = null;

    // The Service class, must be interface or abstract class
    private Class<I> iface;


    public static <T> FilterSpiLoader<T> instance(Class<T> service) {

        String className = service.getName();
        FilterSpiLoader<T> spiLoader = SPI_LOADER_MAP.get(className);
        if (spiLoader == null) {
            synchronized (FilterSpiLoader.class) {
                spiLoader = SPI_LOADER_MAP.get(className);
                if (spiLoader == null) {
                    SPI_LOADER_MAP.putIfAbsent(className, new FilterSpiLoader<>(service));
                    spiLoader = SPI_LOADER_MAP.get(className);
                }
            }
        }

        return spiLoader;
    }


    private FilterSpiLoader(Class<I> iface) {
        this.iface = iface;
    }

    public void load() {
        if (!loaded.compareAndSet(false, true)) {
            return;
        }

        String fullFileName = SPI_FILE_PREFIX + iface.getName();
        ClassLoader classLoader = iface.getClassLoader();

        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        Enumeration<URL> urls = null;
        try {
            urls = classLoader.getResources(fullFileName);
        } catch (IOException e) {
            LOG.error("Error locating SPI configuration file, filename=" + fullFileName + ", classloader=" + classLoader, e);
        }

        if (urls == null || !urls.hasMoreElements()) {
            return;
        }

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();

            InputStream in = null;
            BufferedReader br = null;
            try {
                in = url.openStream();
                br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    if (StringUtils.isBlank(line)) {
                        // Skip blank line
                        continue;
                    }

                    line = line.trim();
                    int commentIndex = line.indexOf("#");
                    if (commentIndex == 0) {
                        // Skip comment line
                        continue;
                    }

                    if (commentIndex > 0) {
                        line = line.substring(0, commentIndex);
                    }
                    line = line.trim();

                    Class<I> clazz = null;
                    try {
                        clazz = (Class<I>) Class.forName(line, false, classLoader);
                    } catch (ClassNotFoundException e) {
                        LOG.error("class " + line + " not found", e);
                    }

                    if (!iface.isAssignableFrom(clazz)) {
                        LOG.error("class " + clazz.getName() + "is not subtype of " + iface.getName() + ",SPI configuration file=" + fullFileName);
                    }

                    classList.add(clazz);
                    MaslaSpi spi = clazz.getAnnotation(MaslaSpi.class);
                    String aliasName = spi == null || "".equals(spi.value()) ? clazz.getName() : spi.value();
                    if (classMap.containsKey(aliasName)) {
                        Class<? extends I> existClass = classMap.get(aliasName);
                        LOG.warn("Found repeat alias name for " + clazz.getName() + " and "
                                + existClass.getName() + ",SPI configuration file=" + fullFileName);
                    }
                    classMap.put(aliasName, clazz);

                    if (spi != null && spi.isDefault()) {
                        if (defaultClass != null) {
                            LOG.error("Found more than one default Provider, SPI configuration file=" + fullFileName);
                        }
                        defaultClass = clazz;
                    }

                    LOG.info("[MaslaSpiLoader] Found SPI implementation for SPI {}, provider={}, aliasName={}"
                                    + ", isSingleton={}, isDefault={}, order={}",
                            iface.getName(), line, aliasName
                            , spi == null ? true : spi.isSingleton()
                            , spi == null ? false : spi.isDefault()
                            , spi == null ? 0 : spi.order());
                }
            } catch (IOException e) {
                LOG.error("error reading SPI configuration file", e);
            } finally {
                closeResources(in, br);
            }
        }

        sortedClassList.addAll(classList);
        Collections.sort(sortedClassList, new Comparator<Class<? extends I>>() {
            @Override
            public int compare(Class<? extends I> o1, Class<? extends I> o2) {
                MaslaSpi spi1 = o1.getAnnotation(MaslaSpi.class);
                int order1 = spi1 == null ? 0 : spi1.order();

                MaslaSpi spi2 = o2.getAnnotation(MaslaSpi.class);
                int order2 = spi2 == null ? 0 : spi2.order();

                return Integer.compare(order1, order2);
            }
        });
    }

    /**
     *
     * @return all provider service
     */
    public List<I> loadInstanceList() {
        load();

        return createInstanceList(classList);
    }

    /**
     * Load all Provider instances of the specified Service, sorted by order value in class's {@link MaslaSpi} annotation
     *
     * @return Sorted Provider instances list
     */
    public List<I> loadInstanceListSorted() {
        load();

        return createInstanceList(sortedClassList);
    }

    /**
     * Create Provider instance list
     *
     * @param clazzList class types of Providers
     * @return Provider instance list
     */
    private List<I> createInstanceList(List<Class<? extends I>> clazzList) {
        if (clazzList == null || clazzList.size() == 0) {
            return Collections.emptyList();
        }

        List<I> instances = new ArrayList<>(clazzList.size());
        for (Class<? extends I> clazz : clazzList) {
            I instance = createInstance(clazz);
            instances.add(instance);
        }
        return instances;
    }

    /**
     * Create Provider instance
     *
     * @param clazz class type of Provider
     * @return Provider class
     */
    private I createInstance(Class<? extends I> clazz) {
        MaslaSpi spi = clazz.getAnnotation(MaslaSpi.class);
        boolean singleton = true;
        if (spi != null) {
            singleton = spi.isSingleton();
        }
        return createInstance(clazz, singleton);
    }

    /**
     * Create Provider instance
     *
     * @param clazz     class type of Provider
     * @param singleton if instance is singleton or prototype
     * @return Provider instance
     */
    private I createInstance(Class<? extends I> clazz, boolean singleton) {
        I instance = null;
        try {
            if (singleton) {
                instance = singletonMap.get(clazz.getName());
                if (instance == null) {
                    synchronized (this) {
                        instance = singletonMap.get(clazz.getName());
                        if (instance == null) {
                            instance = iface.cast(clazz.newInstance());
                            singletonMap.put(clazz.getName(), instance);
                        }
                    }
                }
            } else {
                instance = iface.cast(clazz.newInstance());
            }
        } catch (Throwable e) {
            LOG.error(clazz.getName() + " could not be instantiated");
        }
        return instance;
    }


    /**
     * Close all resources
     *
     * @param closeables {@link Closeable} resources
     */
    private void closeResources(Closeable... closeables) {
        if (closeables == null || closeables.length == 0) {
            return;
        }

        Exception firstException = null;
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                if (firstException != null) {
                    firstException = e;
                }
            }
        }
        if (firstException != null) {
            LOG.error("error closing resources", firstException);
        }
    }


}
