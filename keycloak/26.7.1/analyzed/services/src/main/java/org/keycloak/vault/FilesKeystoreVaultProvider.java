package org.keycloak.vault;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.Nonnull;

import org.jboss.logging.Logger;

/**
 * 基于 Java KeyStore 文件的 {@link VaultProvider} 实现。
 * <p>从指定 keystore 中按别名读取密钥条目，经 {@link VaultKeyResolver} 解析后的键名查找对应 Secret。</p>
 */
public class FilesKeystoreVaultProvider extends AbstractVaultProvider {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    private final Path keystorePath;
    private final String keystorePass;
    private final String keystoreType;

    /**
     * 创建 {@link FilesKeystoreVaultProvider} 实例。
     *
     * @param keystorePath keystore 文件路径，不可为 null
     * @param keystorePass keystore 密码，不可为 null
     * @param keystoreType keystore 类型，不可为 null（默认 PKCS12）
     * @param realmName 领域名称，不可为 null
     * @param resolvers 密钥解析器列表
     */
    public FilesKeystoreVaultProvider(@Nonnull Path keystorePath, @Nonnull String keystorePass, @Nonnull String keystoreType,
                                      @Nonnull String realmName, @Nonnull List<VaultKeyResolver> resolvers) {
        super(realmName, resolvers);
        this.keystorePath = keystorePath;
        this.keystorePass = keystorePass;
        this.keystoreType = keystoreType;
        logger.debugf("KeystoreVaultProvider will operate in %s directory", keystorePath.toAbsolutePath());
    }

    @Override
    protected VaultRawSecret obtainSecretInternal(String alias) {
        KeyStore ks;
        Key key;
        try {
            if (!Files.exists(keystorePath.toRealPath())) {
                throw new VaultNotFoundException("The keystore file for Keycloak Vault was not found");
            }
            ks = KeyStore.getInstance(keystoreType);
            ks.load(Files.newInputStream(keystorePath.toRealPath()), keystorePass.toCharArray());
            key = ks.getKey(alias, keystorePass.toCharArray());
            if (key == null) {
                logger.warnf("Cannot find secret %s in %s", alias, keystorePath);
                return DefaultVaultRawSecret.forBuffer(Optional.empty());
            }
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new RuntimeException(e);
        }
        return DefaultVaultRawSecret.forBuffer(Optional.of(ByteBuffer.wrap(new String(key.getEncoded()).getBytes())));
    }

    @Override
    public void close() {

    }
}
