/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.utils.ClientBasicParamUtil;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.utils.StringUtils;

import java.util.List;

/**
 * Address server list provider.
 * <p>服务端地址列表提供者抽象基类：在 {@link #init} 中解析 context path 与 namespace，子类实现具体地址来源（properties 固定列表或 endpoint 动态刷新）。</p>
 * 
 * @author totalo
 */
public abstract class AbstractServerListProvider implements ServerListProvider {
    
    /** 请求 Nacos Server 的 context path，默认来自 ClientBasicParamUtil */
    protected String contextPath = ClientBasicParamUtil.getDefaultContextPath();
    
    /** 命名空间，空串表示 public */
    protected String namespace = "";
    
    @Override
    public void init(final NacosClientProperties properties,
        final NacosRestTemplate nacosRestTemplate) throws NacosException {
        if (null == properties) {
            throw new NacosException(NacosException.INVALID_PARAM, "properties is null");
        }
        initContextPath(properties);
        initNameSpace(properties);
    }
    
    /**
     * Get server list.
     * <p>返回当前可用的 Nacos Server 地址列表（ip:port 或完整 URL）。</p>
     * @return server list
     */
    @Override
    public abstract List<String> getServerList();
    
    /**
     * Get server name.
     * <p>生成用于缓存/日志的服务端标识字符串。</p>
     * @return server name
     */
    @Override
    public abstract String getServerName();
    
    /**
     * Get order.
     * <p>SPI 匹配优先级，数值越大越先尝试 {@link #match}。</p>
     * @return order
     */
    @Override
    public abstract int getOrder();
    
    /** 返回已解析的 context path */
    public String getContextPath() {
        return contextPath;
    }
    
    /** 返回已解析的 namespace */
    public String getNamespace() {
        return namespace;
    }
    
    /** 属性非空时覆盖默认 context path */
    private void initContextPath(NacosClientProperties properties) {
        String contentPathTmp = properties.getProperty(PropertyKeyConst.CONTEXT_PATH);
        if (!StringUtils.isBlank(contentPathTmp)) {
            this.contextPath = contentPathTmp;
        }
    }
    
    /** 属性非空时设置 namespace */
    private void initNameSpace(NacosClientProperties properties) {
        String namespace = properties.getProperty(PropertyKeyConst.NAMESPACE);
        if (StringUtils.isNotBlank(namespace)) {
            this.namespace = namespace;
        }
    }
}
