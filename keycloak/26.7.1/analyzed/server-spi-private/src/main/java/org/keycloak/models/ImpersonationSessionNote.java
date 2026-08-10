package org.keycloak.models;

/**
 * 模拟登录会话备注元数据，写入用户会话 notes 并在令牌中暴露 claim。
 * <p>记录模拟者用户 ID、用户名与客户端信息。</p>
 */
public enum ImpersonationSessionNote implements UserSessionNoteDescriptor {
    /** 模拟者用户 ID。 */
    IMPERSONATOR_ID("Impersonator User ID"),
    /** 模拟者用户名。 */
    IMPERSONATOR_USERNAME("Impersonator Username"),
    /** 发起模拟登录的客户端。 */
    IMPERSONATOR_CLIENT("Impersonator Client");

    final String displayName;

    ImpersonationSessionNote(String displayName) {
        this.displayName = displayName;
    }

    /** 管理 UI 显示名称。 */
    public String getDisplayName() {
        return displayName;
    }

    /** 令牌 claim 名称（枚举名转小写点分格式）。 */
    public String getTokenClaim() {
        return this.toString().toLowerCase().replace('_', '.');
    }
}
