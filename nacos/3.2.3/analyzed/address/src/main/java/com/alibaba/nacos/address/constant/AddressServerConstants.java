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

package com.alibaba.nacos.address.constant;

import com.alibaba.nacos.naming.misc.UtilsAndCommons;

/**
 * Uniform constant parameter naming for address servers and default values ​​for related parameters.
 * <p>地址服务器统一常量：默认端口、产品名、集群名、服务名分隔符及 REST 前缀 {@link #ADDRESS_SERVER_REQUEST_URL}。</p>
 *
 * @author pbting
 * @date 2019-06-17 7:23 PM
 * @since 1.1.0
 */
public interface AddressServerConstants {
    
    /**
     * the default server port when create the Instance object.
     * <p>创建 {@link com.alibaba.nacos.api.naming.pojo.Instance} 时未指定端口的默认值（8848）。</p>
     */
    int DEFAULT_SERVER_PORT = 8848;
    
    /**
     * when post ips is not given the product,then use the default.
     * <p>POST 注册 IP 时未传 product 参数使用的默认产品标识。</p>
     */
    String DEFAULT_PRODUCT = "nacos";
    
    /**
     * the separator for service name between raw service name and group.
     * <p>服务名与分组之间的分隔符（{@code @@}）。</p>
     */
    String GROUP_SERVICE_NAME_SEP = "@@";
    
    /**
     * when post ips is not given the cluster,then use the default.
     * <p>未指定 cluster 时使用的默认集群名 serverlist。</p>
     */
    String DEFAULT_GET_CLUSTER = "serverlist";
    
    /**
     * post multi ip will use the "," to separator.
     * <p>批量 POST 多个 IP 时的逗号分隔符。</p>
     */
    String MULTI_IPS_SEPARATOR = ",";
    
    /**
     * the default product name when deploy nacos with naming and config.
     * <p>命名与配置合部署时的默认产品域名 nacos.as.default。</p>
     */
    String ALIWARE_NACOS_DEFAULT_PRODUCT_NAME = "nacos.as.default";
    
    /**
     * when the config and naming will separate deploy,then must specify product name by the client.
     * <p>配置与命名分离部署时，客户端需指定产品名，模板为 nacos.as.%s。</p>
     */
    String ALIWARE_NACOS_PRODUCT_DOM_TEMPLATE = "nacos.as.%s";
    
    /**
     * the url for address server prefix.
     * <p>地址服务 REST API 路径前缀（/nacos/v1/as）。</p>
     */
    String ADDRESS_SERVER_REQUEST_URL =
        UtilsAndCommons.NACOS_SERVER_CONTEXT + UtilsAndCommons.NACOS_SERVER_VERSION + "/as";
    
}
