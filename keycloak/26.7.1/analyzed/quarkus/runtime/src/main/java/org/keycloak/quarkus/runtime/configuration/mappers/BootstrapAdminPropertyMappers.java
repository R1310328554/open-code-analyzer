/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.configuration.mappers;

import java.util.List;

import org.keycloak.config.BootstrapAdminOptions;

import static org.keycloak.quarkus.runtime.configuration.Configuration.getOptionalKcValue;
import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

/**
 * 引导管理员（bootstrap-admin）相关 {@link PropertyMapper} 分组：
 * 用户名/密码与服务账户 client-id/secret 的 CLI 与配置映射。
 */
public final class BootstrapAdminPropertyMappers implements PropertyMapperGrouping {

    /** 启用用户名选项时的校验提示：已设置 bootstrap 管理员密码。 */
    private static final String PASSWORD_SET = "bootstrap admin password is set";
    /** 启用 client-id 选项时的校验提示：已设置 client secret。 */
    private static final String CLIENT_SECRET_SET = "bootstrap admin client secret is set";

    // 使用 addValidateEnabled 而非 isEnabled，使选项始终出现在帮助中
    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(BootstrapAdminOptions.USERNAME)
                        .paramLabel("username")
                        .addValidateEnabled(BootstrapAdminPropertyMappers::isPasswordSet, PASSWORD_SET)
                        .build(),
                fromOption(BootstrapAdminOptions.PASSWORD)
                        .paramLabel("password")
                        .isMasked(true)
                        .build(),
                /*fromOption(BootstrapAdminOptions.EXPIRATION)
                        .paramLabel("expiration")
                        .isEnabled(BootstrapAdminPropertyMappers::isPasswordSet, PASSWORD_SET)
                        .build(),*/
                fromOption(BootstrapAdminOptions.CLIENT_ID)
                        .paramLabel("client id")
                        .addValidateEnabled(BootstrapAdminPropertyMappers::isClientSecretSet, CLIENT_SECRET_SET)
                        .build(),
                fromOption(BootstrapAdminOptions.CLIENT_SECRET)
                        .paramLabel("client secret")
                        .isMasked(true)
                        .build()
        );
    }

    private static boolean isPasswordSet() {
        return getOptionalKcValue(BootstrapAdminOptions.PASSWORD.getKey()).isPresent();
    }

    private static boolean isClientSecretSet() {
        return getOptionalKcValue(BootstrapAdminOptions.CLIENT_SECRET.getKey()).isPresent();
    }

}
