package org.keycloak.ssf.transmitter.subject;


import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.ssf.event.stream.SsfStreamUpdatedEvent;
import org.keycloak.ssf.event.stream.SsfStreamVerificationEvent;
import org.keycloak.ssf.event.token.SsfSecurityEventToken;
import org.keycloak.ssf.metadata.DefaultSubjects;
import org.keycloak.ssf.subject.ComplexSubjectId;
import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.subject.SubjectUserLookup;
import org.keycloak.ssf.transmitter.stream.StreamConfig;

import org.jboss.logging.Logger;

/**
 * 分发时检查：基于流的 {@code default_subjects} 设置及事件主体上的
 * {@code ssf.notify.<clientId>} 属性，控制按主体的事件投递。
 *
 * <p>语义：
 * <ul>
 *     <li>{@link DefaultSubjects#ALL}：向所有人投递，除非用户/组织被显式排除
 *         （{@code ssf.notify.<clientId>=false}）。</li>
 *     <li>{@link DefaultSubjects#NONE}：仅在用户/组织被显式包含
 *         （{@code ssf.notify.<clientId>=true}）时投递。</li>
 * </ul>
 *
 * <p>由
 * {@link org.keycloak.ssf.transmitter.delivery.SecurityEventTokenDispatcher}
 * 在事件类型过滤之后、编码/推送之前调用。
 */
public class SubjectSubscriptionFilter {

    private static final Logger log = Logger.getLogger(SubjectSubscriptionFilter.class);

    /**
     * 宽限窗口（秒）：最近被移除的主体在 {@link DefaultSubjects#NONE} 流上仍可接收事件。
     * 防御 SSF 1.0 §9.3「恶意主题移除」场景。{@code 0} 禁用宽限检查（SPI 未设置时保持当前行为）。
     */
    protected final long subjectRemovalGraceSeconds;

    /**
     * 可插拔的包含/排除谓词。默认委托 {@link SsfNotifyAttributes}；扩展通过发送方 Provider 的
     * {@link org.keycloak.ssf.transmitter.SsfTransmitterProvider#subjectInclusionResolver}
     * 接入自定义逻辑（组属性、基于角色的 opt-in 等）。
     */
    protected final SsfSubjectInclusionResolver subjectInclusionResolver;

    /** 使用默认宽限（0）与默认包含解析器。 */
    public SubjectSubscriptionFilter() {
        this(0L, new DefaultSsfSubjectInclusionResolver());
    }

    /** @param subjectRemovalGraceSeconds 移除宽限秒数 */
    public SubjectSubscriptionFilter(long subjectRemovalGraceSeconds) {
        this(subjectRemovalGraceSeconds, new DefaultSsfSubjectInclusionResolver());
    }

    /**
     * @param subjectRemovalGraceSeconds 移除宽限秒数
     * @param subjectInclusionResolver 可插拔包含/排除解析器
     */
    public SubjectSubscriptionFilter(long subjectRemovalGraceSeconds,
                                     SsfSubjectInclusionResolver subjectInclusionResolver) {
        this.subjectRemovalGraceSeconds = Math.max(0L, subjectRemovalGraceSeconds);
        this.subjectInclusionResolver = subjectInclusionResolver != null
                ? subjectInclusionResolver
                : new DefaultSsfSubjectInclusionResolver();
    }

    /**
     * 事件是否应投递到给定流：{@code true} 投递，{@code false} 静默跳过。
     */
    public boolean shouldDispatch(SsfSecurityEventToken eventToken,
                                         StreamConfig stream,
                                         String receiverClientId,
                                         KeycloakSession session) {

        // 流管理事件（验证、stream-updated）始终投递——关乎流本身，而非特定用户主体。
        if (isSsfStreamEvent(eventToken)) {
            return true;
        }

        RealmModel realm = session.getContext().getRealm();
        UserModel user = resolveUserFromEvent(eventToken, session, realm);

        return evaluateSubjectSubscription(user, stream, receiverClientId, session, eventToken.getJti());
    }

