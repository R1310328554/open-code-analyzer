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
 * SPI（Service Provider Interface）描述：定义 Provider 类型、工厂类及可见性。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface Spi {

    /** @return 是否为 Keycloak 内部 SPI（不对外暴露） */
    boolean isInternal();
    /** @return SPI 名称 */
    String getName();
    /** @return Provider 接口类型 */
    Class<? extends Provider> getProviderClass();
    /** @return ProviderFactory 接口类型 */
    Class<? extends ProviderFactory> getProviderFactoryClass();
    /** @return SPI 是否启用，默认 {@code true} */
    default boolean isEnabled() {
        return true;
    }

}
