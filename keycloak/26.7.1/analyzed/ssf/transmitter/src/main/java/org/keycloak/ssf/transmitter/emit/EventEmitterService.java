package org.keycloak.ssf.transmitter.emit;

import java.util.Map;
import java.util.function.Supplier;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.ssf.SsfException;
import org.keycloak.ssf.event.SsfEvent;
import org.keycloak.ssf.event.SsfEventRegistry;
import org.keycloak.ssf.event.SsfEventValidationException;
import org.keycloak.ssf.event.token.SsfSecurityEventToken;
import org.keycloak.ssf.metadata.DefaultSubjects;
import org.keycloak.ssf.subject.ComplexSubjectId;
import org.keycloak.ssf.subject.OpaqueSubjectId;
import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.subject.SubjectUserLookup;
import org.keycloak.ssf.transmitter.delivery.SecurityEventTokenDispatcher;
import org.keycloak.ssf.transmitter.event.SecurityEventTokenMapper;
import org.keycloak.ssf.transmitter.stream.StreamConfig;
import org.keycloak.ssf.transmitter.stream.storage.client.ClientStreamStore;
import org.keycloak.ssf.transmitter.subject.SsfSubjectInclusionResolver;
import org.keycloak.ssf.transmitter.support.SsfUtil;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * 将可信 IAM 管理客户端注入的合成 SSF 事件推入常规发送方派发流水线。
 *
 * <p>供管理端 {@code /admin/realms/{realm}/ssf/clients/{id}/events/emit} 端点使用，
 * 使外部系统（例如实际拥有密码变更的 LDAP/IdM）可将凭证变更等事件转发给 SSF 接收方，
 * 如同 Keycloak 自身观察到一样。已签名的 SET 按接收方流身份（iss/aud/format）构建，
 * 并通过与原生事件相同的发件箱 + push 流水线派发。</p>
 *
 * <p>此处应用的过滤门控与派发器一致：仅派发接收方已订阅的事件（通过 {@code events_requested}）
 * 及接收方感兴趣的主体（通过 {@code ssf.notify.<clientId>} 与 {@code default_subjects}）。
 * 丢弃原因会回传给调用方，便于发射方集成调试其接线。</p>
 */
public class EventEmitterService {

    private static final Logger log = Logger.getLogger(EventEmitterService.class);

    protected final KeycloakSession session;

    protected final ClientStreamStore streamStore;

    protected final SecurityEventTokenMapper eventTokenMapper;

    protected final SecurityEventTokenDispatcher eventTokenDispatcher;

    protected final SsfSubjectInclusionResolver subjectInclusionResolver;

    public EventEmitterService(KeycloakSession session,
                               ClientStreamStore streamStore,
                               SecurityEventTokenMapper eventTokenMapper,
                               SecurityEventTokenDispatcher eventTokenDispatcher,
                               SsfSubjectInclusionResolver subjectInclusionResolver) {
        this.session = session;
        this.streamStore = streamStore;
        this.eventTokenMapper = eventTokenMapper;
        this.eventTokenDispatcher = eventTokenDispatcher;
        this.subjectInclusionResolver = subjectInclusionResolver;
    }

