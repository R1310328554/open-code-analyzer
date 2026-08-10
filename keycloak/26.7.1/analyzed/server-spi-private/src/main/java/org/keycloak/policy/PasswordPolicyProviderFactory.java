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

package org.keycloak.policy;

import org.keycloak.provider.ProviderFactory;

/**
 * 单条密码策略的 SPI 工厂接口：定义策略 ID、显示名、配置类型及是否支持多条实例。
 *
 * @author <a href="mailto:roelof.naude@epiuse.com">Roelof Naude</a>
 */
public interface PasswordPolicyProviderFactory extends ProviderFactory<PasswordPolicyProvider> {

    /** @return 管理控制台中的策略显示名称 */
    String getDisplayName();
    /** @return 策略配置值类型（如 {@link PasswordPolicyProvider#INT_CONFIG_TYPE}） */
    String getConfigType();
    /** @return 默认配置值字符串 */
    String getDefaultConfigValue();
    /** @return 是否允许在同一 realm 中配置多条该策略 */
    boolean isMultiplSupported();

}
