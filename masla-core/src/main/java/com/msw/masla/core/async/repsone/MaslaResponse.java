package com.msw.masla.core.async.repsone;


import com.msw.masla.protocol.http.netty.context.SessionContext;

/**
 * Created by Gavin.peng on 2017/8/21.
 */
public interface MaslaResponse {

    void witerBack(SessionContext maslaContext);
}
