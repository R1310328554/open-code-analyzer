package org.keycloak.ssf.transmitter.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.events.Details;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.ssf.SsfException;
import org.keycloak.ssf.event.InitiatingEntity;
import org.keycloak.ssf.event.caep.CaepCredentialChange;
import org.keycloak.ssf.event.caep.CaepEvent;
import org.keycloak.ssf.event.caep.CaepSessionRevoked;
import org.keycloak.ssf.event.stream.SsfStreamUpdatedEvent;
import org.keycloak.ssf.event.stream.SsfStreamVerificationEvent;
import org.keycloak.ssf.event.token.SsfSecurityEventToken;
import org.keycloak.ssf.stream.StreamStatus;
import org.keycloak.ssf.subject.ComplexSubjectId;
import org.keycloak.ssf.subject.EmailSubjectId;
import org.keycloak.ssf.subject.IssuerSubjectId;
import org.keycloak.ssf.subject.OpaqueSubjectId;
import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.transmitter.SsfTransmitterConfig;
import org.keycloak.ssf.transmitter.stream.StreamConfig;
import org.keycloak.ssf.transmitter.support.SsfUtil;

import org.jboss.logging.Logger;


/**
 * 安全事件令牌（SET）生成器。
 */
public class SecurityEventTokenMapper {

    protected static final Logger log = Logger.getLogger(SecurityEventTokenMapper.class);

    // Each group matches a single path segment ([^/]+) rather than .* — path
    // segments are UUIDs that never contain '/', and .* across segment
    // boundaries enables polynomial backtracking on adversarial input like
    // "users//credentials/a/credentials/a/…" (CodeQL js/polynomial-redos).
    protected static final Pattern USER_LOGGED_OUT_BY_ADMIN_PATH_PATTERN = Pattern.compile("^users/([^/]+)/logout$");

    protected static final Pattern USER_RESET_PASSWORD_BY_ADMIN_PATH_PATTERN = Pattern.compile("^users/([^/]+)/reset-password$");

    protected static final Pattern USER_CREDENTIALS_CHANGED_BY_ADMIN_PATH_PATTERN = Pattern.compile("^users/([^/]+)/credentials/([^/]+)$");

    public static final String KC_CREDENTIAL_ID = "kc_credential_id";

    public static final String KC_CREDENTIAL_TYPE = "kc_credential_type";

    public static final String KC_CREDENTIAL_USER_LABEL = "kc_credential_user_label";

    /**
     * 发行方 URL 解析器。在令牌构建时惰性调用而非构造时调用，
     * 使脱离请求的调用方（例如定时 SSF 发件箱排空器）在仅需发送方 provider 的无 mapper 切片时
     * 不会因依赖 {@code HttpRequest} 的主机名解析而失败。
     */
    protected final Function<KeycloakSession, String> issuerGenerator;

    protected String issuer;

    protected final KeycloakSession session;

    protected final SsfTransmitterConfig transmitterConfig;

    public SecurityEventTokenMapper(KeycloakSession session, SsfTransmitterConfig transmitterConfig, Function<KeycloakSession, String> issuerGenerator) {
        this.session = session;
        this.issuerGenerator = issuerGenerator;
        this.transmitterConfig = transmitterConfig;
    }

    protected String getIssuer() {
        if (issuer == null) {
            issuer = issuerGenerator.apply(session);
        }
        return issuer;
    }

