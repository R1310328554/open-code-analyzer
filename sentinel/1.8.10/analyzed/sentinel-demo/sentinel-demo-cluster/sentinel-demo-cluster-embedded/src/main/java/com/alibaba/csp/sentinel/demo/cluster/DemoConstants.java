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
package com.alibaba.csp.sentinel.demo.cluster;

/**
 * 集群流控嵌入式 Demo 的 Apollo/Nacos 等动态配置 Key 后缀常量。
 *
 * @author Eric Zhao
 */
public final class DemoConstants {

    /** 流控规则配置 Key 后缀。 */
    public static final String FLOW_POSTFIX = "-flow-rules";
    /** 热点参数规则配置 Key 后缀。 */
    public static final String PARAM_FLOW_POSTFIX = "-param-rules";
    /** 集群服务端命名空间集合配置 Key 后缀。 */
    public static final String SERVER_NAMESPACE_SET_POSTFIX = "-cs-namespace-set";
    /** 集群客户端配置 Key 后缀。 */
    public static final String CLIENT_CONFIG_POSTFIX = "-cc-config";
    /** 集群 Token Server/Client 映射配置 Key 后缀。 */
    public static final String CLUSTER_MAP_POSTFIX = "-cluster-map";

    private DemoConstants() {}
}