    /**
     * 解析请求、执行接收方派发过滤器，并通过现有发件箱路径推送单个已签名 SET。
     *
     * <p>{@code subjectId} 为 RFC 9493 主体标识符——{@code EmailSubjectId}、
     * {@code IssuerSubjectId}、{@code OpaqueSubjectId} 或 {@code ComplexSubjectId} 之一。
     * 对复合主体，服务会深入 {@link ComplexSubjectId#getUser()} 执行订阅检查，
     * 但将整个 {@code sub_id} 原样传入 SET，使接收方看到发射方提供的精确形状。</p>
     *
     * <p>流管理事件类型（verification、stream-updated）与原生派发器一样绕过主体订阅过滤器——
     * 这些事件针对流本身，而非用户。</p>
     *
     * @return 派发结果，永不为 {@code null}。
     */
    public EmitEventResult emit(ClientModel receiverClient,
                                String eventTypeAliasOrUri,
                                SubjectId subjectId,
                                Map<String, Object> eventAttributes) {

        if (receiverClient == null) {
            return EmitEventResult.dropped(EmitEventStatus.STREAM_NOT_FOUND);
        }
        // Programmatic callers can pass any ClientModel they hold —
        // guard early so an extension that picks the wrong client gets
        // a clear error instead of a misleading STREAM_NOT_FOUND once
        // the stream lookup later returns null. The REST emit path
        // routes through the same gate, so a request targeting a
        // non-SSF client surfaces this as a 500 with the message —
        // intentional: the caller's configuration is wrong.
        if (!SsfUtil.isReceiverClient(receiverClient)) {
            throw new SsfException("Client '" + receiverClient.getClientId()
                    + "' is not an SSF Receiver");
        }
        // A disabled receiver keeps its stream config but is off the air —
        // mirror the dispatch-path gate (SsfUtil#isReceiverEnabled)
        // so synthetic emit doesn't deliver to a client the operator has
        // switched off. Re-enabling the client resumes delivery
        if (!receiverClient.isEnabled()) {
            return EmitEventResult.dropped(EmitEventStatus.RECEIVER_DISABLED);
        }
        if (eventTypeAliasOrUri == null || eventTypeAliasOrUri.isBlank()) {
            return EmitEventResult.dropped(EmitEventStatus.INVALID_REQUEST);
        }
        if (subjectId == null) {
            return EmitEventResult.dropped(EmitEventStatus.INVALID_REQUEST);
        }

        // 1. Resolve event type alias / URI to canonical URI.
        SsfEventRegistry registry = registryOrNull();
        if (registry == null) {
            return EmitEventResult.dropped(EmitEventStatus.UNKNOWN_EVENT_TYPE);
        }
        String eventTypeUri = registry.resolveEventTypeForAlias(eventTypeAliasOrUri);
        if (eventTypeUri == null && registry.getEventClassByType(eventTypeAliasOrUri).isPresent()) {
            eventTypeUri = eventTypeAliasOrUri;
        }
        if (eventTypeUri == null) {
            return EmitEventResult.dropped(EmitEventStatus.UNKNOWN_EVENT_TYPE);
        }

        // 2. Stream-management events (verification, stream-updated)
        //    are protocol-internal lifecycle signals the transmitter
        //    itself owns. Letting an external emitter forge them would
        //    let it spoof transmitter behaviour towards the receiver,
        //    so reject up front.
        if (isStreamEvent(eventTypeUri)) {
            return EmitEventResult.dropped(EmitEventStatus.EVENT_TYPE_NOT_EMITTABLE);
        }

        // 3. Receiver must have a registered SSF stream.
        StreamConfig stream = streamStore.getStreamForClient(receiverClient);
        if (stream == null) {
            return EmitEventResult.dropped(EmitEventStatus.STREAM_NOT_FOUND);
        }
        // ... with a delivery configuration. Without one the dispatcher
        // has nowhere to send the SET and would skip delivery before the
        // outbox enqueue — the emitter would see a "dispatched" result
        // (and a jti) for an event that never existed anywhere. Fail
        // early with an explicit status instead.
        if (stream.getDelivery() == null) {
            return EmitEventResult.dropped(EmitEventStatus.NO_DELIVERY_CONFIG,
                    "Stream has no delivery method configured — configure push or poll delivery for the stream first");
        }

        // 4. Event type must be in the receiver's events_requested
        //    set. Receivers that pass null opt into everything.
        if (stream.getEventsRequested() != null && !stream.getEventsRequested().contains(eventTypeUri)) {
            return EmitEventResult.dropped(EmitEventStatus.DROPPED_FILTERED);
        }

        // 5. Subject resolution + subscription filter. ComplexSubjectId
        //    can carry a user, an org (via the tenant slot), or both —
        //    we accept any resolvable combination and apply the
        //    receiver's notify-attribute subscription on whichever
        //    facets resolve.
        EmitSubjectResolution resolved = resolveSubject(subjectId);
        if (resolved.user() == null && resolved.organization() == null) {
            return EmitEventResult.dropped(EmitEventStatus.SUBJECT_NOT_FOUND);
        }
        // Drop early so the emitter sees a clean status without
        // paying the SET signing cost for a filtered subject.
        if (!isSubjectDispatchable(resolved, stream, receiverClient)) {
            return EmitEventResult.dropped(EmitEventStatus.DROPPED_UNSUBSCRIBED);
        }

        // 5. Deserialize the event payload into the registry's typed
        //    event class so the dispatcher's per-event narrowing (e.g.
        //    SSE_CAEP conversion for Apple Business Manager) applies
        //    just like for native events. On failure, surface the
        //    Jackson error message via the result so the admin caller
        //    can see exactly which field shape is wrong instead of a
        //    generic invalid_request.
        Object eventPayload;
        try {
            eventPayload = deserializeEventOrThrow(registry, eventTypeUri, eventAttributes);
        } catch (EventPayloadDeserializationException e) {
            return EmitEventResult.dropped(EmitEventStatus.INVALID_REQUEST, e.getMessage());
        }
        if (eventPayload == null) {
            return EmitEventResult.dropped(EmitEventStatus.INVALID_REQUEST,
                    "No registered event class for eventType=" + eventTypeUri);
        }

        // Per-event spec validation. Default SsfEvent.validate() is a
        // no-op; CAEP / RISC / custom event subclasses override it to
        // enforce spec-required fields (e.g. change_type on
        // CaepCredentialChange). Run after Jackson conversion so the
        // typed field values are populated by the time we look at them.
        // The exception's getMessage() composition matches the wire
        // status enum (invalid_event_data) so callers get one stable
        // identifier that names both the failure category and the
        // offending alias.field — they can localise from there.
        if (eventPayload instanceof SsfEvent typedEvent) {
            try {
                typedEvent.validate();
            } catch (SsfEventValidationException e) {
                return EmitEventResult.dropped(EmitEventStatus.INVALID_EVENT_DATA, e.getMessage());
            }
        }

        // 6. Build the SET (sub_id verbatim from the emitter) and hand
        //    off to the existing dispatcher.
        SsfSecurityEventToken token = eventTokenMapper.generateSyntheticEvent(stream, eventTypeUri, eventPayload, subjectId);
        if (token == null) {
            return EmitEventResult.dropped(EmitEventStatus.INVALID_REQUEST);
        }

        // Filters above mirror what dispatcher.dispatchEvent would run,
        // so deliverEvent is the right entry point — same async outbox
        // path native events take, no double filtering.
        eventTokenDispatcher.deliverEvent(token, stream);

        log.debugf("SSF synthetic event dispatched. receiverClientId=%s streamId=%s eventType=%s jti=%s",
                receiverClient.getClientId(), stream.getStreamId(), eventTypeUri, token.getJti());

        return EmitEventResult.dispatched(token.getJti());
    }

