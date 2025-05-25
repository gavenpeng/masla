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
package com.msw.masla.common.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author: gavin.peng
 */
@Data
@Accessors(chain = true)
public class ParameterRewriteDO {

    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     * */
    private String name;

    /**
     * 参数名
     */
    private String key;

    /**
     * 参数类型
     */
    private Integer type;

    /**
     * 重写的值，empty则删除对应的参数
     */
    private String value;

    /**
     * 操作 add=0, set=1, remove=2
     * */
    private Integer operate;

    /**
     * 启用1/禁用0
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModify;

    /**
     * 创建人
     */
    private String addUser;

    public void clone(ParameterRewriteDO parameterRewriteDO) {
        this.setType(parameterRewriteDO.getType());
        this.setKey(parameterRewriteDO.getKey());
        this.setValue(parameterRewriteDO.getValue());
        this.setOperate(parameterRewriteDO.getOperate());
        this.setStatus(parameterRewriteDO.getStatus());
        this.setGmtCreate(parameterRewriteDO.getGmtCreate());
        this.setGmtModify(parameterRewriteDO.getGmtModify());
        this.setAddUser(parameterRewriteDO.getAddUser());
    }

}
