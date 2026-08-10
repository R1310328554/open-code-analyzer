/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.vault;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于 {@code byte[]} 的默认 {@link VaultRawSecret} 实现。
 * <p>关闭时用随机字节覆写缓冲区，降低内存残留风险。</p>
 * @author hmlnarik
 */
public class DefaultVaultRawSecret implements VaultRawSecret {

    /** 空 ByteBuffer 占位符。 */
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    /** 空密钥单例（Optional.empty）。 */
    private static final VaultRawSecret EMPTY_VAULT_SECRET = new VaultRawSecret() {
        @Override
        public Optional<ByteBuffer> get() {
            return Optional.empty();
        }

        @Override
        public Optional<byte[]> getAsArray() {
            return Optional.empty();
        }

        @Override
        public void close() {
        }
    };

    /** 底层字节缓冲区。 */
    private ByteBuffer rawSecret;

    /** 按需缓存的 byte[] 副本。 */
    private byte[] secretArray;

    /**
     * 从 Optional {@link ByteBuffer} 创建 {@link VaultRawSecret}。
     * @param buffer 密钥缓冲区
     * @return 非空时返回包装实例，否则返回空单例
     */
    public static VaultRawSecret forBuffer(Optional<ByteBuffer> buffer) {
        if (buffer == null || ! buffer.isPresent()) {
            return EMPTY_VAULT_SECRET;
        }
        return new DefaultVaultRawSecret(buffer.get());
    }

    /** 私有构造，通过 {@link #forBuffer(Optional)} 创建。 */
    private DefaultVaultRawSecret(ByteBuffer rawSecret) {
        this.rawSecret = rawSecret;
    }

    /** @return 密钥 {@link ByteBuffer} */
    @Override
    public Optional<ByteBuffer> get() {
        return Optional.of(this.rawSecret);
    }

    /** @return 密钥 byte[] 副本 */
    @Override
    public Optional<byte[]> getAsArray() {
        if (this.secretArray == null) {
            // initialize internal array on demand.
            if (this.rawSecret.hasArray()) {
                this.secretArray = this.rawSecret.array();
            } else {
                secretArray = new byte[this.rawSecret.capacity()];
                this.rawSecret.get(secretArray);
            }
        }
        return Optional.of(this.secretArray);
    }

    /** 用随机字节覆写缓冲区后清空，安全释放密钥。 */
    @Override
    public void close() {
        if (rawSecret.hasArray()) {
            ThreadLocalRandom.current().nextBytes(rawSecret.array());
        }
        if (this.secretArray != null) {
            ThreadLocalRandom.current().nextBytes(this.secretArray);
            this.secretArray = null;    // 释放 secretArray 引用
        }
        rawSecret.clear();
        rawSecret = EMPTY_BUFFER;
    }
}
