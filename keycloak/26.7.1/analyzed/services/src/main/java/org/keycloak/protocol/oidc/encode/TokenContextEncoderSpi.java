/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.encode;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 访问令牌 ID 上下文编码 SPI：在 token id 中嵌入会话/令牌类型等元数据。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TokenContextEncoderSpi implements Spi {

    /** @return 内部 SPI，不对外暴露配置 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code tokenContextEncoder} */
    @Override
    public String getName() {
        return "tokenContextEncoder";
    }

    /** @return Provider 接口类 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return TokenContextEncoderProvider.class;
    }

    /** @return ProviderFactory 接口类 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return TokenContextEncoderProviderFactory.class;
    }

}
