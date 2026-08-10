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

package com.alibaba.nacos.plugin.control.tps.request;

/**
 * 规则屏障校验请求。
 *
 * <p>携带限流点名称、时间戳与本次计数增量，供 {@link com.alibaba.nacos.plugin.control.tps.barrier.RuleBarrier}
 * 执行本地速率校验。</p>
 *
 * @author shiyiyue
 */
public class BarrierCheckRequest {
    
    /** 限流点名称。 */
    private String pointName;
    
    /** 请求时间戳，默认当前毫秒时间。 */
    private long timestamp = System.currentTimeMillis();
    
    /** 本次请求计数增量，默认为 1。 */
    private long count = 1;
    
    /**
     * 获取限流点名称。
     *
     * @return 限流点名称
     */
    public String getPointName() {
        return pointName;
    }
    
    /**
     * 设置限流点名称。
     *
     * @param pointName 限流点名称
     */
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }
    
    /**
     * 获取请求时间戳。
     *
     * @return 毫秒时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 设置请求时间戳。
     *
     * @param timestamp 毫秒时间戳
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 获取本次计数增量。
     *
     * @return 计数增量
     */
    public long getCount() {
        return count;
    }
    
    /**
     * 设置本次计数增量。
     *
     * @param count 计数增量
     */
    public void setCount(long count) {
        this.count = count;
    }
    
}
