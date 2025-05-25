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
package com.msw.masla.common.circuit;

/**
 * Created by Gavin.peng on 2018/1/17.
 */
public class CircuitConfig {
    //自动升降级比例
    public static long CIRCUIT_UP_AND_DOWN_PERCENT = 5;
    //自动升级的条件,即错误数少于等于该值时，可以升级
    public static float CIRCUIT_LOW_WATER_MARK = 0.5f;

    public static int MIDDLE_PERCENT_LEVEL = 4;//错误比例达到50%后，直接熔断50%
}
