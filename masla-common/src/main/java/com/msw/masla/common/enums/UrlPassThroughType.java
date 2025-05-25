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

public enum UrlPassThroughType {

    /**透传contextRoot和path*/
    CONTEXT_ROOT_AND_PATH_PASS_THROUGH(0),
    /**不透传contextRoot和path*/
    NO_PASS_THROUGH(1),
    /**只透传contextRoot*/
    CONTEXT_ROOT_PASS_THROUGH(2),
    /**只透传path*/
    PATH_PASS_THROUGH(3);

    int code;

    UrlPassThroughType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
