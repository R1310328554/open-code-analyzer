package org.keycloak.models;

/**
 * 用户会话 note 描述符：用于简化、通用化 {@link ProtocolMapperModel} 的创建。
 * <p>定义 note 的展示名称与写入令牌的 claim 路径。</p>
 */
public interface UserSessionNoteDescriptor {
    /**
     * @return 会话 note 的可读名称，便于终端用户理解其含义
     */
    String getDisplayName();

    /**
     * @return 将会话 note 值写入令牌的 claim 名称/路径
     */
    String getTokenClaim();
}
