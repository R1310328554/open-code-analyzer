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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Sts credential for aliyun RAM.
 * <p>阿里云 RAM STS 临时凭证模型：与 ECS 元数据或 STS API 返回 JSON 字段一一对应，Jackson 反序列化后供 {@link StsCredentialHolder} 缓存与刷新。</p>
 *
 * @author xiweng.yy
 */
public class StsCredential {
    
    /** 临时 AccessKey ID */
    @JsonProperty(value = "AccessKeyId")
    private String accessKeyId;
    
    /** 临时 AccessKey Secret */
    @JsonProperty(value = "AccessKeySecret")
    private String accessKeySecret;
    
    /** 凭证过期时间（UTC） */
    @JsonProperty(value = "Expiration")
    private Date expiration;
    
    /** STS SecurityToken，请求头需携带以完成临时身份鉴权 */
    @JsonProperty(value = "SecurityToken")
    private String securityToken;
    
    /** 凭证最后更新时间 */
    @JsonProperty(value = "LastUpdated")
    private Date lastUpdated;
    
    /** 元数据接口返回状态码（如 Success） */
    @JsonProperty(value = "Code")
    private String code;
    
    /** 返回临时 AccessKey ID */
    public String getAccessKeyId() {
        return accessKeyId;
    }
    
    /** 设置临时 AccessKey ID */
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }
    
    /** 返回临时 AccessKey Secret */
    public String getAccessKeySecret() {
        return accessKeySecret;
    }
    
    /** 设置临时 AccessKey Secret */
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
    
    /** 返回凭证过期时间 */
    public Date getExpiration() {
        return expiration;
    }
    
    /** 设置凭证过期时间 */
    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
    
    /** 返回 SecurityToken */
    public String getSecurityToken() {
        return securityToken;
    }
    
    /** 设置 SecurityToken */
    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
    
    /** 返回最后更新时间 */
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    /** 设置最后更新时间 */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /** 返回元数据状态码 */
    public String getCode() {
        return code;
    }
    
    /** 设置元数据状态码 */
    public void setCode(String code) {
        this.code = code;
    }
    
    /** 调试输出（含敏感字段，生产日志慎用） */
    @Override
    public String toString() {
        return "STSCredential{" + "accessKeyId='" + accessKeyId + '\'' + ", accessKeySecret='"
            + accessKeySecret
            + '\'' + ", expiration=" + expiration + ", securityToken='" + securityToken + '\''
            + ", lastUpdated=" + lastUpdated + ", code='" + code + '\'' + '}';
    }
}
