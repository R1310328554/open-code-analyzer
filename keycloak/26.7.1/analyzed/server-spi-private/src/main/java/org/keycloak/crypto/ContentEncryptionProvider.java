/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.crypto;

import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;
import org.keycloak.provider.Provider;

/**
 * JWE 内容加密（enc）算法提供者 SPI。
 * <p>封装 AES-GCM、AES-CBC+HMAC 等载荷加密算法的实现。</p>
 */
public interface ContentEncryptionProvider extends Provider {

    /** @return 对应的 JWE 内容加密算法提供者 */
    JWEEncryptionProvider jweEncryptionProvider();

    /** 默认空实现，无资源需释放。 */
    @Override
    default void close() {
    }

}
