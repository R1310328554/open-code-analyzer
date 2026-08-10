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

package org.keycloak.models;

import java.util.List;
import java.util.stream.Stream;

/**
 * 协议映射器容器：管理客户端或客户端范围上的 OIDC/SAML 协议映射器。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ProtocolMapperContainerModel {
    /**
     * 以流形式返回所有协议映射器。
     * Returns protocol mappers as a stream.
     * @return Stream of protocol mapper. Never returns {@code null}.
     */
    Stream<ProtocolMapperModel> getProtocolMappersStream();

    /** @param model 映射器模型
     * @return 添加后的映射器 */
    ProtocolMapperModel addProtocolMapper(ProtocolMapperModel model);

    /** @param mapping 待移除的映射器 */
    void removeProtocolMapper(ProtocolMapperModel mapping);

    /** @param mapping 待更新的映射器 */
    void updateProtocolMapper(ProtocolMapperModel mapping);

    /** @param id 映射器 ID
     * @return 匹配的映射器 */
    ProtocolMapperModel getProtocolMapperById(String id);

    /** @param protocol 协议
     * @param name 映射器名称
     * @return 匹配的映射器 */
    ProtocolMapperModel getProtocolMapperByName(String protocol, String name);

    /** @param type 映射器类型
     * @return 匹配类型的映射器列表（默认空） */
    default List<ProtocolMapperModel> getProtocolMapperByType(String type) {
        return List.of();
    }
}
