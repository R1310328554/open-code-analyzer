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

package com.alibaba.nacos.client.address;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.utils.ClientBasicParamUtil;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.lifecycle.Closeable;

import java.util.List;

/**
 * Server list provider.
 * <p>服务端地址列表 SPI 接口：定义初始化、列表获取、匹配优先级及是否固定列表等行为，由 {@link AbstractServerListManager} 按 order 选择实现。</p>
 * 
 * @author totalo 
 */
public interface ServerListProvider extends Closeable {
    
    /**
     * Init.
     * <p>根据客户端属性与 HTTP 模板完成地址源初始化。</p>
     * @param properties nacos client properties
     * @param nacosRestTemplate nacos rest template
     * @throws NacosException nacos exception
     */
    void init(final NacosClientProperties properties, final NacosRestTemplate nacosRestTemplate)
        throws NacosException;
    
    /**
     * Get server list.
     * <p>返回当前应连接的 Nacos Server 地址集合。</p>
     * @return server list
     */
    List<String> getServerList();
    
    /**
     * Get server name.
     * <p>用于标识该地址源实例的唯一名称。</p>
     * @return server name
     */
    default String getServerName() {
        return "";
    }
    
    /**
     * Get namespace.
     * <p>关联的命名空间，默认空串。</p>
     * @return namespace
     */
    default String getNamespace() {
        return "";
    }
    
    /**
     * Get context path.
     * <p>HTTP 请求的 context path 前缀。</p>
     * @return context path
     */
    default String getContextPath() {
        return ClientBasicParamUtil.getDefaultContextPath();
    }
    
    /**
     * Get order.
     * <p>SPI 匹配顺序，值越大优先级越高。</p>
     * @return order
     */
    int getOrder();
    
    /**
     * Match.
     * <p>判断当前 provider 是否适用于给定客户端配置。</p>
     * @param properties nacos client properties
     * @return match
     */
    boolean match(final NacosClientProperties properties);
    
    /**
     * check the server list is fixed or not.
     * <p>true 表示列表来自静态配置，不会后台自动刷新。</p>
     * @return true if the server list is fixed
     */
    default boolean isFixed() {
        return false;
    }
    
    /**
     * Get address source.
     * <p>人类可读的地址来源（如 endpoint URL），便于日志与诊断。</p>
     * @return address source
     */
    default String getAddressSource() {
        return "";
    }
    
}
