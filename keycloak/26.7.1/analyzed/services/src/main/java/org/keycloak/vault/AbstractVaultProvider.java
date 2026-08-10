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

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;

/**
 * 支持密钥解析器的 {@link VaultProvider} 抽象基类。
 * <p>按配置顺序遍历 {@link VaultKeyResolver}，将解析后的键名传给子类实现的 {@link #obtainSecretInternal(String)}；任一解析器返回非空密钥即返回，全部失败则返回空 {@link VaultRawSecret}。</p>
 * <p>子类构造器须调用 {@link AbstractVaultProvider#AbstractVaultProvider(String, List)} 以初始化领域名与解析器列表。</p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public abstract class AbstractVaultProvider implements VaultProvider {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 当前 Keycloak 领域名称。 */
    protected final String realm;
    /** 已配置的密钥解析器列表（按优先级排序）。 */
    protected final List<VaultKeyResolver> resolvers;


    /**
     * 构造抽象 Vault 提供者。
     *
     * @param realm Keycloak 领域名称
     * @param configuredResolvers 已配置的密钥解析器列表
     */
    public AbstractVaultProvider(final String realm, final List<VaultKeyResolver> configuredResolvers) {
        this.realm = realm;
        this.resolvers = configuredResolvers;
    }

    /** 遍历解析器获取密钥：先校验键名，再按序尝试 {@link #obtainSecretInternal(String)}。 */
    @Override
    public VaultRawSecret obtainSecret(String vaultSecretId) {
        for (VaultKeyResolver resolver : this.resolvers) {
            String resolvedKey = resolver.apply(this.realm, vaultSecretId);
            if (!validate(resolver, vaultSecretId, resolvedKey)) {
                logger.warnf("Validation failed for secret %s with resolved key %s", vaultSecretId, resolvedKey);
                return DefaultVaultRawSecret.forBuffer(Optional.empty());
            }
        }

        for (VaultKeyResolver resolver : this.resolvers) {
            String resolvedKey = resolver.apply(this.realm, vaultSecretId);
            VaultRawSecret secret = this.obtainSecretInternal(resolvedKey);
            if (secret != null && secret.get().isPresent()) {
                return secret;
            }
            checkForLegacyKey(resolver, vaultSecretId, resolvedKey);
        }
        return DefaultVaultRawSecret.forBuffer(Optional.empty());
    }

    /** 检测旧版双下划线密钥格式并记录迁移警告。 */
    private void checkForLegacyKey(VaultKeyResolver resolver, String vaultSecretId, String resolvedKey) {
        if (resolver == AbstractVaultProviderFactory.AvailableResolvers.KEY_ONLY.getVaultKeyResolver() && vaultSecretId.contains("_")) {
            String legacyKey = vaultSecretId.replaceAll("__", "_");
            VaultRawSecret legacySecret = this.obtainSecretInternal(legacyKey);
            if (legacySecret != null && legacySecret.get().isPresent()) {
                logger.warnf("Secret was found using legacy key '%s'. Please rename the key to '%s' and repeat the action.", legacyKey, resolvedKey);
            }
        }
    }

    /**
     * 校验解析后的密钥名（如禁止含文件分隔符）。
     *
     * @param resolver 使用的 {@link VaultKeyResolver}
     * @param key 原始密钥 ID
     * @param resolvedKey 解析后的密钥名
     * @return 校验通过返回 true
     */
    protected boolean validate(VaultKeyResolver resolver, String key, String resolvedKey) {
        if (key.contains(File.separator)) {
            logger.warnf("Key %s contains invalid file separator character", key);
            return false;
        }
        return true;
    }

    /**
     * 子类实现：使用已解析的 vault 键名直接获取密钥（不再二次解析）。
     *
     * @param vaultKey 解析后的 vault 条目名
     * @return 获取到的 {@link VaultRawSecret}，未找到时可为空
     */
    protected abstract VaultRawSecret obtainSecretInternal(final String vaultKey);

}
