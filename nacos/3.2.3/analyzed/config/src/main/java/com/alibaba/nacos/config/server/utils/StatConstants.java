/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.config.server.utils;

/**
 * 配置模块监控统计指标名常量。
 * Stat constant.
 *
 * @author Nacos
 */
public class StatConstants {
    
    private StatConstants() {
    }
    
    /** 监控上报应用名 */
    public static final String APP_NAME = "nacos";
    
    /** HTTP GET 200 平均耗时指标名 */
    public static final String STAT_AVERAGE_HTTP_GET_OK = "AverageHttpGet_OK";
    
    /** HTTP GET 304 Not Modified 平均耗时指标名 */
    public static final String STAT_AVERAGE_HTTP_GET_NOT_MODIFIED = "AverageHttpGet_Not_Modified";
    
    /** HTTP GET 其他状态码平均耗时指标名 */
    public static final String STAT_AVERAGE_HTTP_GET_OTHER = "AverageHttpGet_Other_Status";
    
    /** HTTP POST 长轮询 Check 平均耗时指标名 */
    public static final String STAT_AVERAGE_HTTP_POST_CHECK = "AverageHttpPost_Check";
    
}
