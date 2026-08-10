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

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认 {@link VaultTranscriber} 实现：通过配置的 {@link VaultProvider} 获取原始密钥并转换为其他类型。
 * <p>默认情况下 {@link VaultProvider} 以 {@link ByteBuffer} 提供原始密钥；本类提供将其转为 {@link VaultCharSecret}、{@link VaultStringSecret} 等类型的方法。</p>
 *
 * @see VaultRawSecret
 * @see VaultCharSecret
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class DefaultVaultTranscriber implements VaultTranscriber {

    /** 匹配 {@code ${vault.<KEY>}} 表达式的正则。 */
    private static final Pattern pattern = Pattern.compile("^\\$\\{vault\\.(.+?)}$");

    /** 用于检索密钥条目的 Vault 提供者。 */
    private final VaultProvider provider;

    /**
     * 构造转录器；{@code provider} 为 null 时使用空提供者（始终返回空密钥）。
     * @param provider Vault 提供者，可为 null
     */
    public DefaultVaultTranscriber(final VaultProvider provider) {
        if (provider == null) {
            this.provider = new VaultProvider() {
                @Override
                public VaultRawSecret obtainSecret(String vaultSecretId) {
                    return DefaultVaultRawSecret.forBuffer(null);
                }

                @Override
                public void close() {
                }
            };
        } else {
            this.provider = provider;
        }
    }

    @Override
    public VaultRawSecret getRawSecret(final String value) {
        String entryId = this.getVaultEntryKey(value);
        if (entryId != null) {
            // 有效的 ${vault.<KEY>} 表达式，通过 provider 检索条目
            return this.provider.obtainSecret(entryId);
        } else {
            // 非 vault 表达式，将值本身编码为字节缓冲区
            ByteBuffer buffer = value != null ? ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8)) : null;
            return DefaultVaultRawSecret.forBuffer(Optional.ofNullable(buffer));
        }
    }

    @Override
    public VaultCharSecret getCharSecret(final String value) {
        // 获取原始密钥并转为字符密钥
        try (VaultRawSecret rawSecret = this.getRawSecret(value)) {
            if (!rawSecret.get().isPresent()) {
                return DefaultVaultCharSecret.forBuffer(Optional.empty());
            }
            ByteBuffer rawSecretBuffer = rawSecret.get().get();
            CharBuffer charSecretBuffer = StandardCharsets.UTF_8.decode(rawSecretBuffer);
            return DefaultVaultCharSecret.forBuffer(Optional.of(charSecretBuffer));
        }
    }

    @Override
    public VaultStringSecret getStringSecret(final String value) {
        // 获取原始密钥并转为字符串密钥
        try (VaultRawSecret rawSecret = this.getRawSecret(value)) {
            if (!rawSecret.get().isPresent()) {
                return DefaultVaultStringSecret.forString(Optional.empty());
            }
            ByteBuffer rawSecretBuffer = rawSecret.get().get();
            return DefaultVaultStringSecret.forString(Optional.of(StandardCharsets.UTF_8.decode(rawSecretBuffer).toString()));
        }
    }

    /**
     * 若值为合法的 {@code ${vault.<KEY>}} 表达式，则提取 vault 条目键名。
     * <p>例如传入 {@code ${vault.smtp_secret}} 将返回 {@code smtp_secret}。</p>
     *
     * @param value 可能包含 vault 条目键名的字符串
     * @return 符合 {@code ${vault.<KEY>}} 格式时返回提取的键名，否则返回 null
     */
    private String getVaultEntryKey(final String value) {
        if (value != null) {
            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}
