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

package org.keycloak.provider;


/**
 * 异常转换器 SPI：注册 {@link ExceptionConverter} 提供者。
 *
 * @author <a href="mailto:bburke@redhat.com">Bill Burke</a>
 */
public class ExceptionConverterSpi implements Spi {

    /** @return 内部 SPI */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return SPI 名称 {@code exception-converter} */
    @Override
    public String getName() {
        return "exception-converter";
    }

    /** @return 提供者接口 {@link ExceptionConverter} */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return ExceptionConverter.class;
    }

    /** @return 工厂接口（与 {@link ExceptionConverter} 合并） */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ExceptionConverter.class;
    }

}