    /**
     * 原生事件监听器使用的预令牌门控：在运行映射器前跳过流。
     * 接受预解析用户而非令牌主体——需要令牌主体语义（复合主体、流管理事件）的调用方
     * 应使用 {@link #shouldDispatch(SsfSecurityEventToken, StreamConfig, String, KeycloakSession)}。
     * 分发器侧门控仍会运行，故 {@code event.getUserId()} 与最终令牌主体不一致
     * （ impersonation、actor-on-behalf）仍安全。
     */
    public boolean shouldDispatchForUser(UserModel user,
                                         StreamConfig stream,
                                         String receiverClientId,
                                         KeycloakSession session) {
        return evaluateSubjectSubscription(user, stream, receiverClientId, session, null);
    }

    protected boolean evaluateSubjectSubscription(UserModel user,
                                                  StreamConfig stream,
                                                  String receiverClientId,
                                                  KeycloakSession session,
                                                  String jti) {

        DefaultSubjects defaultSubjects = stream.getDefaultSubjects();

        if (user == null) {
            // 事件含主体但无法解析用户（已删、issuer 不匹配、未知格式等）。
            // ALL 模式：投递（ benefit of the doubt）。
            // NONE 模式：阻断（无法验证订阅）。
            boolean deliver = defaultSubjects == DefaultSubjects.ALL;
            if (!deliver) {
                log.debugf("SSF subject filter: skipping event — user subject unresolvable (default_subjects=NONE). "
                                + "streamId=%s clientId=%s jti=%s",
                        stream.getStreamId(), receiverClientId, jti);
            }
            return deliver;
        }

        // 按用户显式设置始终优先于组织继承及 default_subjects 回退。
        // 管理员对特定用户点击「包含」或「忽略」时期望该决定不受组织成员资格默认值影响。
        if (subjectInclusionResolver.isUserNotified(session, user, receiverClientId)) {
            return true;
        }
        if (subjectInclusionResolver.isUserExcluded(session, user, receiverClientId)) {
            log.debugf("SSF subject filter: skipping event — user is explicitly excluded. "
                            + "streamId=%s clientId=%s userId=%s jti=%s",
                    stream.getStreamId(), receiverClientId,
                    user.getId(), jti);
            return false;
        }

        if (defaultSubjects == DefaultSubjects.ALL) {
            // 广播模式：除非任一组织排除该用户，否则投递。
            if (isOrganizationExcluded(user, receiverClientId, session)) {
                log.debugf("SSF subject filter: skipping event — user is excluded via organization (default_subjects=ALL). "
                                + "streamId=%s clientId=%s userId=%s jti=%s",
                        stream.getStreamId(), receiverClientId,
                        user.getId(), jti);
                return false;
            }
            return true;
        }

        // NONE 模式：仅在显式包含时投递。
        if (isOrganizationNotified(user, receiverClientId, session)) {
            return true;
        }

        // SSF §9.3 宽限窗口——接收方驱动的 remove 留下墓碑；在配置宽限内继续投递，
        // 防止被攻破的接收方静默屏蔽目标事件。有效宽限为每接收方覆盖
        // （ssf.subjectRemovalGraceSeconds 客户端属性）若已设置，否则为本过滤器构造时的发送方级默认值。0 禁用。
        long effectiveGrace = effectiveGraceSeconds(stream);
        if (effectiveGrace > 0
                && isWithinRemovalGrace(user, receiverClientId, session, effectiveGrace)) {
            log.debugf("SSF subject filter: delivering inside removal grace window (default_subjects=NONE). "
                            + "streamId=%s clientId=%s userId=%s jti=%s graceSeconds=%d",
                    stream.getStreamId(), receiverClientId,
                    user.getId(), jti, effectiveGrace);
            return true;
        }

        log.debugf("SSF subject filter: skipping event — user has no notification preference (default_subjects=NONE). "
                        + "streamId=%s clientId=%s userId=%s jti=%s",
                stream.getStreamId(), receiverClientId,
                user.getId(), jti);
        return false;
    }

