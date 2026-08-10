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

import org.keycloak.jose.jws.crypto.HashUtils;

/**
 * 基于 Java {@code MessageDigest} 的哈希提供者实现。
 * <p>将算法名委托给 {@link HashUtils#hash(String, byte[])} 计算摘要。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JavaAlgorithmHashProvider implements HashProvider {

    /** Java 标准哈希算法名（如 SHA-256）。 */
    private final String javaAlgorithm;

    /** @param javaAlgorithm Java MessageDigest 算法标识 */
    public JavaAlgorithmHashProvider(String javaAlgorithm) {
        this.javaAlgorithm = javaAlgorithm;
    }

    @Override
    /** 对输入字节序列计算哈希摘要。 */
    public byte[] hash(byte[] input) throws HashException {
        return HashUtils.hash(javaAlgorithm, input);
    }
}
