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

package com.alibaba.nacos.client.auth.ram.identify;

/**
 * Credentials.
 * <p>RAM/SPAS 凭证值对象：AccessKey、SecretKey 与可选 tenantId，实现 {@link SpasCredential} 供签名模块读取。</p>
 *
 * @author Nacos
 */
public class Credentials implements SpasCredential {
    
    /** 访问密钥 ID */
    private volatile String accessKey;
    
    /** 访问密钥 Secret */
    private volatile String secretKey;
    
    /** 租户/账号标识，部分部署场景必填 */
    private volatile String tenantId;
    
    /** 构造空凭证（尚未加载）。 */
    public Credentials() {
        this(null, null, null);
    }
    
    /** @param accessKey AK @param secretKey SK @param tenantId 租户 ID */
    public Credentials(String accessKey, String secretKey, String tenantId) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.tenantId = tenantId;
    }
    
    @Override
    public String getAccessKey() {
        return accessKey;
    }
    
    /** @param accessKey 设置 AccessKey */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
    
    @Override
    public String getSecretKey() {
        return secretKey;
    }
    
    /** @param secretKey 设置 SecretKey */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    /** @return 租户 ID */
    public String getTenantId() {
        return tenantId;
    }
    
    /** @param tenantId 租户 ID */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    /** @return accessKey 与 secretKey 均非空时为 true */
    public boolean valid() {
        return accessKey != null && !accessKey.isEmpty() && secretKey != null
            && !secretKey.isEmpty();
    }
    
    /**
     * Identical.
     *
     * @param other other
     * @return true if identical
     *         AK/SK 与另一实例相同（含双 null）时为 true
     */
    public boolean identical(Credentials other) {
        return this == other || (other != null && (accessKey == null && other.accessKey == null
            || accessKey != null && accessKey.equals(other.accessKey))
            && (secretKey == null && other.secretKey == null || secretKey != null && secretKey
                .equals(other.secretKey)));
    }
}
