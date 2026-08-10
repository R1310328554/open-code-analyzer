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

package org.keycloak.models;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 一次性对象 SPI，注册 {@link SingleUseObjectProvider} 提供者类型。
 * <p>用于验证码、操作链接等仅能使用一次的临时数据。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SingleUseObjectSpi implements Spi {

    /** SPI 名称常量：{@code singleUseObject}。 */
    public static final String NAME = "singleUseObject";

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

    /** 一次性对象提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return SingleUseObjectProvider.class;
    }

    /** 一次性对象工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return SingleUseObjectProviderFactory.class;
    }
}
