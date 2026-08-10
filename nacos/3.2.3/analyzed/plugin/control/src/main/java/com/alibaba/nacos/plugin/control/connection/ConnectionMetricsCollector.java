/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.control.connection;

/**
 * 连接数指标采集 SPI 接口。
 *
 * <p>各模块通过 SPI 注册采集器，供 {@link ConnectionControlManager} 汇总连接数并执行限连校验。</p>
 *
 * @author shiyiyue
 */
public interface ConnectionMetricsCollector {
    
    /**
     * 获取采集器名称，用于指标汇总时的键标识。
     *
     * @return 采集器名称
     */
    String getName();
    
    /**
     * 获取当前总连接数。
     *
     * @return 连接总数
     */
    int getTotalCount();
    
    /**
     * 获取指定 IP 的连接数。
     *
     * @param ip 客户端 IP 地址
     * @return 该 IP 的连接数
     */
    int getCountForIp(String ip);
}