    /**
     * 为流生成验证事件。
     *
     * @param stream 流配置
     * @param state  验证状态
     * @return 验证事件的 JSON 字符串表示
     */
    /**
     * 按 SSF §8.1.5 生成 stream-updated SET，向接收方传达流状态变更。
     * 主体为流本身（opaque {@code stream_id}），事件载荷携带新状态及可选原因——
     * 与 {@code GET /stream/status} 使用的形状相同。
     *
     * <p>调用方应通过 {@link org.keycloak.ssf.transmitter.delivery.SecurityEventTokenDispatcher#deliverEvent
     * deliverEvent}（绕过门控、异步）派发返回的令牌，使接收方在新状态为
     * {@code paused}/{@code disabled} 时仍能看到状态变更。</p>
     */
    public SsfSecurityEventToken generateStreamUpdatedEvent(StreamConfig stream, StreamStatus newStatus) {
        try {
            SsfSecurityEventToken token = newSecurityEventToken(stream);
            token.setTxn(UUID.randomUUID().toString());

            OpaqueSubjectId subId = new OpaqueSubjectId();
            subId.setId(stream.getStreamId());
            token.setSubjectId(subId);

            SsfStreamUpdatedEvent payload = new SsfStreamUpdatedEvent();
            payload.setStatus(newStatus);
            payload.setReason(newStatus.getReason());

            Map<String, Object> events = new HashMap<>();
            events.put(SsfStreamUpdatedEvent.TYPE, payload);
            token.setEvents(events);

            return token;
        } catch (Exception e) {
            log.error("Error generating stream-updated event", e);
            return null;
        }
    }

    public SsfSecurityEventToken generateVerificationEvent(StreamConfig stream, String state) {
        try {
            SsfSecurityEventToken verificationEventToken = newSecurityEventToken(stream);

            // Set transaction ID
            verificationEventToken.setTxn(UUID.randomUUID().toString());

            // Set subject ID
            OpaqueSubjectId subId = new OpaqueSubjectId();
            subId.setId(stream.getStreamId());
            verificationEventToken.setSubjectId(subId);

            // Set events
            Map<String, Object> events = new HashMap<>();
            SsfStreamVerificationEvent verificationEvent = new SsfStreamVerificationEvent();
            verificationEvent.setState(state);
            events.put(SsfStreamVerificationEvent.TYPE, verificationEvent);
            verificationEventToken.setEvents(events);

            return verificationEventToken;
        } catch (Exception e) {
            log.error("Error generating verification event", e);
            return null;
        }
    }

    protected SsfSecurityEventToken newSecurityEventToken(StreamConfig stream) {
        SsfSecurityEventToken securityEventToken = new SsfSecurityEventToken();

        securityEventToken.setJti(SecretGenerator.getInstance().generateSecureID());
        securityEventToken.setIss(getIssuer());
        securityEventToken.setIat(Time.currentTime());

        // Set the SET audience to the stream's audience
        if (stream.getDelivery() != null && stream.getDelivery().getEndpointUrl() != null) {
            securityEventToken.setAud(stream.getAudience().toArray(new String[0]));
        }

        return securityEventToken;
    }


    /**
     * 生成会话撤销事件。
     *
     * @param event                用户事件
     * @param sessionId            被撤销会话的 ID
     * @param userId               用户 ID
     * @param eventTokenCustomizer 事件令牌定制器
     * @param adminEvent           管理事件
     * @param stream               流配置
     * @param reason               撤销原因
     * @return 会话撤销事件，形式为 SecurityEventToken
     */
    public SsfSecurityEventToken generateSessionRevokedEvent(Event userEvent, AdminEvent adminEvent, StreamConfig stream, String reason) {
        try {

            String sessionId = userEvent.getSessionId();
            String userId = userEvent.getUserId();

            SsfSecurityEventToken eventToken = newSecurityEventToken(stream);
            eventToken.setTxn(UUID.randomUUID().toString());

            // Set subject ID (complex subject with user and session,
            // plus tenant when the configured user-subject format
            // carries the +tenant composition suffix).
            ComplexSubjectId subId = new ComplexSubjectId();

            subId.setUser(buildUserSubjectId(eventToken, userId, stream));

            OpaqueSubjectId sessionSubject = new OpaqueSubjectId();
            sessionSubject.setId(sessionId);
            subId.setSession(sessionSubject);

            addTenantIfConfigured(subId, userId, stream);

            eventToken.setSubjectId(subId);

            // Set events
            Map<String, Object> events = new HashMap<>();
            CaepSessionRevoked sessionRevokedEvent = new CaepSessionRevoked();
            applyInitiatingEntity(userEvent, adminEvent, sessionRevokedEvent);

            if (reason != null) {
                sessionRevokedEvent.setReasonAdmin(Map.of("en", reason));
            }
            sessionRevokedEvent.setEventTimestamp(Time.currentTime());

            events.put(CaepSessionRevoked.TYPE, sessionRevokedEvent);
            eventToken.setEvents(events);

            return eventToken;
        } catch (Exception e) {
            log.error("Error generating session revoked event", e);
            return null;
        }
    }

