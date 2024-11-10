package com.msw.masla.common.stream;

import com.msw.masla.common.pojo.ServiceApi;
import com.msw.masla.common.pojo.ServiceApp;

/**
 * Created by Gavin.peng on 2017/11/27.
 */
public interface DiscardPolicy {

    boolean discard();

    void reset(ServiceApi apiDO);

    void reset(ServiceApp apiDO);

    String genernateDiscardResponse(String url);
}
