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

package org.keycloak.authentication.authenticators.broker.util;

import java.io.IOException;

import org.keycloak.util.JsonSerialization;

/**
 * 已存在用户信息：broker 检测到属性冲突时记录重复用户 ID 及冲突属性名/值，可序列化存入认证会话 note。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ExistingUserInfo {
    /** 已存在用户的 ID。 */
    private String existingUserId;
    /** 发生冲突的属性名。 */
    private String duplicateAttributeName;
    /** 发生冲突的属性值。 */
    private String duplicateAttributeValue;

    public ExistingUserInfo() {}

    /** 构造包含冲突详情的已存在用户信息。 */
    public ExistingUserInfo(String existingUserId, String duplicateAttributeName, String duplicateAttributeValue) {
        this.existingUserId = existingUserId;
        this.duplicateAttributeName = duplicateAttributeName;
        this.duplicateAttributeValue = duplicateAttributeValue;
    }

    public String getExistingUserId() {
        return existingUserId;
    }

    public void setExistingUserId(String existingUserId) {
        this.existingUserId = existingUserId;
    }

    public String getDuplicateAttributeName() {
        return duplicateAttributeName;
    }

    public void setDuplicateAttributeName(String duplicateAttributeName) {
        this.duplicateAttributeName = duplicateAttributeName;
    }

    public String getDuplicateAttributeValue() {
        return duplicateAttributeValue;
    }

    public void setDuplicateAttributeValue(String duplicateAttributeValue) {
        this.duplicateAttributeValue = duplicateAttributeValue;
    }

    /** 将对象序列化为 JSON 字符串。 */
    public String serialize() {
        try {
            return JsonSerialization.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 从 JSON 字符串反序列化为 {@link ExistingUserInfo}。 */
    public static ExistingUserInfo deserialize(String serialized) {
        try {
            return JsonSerialization.readValue(serialized, ExistingUserInfo.class);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
