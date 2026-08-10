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

package org.keycloak.protocol.saml.mappers;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.saml.SamlProtocol;

/**
 * SAML 协议映射器抽象基类。
 * <p>绑定 {@link SamlProtocol#LOGIN_PROTOCOL}，提供 {@link ProtocolMapper} 默认实现；子类实现具体 SAML 断言/响应转换逻辑。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class AbstractSAMLProtocolMapper implements ProtocolMapper {


    /** @return SAML 登录协议标识 */
    @Override
    public String getProtocol() {
        return SamlProtocol.LOGIN_PROTOCOL;
    }

    /** 关闭资源（无操作） */
    @Override
    public void close() {

    }

    /** 不支持工厂创建，子类应通过 SPI 注册 @throws RuntimeException 始终抛出 */
    @Override
    public final ProtocolMapper create(KeycloakSession session) {
        throw new RuntimeException("UNSUPPORTED METHOD");
    }

    /** 初始化（无操作） @param config 配置作用域 */
    @Override
    public void init(Config.Scope config) {
    }

    /** 工厂初始化后回调（无操作） @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }
}
