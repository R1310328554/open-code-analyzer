/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authentication.actiontoken;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 操作令牌处理器 SPI 描述，Provider 类型为 {@link ActionTokenHandler}。
 *
 * @author hmlnarik
 */
public class ActionTokenHandlerSpi implements Spi {

    @Override
    /** @return 是否为 Keycloak 内部 SPI */
    public boolean isInternal() {
        return true;
    }

    @Override
    /** @return SPI 名称 actionTokenHandler */
    public String getName() {
        return NAME;
    }
    /** SPI 注册名。 */
    private static final String NAME = "actionTokenHandler";

    @Override
    /** @return Provider 接口类 */
    public Class<? extends Provider> getProviderClass() {
        return ActionTokenHandler.class;
    }

    @Override
    /** @return Provider 工厂接口类 */
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ActionTokenHandlerFactory.class;
    }

}
