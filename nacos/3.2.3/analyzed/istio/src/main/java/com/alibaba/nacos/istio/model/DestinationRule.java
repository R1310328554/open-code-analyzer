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

package com.alibaba.nacos.istio.model;

import java.util.List;

/**
 * Istio {@code DestinationRule} CRD 的 Java 映射，描述目标服务子集与负载均衡策略。
 *
 * <p>用于解析 Nacos 中存储的 DestinationRule YAML/JSON，供 xDS 生成器消费。</p>
 */
public class DestinationRule {
    
    /** Kubernetes API 版本，如 {@code networking.istio.io/v1alpha3}。 */
    private String apiVersion;
    
    /** 资源类型，固定为 {@code DestinationRule}。 */
    private String kind;
    
    /** 资源元数据（名称、命名空间）。 */
    private Metadata metadata;
    
    /** 规则主体：目标主机与子集定义。 */
    private Spec spec;
    
    /** DestinationRule 的 Kubernetes 元数据。 */
    public static class Metadata {
        
        /** 规则名称。 */
        private String name;
        
        /** 规则所在命名空间。 */
        private String namespace;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getNamespace() {
            return namespace;
        }
        
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
    }
    
    /** DestinationRule 规格：绑定主机及其子集列表。 */
    public static class Spec {
        
        /** 目标服务主机名（FQDN 或服务名）。 */
        private String host;
        
        /** 按标签划分的子集列表，用于版本/环境路由。 */
        private List<Subset> subsets;
        
        /** 单个子集：名称 + 标签选择器。 */
        public static class Subset {
            
            /** 子集名称，VirtualService 路由时可引用。 */
            private String name;
            
            /** 实例标签匹配条件。 */
            private Labels labels;
            
            /** 子集标签，常见键为 {@code version}。 */
            public static class Labels {
                
                /** 版本标签值，用于灰度/多版本路由。 */
                private String version;
                
                public String getVersion() {
                    return version;
                }
                
                public void setVersion(String version) {
                    this.version = version;
                }
            }
            
            public String getName() {
                return name;
            }
            
            public void setName(String name) {
                this.name = name;
            }
            
            public Labels getLabels() {
                return labels;
            }
            
            public void setLabels(Labels labels) {
                this.labels = labels;
            }
        }
        
        public String getHost() {
            return host;
        }
        
        public void setHost(String host) {
            this.host = host;
        }
        
        public List<Subset> getSubsets() {
            return subsets;
        }
        
        public void setSubsets(List<Subset> subsets) {
            this.subsets = subsets;
        }
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
    public String getKind() {
        return kind;
    }
    
    public void setKind(String kind) {
        this.kind = kind;
    }
    
    public Metadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    
    public Spec getSpec() {
        return spec;
    }
    
    public void setSpec(Spec spec) {
        this.spec = spec;
    }
}