    /**
     * 生成凭证变更事件。
     *
     * <p>CAEP {@code change_type} 由调用方提供，因为 Keycloak 的
     * {@code UPDATE_CREDENTIAL}/{@code REMOVE_CREDENTIAL}/{@code RESET_PASSWORD} 事件类型
     * 各自映射到不同的 CAEP change_type（UPDATE/DELETE/UPDATE），
     * 而 {@link #toSecurityEventToken(Event, StreamConfig)} 中的派发器知道各类型的正确值。
     * {@code credentialType} 字符串在存在时从 {@code userEvent.getDetails().get(Details.CREDENTIAL_TYPE)} 读取，
     * 否则回退至 {@code credentialTypeFallback}——用于 {@code RESET_PASSWORD} 等未设置该详情
     * 但凭证类型隐含为 "password" 的事件。</p>
     *
     * <p>此路径内区分 CREATE 与 UPDATE 尚未实现：Keycloak 对「首次添加该类型凭证」与
     * 「修改现有凭证」均触发 {@code UPDATE_CREDENTIAL} 且事件本身无标记，
     * 此处启发式判断不可靠。对新增与修改均保守采用 UPDATE。</p>
     */
    public SsfSecurityEventToken generateCredentialChangeEvent(Event userEvent,
                                                               AdminEvent adminEvent,
                                                               StreamConfig streamConfig,
                                                               CaepCredentialChange.ChangeType changeType,
                                                               String credentialTypeFallback) {
        try {

            String userId = userEvent.getUserId();
            String credentialType = userEvent.getDetails() != null
                    ? userEvent.getDetails().get(Details.CREDENTIAL_TYPE)
                    : null;
            if (credentialType == null || credentialType.isBlank()) {
                credentialType = credentialTypeFallback;
            }

            SsfSecurityEventToken eventToken = newSecurityEventToken(streamConfig);
            eventToken.setTxn(UUID.randomUUID().toString());

            // Set subject ID — composeUserSubject wraps in a complex
            // subject with a tenant sibling when the configured format
            // carries the +tenant composition suffix; otherwise the
            // bare user subject goes on as before.
            eventToken.setSubjectId(composeUserSubject(eventToken, userId, streamConfig));

            String caepCredentialType = narrowCaepCredentialType(credentialType);

            CaepCredentialChange credentialChangeEvent = new CaepCredentialChange();
            credentialChangeEvent.setChangeType(changeType);
            credentialChangeEvent.setEventTimestamp(Time.currentTime());
            credentialChangeEvent.setCredentialType(caepCredentialType);
            applyInitiatingEntity(userEvent, adminEvent, credentialChangeEvent);
            applyCustomAttributes(userEvent, adminEvent, credentialChangeEvent);

            // Set events
            Map<String, Object> events = new HashMap<>();
            events.put(CaepCredentialChange.TYPE, credentialChangeEvent);
            eventToken.setEvents(events);

            return eventToken;
        } catch (Exception e) {
            log.error("Error generating credential change event", e);
            return null;
        }
    }

    protected void applyCustomAttributes(Event userEvent, AdminEvent adminEvent, CaepCredentialChange credentialChangeEvent) {
        // Keycloak user events aren't required to carry a details map —
        // RESET_PASSWORD in particular doesn't populate it. Skip the
        // custom-attribute enrichment in that case rather than NPE.
        Map<String, String> userEventDetails = userEvent != null ? userEvent.getDetails() : null;
        if (userEventDetails == null) {
            return;
        }
        String kcCredentialId = userEventDetails.get(Details.CREDENTIAL_ID);
        String kcCredentialType = userEventDetails.get(Details.CREDENTIAL_TYPE);
        String kcUserLabel = userEventDetails.get(Details.CREDENTIAL_USER_LABEL);

        credentialChangeEvent.setAttributeValue(KC_CREDENTIAL_ID, kcCredentialId);
        credentialChangeEvent.setAttributeValue(KC_CREDENTIAL_TYPE, kcCredentialType);
        credentialChangeEvent.setAttributeValue(KC_CREDENTIAL_USER_LABEL, kcUserLabel);
    }

