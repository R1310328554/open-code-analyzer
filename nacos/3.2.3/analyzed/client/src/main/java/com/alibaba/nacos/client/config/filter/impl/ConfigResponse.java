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

import com.alibaba.nacos.api.config.filter.IConfigContext;
import com.alibaba.nacos.api.config.filter.IConfigResponse;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.client.config.common.ConfigConstants.CONFIG_TYPE;
import static com.alibaba.nacos.client.config.common.ConfigConstants.CONTENT;
import static com.alibaba.nacos.client.config.common.ConfigConstants.DATA_ID;
import static com.alibaba.nacos.client.config.common.ConfigConstants.ENCRYPTED_DATA_KEY;
import static com.alibaba.nacos.client.config.common.ConfigConstants.GROUP;
import static com.alibaba.nacos.client.config.common.ConfigConstants.MD5;
import static com.alibaba.nacos.client.config.common.ConfigConstants.TENANT;

/**
 * 配置拉取响应封装，供过滤器链在返回前处理（如解密）。
 *
 * <p>实现 {@link IConfigResponse}，可携带 MD5、configType 等元数据。</p>
 *
 * @author Nacos
 */
public class ConfigResponse implements IConfigResponse {
    
    /** 响应参数字典。 */
    private final Map<String, Object> param = new HashMap<>();
    
    /** 过滤器上下文。 */
    private final IConfigContext configContext = new ConfigContext();
    
    /** 获取租户。 */
    public String getTenant() {
        return (String) param.get(TENANT);
    }
    
    /** 设置租户。 */
    public void setTenant(String tenant) {
        param.put(TENANT, tenant);
    }
    
    /** 获取 dataId。 */
    public String getDataId() {
        return (String) param.get(DATA_ID);
    }
    
    /** 设置 dataId。 */
    public void setDataId(String dataId) {
        param.put(DATA_ID, dataId);
    }
    
    /** 获取 group。 */
    public String getGroup() {
        return (String) param.get(GROUP);
    }
    
    /** 设置 group。 */
    public void setGroup(String group) {
        param.put(GROUP, group);
    }
    
    /** 获取配置内容。 */
    public String getContent() {
        return (String) param.get(CONTENT);
    }
    
    /** 设置配置内容。 */
    public void setContent(String content) {
        param.put(CONTENT, content);
    }
    
    /** 获取配置类型。 */
    public String getConfigType() {
        return (String) param.get(CONFIG_TYPE);
    }
    
    /** 设置配置类型。 */
    public void setConfigType(String configType) {
        param.put(CONFIG_TYPE, configType);
    }
    
    /** 获取加密数据密钥。 */
    public String getEncryptedDataKey() {
        return (String) param.get(ENCRYPTED_DATA_KEY);
    }
    
    /** 设置加密数据密钥。 */
    public void setEncryptedDataKey(String encryptedDataKey) {
        param.put(ENCRYPTED_DATA_KEY, encryptedDataKey);
    }
    
    /**
     * 获取配置内容的 MD5 摘要。
     *
     * @return MD5 字符串
     * @since 3.0
     */
    public String getMd5() {
        return (String) param.get(MD5);
    }
    
    /**
     * 设置配置内容的 MD5 摘要。
     *
     * @param md5 MD5 字符串
     * @since 3.0
     */
    public void setMd5(String md5) {
        param.put(MD5, md5);
    }
    
    @Override
    public Object getParameter(String key) {
        return param.get(key);
    }
    
    @Override
    public void putParameter(String key, Object value) {
        param.put(key, value);
    }
    
    @Override
    public IConfigContext getConfigContext() {
        return configContext;
    }
    
}
