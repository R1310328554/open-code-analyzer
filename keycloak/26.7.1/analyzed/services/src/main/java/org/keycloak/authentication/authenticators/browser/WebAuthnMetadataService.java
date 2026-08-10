package org.keycloak.authentication.authenticators.browser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.FileUtils;
import org.keycloak.utils.StringUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

/**
 * Provides metadata for WebAuthn credentials.
 * Based on <a href="https://github.com/passkeydeveloper/passkey-authenticator-aaguids">passkey-authenticator-aaguids</a>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class WebAuthnMetadataService {

    private static final Logger logger = Logger.getLogger(WebAuthnMetadataService.class);
    /** 元数据 JSON 文件名。 */
    private static final String FILE_NAME = "keycloak-webauthn-metadata.json";

    /** AAGUID 到认证器元数据的缓存映射。 */
    private static volatile Map<String, WebAuthnAuthenticatorMetadata> aaguidToMetadata;

    /** 测试或启动时注入默认元数据（仅首次生效）。 */
    public static void setDefaultMetadata(Map<String, WebAuthnAuthenticatorMetadata> metadata) {
        if (aaguidToMetadata == null) {
            aaguidToMetadata = metadata;
        }
    }

    private Map<String, WebAuthnAuthenticatorMetadata> getAaguidToMetadata() {
        if (aaguidToMetadata == null) {
            synchronized (this) {
                if (aaguidToMetadata == null) {
                    aaguidToMetadata = parseMetadata();
                }
            }
        }
        return aaguidToMetadata;
    }

    /** 从 classpath 或 conf 目录解析并校验元数据文件。 */
    public static Map<String, WebAuthnAuthenticatorMetadata> parseMetadata() {
        try {
            try (InputStream is = FileUtils.getJsonFileFromClasspathOrConfFolder(FILE_NAME)) {
                Map<String, WebAuthnAuthenticatorMetadata> parsed = JsonSerialization.readValue(is, new TypeReference<>() {});
                for (Map.Entry<String, WebAuthnAuthenticatorMetadata> entry : parsed.entrySet()) {
                    if (StringUtil.isBlank(entry.getValue().name())) {
                        throw new IllegalStateException("Not found 'name' for the AAGUID '" + entry.getKey() + "' in the file '" + FILE_NAME + "'.");
                    }
                }
                return parsed;
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Error loading the webauthn metadata from file " + FILE_NAME, ioe);
        }
    }

    /** @param aaguid 认证器 AAGUID
     * @return 对应元数据，不存在时返回 null */
    public WebAuthnAuthenticatorMetadata getAuthenticatorMetadata(String aaguid) {
        return aaguid == null ? null : getAaguidToMetadata().get(aaguid);
    }

    /** @param aaguid 认证器 AAGUID
     * @return 认证器提供商可读名称 */
    public String getAuthenticatorProvider(String aaguid) {
        WebAuthnAuthenticatorMetadata metadata = getAuthenticatorMetadata(aaguid);
        return metadata == null ? null : metadata.name();
    }
}
