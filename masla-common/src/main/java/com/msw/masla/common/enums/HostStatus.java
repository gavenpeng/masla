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
package com.msw.masla.common.enums;

/**
 * Created by Gavin.peng on 2017/6/22.
 * 机器状态，为描述服务降级和复活
 */
public enum HostStatus {

    ENABLE(1), DISENABLE(-1), PUBLISH(2), TEMP_DISENABLE(3), EXCLUDE(4), EXC_DISENABLE(5), EXC_TEMP_DISENABLE(6), BUSYING(7);

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