    /**
     * 解析发射方 {@code sub_id} 所引用的实体。
     * 对 {@link ComplexSubjectId}，会深入 {@link ComplexSubjectId#getUser()} 与
     * {@link ComplexSubjectId#getTenant()}——用户方面驱动按用户的 notify 订阅，
     * 租户方面驱动组织级 notify 订阅。对非复合 {@link SubjectId} 仅解析用户。
     *
     * <p>组织解析优先将 {@link OpaqueSubjectId} 的 {@code id} 视为组织别名，
     * 再回退至组织 UUID。租户槽中其他 {@link SubjectId} 格式当前不支持，解析为无组织。</p>
     */
    protected EmitSubjectResolution resolveSubject(SubjectId subjectId) {
        RealmModel realm = session.getContext().getRealm();

        if (subjectId instanceof ComplexSubjectId complex) {
            UserModel user = complex.getUser() != null
                    ? SubjectUserLookup.lookupUser(session, realm, complex.getUser())
                    : null;
            OrganizationModel org = resolveOrganization(complex.getTenant());
            return new EmitSubjectResolution(user, org);
        }

        UserModel user = SubjectUserLookup.lookupUser(session, realm, subjectId);
        return new EmitSubjectResolution(user, null);
    }

    protected OrganizationModel resolveOrganization(SubjectId tenantFacet) {
        if (tenantFacet == null || !Organizations.isEnabled(session)) {
            return null;
        }
        if (!(tenantFacet instanceof OpaqueSubjectId opaque) || opaque.getId() == null) {
            return null;
        }
        OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
        // Prefer alias (matches the admin shorthand 'org-alias' convention),
        // then fall back to UUID for emitters that prefer stable identifiers.
        OrganizationModel org = orgProvider.getByAlias(opaque.getId());
        if (org == null) {
            org = orgProvider.getById(opaque.getId());
        }
        return org;
    }

