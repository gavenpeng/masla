package com.msw.masla.protocol.http.netty.http.handler;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: Gavin.peng
 * @Date: 2020/5/28 17:47
 */
public class HeaderFlushCallbackListener implements FutureListener<Channel> {

    protected static final Logger LOG = LoggerFactory.getLogger(HeaderFlushCallbackListener.class);


    private HttpRequest req;

    public HeaderFlushCallbackListener(HttpRequest request){
        this.req = request;
    }

    /**
     * Invoked when the operation associated with the {@link Future} has been completed.
     *
     * @param future the source {@link Future} which called this callback
     */
    @Override
    public void operationComplete(Future<Channel> future) throws Exception {
        if(LOG.isDebugEnabled()){
            LOG.debug("Masla receive request {} header part is flush complete",req.uri());
        }
    }
}