    protected void applyInitiatingEntity(Event userEvent, AdminEvent adminEvent, CaepEvent caepEvent) {
        if (adminEvent != null) {
            caepEvent.setInitiatingEntity(InitiatingEntity.ADMIN);
        } else {
            caepEvent.setInitiatingEntity(InitiatingEntity.USER);
        }
    }

    protected String narrowCaepCredentialType(String credentialType) {

        // best effort attempt at mapping Keycloak credential types to CAEP credential types
        // see: https://openid.net/specs/openid-caep-1_0-final.html#section-3.3.1-1

        if (credentialType == null) {
            return "unknown";
        }

        if (PasswordCredentialModel.TYPE.equals(credentialType)) {
            return "password";
        }

        if (OTPCredentialModel.TYPE.equals(credentialType)) {
            return "app";
        }

        if (WebAuthnCredentialModel.TYPE_TWOFACTOR.equals(credentialType)) {
            // could be fido2-platform or fido2-roaming, assume roaming
            return "fido2-roaming";
        }

        if (WebAuthnCredentialModel.TYPE_PASSWORDLESS.equals(credentialType)) {
            // could be fido2-platform, assume platform
            return "fido2-platform";
        }

        return credentialType;
    }

    /**
     * 为管理端事件发射端点产生的合成事件构建安全事件令牌——即 Keycloak 自身未观察到、
     * 但可信 IAM 管理客户端希望发送方代为转发的事件。
     *
     * <p>调用方提供已解析的事件类型 URI、反序列化后的事件载荷及发射方选择的 RFC 9493 {@link SubjectId}。
     * {@code sub_id} 原样传入——发射方受信会选择适合接收方的格式
     *（与原生发出的事件不同，上游系统拥有主体身份，超出发送方所知范围）。</p>
     *
     * <p>SET 其余部分按标准头（iss、jti、iat、aud、txn）构建，与原生发出的事件相同。</p>
     *
     * <p>构建失败时返回 {@code null}（已记录日志）。</p>
     */
    public SsfSecurityEventToken generateSyntheticEvent(StreamConfig stream,
                                                        String eventTypeUri,
                                                        Object eventPayload,
                                                        SubjectId subjectId) {
        try {
            SsfSecurityEventToken token = newSecurityEventToken(stream);
            token.setTxn(UUID.randomUUID().toString());
            token.setSubjectId(subjectId);

            Map<String, Object> events = new HashMap<>();
            events.put(eventTypeUri, eventPayload);
            token.setEvents(events);

            return token;
        } catch (Exception e) {
            log.error("Error generating synthetic event", e);
            return null;
        }
    }

