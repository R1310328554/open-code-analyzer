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
 *
 */

package org.keycloak.services.clienttype;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.client.clienttype.ClientTypeException;
import org.keycloak.client.clienttype.ClientTypeManager;
import org.keycloak.client.clienttype.ClientTypeManagerFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.representations.idm.ClientTypeRepresentation;
import org.keycloak.representations.idm.ClientTypesRepresentation;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * 默认客户端类型管理器工厂。
 * <p>在 {@link Profile.Feature#CLIENT_TYPES} 特性启用时创建 {@link DefaultClientTypeManager}， 并从 classpath JSON 加载全局客户端类型。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultClientTypeManagerFactory implements ClientTypeManagerFactory {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(DefaultClientTypeManagerFactory.class);

    /** 懒加载的全局客户端类型缓存 */
    private volatile List<ClientTypeRepresentation> globalClientTypes;

    /** {@inheritDoc} 创建带全局类型列表的 {@link DefaultClientTypeManager} */
    @Override
    public ClientTypeManager create(KeycloakSession session) {
        return new DefaultClientTypeManager(session, getGlobalClientTypes(session));
    }

    /** {@inheritDoc} 无额外初始化逻辑 */
    @Override
    public void init(Config.Scope config) {

    }

    /** {@inheritDoc} 无后置初始化逻辑 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    /** {@inheritDoc} 无资源需释放 */
    @Override
    public void close() {

    }

    /** {@inheritDoc} 返回 {@code "default"} */
    @Override
    public String getId() {
        return "default";
    }

    /** {@inheritDoc} 仅在 CLIENT_TYPES 特性开启时可用 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.CLIENT_TYPES);
    }

    /** 双重检查锁定地从 JSON 资源加载并校验全局客户端类型。 */
    protected List<ClientTypeRepresentation> getGlobalClientTypes(KeycloakSession session) {
        if (globalClientTypes == null) {
            synchronized (this) {
                if (globalClientTypes == null) {
                    logger.info("Loading global client types");

                    try {
                        ClientTypesRepresentation globalTypesRep  = JsonSerialization.readValue(getClass().getResourceAsStream("/keycloak-default-client-types.json"), ClientTypesRepresentation.class);
                        this.globalClientTypes = DefaultClientTypeManager.validateAndCastConfiguration(session, globalTypesRep.getRealmClientTypes(), Collections.emptyList());
                    } catch (IOException e) {
                        logger.error("Failed to deserialize global proposed client types from JSON.");
                        throw ClientTypeException.Message.CLIENT_TYPE_FAILED_TO_LOAD.exception(e);
                    }
                }
            }
        }
        return globalClientTypes;
    }

}