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

import java.nio.CharBuffer;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于 {@link CharBuffer} 的默认 {@link VaultCharSecret} 实现。
 * <p>关闭时用随机字符覆写缓冲区，降低内存残留风险。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class DefaultVaultCharSecret implements VaultCharSecret {

    /** 空密钥单例（Optional.empty）。 */
    private static final VaultCharSecret EMPTY_VAULT_SECRET = new VaultCharSecret() {
        @Override
        public Optional<CharBuffer> get() {
            return Optional.empty();
        }

        @Override
        public Optional<char[]> getAsArray() {
            return Optional.empty();
        }

        @Override
        public void close() {
        }
    };

    /**
     * 从 Optional {@link CharBuffer} 创建 {@link VaultCharSecret}。
     * @param buffer 密钥缓冲区
     * @return 非空时返回包装实例，否则返回空单例
     */
    public static VaultCharSecret forBuffer(Optional<CharBuffer> buffer) {
        if (buffer == null || ! buffer.isPresent()) {
            return EMPTY_VAULT_SECRET;
        }
        return new DefaultVaultCharSecret(buffer.get());
    }

    /** 底层字符缓冲区。 */
    private final CharBuffer buffer;

    /** 按需缓存的 char[] 副本。 */
    private char[] secretArray;

    /** 私有构造，通过 {@link #forBuffer(Optional)} 创建。 */
    private DefaultVaultCharSecret(final CharBuffer buffer) {
        this.buffer = buffer;
    }

    /** @return 密钥 {@link CharBuffer} */
    @Override
    public Optional<CharBuffer> get() {
        return Optional.of(this.buffer);
    }

    /** @return 密钥 char[] 副本 */
    @Override
    public Optional<char[]> getAsArray() {
        if (this.secretArray == null) {
            // 按需初始化内部 char[] 副本
            if (this.buffer.hasArray()) {
                this.secretArray = buffer.array();
            } else {
                secretArray = new char[buffer.capacity()];
                buffer.get(secretArray);
            }
        }
        return Optional.of(this.secretArray);
    }

    /** 用随机字符覆写缓冲区后清空，安全释放密钥。 */
    @Override
    public void close() {
        if (this.buffer.hasArray()) {
            char[] internalArray = this.buffer.array();
            for (int i = 0; i < internalArray.length; i++) {
                internalArray[i] = (char) ThreadLocalRandom.current().nextInt();
            }
        } else if (this.secretArray != null) {
            for (int i = 0; i < this.secretArray.length; i++) {
                this.secretArray[i] = (char) ThreadLocalRandom.current().nextInt();
            }
        }
        this.buffer.clear();
    }
}