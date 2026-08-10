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

package org.keycloak.events;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

/**
 * 事件存储 SPI，注册 {@link EventStoreProvider} 用于持久化用户与客户端事件。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class EventStoreSpi implements Spi {

    /** SPI 名称常量：{@code eventsStore}。 */
    public static final String NAME = "eventsStore";

    /** 内部 SPI，不对扩展模块公开。 */
    @Override
    public boolean isInternal() {
        return true;
    }

    /** @return {@link #NAME} */
    @Override
    public String getName() {
        return NAME;
    }

    /** 事件存储提供者接口类型。 */
    @Override
    public Class<? extends Provider> getProviderClass() {
        return EventStoreProvider.class;
    }

    /** 事件存储工厂类型。 */
    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return EventStoreProviderFactory.class;
    }

}
