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
package com.msw.masla.common.monitor.vo;

import java.util.Collections;
import java.util.List;

/**
 * Created by gaoyue on 17/7/31.
 */
public class AllMonitorVO {
    private List<ChannelPoolMonitorVO> channelPoolMonitorVOList = Collections.EMPTY_LIST;
    private List<NettyPoolArenaMonitorVO> nettyPoolArenaMonitorVOList = Collections.EMPTY_LIST;
    private List<NettyThreadMonitorSingle> slowThreadMonitorSingleList = Collections.EMPTY_LIST;
    private List<NettyThreadMonitorSingle> defaultThreadMonitorSingleList = Collections.EMPTY_LIST;
    private List<NettyThreadMonitorSingle> fastThreadMonitorSingleList = Collections.EMPTY_LIST;
    private List<NettyThreadMonitorSingle> newThreadMonitorSingleList = Collections.EMPTY_LIST;
    private List<NettyServerConnectorMonitorVO> nettyServerConnectorMonitorVOList = Collections.EMPTY_LIST;

    private PushThreadMonitorVO pushThreadMonitorVO;
    private List<ApiQpsMonitorVO> apiQpsMonitorVOList = Collections.EMPTY_LIST;

    public List<ChannelPoolMonitorVO> getChannelPoolMonitorVOList() {
        return channelPoolMonitorVOList;
    }

    public void setChannelPoolMonitorVOList(List<ChannelPoolMonitorVO> channelPoolMonitorVOList) {
        this.channelPoolMonitorVOList = channelPoolMonitorVOList;
    }

    public List<NettyPoolArenaMonitorVO> getNettyPoolArenaMonitorVOList() {
        return nettyPoolArenaMonitorVOList;
    }

    public void setNettyPoolArenaMonitorVOList(List<NettyPoolArenaMonitorVO> nettyPoolArenaMonitorVOList) {
        this.nettyPoolArenaMonitorVOList = nettyPoolArenaMonitorVOList;
    }

    public List<NettyThreadMonitorSingle> getSlowThreadMonitorSingleList() {
        return slowThreadMonitorSingleList;
    }

    public void setSlowThreadMonitorSingleList(List<NettyThreadMonitorSingle> slowThreadMonitorSingleList) {
        this.slowThreadMonitorSingleList = slowThreadMonitorSingleList;
    }

    public List<NettyThreadMonitorSingle> getDefaultThreadMonitorSingleList() {
        return defaultThreadMonitorSingleList;
    }

    public void setDefaultThreadMonitorSingleList(List<NettyThreadMonitorSingle> defaultThreadMonitorSingleList) {
        this.defaultThreadMonitorSingleList = defaultThreadMonitorSingleList;
    }

    public List<NettyThreadMonitorSingle> getFastThreadMonitorSingleList() {
        return fastThreadMonitorSingleList;
    }

    public void setFastThreadMonitorSingleList(List<NettyThreadMonitorSingle> fastThreadMonitorSingleList) {
        this.fastThreadMonitorSingleList = fastThreadMonitorSingleList;
    }

    public List<NettyThreadMonitorSingle> getNewThreadMonitorSingleList() {
        return newThreadMonitorSingleList;
    }

    public void setNewThreadMonitorSingleList(List<NettyThreadMonitorSingle> newThreadMonitorSingleList) {
        this.newThreadMonitorSingleList = newThreadMonitorSingleList;
    }

    public PushThreadMonitorVO getPushThreadMonitorVO() {
        return pushThreadMonitorVO;
    }

    public void setPushThreadMonitorVO(PushThreadMonitorVO pushThreadMonitorVO) {
        this.pushThreadMonitorVO = pushThreadMonitorVO;
    }

    public List<ApiQpsMonitorVO> getApiQpsMonitorVOList() {
        return apiQpsMonitorVOList;
    }

    public void setApiQpsMonitorVOList(List<ApiQpsMonitorVO> apiQpsMonitorVOList) {
        this.apiQpsMonitorVOList = apiQpsMonitorVOList;
    }

    public List<NettyServerConnectorMonitorVO> getNettyServerConnectorMonitorVOList() {
        return nettyServerConnectorMonitorVOList;
    }

    public void setNettyServerConnectorMonitorVOList(List<NettyServerConnectorMonitorVO> nettyServerConnectorMonitorVOList) {
        this.nettyServerConnectorMonitorVOList = nettyServerConnectorMonitorVOList;
    }
}
