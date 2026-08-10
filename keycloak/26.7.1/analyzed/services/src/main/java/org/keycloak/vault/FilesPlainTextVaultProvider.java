package org.keycloak.vault;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.Nonnull;

import org.jboss.logging.Logger;

/**
 * 纯文本文件型 Vault 提供者：每个密钥存储在独立文件中，文件名须与 vault 密钥 ID（简称 key）匹配，格式由配置的 {@link VaultKeyResolver} 决定。
 * <p>典型目录布局示例：</p>
 * <pre>
 *     ${VAULT}/realma__key1 （key1 的密钥）
 *     ${VAULT}/realma__key2 （key2 的密钥）
 *     …
 * </pre>
 * <p>上述布局中每个 key 以领域名前缀；Kubernetes 默认挂载 Secret 卷时采用类似结构，可通过 {@code REALM_UNDERSCORE_KEY} 解析器（未配置时的默认解析器）启用。其他布局可通过不同解析器实现。</p>
 *
 * See https://kubernetes.io/docs/concepts/configuration/secret/
 * See https://github.com/keycloak/keycloak-community/blob/main/design/secure-credentials-store.md#plain-text-file-per-secret-kubernetes--openshift
 *
 * @author Sebastian Łaskawiec
 */
public class FilesPlainTextVaultProvider extends AbstractVaultProvider {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private final Path vaultPath;

    /**
     * 创建 {@link FilesPlainTextVaultProvider} 实例。
     *
     * @param path vault 目录路径，不可为 null
     * @param realmName 领域名称，不可为 null
     * @param resolvers 密钥解析器列表
     */
    public FilesPlainTextVaultProvider(@Nonnull Path path, @Nonnull String realmName, @Nonnull List<VaultKeyResolver> resolvers) {
        super(realmName, resolvers);
        this.vaultPath = path;
        logger.debugf("PlainTextVaultProvider will operate in %s directory", vaultPath.toAbsolutePath());
    }

    @Override
    protected VaultRawSecret obtainSecretInternal(String vaultSecretId) {
        Path secretPath = vaultPath.resolve(vaultSecretId).normalize();
        if (!Files.exists(secretPath)) {
            logger.warnf("Cannot find secret %s in %s", vaultSecretId, secretPath);
            return DefaultVaultRawSecret.forBuffer(Optional.empty());
        }

        try {
            byte[] bytes = Files.readAllBytes(secretPath);
            return DefaultVaultRawSecret.forBuffer(Optional.of(ByteBuffer.wrap(bytes)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean validate(VaultKeyResolver resolver, String key, String resolvedKey) {
        if (!super.validate(resolver, key, resolvedKey)) {
            return false;
        }
        Path secretPath = vaultPath.resolve(resolvedKey).normalize();

        Path expectedPath = vaultPath;
        if (resolver == AbstractVaultProviderFactory.AvailableResolvers.REALM_FILESEPARATOR_KEY.getVaultKeyResolver()) {
            expectedPath = expectedPath.resolve(realm);
        }
        expectedPath = expectedPath.normalize();

        Path parent = secretPath.getParent();
        if (parent == null || !parent.equals(expectedPath)) {
            logger.warnf("Path traversal attempt detected in secret %s.", key);
            return false;
        }
        return true;
    }

    @Override
    public void close() {

    }
}
