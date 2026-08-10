/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.api;

import com.alibaba.nacos.plugin.auth.constant.SignType;

/**
 * 客户端请求资源描述，用于标识一次 API 调用所涉及的业务资源。
 *
 * <p>通过 Builder 模式构建，支持 naming、config、lock、ai 等多种请求类型，
 * 客户端认证插件可据此生成对应的 {@link LoginIdentityContext}。</p>
 *
 * @author xiweng.yy
 */
public class RequestResource {
    
    /**
     * 请求类型：naming 或 config 等，参见 {@link SignType}。
     */
    private String type;
    
    /**
     * 资源所属命名空间。
     */
    private String namespace;
    
    /**
     * 资源所属分组。
     */
    private String group;
    
    /**
     * 具体资源名称。
     * <p>naming 类型时为服务名；config 类型时为 dataId。</p>
     */
    private String resource;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    /**
     * 创建 naming 类型请求资源的 Builder。
     *
     * @return naming 请求资源构建器
     */
    public static Builder namingBuilder() {
        Builder result = new Builder();
        result.setType(SignType.NAMING);
        return result;
    }
    
    /**
     * 创建 config 类型请求资源的 Builder。
     *
     * @return config 请求资源构建器
     */
    public static Builder configBuilder() {
        Builder result = new Builder();
        result.setType(SignType.CONFIG);
        return result;
    }
    
    /**
     * 创建 lock 类型请求资源的 Builder。
     *
     * @return lock 请求资源构建器
     */
    public static Builder lockBuilder() {
        Builder result = new Builder();
        result.setType(SignType.LOCK);
        return result;
    }
    
    /**
     * 创建 AI 类型请求资源的 Builder。
     *
     * @return AI 请求资源构建器
     */
    public static Builder aiBuilder() {
        Builder result = new Builder();
        result.setType(SignType.AI);
        return result;
    }
    
    /**
     * {@link RequestResource} 的流式构建器。
     */
    public static class Builder {
        
        private String type;
        
        private String namespace;
        
        private String group;
        
        private String resource;
        
        public void setType(String type) {
            this.type = type;
        }
        
        public Builder setNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }
        
        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }
        
        public Builder setResource(String resource) {
            this.resource = resource;
            return this;
        }
        
        /**
         * 构建 {@link RequestResource} 实例。
         *
         * @return 组装完成的请求资源对象
         */
        public RequestResource build() {
            RequestResource result = new RequestResource();
            result.setType(type);
            result.setNamespace(namespace);
            result.setGroup(group);
            result.setResource(resource);
            return result;
        }
    }
}