    protected boolean isStreamEvent(String eventTypeUri) {
        // Single source of truth for the protocol-internal lifecycle
        // event list — shared with SsfEventRegistry#getReceiverRequestableEventTypes
        // so the admin UI's "available supported events" list and
        // this gate can never drift apart.
        return SsfEventRegistry.STREAM_LIFECYCLE_EVENT_TYPES.contains(eventTypeUri);
    }

    /**
     * 与原生派发器的 {@code SubjectSubscriptionFilter} 镜像的订阅门控，
     * 但作用于预解析的用户/组织对，使发射方也可发出仅组织事件。
     *
     * <ul>
     *     <li>{@code default_subjects=ALL}：除非用户或组织被显式排除，否则投递。</li>
     *     <li>{@code default_subjects=NONE}：仅当用户/组织方面至少一方被显式 notify 时投递。</li>
     * </ul>
     */
    protected boolean isSubjectDispatchable(EmitSubjectResolution resolved,
                                            StreamConfig stream,
                                            ClientModel receiverClient) {
        String receiverClientId = receiverClient.getClientId();
        DefaultSubjects defaultSubjects = stream.getDefaultSubjects();

        if (defaultSubjects == DefaultSubjects.ALL) {
            boolean userExcluded = resolved.user() != null
                    && subjectInclusionResolver.isUserExcluded(session, resolved.user(), receiverClientId);
            boolean orgExcluded = resolved.organization() != null
                    && subjectInclusionResolver.isOrganizationExcluded(session, resolved.organization(), receiverClientId);
            return !userExcluded && !orgExcluded;
        }

        boolean userNotified = resolved.user() != null
                && subjectInclusionResolver.isUserNotified(session, resolved.user(), receiverClientId);
        boolean orgNotified = resolved.organization() != null
                && subjectInclusionResolver.isOrganizationNotified(session, resolved.organization(), receiverClientId);
        return userNotified || orgNotified;
    }

    /**
     * 将原始事件属性反序列化为给定 URI 已注册的类型化事件类，
     * 将 Jackson 转换错误包装为 {@link EventPayloadDeserializationException}，
     * 以便调用方将消息回传给运维人员。
     */
    protected Object deserializeEventOrThrow(SsfEventRegistry registry,
                                             String eventTypeUri,
                                             Map<String, Object> eventAttributes) {
        Class<? extends SsfEvent> eventClass = registry.getEventClassByType(eventTypeUri).orElse(null);
        if (eventClass == null) {
            return null;
        }
        if (eventAttributes == null) {
            // Empty event body — mint a default instance via the
            // registry's factory (contributed as a SomeEvent::new method
            // reference) so some CAEP events that have no required
            // fields can still be emitted with the event type URI alone
            // carrying the signal. No runtime reflection.
            return registry.getEventFactoryByType(eventTypeUri)
                    .map(Supplier::get)
                    .orElse(null);
        }
        try {
            return JsonSerialization.mapper.convertValue(eventAttributes, eventClass);
        } catch (IllegalArgumentException e) {
            // Jackson wraps mismatch errors in IllegalArgumentException
            // when called via convertValue — its cause is the typed
            // JsonMappingException with the field-pointer details.
            String detail = e.getCause() != null && e.getCause().getMessage() != null
                    ? e.getCause().getMessage()
                    : e.getMessage();
            throw new EventPayloadDeserializationException(
                    "Event payload does not match the schema for " + eventTypeUri + ": " + detail, e);
        }
    }

    /**
     * 内部异常类型，用于将 Jackson 转换失败传回管理端点，而不向线格式泄露原始堆栈。
     */
    protected static class EventPayloadDeserializationException extends RuntimeException {
        public EventPayloadDeserializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    protected SsfEventRegistry registryOrNull() {
        // Reach the registry via the same Ssf.events() entry point the
        // transmitter uses internally — keeps the emitter independent of
        // any future provider-level registry getter.
        try {
            return SsfEventRegistry.of(session);
        } catch (Exception e) {
            log.warn("SSF event registry not available", e);
            return null;
        }
    }
}
