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

package org.keycloak.services.clienttype.impl;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.client.clienttype.ClientType;
import org.keycloak.models.ClientModel;
import org.keycloak.representations.idm.ClientTypeRepresentation;
import org.keycloak.services.clienttype.client.TypeAwareClientModelDelegate;

/**
 * 默认 {@link ClientType} 实现。
 * <p>基于 {@link ClientTypeRepresentation} 解析属性适用性与类型值， 未显式配置的选项继承父类型；通过 {@link TypeAwareClientModelDelegate} 增强客户端模型。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultClientType implements ClientType {

    /** 本类型的配置表示 */
    private final ClientTypeRepresentation clientType;
    /** 父类型（可为 null） */
    private final ClientType parentClientType;

    /** 构造默认客户端类型。
     * @param clientType 类型配置
     * @param parentClientType 父类型，用于继承未配置项
     */
    public DefaultClientType(ClientTypeRepresentation clientType, ClientType parentClientType) {
        this.clientType = clientType;
        this.parentClientType = parentClientType;
    }

    /** {@inheritDoc} 返回类型名称 */
    @Override
    public String getName() {
        return clientType.getName();
    }

    /** {@inheritDoc} 本类型未配置时向父类型查询，均无则默认可用 */
    @Override
    public boolean isApplicable(String optionName) {
        ClientTypeRepresentation.PropertyConfig propertyConfig = clientType.getConfig().get(optionName);
        if (propertyConfig != null) {
            return propertyConfig.getApplicable();
        }

        if (parentClientType != null) {
            return parentClientType.isApplicable(optionName);
        }

        return true;
    }

    /** {@inheritDoc} 本类型未配置时继承父类型的类型值 */
    @Override
    public <T> T getTypeValue(String optionName, Class<T> optionType) {
        ClientTypeRepresentation.PropertyConfig propertyConfig = clientType.getConfig().get(optionName);
        if (propertyConfig != null) {
            return optionType.cast(propertyConfig.getValue());
        } else if (parentClientType != null) {
            return parentClientType.getTypeValue(optionName, optionType);
        }
        return null;
    }

    /** {@inheritDoc} 合并本类型与父类型的全部选项名 */
    @Override
    public Set<String> getOptionNames() {
        Stream<String> optionNames = clientType.getConfig().keySet().stream();
        if (parentClientType != null) {
            optionNames = Stream.concat(optionNames, parentClientType.getOptionNames().stream());
        }
        return optionNames.collect(Collectors.toSet());
    }

    /** {@inheritDoc} 包装为 {@link TypeAwareClientModelDelegate} */
    @Override
    public ClientModel augment(ClientModel client) {
        return new TypeAwareClientModelDelegate(this, () -> client);
    }
}
