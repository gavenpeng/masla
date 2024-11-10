package com.msw.masla.core.push.processor;

import com.msw.masla.protocol.http.netty.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

/**
 * Created by Gavin.peng on 2017/6/5.
 * 对响应头和Body 的加工处理
 */
public interface ResponseProcessor<C,E extends BaseEvent> {

    public static final Logger LOG = LoggerFactory.getLogger(ResponseProcessor.class);



    /**
     * 对Http client应体进行加工
     * @param requestContext
     * @param event
     */
    void process(C requestContext, E event, OutputStream os) throws Throwable;


    /**
     * processor 的代号
     * @return
     */
    String getProcessorName();

}
