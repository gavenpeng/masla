package com.msw.masla.common.pojo;

import lombok.Data;

@Data
public class IOTDevice {

    private String os;

    private final static IOTDevice instance = new IOTDevice();

    public static IOTDevice getInstance() {
        return instance;
    }
}