    /**
     * 解析流的有效宽限窗口——优先每接收方覆盖
     * （{@code ssf.subjectRemovalGraceSeconds} 客户端属性，见
     * {@link StreamConfig#getSubjectRemovalGraceSeconds()}），
     * 否则回退为本过滤器构造时的发送方级默认值。
     */
    protected long effectiveGraceSeconds(StreamConfig stream) {
        Integer perReceiver = stream != null ? stream.getSubjectRemovalGraceSeconds() : null;
        if (perReceiver != null) {
            return Math.max(0L, perReceiver.longValue());
        }
        return subjectRemovalGraceSeconds;
    }

    /**
     * 用户（或其任一组织）是否在最近 {@code graceSeconds} 秒内
     * 经接收方驱动的 {@code /subjects/remove} 移除。
     */
    protected boolean isWithinRemovalGrace(UserModel user, String receiverClientId, KeycloakSession session, long graceSeconds) {
        long now = Time.currentTime();
        Long userTombstone = SsfNotifyAttributes.getRemovedAtForUser(user, receiverClientId);
        if (userTombstone != null && now - userTombstone < graceSeconds) {
            return true;
        }
        if (Organizations.isEnabled(session)) {
            return session.getProvider(OrganizationProvider.class).getByMember(user)
                    .anyMatch(org -> {
                        Long orgTombstone = SsfNotifyAttributes.getRemovedAtForOrganization(org, receiverClientId);
                        return orgTombstone != null
                                && now - orgTombstone < graceSeconds;
                    });
        }
        return false;
    }

    /**
     * 事件令牌是否携带 SSF 流管理事件（验证、stream-updated）。
     * 这些事件关乎流本身而非特定用户，须始终投递，不受主题过滤影响。
     */
    protected boolean isSsfStreamEvent(SsfSecurityEventToken eventToken) {
        var events = eventToken.getEvents();
        if (events == null || events.isEmpty()) {
            return false;
        }
        return events.containsKey(SsfStreamVerificationEvent.TYPE)
                || events.containsKey(SsfStreamUpdatedEvent.TYPE);
    }

    /**
     * 从事件令牌的主体 id 提取并解析用户。支持简单主体（email、iss_sub、opaque）
     * 及复合主体（深入 {@link ComplexSubjectId#getUser()}）。无法解析时返回 {@code null}。
     */
    protected UserModel resolveUserFromEvent(SsfSecurityEventToken eventToken,
                                                  KeycloakSession session,
                                                  RealmModel realm) {
        SubjectId subjectId = eventToken.getSubjectId();
        if (subjectId == null) {
            return null;
        }

        if (subjectId instanceof ComplexSubjectId complex) {
            SubjectId userSubject = complex.getUser();
            if (userSubject == null) {
                return null;
            }
            return lookupUserBySubject(session, realm, userSubject);
        }

        return lookupUserBySubject(session, realm, subjectId);
    }

    /** 按 {@link SubjectId} 查找用户。 */
    protected UserModel lookupUserBySubject(KeycloakSession session, RealmModel realm, SubjectId userSubject) {
        return SubjectUserLookup.lookupUser(session, realm, userSubject);
    }

    /**
     * 用户所属组织中是否有任一被 {@link #subjectInclusionResolver} 显式排除。
     * 此处 intentionally 不检查按用户排除——已在 {@link #evaluateSubjectSubscription} 更早处理且始终覆盖组织级状态；
     * 本辅助方法仅回答「用户是否通过某组织被排除」。
     */
    protected boolean isOrganizationExcluded(UserModel user, String receiverClientId, KeycloakSession session) {
        if (!Organizations.isEnabled(session)) {
            return false;
        }
        return session.getProvider(OrganizationProvider.class).getByMember(user)
                .anyMatch(org -> subjectInclusionResolver.isOrganizationExcluded(session, org, receiverClientId));
    }

    /** 用户所属组织中是否有任一被 {@link #subjectInclusionResolver} 显式订阅。 */
    protected boolean isOrganizationNotified(UserModel user, String receiverClientId, KeycloakSession session) {
        if (!Organizations.isEnabled(session)) {
            return false;
        }
        return session.getProvider(OrganizationProvider.class).getByMember(user)
                .anyMatch(org -> subjectInclusionResolver.isOrganizationNotified(session, org, receiverClientId));
    }
}
