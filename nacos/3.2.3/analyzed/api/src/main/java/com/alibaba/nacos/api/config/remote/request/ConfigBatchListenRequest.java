/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

import java.util.ArrayList;
import java.util.List;

/**
 * 批量监听或取消监听配置的远程请求。
 *
 * <p>客户端长轮询时一次性提交多个 dataId/group/tenant 及本地 MD5。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigBatchListenRequest.java, v 0.1 2020年07月27日 7:46 PM liuzunfei Exp $
 */
public class ConfigBatchListenRequest extends AbstractConfigRequest {
    
    /** {@code true} 表示注册监听，{@code false} 表示取消监听。 */
    private boolean listen = true;
    
    /** 待监听或取消的配置上下文列表。 */
    private List<ConfigListenContext> configListenContexts = new ArrayList<>();
    
    /**
     * 追加一条配置监听上下文。
     *
     * @param group  配置分组
     * @param dataId 配置 Data ID
     * @param tenant 命名空间 ID
     * @param md5    客户端当前内容 MD5
     */
    public void addConfigListenContext(String group, String dataId, String tenant, String md5) {
        ConfigListenContext configListenContext = new ConfigListenContext();
        configListenContext.dataId = dataId;
        configListenContext.group = group;
        configListenContext.md5 = md5;
        configListenContext.tenant = tenant;
        configListenContexts.add(configListenContext);
    }
    
    /**
     * 获取配置监听上下文列表。
     *
     * @return 监听上下文列表
     */
    public List<ConfigListenContext> getConfigListenContexts() {
        return configListenContexts;
    }
    
    /**
     * 设置配置监听上下文列表。
     *
     * @param configListenContexts 监听上下文列表
     */
    public void setConfigListenContexts(List<ConfigListenContext> configListenContexts) {
        this.configListenContexts = configListenContexts;
    }
    
    /**
     * 是否为注册监听（而非取消）。
     *
     * @return 注册监听返回 {@code true}
     */
    public boolean isListen() {
        return listen;
    }
    
    /**
     * 设置监听或取消标志。
     *
     * @param listen {@code true} 注册监听，{@code false} 取消
     */
    public void setListen(boolean listen) {
        this.listen = listen;
    }
    
    /** 单条配置的监听上下文。 */
    public static class ConfigListenContext {
        
        /** 配置分组。 */
        String group;
        
        /** 客户端缓存的内容 MD5。 */
        String md5;
        
        /** 配置 Data ID。 */
        String dataId;
        
        /** 命名空间 ID。 */
        String tenant;
        
        /** 无参构造。 */
        public ConfigListenContext() {
            
        }
        
        @Override
        public String toString() {
            return "ConfigListenContext{" + "group='" + group + '\'' + ", md5='" + md5 + '\''
                + ", dataId='" + dataId
                + '\'' + ", tenant='" + tenant + '\'' + '}';
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
         * 获取内容 MD5。
         *
         * @return MD5 摘要
         */
        public String getMd5() {
            return md5;
        }
        
        /**
         * 设置内容 MD5。
         *
         * @param md5 MD5 摘要
         */
        public void setMd5(String md5) {
            this.md5 = md5;
        }
        
        /**
         * 获取 Data ID。
         *
         * @return Data ID
         */
        public String getDataId() {
            return dataId;
        }
        
        /**
         * 设置 Data ID。
         *
         * @param dataId Data ID
         */
        public void setDataId(String dataId) {
            this.dataId = dataId;
        }
        
        /**
         * 获取命名空间 ID。
         *
         * @return 租户 ID
         */
        public String getTenant() {
            return tenant;
        }
        
        /**
         * 设置命名空间 ID。
         *
         * @param tenant 租户 ID
         */
        public void setTenant(String tenant) {
            this.tenant = tenant;
        }
    }
}
