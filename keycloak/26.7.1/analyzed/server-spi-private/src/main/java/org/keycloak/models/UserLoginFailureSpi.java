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
 */
package org.keycloak.models;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 用户登录失败 SPI，注册 {@link UserLoginFailureProvider} 提供者类型。
 * <p>跟踪登录失败次数并支持 realm 级 brute-force 检测。</p>
 *
 * @author <a href="mailto:mkanis@redhat.com">Martin Kanis</a>
 */
public class UserLoginFailureSpi implements Spi {

    /** SPI 名称常量：{@code loginFailure}。 */
    public static final String NAME = "loginFailure";

    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@link #NAME} */
    @Override
    public String getName() {
        return NAME;
    }

    /** 登录失败提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return UserLoginFailureProvider.class;
    }

    /** 登录失败工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return UserLoginFailureProviderFactory.class;
    }
}
