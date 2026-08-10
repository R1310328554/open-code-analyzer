/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.model.response;

import com.alibaba.nacos.api.utils.StringUtils;

import java.util.Map;

/**
 * 单节点服务端负载指标。
 *
 * <p>记录节点地址、SDK/总连接数、系统负载与 CPU 使用率，用于集群负载均衡决策。</p>
 *
 * @author yunye
 * @since 3.0.0-beta
 */
public class ServerLoaderMetric {
    
    /** 节点地址（ip:port）。 */
    private String address;
    
    /** SDK 客户端连接数。 */
    private int sdkConCount;
    
    /** 总连接数。 */
    private int conCount;
    
    /** 系统负载指标字符串。 */
    private String load;
    
    /** CPU 使用率字符串。 */
    private String cpu;
    
    /** 获取节点地址。 */
    public String getAddress() {
        return address;
    }
    
    /** 设置节点地址。 */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /** 获取 SDK 连接数。 */
    public int getSdkConCount() {
        return sdkConCount;
    }
    
    /** 设置 SDK 连接数。 */
    public void setSdkConCount(int sdkConCount) {
        this.sdkConCount = sdkConCount;
    }
    
    /** 获取总连接数。 */
    public int getConCount() {
        return conCount;
    }
    
    /** 设置总连接数。 */
    public void setConCount(int conCount) {
        this.conCount = conCount;
    }
    
    /** 获取系统负载。 */
    public String getLoad() {
        return load;
    }
    
    /** 设置系统负载。 */
    public void setLoad(String load) {
        this.load = load;
    }
    
    /** 获取 CPU 使用率。 */
    public String getCpu() {
        return cpu;
    }
    
    /** 设置 CPU 使用率。 */
    public void setCpu(String cpu) {
        this.cpu = cpu;
    }
    
    /** {@link ServerLoaderMetric} 建造者，支持链式组装与 Map 转换。 */
    public static class Builder {
        
        private ServerLoaderMetric serverLoaderMetric = new ServerLoaderMetric();
        
        /** 创建新建造者实例。 */
        public static Builder newBuilder() {
            return new Builder();
        }
        
        /** 构建负载指标对象。 */
        public ServerLoaderMetric build() {
            return serverLoaderMetric;
        }
        
        /** 设置节点地址并返回建造者。 */
        public Builder withAddress(String address) {
            serverLoaderMetric.setAddress(address);
            return this;
        }
        
        /**
         * 从键值 Map 填充负载指标字段。
         *
         * @param metric 服务端上报的指标 Map
         * @return 当前建造者
         */
        public Builder convertFromMap(Map<String, String> metric) {
            serverLoaderMetric.setSdkConCount(convertInt(metric, "sdkConCount", 0));
            serverLoaderMetric.setConCount(convertInt(metric, "conCount", 0));
            serverLoaderMetric.setLoad(metric.get("load"));
            serverLoaderMetric.setCpu(metric.get("cpu"));
            return this;
        }
        
        /** 从 Map 解析整型指标，缺省或空值时返回默认值。 */
        private int convertInt(Map<String, String> metric, String key, int defaultValue) {
            String value = metric.get(key);
            if (!StringUtils.isBlank(value)) {
                return Integer.parseInt(value);
            }
            return defaultValue;
        }
    }
}
