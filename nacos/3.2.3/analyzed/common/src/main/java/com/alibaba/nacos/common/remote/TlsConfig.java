/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.remote;

/**
 * gRPC TLS 通用配置基类：封装 SSL 提供者、协议版本、证书链、双向认证等
 * 安全传输参数，供服务端与客户端 TLS 配置类继承。
 * gRPC config.
 *
 * @author githubcheng2978
 */
public class TlsConfig {
    
    /** SSL 引擎提供者，可选 OPENSSL、JDK、OPENSSL_REFCNT */

    private String sslProvider = "";
    
    /** 是否启用 TLS 加密传输 */

    private Boolean enableTls = false;
    
    /** TLS 协议版本，多版本以逗号分隔，如 TLSv1.2,TLSv1.3 */

    private String protocols;
    
    /** 加密套件列表，用法同 protocols，逗号分隔 */

    private String ciphers;
    
    /** 客户端/服务端私钥文件路径 */

    private String certPrivateKey;
    
    /** 证书链文件路径 */

    private String certChainFile;
    
    /** 读取私钥文件时所需的密码 */

    private String certPrivateKeyPassword;
    
    /** 双向 TLS 认证开关；为 true 时必须提供私钥与证书链 */

    private Boolean mutualAuthEnable = false;
    
    /** 是否信任所有证书（跳过校验，仅测试环境使用） */

    private Boolean trustAll = false;
    
    /** 受信任 CA 证书集合文件路径 */

    private String trustCollectionCertFile;
    
    public Boolean getEnableTls() {
        return enableTls;
    }
    
    public void setEnableTls(Boolean enableTls) {
        this.enableTls = enableTls;
    }
    
    public Boolean getMutualAuthEnable() {
        return mutualAuthEnable;
    }
    
    public void setMutualAuthEnable(Boolean mutualAuthEnable) {
        this.mutualAuthEnable = mutualAuthEnable;
    }
    
    public String getProtocols() {
        return protocols;
    }
    
    public void setProtocols(String protocols) {
        this.protocols = protocols;
    }
    
    public Boolean getTrustAll() {
        return trustAll;
    }
    
    public void setTrustAll(Boolean trustAll) {
        this.trustAll = trustAll;
    }
    
    public String getCiphers() {
        return ciphers;
    }
    
    public void setCiphers(String ciphers) {
        this.ciphers = ciphers;
    }
    
    public String getTrustCollectionCertFile() {
        return trustCollectionCertFile;
    }
    
    public void setTrustCollectionCertFile(String trustCollectionCertFile) {
        this.trustCollectionCertFile = trustCollectionCertFile;
    }
    
    public String getCertPrivateKeyPassword() {
        return certPrivateKeyPassword;
    }
    
    public void setCertPrivateKeyPassword(String certPrivateKeyPassword) {
        this.certPrivateKeyPassword = certPrivateKeyPassword;
    }
    
    public String getCertPrivateKey() {
        return certPrivateKey;
    }
    
    public void setCertPrivateKey(String certPrivateKey) {
        this.certPrivateKey = certPrivateKey;
    }
    
    public String getCertChainFile() {
        return certChainFile;
    }
    
    public void setCertChainFile(String certChainFile) {
        this.certChainFile = certChainFile;
    }
    
    public String getSslProvider() {
        return sslProvider;
    }
    
    public void setSslProvider(String sslProvider) {
        this.sslProvider = sslProvider;
    }
    
}
