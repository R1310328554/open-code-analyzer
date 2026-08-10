/*
 *
 *  * Copyright 2021  Red Hat, Inc. and/or its affiliates
 *  * and other contributors as indicated by the @author tags.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.keycloak.userprofile;

import java.util.List;
import java.util.Map;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * 用户配置属性上下文：封装单次属性读写/校验所需的会话、用户、属性值与元数据。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public final class AttributeContext {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 属性名与值列表条目。 */
    private final Map.Entry<String, List<String>> attribute;
    /** 目标用户。 */
    private final UserModel user;
    /** 属性元数据。 */
    private final AttributeMetadata metadata;
    /** 所属 {@link Attributes} 集合。 */
    private final Attributes attributes;
    /** 用户配置上下文（注册、更新、管理等）。 */
    private UserProfileContext context;

    /** 构造属性上下文。
     * @param context 用户配置上下文
     * @param session Keycloak 会话
     * @param attribute 属性名值对
     * @param user 用户模型
     * @param metadata 属性元数据
     * @param attributes 属性集合 */
    public AttributeContext(UserProfileContext context, KeycloakSession session, Map.Entry<String, List<String>> attribute,
            UserModel user, AttributeMetadata metadata, Attributes attributes) {
        this.context = context;
        this.session = session;
        this.attribute = attribute;
        this.user = user;
        this.metadata = metadata;
        this.attributes = attributes;
    }

    /** @return Keycloak 会话 */
    public KeycloakSession getSession() {
        return session;
    }

    /** @return 属性名值条目 */
    public Map.Entry<String, List<String>> getAttribute() {
        return attribute;
    }

    /** @return 用户模型 */
    public UserModel getUser() {
        return user;
    }

    /** @return 用户配置上下文 */
    public UserProfileContext getContext() {
        return context;
    }

    /** @return 属性元数据 */
    public AttributeMetadata getMetadata() {
        return metadata;
    }

    /** @return 属性集合 */
    public Attributes getAttributes() {
        return attributes;
    }
}
