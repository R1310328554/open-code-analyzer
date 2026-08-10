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

package org.keycloak.crypto;

import org.keycloak.models.KeycloakSession;

/**
 * SHA-384 哈希 SPI 工厂。
 * <p>注册 ID 为 {@link JavaAlgorithm#SHA384}，创建 {@link JavaAlgorithmHashProvider} 实例。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SHA384HashProviderFactory implements HashProviderFactory {

    /** SPI 工厂标识：{@code SHA-384}。 */
    public static final String ID = JavaAlgorithm.SHA384;

    @Override
    /** @return {@link #ID} */
    public String getId() {
        return ID;
    }

    @Override
    /** @param session 当前会话 @return SHA-384 哈希提供者 */
    public HashProvider create(KeycloakSession session) {
        return new JavaAlgorithmHashProvider(ID);
    }
}
