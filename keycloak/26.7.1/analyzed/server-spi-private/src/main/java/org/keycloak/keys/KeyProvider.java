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

package org.keycloak.keys;

import java.util.stream.Stream;

import org.keycloak.crypto.KeyWrapper;
import org.keycloak.provider.Provider;

/**
 * Realm 密钥提供者 SPI：暴露签名/加密等 {@link org.keycloak.crypto.KeyWrapper}。
 * <p>由组件模型配置，供令牌签名、JWE 等加密操作使用。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface KeyProvider extends Provider {

    /**
     * 返回本提供者管理的全部 {@link org.keycloak.crypto.KeyWrapper}。
     *
     * @return 密钥包装流
     */
    Stream<KeyWrapper> getKeysStream();

    default void close() {
    }

}
