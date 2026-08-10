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
 * 创建并配置 {@link FilesPlainTextVaultProvider} 的工厂，ID 为 {@code files-plaintext}。
 * <p>从配置读取 vault 目录路径，校验存在性后供 {@link #create(KeycloakSession)} 使用。</p>
 *
 * @author Sebastian Łaskawiec
 */
public class FilesPlainTextVaultProviderFactory extends AbstractVaultProviderFactory {

    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 提供者工厂标识 {@code files-plaintext}。 */
    public static final String PROVIDER_ID = "files-plaintext";

    /** 配置的 vault 目录路径字符串。 */
    private String vaultDirectory;
    /** 解析后的 vault 目录 {@link Path}。 */
    private Path vaultPath;

    @Override
    public VaultProvider create(KeycloakSession session) {
        if (vaultDirectory == null) {
            logger.debug("Can not create a vault since it's not initialized correctly");
            return null;
        }
        return new FilesPlainTextVaultProvider(vaultPath, getRealmName(session), super.keyResolvers);
    }

    @Override
    public void init(Config.Scope config) {
        super.init(config);

        vaultDirectory = config.get("dir");
        if (vaultDirectory == null) {
            logger.debug("PlainTextVaultProviderFactory not configured");
            return;
        }

        vaultPath = Paths.get(vaultDirectory);
        if (!Files.exists(vaultPath)) {
            throw new VaultNotFoundException("The " + vaultPath.toAbsolutePath().toString() + " directory doesn't exist");
        }
        logger.debugf("Configured PlainTextVaultProviderFactory with directory %s", vaultPath.toString());
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