    /**
     * 按流配置的用户主体格式（回退至 {@link SsfUserSubjectFormats#DEFAULT iss_sub}）
     * 为 Keycloak 用户构建 SSF 主体标识符。对所有 {@code sub_id} 携带用户标识符的事件类型调用——
     * 既作为 {@link ComplexSubjectId} 的 {@code user} 字段（如 {@link CaepSessionRevoked}），
     * 也作为较简单事件的顶层 {@code sub_id}（如 {@link CaepCredentialChange}）。
     *
     * <p>当配置格式为 {@code email} 且用户无可用邮箱（事件中途用户被删或无邮箱记录）时，
     * 本方法抛出 {@link SsfException}——静默替换为 {@code iss_sub} 会发出与接收方协商形状不同的 SET，
     * 严格接收方会拒绝，宽松接收方会误路由。调用方捕获异常、记录日志并丢弃事件。
     * 接收方因此看不到错误信号而非被误导的信号；运维人员看到警告后可修复用户记录或更改流的主体格式。</p>
     */
    protected SubjectId buildUserSubjectId(SsfSecurityEventToken eventToken, String userId, StreamConfig stream) {

        // Strip any composition suffix (e.g. "+tenant") — that part is
        // applied by addTenantIfConfigured at the outer complex-subject
        // level. This method only computes the user-identifying portion.
        String format = SsfUserSubjectFormats.userPartOf(
                SsfUserSubjectFormats.resolveForStream(stream, transmitterConfig));

        if (EmailSubjectId.TYPE.equals(format)) {
            String email = lookupUserEmail(userId);
            if (email == null || email.isBlank()) {
                throw new SsfException("Configured user subject format is 'email' but no email is available for user "
                        + userId + " (stream " + (stream != null ? stream.getStreamId() : null) + ")");
            }
            EmailSubjectId emailSubject = new EmailSubjectId();
            emailSubject.setEmail(email);
            return emailSubject;
        }

        // Reuse the issuer that newSecurityEventToken already resolved onto
        // the token — one lookup per event instead of two, and the subject
        // identity stays consistent with the SET's top-level iss.
        IssuerSubjectId issSubject = new IssuerSubjectId();
        issSubject.setIss(eventToken.getIss());
        issSubject.setSub(userId);
        return issSubject;
    }

    /**
     * 若配置的用户主体格式携带 {@code +tenant} 组合后缀，则解析用户的主 Keycloak 组织
     * 并将其作为 {@code tenant} 成员加入给定复合主体。格式不含 tenant 时为 no-op。
     * 用户不属于任何组织时抛出 {@link SsfException}（由调用方事件生成器捕获并记录）——
     * 静默丢弃 tenant 槽会发出与接收方协商形状不同的 SET，与无邮箱时 {@code email} 格式的
     * 显式失败行为一致。
     */
    protected void addTenantIfConfigured(ComplexSubjectId complex, String userId, StreamConfig stream) {
        String format = SsfUserSubjectFormats.resolveForStream(stream, transmitterConfig);
        if (!SsfUserSubjectFormats.includesTenant(format)) {
            return;
        }
        complex.setTenant(buildTenantSubject(userId, stream));
    }

    /**
     * 使用与原生事件发射相同的代码路径为给定接收方构建用户主体——
     * 遵循接收方配置的 {@code ssf.userSubjectFormat}（iss_sub/email/
     * complex.iss_sub+tenant/complex.email+tenant），对缺失邮箱或无组织同样显式失败。
     *
     * <p>供管理端「待处理事件」发射表单使用，运维人员可按 UUID 选择用户，
     * 由发送方按接收方协商的主体形状格式化 sub_id，而非管理 UI 无视配置硬编码 {@code iss_sub}。</p>
     */
    public SubjectId buildSubjectForReceiver(StreamConfig stream, String userId) {
        SsfSecurityEventToken stub = newSecurityEventToken(stream);
        return composeUserSubject(stub, userId, stream);
    }

    /**
     * 当配置格式包含 {@code +tenant} 组合时将用户主体包装为 {@link ComplexSubjectId}；
     * 否则返回裸用户主体。供默认发射形状为单一主体的事件生成器
     *（如 {@code credential-change}）使用，以便在不无条件切换为复合形状的情况下加入 tenant 成员。
     */
    protected SubjectId composeUserSubject(SsfSecurityEventToken eventToken, String userId, StreamConfig stream) {
        SubjectId userSubject = buildUserSubjectId(eventToken, userId, stream);
        String format = SsfUserSubjectFormats.resolveForStream(stream, transmitterConfig);
        if (!SsfUserSubjectFormats.includesTenant(format)) {
            return userSubject;
        }
        ComplexSubjectId complex = new ComplexSubjectId();
        complex.setUser(userSubject);
        complex.setTenant(buildTenantSubject(userId, stream));
        return complex;
    }

