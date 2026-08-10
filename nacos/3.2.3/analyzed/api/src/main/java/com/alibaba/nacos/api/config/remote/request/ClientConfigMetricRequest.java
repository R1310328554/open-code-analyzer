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

package com.alibaba.nacos.api.config.remote.request;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.remote.request.ServerRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 配置模块客户端指标采集请求，由服务端发起拉取客户端缓存等指标。
 *
 * @author liuzunfei
 * @version $Id: ClientConfigMetricRequest.java, v 0.1 2020年12月30日 9:05 PM liuzunfei Exp $
 */
public class ClientConfigMetricRequest extends ServerRequest {
    
    /** 待采集的指标键列表。 */
    private List<MetricsKey> metricsKeys = new ArrayList<>();
    
    /** 返回配置模块标识。 */
    @Override
    public String getModule() {
        return Constants.Config.CONFIG_MODULE;
    }
    
    /** 获取指标键列表。 */
    public List<MetricsKey> getMetricsKeys() {
        return metricsKeys;
    }
    
    /** 设置指标键列表。 */
    public void setMetricsKeys(List<MetricsKey> metricsKeys) {
        this.metricsKeys = metricsKeys;
    }
    
    /** 指标键，由类型与键名组成。 */
    public static class MetricsKey implements Serializable {
        
        private static final long serialVersionUID = -2731160029960311757L;
        
        /** 指标类型。 */
        String type;
        
        /** 指标键名。 */
        String key;
        
        /** 客户端内存缓存数据指标。 */
        public static final String CACHE_DATA = "cacheData";
        
        /** 本地快照数据指标。 */
        public static final String SNAPSHOT_DATA = "snapshotData";
        
        /**
         * 构造指标键实例。
         *
         * @param type 指标类型
         * @param key  指标键名
         * @return 指标键对象
         */
        public static MetricsKey build(String type, String key) {
            MetricsKey metricsKey = new MetricsKey();
            metricsKey.type = type;
            metricsKey.key = key;
            return metricsKey;
        }
        
        /** 获取指标类型。 */
        public String getType() {
            return type;
        }
        
        /** 设置指标类型。 */
        public void setType(String type) {
            this.type = type;
        }
        
        /** 获取指标键名。 */
        public String getKey() {
            return key;
        }
        
        /** 设置指标键名。 */
        public void setKey(String key) {
            this.key = key;
        }
        
        @Override
        public String toString() {
            return "MetricsKey{" + "type='" + type + '\'' + ", key='" + key + '\'' + '}';
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MetricsKey that = (MetricsKey) o;
            return Objects.equals(type, that.type) && Objects.equals(key, that.key);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(type, key);
        }
    }
    
}
