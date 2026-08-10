package org.keycloak.models.credential;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.keycloak.credential.CredentialModel;
import org.keycloak.models.credential.dto.PasswordCredentialData;
import org.keycloak.models.credential.dto.PasswordSecretData;
import org.keycloak.util.JsonSerialization;

import static org.keycloak.utils.StringUtil.isBlank;

/**
 * 密码凭据模型：封装密码哈希算法、盐值、迭代次数与编码后的密码。
 */
public class PasswordCredentialModel extends CredentialModel {

    /** 密码凭据类型标识。 */
    public static final String TYPE = "password";
    /** 密码历史凭据类型标识。 */
    public static final String PASSWORD_HISTORY = "password-history";

    private final PasswordCredentialData credentialData;
    private final PasswordSecretData secretData;

    private PasswordCredentialModel(PasswordCredentialData credentialData, PasswordSecretData secretData) {
        this.credentialData = credentialData;
        this.secretData = secretData;
    }

    /** 从 DTO 创建密码凭据。 */
    public static PasswordCredentialModel createFromValues(PasswordCredentialData credentialData, PasswordSecretData secretData) {
        return new PasswordCredentialModel(credentialData, secretData);
    }

    /** 从哈希参数创建密码凭据。 */
    public static PasswordCredentialModel createFromValues(String algorithm, byte[] salt, int hashIterations, String encodedPassword){
        return createFromValues(algorithm, salt, hashIterations, null, encodedPassword);
    }

    public static PasswordCredentialModel createFromValues(String algorithm, byte[] salt, int hashIterations, Map<String, List<String>> additionalParameters, String encodedPassword){
        PasswordCredentialData credentialData = new PasswordCredentialData(hashIterations, algorithm, additionalParameters);
        PasswordSecretData secretData = new PasswordSecretData(encodedPassword, salt);

        PasswordCredentialModel passwordCredentialModel = new PasswordCredentialModel(credentialData, secretData);

        try {
            passwordCredentialModel.setCredentialData(JsonSerialization.writeValueAsString(credentialData));
            passwordCredentialModel.setSecretData(JsonSerialization.writeValueAsString(secretData));
            passwordCredentialModel.setType(TYPE);
            return passwordCredentialModel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 从通用 {@link CredentialModel} 反序列化密码凭据。 */
    public static PasswordCredentialModel createFromCredentialModel(CredentialModel credentialModel) {
        try {
            PasswordCredentialData credentialData = isBlank(credentialModel.getCredentialData()) ? null : JsonSerialization.readValue(credentialModel.getCredentialData(),
                    PasswordCredentialData.class);
            PasswordSecretData secretData = isBlank(credentialModel.getSecretData()) ? null : JsonSerialization.readValue(credentialModel.getSecretData(), PasswordSecretData.class);
            PasswordCredentialModel passwordCredentialModel = new PasswordCredentialModel(credentialData, secretData);
            passwordCredentialModel.setCreatedDate(credentialModel.getCreatedDate());
            passwordCredentialModel.setCredentialData(credentialModel.getCredentialData());
            passwordCredentialModel.setId(credentialModel.getId());
            passwordCredentialModel.setSecretData(credentialModel.getSecretData());
            passwordCredentialModel.setType(credentialModel.getType());
            passwordCredentialModel.setUserLabel(credentialModel.getUserLabel());

            return passwordCredentialModel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /** @return 密码公开凭据数据 */
    public PasswordCredentialData getPasswordCredentialData() {
        return credentialData;
    }

    /** @return 密码密钥数据 */
    public PasswordSecretData getPasswordSecretData() {
        return secretData;
    }


}
