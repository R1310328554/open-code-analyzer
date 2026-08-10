/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.testframework.realm;

import java.util.HashMap;

import org.keycloak.representations.idm.ClientScopeRepresentation;

/**
 * {@link ClientScopeRepresentation} 的流式构建器，用于在测试中定义客户端作用域。
 *
 * @author <a href="mailto:yoshiyuki.tabata.jy@hitachi.com">Yoshiyuki Tabata</a>
 */
public class ClientScopeBuilder extends Builder<ClientScopeRepresentation> {

    /** 基于已有表示对象构造构建器。 */
    private ClientScopeBuilder(ClientScopeRepresentation rep) {
        super(rep);
    }

    /** 创建空的客户端作用域构建器。 */
    public static ClientScopeBuilder create() {
        return new ClientScopeBuilder(new ClientScopeRepresentation());
    }

    /** 基于已有作用域表示对象创建更新用构建器。 */
    public static ClientScopeBuilder update(ClientScopeRepresentation rep) {
        return new ClientScopeBuilder(rep);
    }

    /** 设置作用域名称。 */
    public ClientScopeBuilder name(String name) {
        rep.setName(name);
        return this;
    }

    /** 设置作用域描述。 */
    public ClientScopeBuilder description(String description) {
        rep.setDescription(description);
        return this;
    }

    /** 设置作用域关联的协议（如 openid-connect）。 */
    public ClientScopeBuilder protocol(String protocol) {
        rep.setProtocol(protocol);
        return this;
    }

    /** 添加自定义属性键值对。 */
    public ClientScopeBuilder attribute(String key, String value) {
        rep.setAttributes(Builder.createIfNull(rep.getAttributes(), HashMap::new));
        rep.getAttributes().put(key, value);
        return this;
    }
}
