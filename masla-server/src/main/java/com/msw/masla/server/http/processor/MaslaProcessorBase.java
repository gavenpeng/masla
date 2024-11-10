package com.msw.masla.server.http.processor;

import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Gavin.Peng on 2024/03/19.
 */
@Data
public abstract class MaslaProcessorBase implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(MaslaProcessorBase.class);


    protected IOSession session;

    protected FullHttpRequest request;

    public MaslaProcessorBase(IOSession session, FullHttpRequest request){
        this.reset(session,request);
    }

    public void reset(IOSession session, FullHttpRequest request){
        this.session = session;
        this.request = request;
    }

    @Override
    public final void run() {

        try {
            doRun();
        }catch (Throwable e){
            try {
                LOG.error("Masla process request {} failed:", request.uri(), e);
            }catch (Throwable ee){
                LOG.error("Masla record error log failed:", ee);
            }

        }
    }

    public abstract void doRun();

}
