package org.keycloak.forms.login.freemarker.model;

import java.util.List;

import org.keycloak.common.util.Time;
import org.keycloak.models.utils.RecoveryAuthnCodesUtils;

/**
 * 恢复认证码生成 FreeMarker Bean：在配置流程中生成一批一次性恢复码供用户保存。
 * <p>构造时调用 {@link RecoveryAuthnCodesUtils#generateRawCodes()} 并记录生成时间戳。</p>
 */
public class RecoveryAuthnCodesBean {

    /** 本次生成的原始恢复码列表。 */
    private final List<String> generatedRecoveryAuthnCodesList;
    /** 生成时间戳（毫秒）。 */
    private final long generatedAt;

    /** 生成新的恢复码集合并记录当前时间。 */
    public RecoveryAuthnCodesBean() {
        this.generatedRecoveryAuthnCodesList = RecoveryAuthnCodesUtils.generateRawCodes();
        this.generatedAt = Time.currentTimeMillis();
    }

    /** @return 恢复码列表 */
    public List<String> getGeneratedRecoveryAuthnCodesList() {
        return this.generatedRecoveryAuthnCodesList;
    }

    /** @return 逗号分隔的恢复码字符串 */
    public String getGeneratedRecoveryAuthnCodesAsString() {
        return String.join(",", this.generatedRecoveryAuthnCodesList);
    }

    /** @return 生成时间戳 */
    public long getGeneratedAt() {
        return generatedAt;
    }

}
