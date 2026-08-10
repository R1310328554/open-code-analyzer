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

package org.keycloak.client.clienttype;


import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.ClientTypesRepresentation;

/**
 * 客户端类型管理器 SPI：加载、更新领域级客户端类型定义，并在创建/更新客户端时应用类型约束。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientTypeManager extends Provider {

    // 内置全局客户端类型名称常量
    String STANDARD = "standard";
    /** 服务账户客户端类型名称。 */
    String SERVICE_ACCOUNT = "service-account";

    /** 获取领域内已配置的客户端类型集合表示。 */
    ClientTypesRepresentation getClientTypes(RealmModel realm) throws ClientTypeException;

    /** 校验并持久化领域客户端类型配置。 */
    void updateClientTypes(RealmModel realm, ClientTypesRepresentation clientTypes) throws ClientTypeException;

    /** 按名称解析单个 {@link ClientType} 定义。 */
    ClientType getClientType(RealmModel realm, String typeName)  throws ClientTypeException;

    /** 根据客户端关联的类型对其执行 augment 增强。 */
    ClientModel augmentClient(ClientModel client) throws ClientTypeException;

    @Override
    default void close() {
    }
}