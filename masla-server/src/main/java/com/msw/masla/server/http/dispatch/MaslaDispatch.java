package com.msw.masla.server.http.dispatch;

import com.msw.masla.protocol.http.netty.session.IOSession;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Created by Gavin.peng on 2017/9/26.
 * dispatch 负责 匹配filter 和 servlet
 */
public interface MaslaDispatch {



    void dispatch(IOSession session, FullHttpRequest request);

}
