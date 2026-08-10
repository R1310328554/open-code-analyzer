package org.keycloak.ssf.transmitter.subject;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.UserModel;

/**
 * 读侧门控：判断用户或组织是否应视为某接收方的已订阅主体。
 * 驱动分发器的主题筛选逻辑以及合成事件的可投递性检查。
 *
 * <p>默认实现 {@link DefaultSsfSubjectInclusionResolver} 通过
 * {@link SsfNotifyAttributes} 读取用户及其所属组织上的
 * {@code ssf.notify.<receiverClientId>} 属性。扩展可子类化并在默认行为之上
 * 叠加额外来源（组属性查询、基于角色的订阅、外部策略服务等），例如：
 *
 * <pre>{@code
 * @Override
 * public boolean isUserNotified(KeycloakSession s, UserModel u, String c) {
 *     return super.isUserNotified(s, u, c) || isInNotifyGroup(u, c);
 * }
 * }</pre>
 *
 * <p>按会话通过
 * {@link org.keycloak.ssf.transmitter.SsfTransmitterProvider#subjectInclusionResolver()}
 * 解析。无独立 Keycloak SPI 注册——扩展通过覆盖发送方 Provider 的
 * {@code subjectInclusionResolver()} 访问器，或提供自定义
 * {@link org.keycloak.ssf.transmitter.SsfTransmitterServiceBuilder} 接入。
 *
 * <p>墓碑读取（用于 SSF §9.3 宽限窗口的 {@code ssf.notifyRemovedAt.*} 属性）
 * 有意不在此接口中：它们属于写侧的时间戳查询
 *（{@link SsfNotifyAttributes#stampRemovedAtForUser}），而非订阅判定，
 * 因此保留在静态辅助类上。
 */
public interface SsfSubjectInclusionResolver {

    /**
     * 用户是否应视为对该接收方显式订阅。
     * 驱动 {@code default_subjects=NONE} 的包含检查——返回 {@code true} 时，
     * 即使流默认为 opt-in 也会投递事件。
     */
    boolean isUserNotified(KeycloakSession session, UserModel user, String receiverClientId);

    /**
     * 用户是否应视为对该接收方显式排除。
     * 驱动 {@code default_subjects=ALL} 的跳过检查——返回 {@code true} 时，
     * 即使流默认为广播也会丢弃事件。
     */
    boolean isUserExcluded(KeycloakSession session, UserModel user, String receiverClientId);

    /**
     * {@link #isUserNotified} 的组织级对应方法。
     * 对用户所属的每个组织调用；任一匹配即视为已订阅。
     */
    boolean isOrganizationNotified(KeycloakSession session, OrganizationModel organization, String receiverClientId);

    /**
     * {@link #isUserExcluded} 的组织级对应方法。
     * 对用户所属的每个组织调用；任一匹配即排除该主体。
     */
    boolean isOrganizationExcluded(KeycloakSession session, OrganizationModel organization, String receiverClientId);
}
