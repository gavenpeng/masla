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
package com.msw.masla.common.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * 
 * 经常遇到这种情况：获取一个数据（资源），如果不存在则初始化后返回，否则直接返回
 * 这个类将这种情况的初始化中要考虑的多线程并发问题的解决方法抽取出来，作为模板。
 * 使用者只要实现初始化方法，不用再考虑初始化本身之外的线程安全和并发性能的问题。
 * @param <K> 获取资源的Key类型，若资源只是单个对象，不是map结构，则现实方法可以不使用key
 * @param <V> 资源的类型
 * @param <E> 资源初始化时可能抛出的异常。若没有异常抛出，可以设置E为RuntimeException
 */
public class ConcurrentInitializer<K, V, E extends Throwable> {
    
    private ResourceHolder<K, V, E> resourceHolder;
    private ResourcePrimaryLockHold<K> lockHolder;
    
    
    public ConcurrentInitializer(ResourceHolder<K, V, E> resourceHolder){
        this.resourceHolder = resourceHolder;
        this.lockHolder = new DefaultLockHold();
    }
    
    public ConcurrentInitializer(ResourceHolder<K, V, E> resourceHolder,ResourcePrimaryLockHold<K> lockHolder){
        this.resourceHolder = resourceHolder;
        this.lockHolder = lockHolder;
    }
    

	public V getData(K key) throws E {
        V data = resourceHolder.currentData(key);
        if (data != null)
            return data;
        return initData(key);
    }

    private V initData(K key) throws E {
    	//get specified lock of the key, to prevent too many threads of 
    	//same key to initialize the cache at the same time, thus protect
    	//the backing system.
        ReadWriteLock keyLock = lockHolder.getLock(key);
        
        if(keyLock.writeLock().tryLock()) {
            try {
                V data = resourceHolder.currentData(key);
                if (data == null) {
                    data = resourceHolder.initializeDate(key);
                }
                return data;
            } finally {
            	keyLock.writeLock().unlock();
            	// to prevent the map grows
            	lockHolder.removeLock(key);
            }        	
        } else {
        	keyLock.readLock().lock();
        	//when threads come here, means that the writelock has been unlocked, 
        	//which means data should have been initialized.
        	try {
            	return resourceHolder.currentData(key);        		
        	} finally {
        		keyLock.readLock().unlock();        		
        	}
        }    
    }


    class DefaultLockHold implements ResourcePrimaryLockHold<K>{

    	private ConcurrentHashMap<K, ReadWriteLock> keyLockMap = new ConcurrentHashMap<K, ReadWriteLock>();
    	
		@Override
		public ReadWriteLock getLock(K key) {

	    	ReadWriteLock lock = keyLockMap.get(key);
	        if(lock == null) {
	            lock = new ReentrantReadWriteLock();
	            ReadWriteLock oldLock = keyLockMap.putIfAbsent(key, lock); 
	            if(oldLock != null) {
	            	lock = oldLock;
	            }
	        }
	        System.out.println(lock);
	        return lock;
	    
		}

		@Override
		public void removeLock(K k) {
			keyLockMap.remove(k);
			
		}
    	
    }
    
    
}
