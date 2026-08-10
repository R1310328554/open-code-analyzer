/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.testsuite.user.profile;

import org.keycloak.models.KeycloakSession;
import org.keycloak.userprofile.DeclarativeUserProfileProviderFactory;

/**
 * 自定义用户配置提供者工厂，注册 ID 为 {@code custom-user-profile} 的测试用提供者，
 * 优先级高于默认工厂以便在测试中覆盖标准行为。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CustomUserProfileProviderFactory extends DeclarativeUserProfileProviderFactory {

    /** 提供者在 SPI 中的唯一标识符。 */
    public static final String ID = "custom-user-profile";

    @Override
    public CustomUserProfileProvider create(KeycloakSession session) {
        return new CustomUserProfileProvider(session, this);
    }

    /** 返回比默认工厂更高的优先级（数值更小）。 */
    @Override
    public int order() {
        return super.order() - 1;
    }

    @Override
    public String getId() {
        return ID;
    }
}
