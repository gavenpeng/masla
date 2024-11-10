package com.msw.masla.protocol.http.netty.http.handler;

import com.msw.masla.protocol.http.netty.codec.MaslaHttpRequestDecoder;


public class MaslaServerHttpRequestDecode extends MaslaHttpRequestDecoder {



    public MaslaServerHttpRequestDecode() {
        super();
    }

    public MaslaServerHttpRequestDecode(
            int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        super(maxInitialLineLength, maxHeaderSize, maxChunkSize, true);
    }

}
