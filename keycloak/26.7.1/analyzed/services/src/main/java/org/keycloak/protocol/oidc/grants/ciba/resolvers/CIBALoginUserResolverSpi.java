/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oidc.grants.ciba.resolvers;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * CIBA 登录用户解析器 SPI 定义。
 * <p>注册 {@link CIBALoginUserResolver} 提供者及其工厂。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class CIBALoginUserResolverSpi implements Spi {

    /** @return 是否为内部 SPI（true） */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code ciba-login-user-resolver} */
    @Override
    public String getName() {
        return "ciba-login-user-resolver";
    }

    /** @return 提供者接口类 {@link CIBALoginUserResolver} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return CIBALoginUserResolver.class;
    }

    /** @return 工厂接口类 {@link CIBALoginUserResolverFactory} */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return CIBALoginUserResolverFactory.class;
    }

}