package com.msw.masla.common.enums;

/**
 * Created by Gavin.peng on 2017/6/22.
 * 机器状态，为描述服务降级和复活
 */
public enum HostStatus {

    ENABLE(1), DISENABLE(-1), PUBLISH(2),TEMP_DISENABLE(3),EXCLUDE(4),EXC_DISENABLE(5),EXC_TEMP_DISENABLE(6),BUSYING(7);

    private int code;

    HostStatus(int code) {
        this.setCode(code);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
