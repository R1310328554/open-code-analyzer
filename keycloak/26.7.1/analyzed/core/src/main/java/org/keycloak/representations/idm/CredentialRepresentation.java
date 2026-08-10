/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.representations.idm;

import org.keycloak.common.util.MultivaluedHashMap;

/**
 * 用户凭据（Credential）的 REST 表示，用于创建、更新或展示密码、OTP 等各类认证凭据。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CredentialRepresentation {
    /** 客户端密钥凭据类型常量。 */
    public static final String SECRET = "secret";
    /** 密码凭据类型常量。 */
    public static final String PASSWORD = "password";
    /** TOTP 凭据类型常量。 */
    public static final String TOTP = "totp";
    /** HOTP 凭据类型常量。 */
    public static final String HOTP = "hotp";
    /** Kerberos 凭据类型常量。 */
    public static final String KERBEROS = "kerberos";

    /** 凭据持久化 ID。 */
    private String id;
    /** 凭据类型（如 password、otp 等）。 */
    private String type;
    /** 用户自定义标签（如"工作手机"）。 */
    private String userLabel;
    /** 凭据创建时间（Unix 毫秒）。 */
    private Long createdDate;
    /** 敏感数据 JSON 字符串（由 CredentialProvider 解析）。 */
    private String secretData;
    /** 非敏感元数据 JSON 字符串。 */
    private String credentialData;
    /** 凭据优先级（数值越小优先级越高）。 */
    private Integer priority;

    /** 明文凭据值（仅用于创建/更新时提交）。 */
    private String value;

    // 仅用于更新凭据时，可能触发 Required Action
    /** 是否为临时凭据（首次登录后须修改）。 */
    protected Boolean temporary;

    // 以下字段仅为向后兼容保留
    /** @deprecated 旧版 OTP 设备标识 */
    @Deprecated
    protected String device;
    /** @deprecated 旧版哈希加盐值 */
    @Deprecated
    protected String hashedSaltedValue;
    /** @deprecated 旧版盐值 */
    @Deprecated
    protected String salt;
    /** @deprecated 旧版哈希迭代次数 */
    @Deprecated
    protected Integer hashIterations;
    /** @deprecated 旧版 HOTP 计数器 */
    @Deprecated
    protected Integer counter;
    /** @deprecated 旧版算法名称 */
    @Deprecated
    private String algorithm;
    /** @deprecated 旧版 OTP 位数 */
    @Deprecated
    private Integer digits;
    /** @deprecated 旧版 TOTP 周期（秒） */
    @Deprecated
    private Integer period;
    /** @deprecated 旧版扩展配置 */
    @Deprecated
    private MultivaluedHashMap<String, String> config;
    /** 联邦存储组件链接 ID。 */
    private String federationLink;

    /** @return 凭据 ID */
    public String getId() {
        return id;
    }
    /** @param id 凭据 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 凭据类型 */
    public String getType() {
        return type;
    }
    /** @param type 凭据类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** @return 用户标签 */
    public String getUserLabel() {
        return userLabel;
    }
    /** @param userLabel 用户标签 */
    public void setUserLabel(String userLabel) {
        this.userLabel = userLabel;
    }

    /** @return 敏感数据 JSON */
    public String getSecretData() {
        return secretData;
    }
    /** @param secretData 敏感数据 JSON */
    public void setSecretData(String secretData) {
        this.secretData = secretData;
    }

    /** @return 凭据元数据 JSON */
    public String getCredentialData() {
        return credentialData;
    }
    /** @param credentialData 凭据元数据 JSON */
    public void setCredentialData(String credentialData) {
        this.credentialData = credentialData;
    }

    /** @return 凭据优先级 */
    public Integer getPriority() {
        return priority;
    }

    /** @param priority 凭据优先级 */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /** @return 创建时间（毫秒） */
    public Long getCreatedDate() {
        return createdDate;
    }
    /** @param createdDate 创建时间（毫秒） */
    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }


    /** @return 明文凭据值 */
    public String getValue() {
        return value;
    }
    /** @param value 明文凭据值 */
    public void setValue(String value) {
        this.value = value;
    }

    /** @return 是否为临时凭据 */
    public Boolean isTemporary() {
        return temporary;
    }
    /** @param temporary 是否为临时凭据 */
    public void setTemporary(Boolean temporary) {
        this.temporary = temporary;
    }

    /** @deprecated 旧版 OTP 设备标识 */
    @Deprecated
    public String getDevice() {
        return device;
    }

    /** @deprecated 旧版哈希加盐值 */
    @Deprecated
    public String getHashedSaltedValue() {
        return hashedSaltedValue;
    }

    /** @deprecated 旧版盐值 */
    @Deprecated
    public String getSalt() {
        return salt;
    }

    /** @deprecated 旧版哈希迭代次数 */
    @Deprecated
    public Integer getHashIterations() {
        return hashIterations;
    }

    /** @deprecated 旧版 HOTP 计数器 */
    @Deprecated
    public Integer getCounter() {
        return counter;
    }

    /** @deprecated 旧版算法名称 */
    @Deprecated
    public String getAlgorithm() {
        return algorithm;
    }

    /** @deprecated 旧版 OTP 位数 */
    @Deprecated
    public Integer getDigits() {
        return digits;
    }

    /** @deprecated 旧版 TOTP 周期 */
    @Deprecated
    public Integer getPeriod() {
        return period;
    }

    /** @deprecated 旧版扩展配置 */
    @Deprecated
    public MultivaluedHashMap<String, String> getConfig() {
        return config;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
        result = prime * result + ((userLabel == null) ? 0 : userLabel.hashCode());
        result = prime * result + ((secretData == null) ? 0 : secretData.hashCode());
        result = prime * result + ((credentialData == null) ? 0 : credentialData.hashCode());
        result = prime * result + ((temporary == null) ? 0 : temporary.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((priority == null) ? 0 : priority);
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
        CredentialRepresentation other = (CredentialRepresentation) obj;
        if (secretData == null) {
            if (other.secretData != null)
                return false;
        } else if (!secretData.equals(other.secretData))
            return false;
        if (credentialData == null) {
            if (other.credentialData != null)
                return false;
        } else if (!credentialData.equals(other.credentialData))
            return false;
        if (createdDate == null) {
            if (other.createdDate != null)
                return false;
        } else if (!createdDate.equals(other.createdDate))
            return false;
        if (userLabel == null) {
            if (other.userLabel != null)
                return false;
        } else if (!userLabel.equals(other.userLabel))
            return false;
        if (temporary == null) {
            if (other.temporary != null)
                return false;
        } else if (!temporary.equals(other.temporary))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (priority == null) {
            if (other.priority != null)
                return false;
        } else if (!priority.equals(other.priority))
            return false;
        return true;
    }

    /** @param federationLink 联邦存储组件链接 ID */
    public void setFederationLink(String federationLink) {
        this.federationLink = federationLink;
    }

    /** @return 联邦存储组件链接 ID */
    public String getFederationLink() {
        return federationLink;
    }
}