    /**
     * 构建携带用户主 Keycloak 组织别名的 {@link OpaqueSubjectId}。
     * 用户不属于任何组织时抛出 {@link SsfException}——显式失败原因参见 {@link #addTenantIfConfigured}。
     *
     * <p><b>多组织解析策略：优先 MANAGED。</b> 用户属于多个组织时，优先选择 {@code MANAGED} 成员关系
     *（配置该用户的组织——按 Keycloak 组织模型每用户至多一个），否则回退至第一个 {@code UNMANAGED} 成员关系。
     * 这使具有明确配置来源（SCIM、IdP 联邦、JIT）的用户获得稳定的「所属组织」答案；
     * 仅关联一个或多个组织的用户则得到确定但任意的流内首个选择。
     * 需要更严格语义（仅 managed）或不同策略的部署可子类化此方法。</p>
     */
    protected SubjectId buildTenantSubject(String userId, StreamConfig stream) {
        if (session == null || userId == null) {
            throw new SsfException("Cannot build tenant subject: missing session or userId (stream "
                    + (stream != null ? stream.getStreamId() : null) + ")");
        }
        RealmModel realm = session.getContext().getRealm();
        if (!Organizations.isEnabled(session)) {
            throw new SsfException("Cannot build tenant subject: organization feature is not enabled (stream "
                    + (stream != null ? stream.getStreamId() : null) + ")");
        }
        OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            throw new SsfException("Cannot build tenant subject: user " + userId + " not found (stream "
                    + (stream != null ? stream.getStreamId() : null) + ")");
        }
        OrganizationModel org = orgProvider.getByMember(user)
                .filter(candidate -> orgProvider.isManagedMember(candidate, user))
                .findFirst()
                .orElseGet(() -> orgProvider.getByMember(user).findFirst().orElse(null));
        if (org == null) {
            throw new SsfException("Configured user subject format includes '+tenant' but user " + userId
                    + " belongs to no organization (stream " + (stream != null ? stream.getStreamId() : null) + ")");
        }
        // Emit alias rather than the internal UUID — alias is the stable,
        // human-readable organization identifier and the receiver-side
        // SubjectResolver tries getByAlias as a fallback to getById, so
        // this resolves on round-trip without requiring receivers to
        // know the transmitter's internal UUIDs.
        return createTenantSubjectId(org, user);
    }

    protected OpaqueSubjectId createTenantSubjectId(OrganizationModel org, UserModel user) {
        OpaqueSubjectId tenantSubject = new OpaqueSubjectId();
        tenantSubject.setId(org.getAlias());
        return tenantSubject;
    }

    /**
     * 解析当前 realm 中 Keycloak 用户的邮箱地址。
     * session 不可用、用户无法解析或用户无邮箱时返回 {@code null}——调用方在此情况下回退至 {@code iss_sub}。
     */
    protected String lookupUserEmail(String userId) {
        if (session == null || userId == null) {
            return null;
        }
        RealmModel realm = session.getContext().getRealm();
        if (realm == null) {
            return null;
        }
        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            return null;
        }
        return user.getEmail();
    }

    /**
     * 轻量谓词：仅基于事件类型及事件上现有 details，判断 {@link #toSecurityEventToken(Event, StreamConfig)}
     * 对 {@code event} 是否会产出非 null SET 时返回 {@code true}。
     * 调用方用于在 {@code SsfTransmitterEventListener} 中短路逐事件的流查找——
     * 若无法映射，则无需访问客户端存储查找 eligible 流。
     *
     * <p>该检查刻意镜像 {@link #toSecurityEventToken(Event, StreamConfig)} 中的 {@code switch} 以保持同步。
     * mapper 新增的事件类型须在此同步反映，否则监听器会静默丢弃它们。</p>
     */
    public boolean canConvert(Event event) {
        if (event == null || shouldIgnoreEvent(event)) {
            return false;
        }
        return switch (event.getType()) {
            case LOGOUT -> !shouldIgnoreLogout(event);
            case UPDATE_CREDENTIAL,
                 REMOVE_CREDENTIAL,
                 RESET_PASSWORD -> !shouldIgnoreCredentialChange(event);
            default -> false;
        };
    }

    /**
     * 轻量谓词：判断 {@link #toSecurityEventToken(AdminEvent, StreamConfig)} 对 {@code adminEvent}
     * 是否会产出非 null SET 时返回 {@code true}。当前唯一映射的管理操作为
     * 「注销用户全部会话」路径（{@code users/{userId}/logout}）；其余均返回 null，
     * 应在任何流查找之前短路。
     */
    public boolean canConvert(AdminEvent adminEvent) {
        if (adminEvent == null) {
            return false;
        }
        if (!ResourceType.USER.equals(adminEvent.getResourceType())) {
            return false;
        }
        String path = adminEvent.getResourcePath();
        if (path == null) {
            return false;
        }

        for (Pattern pattern : supportedAdminPathPatters()) {
            if (pattern.matcher(path).matches()) {
                return true;
            }
        }

        return false;
    }

    protected List<Pattern> supportedAdminPathPatters() {
        return List.of(USER_LOGGED_OUT_BY_ADMIN_PATH_PATTERN,
                USER_RESET_PASSWORD_BY_ADMIN_PATH_PATTERN,
                USER_CREDENTIALS_CHANGED_BY_ADMIN_PATH_PATTERN);
    }

    protected boolean shouldIgnoreEvent(Event event) {
        return false;
    }

    public SsfSecurityEventToken toSecurityEventToken(Event event, StreamConfig stream) {
        return toSecurityEventToken(event, null, stream);
    }

    public SsfSecurityEventToken toSecurityEventToken(Event event, AdminEvent adminEvent, StreamConfig stream) {

        SsfSecurityEventToken securityEvent = switch (event.getType()) {

            case LOGOUT -> {

                // ignore expired session cleanup, we only want to propagate real logouts!
                if (shouldIgnoreLogout(event)) {
                    yield null;
                }

                yield generateSessionRevokedEvent(event, adminEvent, stream, "User logout");
            }

            case UPDATE_CREDENTIAL -> {

                // ignore credential changes for credentials that are not used for authentication
                if (shouldIgnoreCredentialChange(event)) {
                    yield null;
                }

                // CAEP change_type=update covers both "first credential of
                // this type added" and "existing credential modified" —
                // Keycloak's UPDATE_CREDENTIAL fires for both without a
                // distinguishing detail, so we conservatively report
                // UPDATE for both. credential_type is read from the
                // event's Details.CREDENTIAL_TYPE.
                yield generateCredentialChangeEvent(event, adminEvent, stream,
                        CaepCredentialChange.ChangeType.UPDATE, null);
            }

            case REMOVE_CREDENTIAL -> {

                if (shouldIgnoreCredentialChange(event)) {
                    yield null;
                }

                // CAEP change_type=delete: the credential was removed
                // (required-action DeleteCredentialAction). credential_type
                // is on the event's Details.
                yield generateCredentialChangeEvent(event, adminEvent, stream,
                        CaepCredentialChange.ChangeType.DELETE, null);
            }

            case RESET_PASSWORD -> {

                if (shouldIgnoreCredentialChange(event)) {
                    yield null;
                }

                // RESET_PASSWORD is the forgot-password completion flow —
                // CAEP change_type=update on the password credential.
                // Keycloak doesn't set Details.CREDENTIAL_TYPE on this
                // event, so pass "password" as the fallback.
                yield generateCredentialChangeEvent(event, adminEvent, stream,
                        CaepCredentialChange.ChangeType.UPDATE, "password");
            }
            // Add more event mappings here as needed.
            // Deliberately NOT mapped: UPDATE_PASSWORD / UPDATE_TOTP /
            // REMOVE_TOTP — these are deprecated event types Keycloak
            // fires as clones alongside UPDATE_CREDENTIAL /
            // REMOVE_CREDENTIAL. Mapping them here would emit a
            // duplicate SET per real change.

            default -> {

                if (shouldIgnoreEvent(event)) {
                    yield null;
                }

                yield generateSecurityEventTokenFromEvent(event, stream);
            }
        };
        // Map Keycloak events to SSF events

        return securityEvent;
    }

    protected boolean shouldIgnoreCredentialChange(Event event) {
        return false;
    }

    protected boolean shouldIgnoreLogout(Event event) {
        String reason = event.getDetails().get(Details.REASON);
        return Details.USER_SESSION_EXPIRED_REASON.equals(reason) || Details.INVALID_USER_SESSION_REMEMBER_ME_REASON.equals(reason);
    }

    protected SsfSecurityEventToken generateSecurityEventTokenFromEvent(Event event, StreamConfig stream) {
        return null;
    }

    public SsfSecurityEventToken toSecurityEventToken(AdminEvent adminEvent, StreamConfig stream) {

        String userId = SsfUtil.userIdFromAdminEventPath(adminEvent);
        if (userId == null) {
            return null;
        }

        String path = adminEvent.getResourcePath();
        Matcher matcher = USER_LOGGED_OUT_BY_ADMIN_PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return generateLogoutEventForAdminLogoutAllUserSessions(userId, adminEvent, stream);
        }

        matcher = USER_RESET_PASSWORD_BY_ADMIN_PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return generateCredentialChangeEventForAdminAction(userId, adminEvent, stream, CaepCredentialChange.ChangeType.UPDATE, null, "password_reset");
        }

        matcher = USER_CREDENTIALS_CHANGED_BY_ADMIN_PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            String credentialId = matcher.group(2);

            CaepCredentialChange.ChangeType changeType = CaepCredentialChange.ChangeType.UPDATE;
            if (adminEvent.getOperationType() == OperationType.DELETE || adminEvent.getOperationType() == OperationType.ACTION) {
                changeType = CaepCredentialChange.ChangeType.DELETE;
            }

            return generateCredentialChangeEventForAdminAction(userId, adminEvent, stream, changeType, credentialId, "credential_management");
        }

        return null;
    }

    protected SsfSecurityEventToken generateCredentialChangeEventForAdminAction(String userId,
                                                                                AdminEvent adminEvent,
                                                                                StreamConfig stream,
                                                                                CaepCredentialChange.ChangeType changeType,
                                                                                String credentialId,
                                                                                String reason) {

        Event event = new Event();

        String credentialType = PasswordCredentialModel.TYPE;
        String userLabel = null;
        if (credentialId != null) {
            RealmModel realm = session.realms().getRealm(adminEvent.getRealmId());
            UserModel user = session.users().getUserById(realm, userId);
            CredentialModel storedCredentialById = user.credentialManager().getStoredCredentialById(credentialId);
            if (storedCredentialById != null) {
                credentialType = storedCredentialById.getType();
                userLabel = storedCredentialById.getUserLabel();
            } else if (changeType == CaepCredentialChange.ChangeType.DELETE) {
                credentialType = adminEvent.getDetails().get(Details.CREDENTIAL_TYPE);
                credentialId = adminEvent.getDetails().get(Details.CREDENTIAL_ID);
                userLabel = adminEvent.getDetails().get(Details.CREDENTIAL_USER_LABEL);
            }
        }

        event.setType(EventType.RESET_PASSWORD);
        event.setUserId(userId);
        event.setDetails(new HashMap<>());
        event.getDetails().put("admin", "true");
        event.getDetails().put(Details.REASON, reason);
        event.getDetails().put(Details.CREDENTIAL_TYPE, credentialType);
        event.getDetails().put(Details.CREDENTIAL_ID, credentialId);
        event.getDetails().put(Details.CREDENTIAL_USER_LABEL, userLabel);

        if (shouldIgnoreCredentialChange(event)) {
            return null;
        }

        return generateCredentialChangeEvent(event, adminEvent, stream, changeType, null);
    }

    protected SsfSecurityEventToken generateLogoutEventForAdminLogoutAllUserSessions(String userId, AdminEvent adminEvent, StreamConfig stream) {

        Event event = new Event();
        event.setType(EventType.LOGOUT);
        event.setUserId(userId);
        event.setSessionId("ALL"); // all sessions
        event.setDetails(new HashMap<>());
        event.getDetails().put("admin", "true");
        event.getDetails().put(Details.REASON, "logout_all_user_sessions");

        return toSecurityEventToken(event, stream);
    }
}
