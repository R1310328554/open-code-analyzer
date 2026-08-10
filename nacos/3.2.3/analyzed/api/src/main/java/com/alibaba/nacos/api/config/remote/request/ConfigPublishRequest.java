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

import java.util.HashMap;
import java.util.Map;

/**
 * 发布配置的远程请求。
 *
 * <p>携带配置内容、CAS MD5 及可选扩展参数，由客户端发往服务端。</p>
 *
 * @author liuzunfei
 * @version $Id: ConfigPublishRequest.java, v 0.1 2020年07月16日 4:30 PM liuzunfei Exp $
 */
public class ConfigPublishRequest extends AbstractConfigRequest {
    
    /** 待发布的配置内容。 */
    String content;
    
    /** CAS 发布时期望的当前内容 MD5。 */
    String casMd5;
    
    /** 附加参数字典（如配置类型、加密密钥等）。 */
    private Map<String, String> additionMap;
    
    /** 无参构造。 */
    public ConfigPublishRequest() {
        
    }
    
    /**
     * 构造发布请求。
     *
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param tenant  命名空间 ID
     * @param content 配置内容
     */
    public ConfigPublishRequest(String dataId, String group, String tenant, String content) {
        this.content = content;
        super.setGroup(group);
        super.setTenant(tenant);
        super.setDataId(dataId);
    }
    
    /**
     * 获取附加参数值。
     *
     * @param key 参数键
     * @return 参数值，不存在时返回 {@code null}
     */
    public String getAdditionParam(String key) {
        return additionMap == null ? null : additionMap.get(key);
    }
    
    /**
     * 写入附加参数，已存在则覆盖。
     *
     * @param key   参数键
     * @param value 参数值
     */
    public void putAdditionalParam(String key, String value) {
        if (additionMap == null) {
            additionMap = new HashMap<>(2);
        }
        additionMap.put(key, value);
    }
    
    /**
     * 获取配置内容。
     *
     * @return 配置正文
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 设置配置内容。
     *
     * @param content 配置正文
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * 获取 CAS MD5。
     *
     * @return 期望的当前内容 MD5
     */
    public String getCasMd5() {
        return casMd5;
    }
    
    /**
     * 设置 CAS MD5。
     *
     * @param casMd5 期望的当前内容 MD5
     */
    public void setCasMd5(String casMd5) {
        this.casMd5 = casMd5;
    }
    
    /**
     * 获取附加参数字典。
     *
     * @return 附加参数映射
     */
    public Map<String, String> getAdditionMap() {
        return additionMap;
    }
    
    /**
     * 设置附加参数字典。
     *
     * @param additionMap 附加参数映射
     */
    public void setAdditionMap(Map<String, String> additionMap) {
        this.additionMap = additionMap;
    }
    
}
