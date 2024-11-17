package com.msw.masla.server;

import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.core.discovery.healthcheck.HealthcheckManager;
import com.msw.masla.metrics.frame.AsyncAppAgregateAdminReporter;
import com.msw.masla.metrics.frame.Reporter;
import com.msw.masla.protocol.http.netty.factory.EventLoopFactory;
import com.msw.masla.protocol.http.netty.factory.MaslaEventLoopGroupFactory;
import com.msw.masla.common.constant.Constants;

import java.util.*;

import com.msw.masla.server.dispatch.MaslaServerDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Gavin.peng on 2023/12/1.
 */
public class MaslaServer {

    private static final String HTTP = "http";

    protected static final Logger LOG = LoggerFactory.getLogger(MaslaServer.class);
    private volatile boolean running = true;
    private ClassPathXmlApplicationContext applicationContext;

    private static final int REWARN_CONN_NUMS = 5;
    private static final int FOUR_HOUR = 4;
    private static final int TWO_MINUTE = 120 * 1000;
    //修改为每天凌晨主动执行一次。能扛主业务堵时更多的时间
    private static final int TWO_DAY_TIME = 12 * 60 * TWO_MINUTE;
    private Thread flushThread;

    private Reporter reporter;

    private static class MaslaServerHolder{
        static final MaslaServer instance = new MaslaServer();
    }

    public static MaslaServer getInstance(){
        return MaslaServerHolder.instance;
    }




    public static void main(String[] args)
    {
        MaslaServer maslaServer = new MaslaServer();
        maslaServer.startup(args);
    }

    private MaslaServer(){
        flushThread = new Thread(new MetaFlushTask());
        flushThread.setName(Constants.MASLA_METADATA_FLUSH_THREAD);

    }

    private void startup(String[] args){
        LOG.warn("Masla server start up.............");
        try {

            initSpringContext();
            LOG.warn("Masla spring context init complete!!!");
            LOG.warn("Masla meta data init complete!!!");
            String protocol = MaslaServer.HTTP;
            if(args.length>1) {
                protocol = args[1];
            }
            LOG.warn("Masla start server with {} protocol",protocol);
            startListenerServer(protocol);
            LOG.warn("Masla server start up complete and ready!!!");
            flushThread.start();
            addShutdownHook();
            startMetricTask();
        }catch (Throwable e){
            LOG.error("Masla startup failed:",e);
            LOG.error("Masla server init failed will exit!!!");
            System.exit(-1);
        }

    }


    public void addShutdownHook() {
        final MaslaServer nioServer = MaslaServer.getInstance();
        final HealthcheckManager healthcheckManager = HealthcheckManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    nioServer.stopInternal();
                    healthcheckManager.destory();
                    running = false;
                    flushThread.interrupt();
                    if (reporter != null) {
                        reporter.stop();
                    }
                }catch (Throwable e){
                    LOG.error("Masla graceful shutdown failed:",e);
                }
            }
        });
    }

    private void initDataCenter(){

    }

    private void initSpringContext() throws Throwable{
        applicationContext =  new ClassPathXmlApplicationContext(new String[] {
                "applicationContext-web.xml"});
    }


    private void startListenerServer(String protocol) throws Throwable{

        //如果需要开启https协议，则开启443端口
        EventLoopFactory eventLoopFactory = MaslaEventLoopGroupFactory.getInstance();
        MaslaMultiProtocolServer.getInstance().setMaslaDispatch(MaslaServerDispatch.getInstance(MaslaMultiProtocolServer.getInstance()));
        MaslaMultiProtocolServer.getInstance().init(eventLoopFactory.getNettyConfig());
        MaslaMultiProtocolServer.getInstance().start();

    }

    public void stopInternal() throws Exception {
        LOG.warn("Masla server start to exit..............");
        MaslaMultiProtocolServer.getInstance().stopInternal();
    }


    protected class MetaFlushTask implements Runnable{

        private long lastFullGCTime;

        @Override
        public void run() {
            while (running) {
                //物理机大堆，增加触发下GC,在零晨低峰期
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                long now = calendar.getTimeInMillis();
                if(hour == FOUR_HOUR && (now - this.lastFullGCTime)>TWO_DAY_TIME){
                    LOG.warn("Masla start auto do full gc at hour {} time {}",hour,now);
                    System.gc();
                    LOG.warn("Masla finish auto do full gc at hour {} time {}",hour,System.currentTimeMillis());
                    this.lastFullGCTime = now;
                }

                try {
                    Thread.sleep(60000);
                }catch (InterruptedException e){
                    LOG.warn("Masla meta data flush thread is interrupted!!!!");
                }
            }
            LOG.warn("Masla meta data flush thread exit!!!");
        }
    }


    private void startMetricTask(){

        MaslaConfConfig confConfig = MaslaSpringContextUtil.getMaslaConfConfigBean();
        if (confConfig.isReportMaslaMetrics()) {
            reporter = AsyncAppAgregateAdminReporter.getReporter();
            reporter.start();
        }

    }
}
