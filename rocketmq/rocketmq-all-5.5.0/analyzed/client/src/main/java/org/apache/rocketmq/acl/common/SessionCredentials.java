/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.acl.common;

import org.apache.rocketmq.common.MixAll;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * ACL 会话凭证：承载 AccessKey、SecretKey、SecurityToken 与请求签名，
 * 默认可从 {@link #KEY_FILE} 指定的本地 key 文件加载。
 */
public class SessionCredentials {
    /** 凭证与签名使用的字符集。 */
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    /** Remoting 扩展字段：AccessKey 键名。 */
    public static final String ACCESS_KEY = "AccessKey";
    /** Remoting 扩展字段：SecretKey 键名。 */
    public static final String SECRET_KEY = "SecretKey";
    /** Remoting 扩展字段：Signature 键名。 */
    public static final String SIGNATURE = "Signature";
    /** 临时安全令牌字段名（STS 场景）。 */
    public static final String SECURITY_TOKEN = "SecurityToken";

    /** 本地 key 文件路径，可通过系统属性 rocketmq.client.keyFile 覆盖。 */
    public static final String KEY_FILE = System.getProperty("rocketmq.client.keyFile",
        System.getProperty("user.home") + File.separator + "key");

    private String accessKey;
    private String secretKey;
    private String securityToken;
    private String signature;

    /** 无参构造：尝试从 {@link #KEY_FILE} 加载 AccessKey/SecretKey。 */
    public SessionCredentials() {
        String keyContent = null;
        try {
            keyContent = MixAll.file2String(KEY_FILE);
        } catch (IOException ignore) {
        }
        if (keyContent != null) {
            Properties prop = MixAll.string2Properties(keyContent);
            if (prop != null) {
                this.updateContent(prop);
            }
        }
    }

    /** 使用 AccessKey 与 SecretKey 构造凭证。 */
    public SessionCredentials(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    /** 构造带临时 SecurityToken 的凭证。 */
    public SessionCredentials(String accessKey, String secretKey, String securityToken) {
        this(accessKey, secretKey);
        this.securityToken = securityToken;
    }

    /** 从 Properties 更新 AccessKey、SecretKey 与 SecurityToken。 */
    public void updateContent(Properties prop) {
        {
            String value = prop.getProperty(ACCESS_KEY);
            if (value != null) {
                this.accessKey = value.trim();
            }
        }
        {
            String value = prop.getProperty(SECRET_KEY);
            if (value != null) {
                this.secretKey = value.trim();
            }
        }
        {
            String value = prop.getProperty(SECURITY_TOKEN);
            if (value != null) {
                this.securityToken = value.trim();
            }
        }
    }

    /** 返回 AccessKey。 */
    public String getAccessKey() {
        return accessKey;
    }

    /** 设置 AccessKey。 */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /** 返回 SecretKey。 */
    public String getSecretKey() {
        return secretKey;
    }

    /** 设置 SecretKey。 */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /** 返回当前请求签名。 */
    public String getSignature() {
        return signature;
    }

    /** 设置请求签名。 */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /** 返回 SecurityToken。 */
    public String getSecurityToken() {
        return securityToken;
    }

    /** 设置 SecurityToken。 */
    public void setSecurityToken(final String securityToken) {
        this.securityToken = securityToken;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessKey == null) ? 0 : accessKey.hashCode());
        result = prime * result + ((secretKey == null) ? 0 : secretKey.hashCode());
        result = prime * result + ((signature == null) ? 0 : signature.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        SessionCredentials other = (SessionCredentials) obj;
        if (accessKey == null) {
            if (other.accessKey != null)
                return false;
        } else if (!accessKey.equals(other.accessKey))
            return false;

        if (secretKey == null) {
            if (other.secretKey != null)
                return false;
        } else if (!secretKey.equals(other.secretKey))
            return false;

        if (signature == null) {
            return other.signature == null;
        } else return signature.equals(other.signature);
    }

    @Override
    public String toString() {
        return "SessionCredentials [accessKey=" + accessKey + ", secretKey=" + secretKey + ", signature="
            + signature + ", SecurityToken=" + securityToken + "]";
    }
}