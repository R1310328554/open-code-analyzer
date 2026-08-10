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
package org.keycloak.protocol.oidc.grants.ciba.channel;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * CIBA 认证通道 SPI：定义如何将后台认证请求投递至外部认证设备。
 * <p>内置 HTTP 实现见 {@link HttpAuthenticationChannelProviderFactory}。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class AuthenticationChannelSpi implements Spi {

    /** @return 是否为内部 SPI（不对外暴露） */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code ciba-auth-channel} */
    @Override
    public String getName() {
        return "ciba-auth-channel";
    }

    /** @return 认证通道 Provider 接口类型 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return AuthenticationChannelProvider.class;
    }

    /** @return 认证通道 Provider 工厂类型 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return AuthenticationChannelProviderFactory.class;
    }

}
