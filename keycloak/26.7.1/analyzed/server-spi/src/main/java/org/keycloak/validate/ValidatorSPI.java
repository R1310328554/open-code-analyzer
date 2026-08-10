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
package org.keycloak.validate;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 自定义 {@link Validator} 实现的 {@link Spi} 描述。
 *
 * An {@link Spi} for custom {@link Validator} implementations.
 */
public class ValidatorSPI implements Spi {

    @Override
    /** @return 是否为内部 SPI */
    public boolean isInternal() {
        // 当前为内部 API，后续计划公开。
        // this API is internal for now, but is intended to be public later.
        return true;
    }

    @Override
    /** @return SPI 名称 {@code validator} */
    public String getName() {
        return "validator";
    }

    @Override
    /** @return Provider 类型 {@link Validator} */
    public Class<? extends Provider> getProviderClass() {
        return Validator.class;
    }

    @Override
    /** @return 工厂类型 {@link ValidatorFactory} */
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ValidatorFactory.class;
    }
}
