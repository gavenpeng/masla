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
