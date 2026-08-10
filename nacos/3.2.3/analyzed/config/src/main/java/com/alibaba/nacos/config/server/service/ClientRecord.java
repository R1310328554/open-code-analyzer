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

package com.alibaba.nacos.config.server.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 单客户端订阅与轮询快照：记录 IP、最近活跃时间、各 groupKey 的 MD5 与轮询时间戳。
 * 由 {@link ClientTrackService} 维护生命周期。
 * ClientRecord saves records which fetch from client-side.
 *
 * @author zongtanghu
 */
public class ClientRecord {
    
    /** 客户端 IP 地址（构造后不可变） */
    private final String ip;
    
    /** 最近一次 track 更新的毫秒时间戳 */
    private volatile long lastTime;
    
    /** groupKey → 客户端上报的配置 MD5 */
    private final ConcurrentMap<String, String> groupKey2md5Map;
    
    /** groupKey → 最近一次长轮询/监听时间戳 */
    private final ConcurrentMap<String, Long> groupKey2pollingTsMap;
    
    public ClientRecord(final String clientIp) {
        this.ip = clientIp;
        this.groupKey2md5Map = new ConcurrentHashMap<>(20, 0.75f, 1);
        this.groupKey2pollingTsMap = new ConcurrentHashMap<>(20, 0.75f, 1);
    }
    
    public String getIp() {
        return ip;
    }
    
    public long getLastTime() {
        return lastTime;
    }
    
    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }
    
    public ConcurrentMap<String, String> getGroupKey2md5Map() {
        return groupKey2md5Map;
    }
    
    public ConcurrentMap<String, Long> getGroupKey2pollingTsMap() {
        return groupKey2pollingTsMap;
    }
}
