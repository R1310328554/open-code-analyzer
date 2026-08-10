package com.alibaba.nacos.istio.model;
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

import java.util.List;

/**
 * Istio {@code VirtualService} CRD 的 Java 映射，描述 L7 路由、重写与重定向规则。
 *
 * <p>嵌套结构对应 Kubernetes YAML 中的 metadata/spec/http 层级。</p>
 */
public class VirtualService {
    
    /** Kubernetes API 版本。 */
    private String apiVersion;
    
    /** 资源类型，固定为 {@code VirtualService}。 */
    private String kind;
    
    /** 资源元数据。 */
    private Metadata metadata;
    
    /** 路由规则主体。 */
    private Spec spec;
    
    public VirtualService() {}
    
    /** VirtualService 的 Kubernetes 元数据。 */
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
    
    /** VirtualService 规格：匹配主机与 HTTP 路由链。 */
    public static class Spec {
        
        /** 本规则适用的目标主机列表。 */
        private List<String> hosts;
        
        /** HTTP 层路由规则列表（按顺序匹配）。 */
        private List<Http> http;
        
        /** 单条 HTTP 路由规则。 */
        public static class Http {
            
            /** 规则名称，便于调试与引用。 */
            private String name;
            
            /** URI/头等匹配条件。 */
            private List<Match> match;
            
            /** 匹配成功后的 URI 重写。 */
            private Rewrite rewrite;
            
            /** 转发目标列表（权重路由）。 */
            private List<Route> route;
    
            /** HTTP 重定向配置。 */
            private Redirect redirect;
            
            /** 请求匹配条件。 */
            public static class Match {
                
                /** URI 匹配规则。 */
                private Uri uri;
                
                /** URI 前缀/精确/正则匹配。 */
                public static class Uri {
                    
                    /** 前缀匹配。 */
                    private String prefix;
    
                    /** 精确匹配。 */
                    private String exact;
    
                    /** 正则匹配。 */
                    private String regex;
    
                    public String getPrefix() {
                        return prefix;
                    }
                    
                    public void setPrefix(String prefix) {
                        this.prefix = prefix;
                    }
    
                    public String getExact() {
                        return exact;
                    }
    
                    public void setExact(String exact) {
                        this.exact = exact;
                    }
    
                    public String getRegex() {
                        return regex;
                    }
    
                    public void setRegex(String regex) {
                        this.regex = regex;
                    }
                }
                
                public Uri getUri() {
                    return uri;
                }
                
                public void setUri(Uri uri) {
                    this.uri = uri;
                }
            }
            
            /** URI 重写规则。 */
            public static class Rewrite {
                
                /** 重写后的 URI 路径。 */
                private String uri;
                
                public String getUri() {
                    return uri;
                }
                
                public void setUri(String uri) {
                    this.uri = uri;
                }
            }
            
            /** 单条转发路由，指向 DestinationRule 子集。 */
            public static class Route {
                
                /** 目标服务与子集。 */
                private Destination destination;
                
                /** 转发目标描述。 */
                public static class Destination {
                    
                    /** 目标服务主机。 */
                    private String host;
                    
                    /** DestinationRule 子集名。 */
                    private String subset;
    
                    /** 目标端口。 */
                    private Port port;
    
                    /** 目标端口号。 */
                    public static class Port {
        
                        /** 端口号。 */
                        private int number;
        
                        public int getNumber() {
                            return number;
                        }
        
                        public void setNumber(int number) {
                            this.number = number;
                        }
                    }
    
                    public Port getPort() {
                        return port;
                    }
    
                    public void setPort(Port port) {
                        this.port = port;
                    }
                    
                    public String getHost() {
                        return host;
                    }
                    
                    public void setHost(String host) {
                        this.host = host;
                    }
                    
                    public String getSubset() {
                        return subset;
                    }
                    
                    public void setSubset(String subset) {
                        this.subset = subset;
                    }
                }
                
                public Destination getDestination() {
                    return destination;
                }
                
                public void setDestination(Destination destination) {
                    this.destination = destination;
                }
            }
    
            /** HTTP 重定向配置。 */
            public static class Redirect {
        
                /** 重定向 URI。 */
                private String uri;
                
                /** 重定向 authority（主机:端口）。 */
                private String authority;
        
                public String getUri() {
                    return uri;
                }
        
                public void setUri(String uri) {
                    this.uri = uri;
                }
        
                public String getAuthority() {
                    return authority;
                }
        
                public void setAuthority(String authority) {
                    this.authority = authority;
                }
            }
            
            public String getName() {
                return name;
            }
            
            public void setName(String name) {
                this.name = name;
            }
            
            public List<Match> getMatch() {
                return match;
            }
            
            public void setMatch(List<Match> match) {
                this.match = match;
            }
            
            public Rewrite getRewrite() {
                return rewrite;
            }
            
            public void setRewrite(Rewrite rewrite) {
                this.rewrite = rewrite;
            }
            
            public List<Route> getRoute() {
                return route;
            }
            
            public void setRoute(List<Route> route) {
                this.route = route;
            }
    
            public Redirect getRedirect() {
                return redirect;
            }
    
            public void setRedirect(Redirect redirect) {
                this.redirect = redirect;
            }
        }
        
        public List<String> getHosts() {
            return hosts;
        }
        
        public void setHosts(List<String> hosts) {
            this.hosts = hosts;
        }
        
        public List<Http> getHttp() {
            return http;
        }
        
        public void setHttp(List<Http> http) {
            this.http = http;
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
