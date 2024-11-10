package com.msw.masla.filter.factory;

import com.msw.masla.filter.frame.MaslaFilter;
import com.msw.masla.protocol.http.netty.exception.NettyServletException;
import com.msw.masla.filter.servlet.MaslaServlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by Gavin.peng on 2017/9/26.
 */
public class MaslaFilterBeanFactory {

    private static final String COMMON_PATTERN_PATH = "/.*";

    private  final List<Pattern> filterMappingList = new ArrayList<Pattern>();

    private  final List<Pattern> servletMappingList = new ArrayList<Pattern>();

    private final static Map<String,List<MaslaFilter>> filterInstance = new ConcurrentHashMap<String,List<MaslaFilter>>();

    private final static Map<String, MaslaServlet> servletInstance = new ConcurrentHashMap<String,MaslaServlet>();

    private MaslaFilterBeanFactory(){
//        this.initGlobalFilter();
    }

    static class MaslaContainerFactoryHolder{
        static MaslaFilterBeanFactory maslaContainerFactory = new MaslaFilterBeanFactory();
    }

    public static MaslaFilterBeanFactory getInstance(){
        return MaslaContainerFactoryHolder.maslaContainerFactory;
    }

    public void registerFilter(String path,MaslaFilter filter){
        if (!filterInstance.containsKey(path)) {
            filterMappingList.add(Pattern.compile(path));
        }
        List<MaslaFilter> nettyFilterList = filterInstance.get(path);
        if (nettyFilterList  == null) {
            nettyFilterList = new ArrayList<MaslaFilter>();
        }
        nettyFilterList.add(filter);
        filterInstance.put(path, nettyFilterList);
    }


    public void registerServlet(String path,MaslaServlet servlet){
        if (!servletInstance.containsKey(path)) {
            servletMappingList.add(Pattern.compile(path));
        }
        servletInstance.put(path, servlet);
    }

    public List<Pattern> getFilterMappingList() {
        return filterMappingList;
    }


    public List<Pattern> getServletMappingList() {
        return servletMappingList;
    }



    public List<MaslaFilter> getFilter(String path) throws ClassNotFoundException, NettyServletException {

        if(filterInstance.containsKey(path)){
            return filterInstance.get(path);
        }
        return Collections.emptyList();
    }




    public MaslaServlet getServlet(String path) throws ClassNotFoundException,NettyServletException{

        if(servletInstance.containsKey(path)){
            return servletInstance.get(path);
        }
        return null;

    }


}
