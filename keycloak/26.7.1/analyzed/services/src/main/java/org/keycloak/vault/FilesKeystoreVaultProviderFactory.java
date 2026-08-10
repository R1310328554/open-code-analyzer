package org.keycloak.vault;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import org.jboss.logging.Logger;

/**
 * 文件 KeyStore 型 Vault 提供者工厂，ID 为 {@code files-keystore}。
 * <p>从配置读取 keystore 路径、密码与类型，创建 {@link FilesKeystoreVaultProvider}。</p>
 */
public class FilesKeystoreVaultProviderFactory extends AbstractVaultProviderFactory {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 提供者工厂标识 {@code files-keystore}。 */
    public static final String PROVIDER_ID = "files-keystore";

    /** 已配置的 keystore 文件路径。 */
    private Path keystoreFile;
    /** keystore 访问密码。 */
    private String keystorePass;
    /** keystore 类型（如 PKCS12）。 */
    private String keystoreType;

    @Override
    public VaultProvider create(KeycloakSession session) {
        if (keystoreFile == null) {
            logger.debug("Can not create a vault since it's not initialized correctly");
            return null;
        }
        return new FilesKeystoreVaultProvider(keystoreFile, keystorePass, keystoreType, getRealmName(session), super.keyResolvers);
    }

    @Override
    public void init(Config.Scope config) {
        super.init(config);

        String pathConfigProperty = config.get("file");
        if (pathConfigProperty == null) {
            logger.debug("Path to the vault keystore is not configured");
            return;
        }
        keystoreFile = Paths.get(pathConfigProperty);
        if (!Files.exists(keystoreFile)) {
            throw new VaultNotFoundException("The vault does not exist on the path " + keystoreFile.toAbsolutePath());
        }

        keystorePass = config.get("pass");
        if (keystorePass == null) {
            logger.debug("Password for the vault keystore is not configured");
            return;
        }

        keystoreType = config.get("type", "PKCS12");
        logger.debugf("A type of the provided keystore is %s", keystoreType);

        logger.debugf("Configured KeystoreVaultProviderFactory with the keystore file located in %s", keystoreFile.toString());
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
