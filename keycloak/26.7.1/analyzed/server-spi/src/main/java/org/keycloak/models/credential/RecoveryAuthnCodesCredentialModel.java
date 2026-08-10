package org.keycloak.models.credential;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.keycloak.credential.CredentialModel;
import org.keycloak.models.credential.dto.RecoveryAuthnCodeRepresentation;
import org.keycloak.models.credential.dto.RecoveryAuthnCodesCredentialData;
import org.keycloak.models.credential.dto.RecoveryAuthnCodesSecretData;
import org.keycloak.models.utils.RecoveryAuthnCodesUtils;
import org.keycloak.util.JsonSerialization;


/**
 * 恢复认证码凭据模型：管理一次性备份码的创建、消费与剩余计数。
 */
public class RecoveryAuthnCodesCredentialModel extends CredentialModel {

    /** 恢复认证码凭据类型标识。 */
    public static final String TYPE = "recovery-authn-codes";

    /** 已使用恢复码数量的 note 键。 */
    public static final String RECOVERY_CODES_NUMBER_USED = "recovery-codes-number-used";
    /** 剩余恢复码数量的 note 键。 */
    public static final String RECOVERY_CODES_NUMBER_REMAINING = "recovery-codes-number-remaining";
    /** 生成新恢复码操作的 note 键。 */
    public static final String RECOVERY_CODES_GENERATE_NEW_CODES = "recovery-codes-generate-new-codes";

    private final RecoveryAuthnCodesCredentialData credentialData;
    private final RecoveryAuthnCodesSecretData secretData;

    private RecoveryAuthnCodesCredentialModel(RecoveryAuthnCodesCredentialData credentialData,
            RecoveryAuthnCodesSecretData secretData) {
        this.credentialData = credentialData;
        this.secretData = secretData;
    }

    /** @return 下一个未使用的恢复码（若已全部用完则为空） */
    public Optional<RecoveryAuthnCodeRepresentation> getNextRecoveryAuthnCode() {
        if (allCodesUsed()) {
            return Optional.empty();
        }
        return Optional.of(this.secretData.getCodes().get(0));
    }

    /** @return 是否已全部用完 */
    public boolean allCodesUsed() {
        return this.secretData.getCodes().isEmpty();
    }

    /** 消费并移除下一个恢复码。 */
    public void removeRecoveryAuthnCode() {
        try {
            this.secretData.removeNextBackupCode();
            this.credentialData.setRemainingCodes(this.secretData.getCodes().size());
            this.setSecretData(JsonSerialization.writeValueAsString(this.secretData));
            this.setCredentialData(JsonSerialization.writeValueAsString(this.credentialData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 从原始明文恢复码列表创建凭据。 */
    public static RecoveryAuthnCodesCredentialModel createFromValues(List<String> originalGeneratedCodes, long generatedAt,
                                                                     String userLabel) {
        RecoveryAuthnCodesSecretData secretData;
        RecoveryAuthnCodesCredentialData credentialData;
        RecoveryAuthnCodesCredentialModel model;

        try {
            List<RecoveryAuthnCodeRepresentation> recoveryCodes = IntStream.range(0, originalGeneratedCodes.size())
                    .mapToObj(i -> new RecoveryAuthnCodeRepresentation(i + 1,
                            Base64.getEncoder().encodeToString(RecoveryAuthnCodesUtils.hashRawCode(originalGeneratedCodes.get(i)))))
                    .collect(Collectors.toList());
            secretData = new RecoveryAuthnCodesSecretData(recoveryCodes);
            credentialData = new RecoveryAuthnCodesCredentialData(null,
                    RecoveryAuthnCodesUtils.NOM_ALGORITHM_TO_HASH, recoveryCodes.size(), recoveryCodes.size());
            model = new RecoveryAuthnCodesCredentialModel(credentialData, secretData);
            model.setCredentialData(JsonSerialization.writeValueAsString(credentialData));
            model.setSecretData(JsonSerialization.writeValueAsString(secretData));
            model.setCreatedDate(generatedAt);
            model.setType(TYPE);

            if (userLabel != null) {
                model.setUserLabel(userLabel);
            }
            return model;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 从通用 {@link CredentialModel} 反序列化恢复码凭据。 */
    public static RecoveryAuthnCodesCredentialModel createFromCredentialModel(CredentialModel credentialModel) {
        RecoveryAuthnCodesCredentialData credentialData;
        RecoveryAuthnCodesSecretData secretData = null;
        RecoveryAuthnCodesCredentialModel newModel;
        try {
            credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(),
                    RecoveryAuthnCodesCredentialData.class);
            secretData = JsonSerialization.readValue(credentialModel.getSecretData(), RecoveryAuthnCodesSecretData.class);
            newModel = new RecoveryAuthnCodesCredentialModel(credentialData, secretData);
            newModel.setUserLabel(credentialModel.getUserLabel());
            newModel.setCreatedDate(credentialModel.getCreatedDate());
            newModel.setType(TYPE);
            newModel.setId(credentialModel.getId());
            newModel.setSecretData(credentialModel.getSecretData());
            newModel.setCredentialData(credentialModel.getCredentialData());
            return newModel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
