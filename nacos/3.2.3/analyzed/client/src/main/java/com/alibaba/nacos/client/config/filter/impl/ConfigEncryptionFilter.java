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

package com.alibaba.nacos.client.config.filter.impl;

import com.alibaba.nacos.api.config.filter.AbstractConfigFilter;
import com.alibaba.nacos.api.config.filter.IConfigFilterChain;
import com.alibaba.nacos.api.config.filter.IConfigRequest;
import com.alibaba.nacos.api.config.filter.IConfigResponse;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.Pair;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.encryption.handler.EncryptionHandler;

import java.util.Objects;
import java.util.Properties;

/**
 * 配置加解密过滤器，在发布时加密、拉取时解密配置内容。
 *
 * <p>通过 {@link EncryptionHandler} 插件处理，order 为 0 优先执行。</p>
 *
 * @author lixiaoshuang
 */
public class ConfigEncryptionFilter extends AbstractConfigFilter {
    
    /** 过滤器默认名称（类全限定名）。 */
    private static final String DEFAULT_NAME = ConfigEncryptionFilter.class.getName();
    
    @Override
    public void init(Properties properties) {
        
    }
    
    @Override
    public void doFilter(IConfigRequest request, IConfigResponse response,
        IConfigFilterChain filterChain)
        throws NacosException {
        if (Objects.nonNull(request) && request instanceof ConfigRequest
            && Objects.isNull(response)) {
            
            // 发布配置：对内容进行加密
            ConfigRequest configRequest = (ConfigRequest) request;
            String dataId = configRequest.getDataId();
            String content = configRequest.getContent();
            
            Pair<String, String> pair = EncryptionHandler.encryptHandler(dataId, content);
            String secretKey = pair.getFirst();
            String encryptContent = pair.getSecond();
            if (!StringUtils.isBlank(encryptContent) && !encryptContent.equals(content)) {
                ((ConfigRequest) request).setContent(encryptContent);
            }
            if (!StringUtils.isBlank(secretKey)
                && !secretKey.equals(((ConfigRequest) request).getEncryptedDataKey())) {
                ((ConfigRequest) request).setEncryptedDataKey(secretKey);
            } else if (StringUtils.isBlank(((ConfigRequest) request).getEncryptedDataKey())
                && StringUtils.isBlank(secretKey)) {
                ((ConfigRequest) request).setEncryptedDataKey("");
            }
        }
        if (Objects.nonNull(response) && response instanceof ConfigResponse
            && Objects.isNull(request)) {
            
            // 拉取配置：对内容进行解密
            ConfigResponse configResponse = (ConfigResponse) response;
            
            String dataId = configResponse.getDataId();
            String encryptedDataKey = configResponse.getEncryptedDataKey();
            String content = configResponse.getContent();
            
            Pair<String, String> pair =
                EncryptionHandler.decryptHandler(dataId, encryptedDataKey, content);
            String secretKey = pair.getFirst();
            String decryptContent = pair.getSecond();
            if (!StringUtils.isBlank(decryptContent) && !decryptContent.equals(content)) {
                ((ConfigResponse) response).setContent(decryptContent);
            }
            if (!StringUtils.isBlank(secretKey)
                && !secretKey.equals(((ConfigResponse) response).getEncryptedDataKey())) {
                ((ConfigResponse) response).setEncryptedDataKey(secretKey);
            } else if (StringUtils.isBlank(((ConfigResponse) response).getEncryptedDataKey())
                && StringUtils.isBlank(secretKey)) {
                ((ConfigResponse) response).setEncryptedDataKey("");
            }
        }
        filterChain.doFilter(request, response);
    }
    
    @Override
    /** 过滤器执行顺序，值越小越先执行。 */
    public int getOrder() {
        return 0;
    }
    
    @Override
    /** 返回过滤器名称。 */
    public String getFilterName() {
        return DEFAULT_NAME;
    }
    
}
