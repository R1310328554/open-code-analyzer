/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.spi.infinispan.impl;

import org.keycloak.config.Option;
import org.keycloak.provider.ProviderConfigurationBuilder;

/**
 * 本包及子包共用的工具方法。
 */
public final class Util {

    private Util() {
    }

    /**
     * 将 {@link Option} 的配置元数据复制到 {@link ProviderConfigurationBuilder}。
     * <p>
     * 包括描述、默认值、可选值及是否标记为密钥字段。
     *
     * @param builder  待填充的 Provider 配置构建器。
     * @param name     目标属性名。
     * @param label    属性参数的标签。
     * @param type     属性值的类型。
     * @param option   源 {@link Option}，提供描述与默认值等信息。
     * @param isSecret {@code true} 表示该属性为密钥（secret）。
     */
    public static void copyFromOption(ProviderConfigurationBuilder builder, String name, String label, String type, Option<?> option, boolean isSecret) {
        var property = builder.property()
                .name(name)
                .helpText(option.getDescription())
                .label(label)
                .type(type)
                .secret(isSecret);
        option.getDefaultValue().ifPresent(property::defaultValue);
        property.options(option.getExpectedValues());
        property.add();
    }

}
