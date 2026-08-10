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

import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.ClientTypeRepresentation;

/**
 * 客户端类型提供者 SPI：将 {@link ClientTypeRepresentation} 解析为运行时 {@link ClientType} 模型。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientTypeProvider extends Provider {

    /**
     * 根据表示对象与可选父类型构建运行时 {@link ClientType} 模型。
     *
     * @param clientTypeRep 客户端类型配置表示
     * @param parent 继承的父类型，可为 {@code null}
     * @return 解析后的客户端类型模型
     */
    ClientType getClientType(ClientTypeRepresentation clientTypeRep, ClientType parent);

    /**
     * 创建或更新客户端类型时校验 JSON 配置格式。
     * <p>TODO:client-types 需增强类型安全，确保返回的配置可正确转型。</p>
     *
     * @param clientType 待校验的客户端类型表示
     * @return 校验通过后的表示对象
     * @throws ClientTypeException 配置不符合客户端类型格式时抛出
     */
    ClientTypeRepresentation checkClientTypeConfig(ClientTypeRepresentation clientType) throws ClientTypeException;

    /** 默认空实现，无资源需释放。 */
    @Override
    default void close() {
    }

}
