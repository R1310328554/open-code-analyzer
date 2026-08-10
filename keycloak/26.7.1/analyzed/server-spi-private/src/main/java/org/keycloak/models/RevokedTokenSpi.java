/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.provider.Spi;

/**
 * 已撤销令牌 SPI，注册 {@link RevokedTokenProvider} 提供者类型。
 * <p>用于跟踪与校验已撤销的访问/刷新令牌，防止重放。</p>
 */
public class RevokedTokenSpi implements Spi {

    /** SPI 名称常量：{@code revokedToken}。 */
    public static final String NAME = "revokedToken";

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

    /** 已撤销令牌提供者接口类型。 */
    @Override
    public Class<RevokedTokenProvider> getProviderClass() {
        return RevokedTokenProvider.class;
    }

    /** 已撤销令牌工厂类型。 */
    @SuppressWarnings("rawtypes")
    @Override
    public Class<RevokedTokenProviderFactory> getProviderFactoryClass() {
        return RevokedTokenProviderFactory.class;
    }
}
