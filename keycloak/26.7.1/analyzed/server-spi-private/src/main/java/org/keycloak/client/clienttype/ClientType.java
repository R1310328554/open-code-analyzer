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

import java.util.Set;

import org.keycloak.models.ClientModel;

/**
 * 客户端类型定义 SPI：描述一类客户端的默认配置与可覆盖选项。
 * <p>运行时通过 {@link #augment(ClientModel)} 将类型约束应用到具体客户端实例。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientType {

    /** 返回客户端类型名称（如 {@code standard}）。 */
    String getName();

    // 运行时判断某配置项是否由本客户端类型管控（属性名或 attribute 名）
    boolean isApplicable(String optionName);

    /** 获取类型定义的选项值；若本类型不提供该选项则返回 {@code null}。 */
    <T> T getTypeValue(String optionName, Class<T> optionType);

    /** 返回本类型声明的全部可配置选项名。 */
    Set<String> getOptionNames();

    /** 将客户端类型的默认值与约束应用到 {@link ClientModel}。 */
    ClientModel augment(ClientModel client);
}
