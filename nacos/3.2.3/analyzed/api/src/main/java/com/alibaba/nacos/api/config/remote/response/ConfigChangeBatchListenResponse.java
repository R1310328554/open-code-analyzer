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

package com.alibaba.nacos.api.config.remote.response;

import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量配置变更监听响应。
 *
 * <p>服务端在批量长轮询返回时，携带自上次监听以来发生变更的配置列表。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigChangeBatchListenResponse.java, v 0.1 2020年07月14日 3:07 PM liuzunfei Exp $
 */
public class ConfigChangeBatchListenResponse extends Response {
    
    /** 自上次监听以来发生变更的配置列表。 */
    List<ConfigContext> changedConfigs = new ArrayList<>();
    
    /** 无参构造，供序列化或框架实例化使用。 */
    public ConfigChangeBatchListenResponse() {
    }
    
    /**
     * 追加一条变更配置记录。
     *
     * @param dataId 配置 Data ID
     * @param group  配置分组
     * @param tenant 命名空间（tenant）
     */
    public void addChangeConfig(String dataId, String group, String tenant) {
        ConfigContext configContext = new ConfigContext();
        configContext.dataId = dataId;
        configContext.group = group;
        configContext.tenant = tenant;
        changedConfigs.add(configContext);
    }
    
    /**
     * 获取变更配置列表。
     *
     * @return 变更配置上下文列表
     */
    public List<ConfigContext> getChangedConfigs() {
        return changedConfigs;
    }
    
    /**
     * 设置变更配置列表。
     *
     * @param changedConfigs 变更配置上下文列表
     */
    public void setChangedConfigs(List<ConfigContext> changedConfigs) {
        this.changedConfigs = changedConfigs;
    }
    
    /**
     * 构建批量监听失败响应。
     *
     * @param errorMessage 错误描述信息
     * @return 失败响应实例
     */
    public static ConfigChangeBatchListenResponse buildFailResponse(String errorMessage) {
        ConfigChangeBatchListenResponse response = new ConfigChangeBatchListenResponse();
        response.setResultCode(ResponseCode.FAIL.getCode());
        response.setMessage(errorMessage);
        return response;
    }
    
    /** 单条变更配置的上下文信息。 */
    public static class ConfigContext {
        
        /** 配置分组。 */
        String group;
        
        /** 配置 Data ID。 */
        String dataId;
        
        /** 命名空间（tenant）。 */
        String tenant;
        
        /** 无参构造，供序列化使用。 */
        public ConfigContext() {
            
        }
        
        /**
         * 获取配置分组。
         *
         * @return 分组名
         */
        public String getGroup() {
            return group;
        }
        
        /**
         * 设置配置分组。
         *
         * @param group 分组名
         */
        public void setGroup(String group) {
            this.group = group;
        }
        
        /**
         * 获取配置 Data ID。
         *
         * @return Data ID
         */
        public String getDataId() {
            return dataId;
        }
        
        /**
         * 设置配置 Data ID。
         *
         * @param dataId Data ID
         */
        public void setDataId(String dataId) {
            this.dataId = dataId;
        }
        
        /**
         * 获取命名空间（tenant）。
         *
         * @return 命名空间标识
         */
        public String getTenant() {
            return tenant;
        }
        
        /**
         * 设置命名空间（tenant）。
         *
         * @param tenant 命名空间标识
         */
        public void setTenant(String tenant) {
            this.tenant = tenant;
        }
        
        @Override
        public String toString() {
            return "ConfigContext{" + "group='" + group + '\'' + ", dataId='" + dataId + '\''
                + ", tenant='" + tenant
                + '\'' + '}';
        }
    }
    
}
