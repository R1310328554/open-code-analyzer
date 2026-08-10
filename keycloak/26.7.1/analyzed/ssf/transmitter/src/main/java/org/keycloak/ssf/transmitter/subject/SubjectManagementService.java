package org.keycloak.ssf.transmitter.subject;

import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.ssf.SsfException;
import org.keycloak.ssf.metadata.DefaultSubjects;
import org.keycloak.ssf.subject.ComplexSubjectId;
import org.keycloak.ssf.subject.OpaqueSubjectId;
import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.subject.SubjectNotFoundException;
import org.keycloak.ssf.subject.SubjectResolution;
import org.keycloak.ssf.subject.SubjectResolver;
import org.keycloak.ssf.transmitter.SsfTransmitterProvider;
import org.keycloak.ssf.transmitter.resources.AddSubjectRequest;
import org.keycloak.ssf.transmitter.resources.RemoveSubjectRequest;
import org.keycloak.ssf.transmitter.stream.StreamConfig;
import org.keycloak.ssf.transmitter.stream.storage.client.ClientStreamStore;
import org.keycloak.storage.ReadOnlyException;

import org.jboss.logging.Logger;

/**
 * 编排 add/remove 主题生命周期：通过 {@link SubjectResolver} 将主题解析为
 * Keycloak 实体，校验流归属，并通过 {@link SsfNotifyAttributes} 切换已解析实体上的
 * {@code ssf.notify.<clientId>} 属性。
 */
public class SubjectManagementService {

    private static final Logger log = Logger.getLogger(SubjectManagementService.class);

    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;

    /** @param session 当前请求会话 */
    public SubjectManagementService(KeycloakSession session) {
        this.session = session;
    }

    /** 接收方调用：向流添加订阅主题。 */
    public SubjectManagementResult addSubject(String callerClientId,
                                              AddSubjectRequest request) {
        SubjectManagementResult ownershipResult = checkOwnership(callerClientId, request.getStreamId());
        if (ownershipResult != null) {
            return ownershipResult;
        }

        String clientClientId = resolveClientClientId(callerClientId);
        SubjectResolution resolution = resolveSubject(request.getSubject());
        return registerSubjectForNotification(clientClientId, resolution);
    }

    /** 将已解析主体注册为接收方通知对象。 */
    protected SubjectManagementResult registerSubjectForNotification(String callerClientId, SubjectResolution resolution) {
        if (resolution instanceof SubjectResolution.User u) {
            // 重新添加优先于先前由接收方触发的移除——清除 SSF §9.3 墓碑，
            // 使分发器使用新的包含标记而非过期的宽限窗口检查。
            try {
                SsfNotifyAttributes.clearRemovedAtForUser(u.user(), callerClientId);
                SsfNotifyAttributes.setForUser(u.user(), callerClientId);
            } catch (ReadOnlyException e) {
                return readOnlySubject("add", callerClientId, u.user());
            }
            log.debugf("SSF subject added. clientId=%s userId=%s", callerClientId, u.user().getId());
            return SubjectManagementResult.OK;
        }
        if (resolution instanceof SubjectResolution.Organization o) {
            SsfNotifyAttributes.clearRemovedAtForOrganization(o.organization(), callerClientId);
            SsfNotifyAttributes.setForOrganization(o.organization(), callerClientId);
            log.debugf("SSF subject added. clientId=%s orgId=%s", callerClientId, o.organization().getId());
            return SubjectManagementResult.OK;
        }
        if (resolution instanceof SubjectResolution.NotFound) {
            return SubjectManagementResult.SUBJECT_NOT_FOUND;
        }
        return SubjectManagementResult.FORMAT_UNSUPPORTED;
    }

    /** 接收方调用：从流移除订阅主题。 */
    public SubjectManagementResult removeSubject(String callerClientId,
                                                 RemoveSubjectRequest request) {
        SubjectManagementResult ownershipResult = checkOwnership(callerClientId, request.getStreamId());
        if (ownershipResult != null) {
            return ownershipResult;
        }

        String clientClientId = resolveClientClientId(callerClientId);
        SubjectResolution resolution = resolveSubject(request.getSubject());

        // 接收方驱动路径：写入 SSF §9.3 墓碑，使分发器在配置的宽限期内继续投递。
        // 管理员驱动的移除走 removeSubjectByAdmin，跳过墓碑写入。
        return unregisterSubjectForNotification(clientClientId, resolution, true);
    }

    /** 将主体显式排除出接收方通知列表。 */
    protected SubjectManagementResult excludeSubjectFromNotification(String callerClientId, SubjectResolution resolution) {
        if (resolution instanceof SubjectResolution.User u) {
            // 显式排除为管理员可信操作——无宽限窗口。清除先前接收方墓碑，
            // 使排除标记立即生效。
            try {
                SsfNotifyAttributes.clearRemovedAtForUser(u.user(), callerClientId);
                SsfNotifyAttributes.excludeForUser(u.user(), callerClientId);
            } catch (ReadOnlyException e) {
                return readOnlySubject("exclude", callerClientId, u.user());
            }
            log.debugf("SSF subject excluded. clientId=%s userId=%s", callerClientId, u.user().getId());
            return SubjectManagementResult.OK;
        }
        if (resolution instanceof SubjectResolution.Organization o) {
            SsfNotifyAttributes.clearRemovedAtForOrganization(o.organization(), callerClientId);
            SsfNotifyAttributes.excludeForOrganization(o.organization(), callerClientId);
            log.debugf("SSF subject excluded. clientId=%s orgId=%s", callerClientId, o.organization().getId());
            return SubjectManagementResult.OK;
        }
        if (resolution instanceof SubjectResolution.NotFound) {
            return SubjectManagementResult.SUBJECT_NOT_FOUND;
        }
        return SubjectManagementResult.FORMAT_UNSUPPORTED;
    }

    /**
     * 清除已解析主体的包含/排除标记；当 {@code applyTombstone} 为 true 时，
     * 写入 SSF §9.3 宽限墓碑，使分发器在配置窗口内继续投递。
     * 管理员驱动的移除有意跳过墓碑——操作员操作可信且立即生效。
     * 接收方无法选择退出：通过 SPI 启用宽限即表示接受合法 churn 移除也获得宽限尾段。
     */
    protected SubjectManagementResult unregisterSubjectForNotification(String callerClientId, SubjectResolution resolution, boolean applyTombstone) {
        if (resolution instanceof SubjectResolution.User u) {
            try {
                if (applyTombstone) {
                    SsfNotifyAttributes.stampRemovedAtForUser(u.user(), callerClientId);
                }
                SsfNotifyAttributes.clearForUser(u.user(), callerClientId);
            } catch (ReadOnlyException e) {
                return readOnlySubject("remove", callerClientId, u.user());
            }
            log.debugf("SSF subject removed. clientId=%s userId=%s tombstone=%s",
                    callerClientId, u.user().getId(), applyTombstone);
            return SubjectManagementResult.OK;
        }
        if (resolution instanceof SubjectResolution.Organization o) {
            if (applyTombstone) {
                SsfNotifyAttributes.stampRemovedAtForOrganization(o.organization(), callerClientId);
            }
            SsfNotifyAttributes.clearForOrganization(o.organization(), callerClientId);
            log.debugf("SSF subject removed. clientId=%s orgId=%s tombstone=%s",
                    callerClientId, o.organization().getId(), applyTombstone);
            return SubjectManagementResult.OK;
        }
        if (resolution instanceof SubjectResolution.NotFound) {
            return SubjectManagementResult.SUBJECT_NOT_FOUND;
        }
        return SubjectManagementResult.FORMAT_UNSUPPORTED;
    }

    /**
     * 记录只读用户存储并将结果映射为 {@link SubjectManagementResult#SUBJECT_READ_ONLY}。
     * 只读 Provider 支撑的主体无法持久化 {@code ssf.notify.<clientId>} 订阅状态
     *（例如 LDAP {@code READ_ONLY} 或未启用导入），调用方以此作为明确限制返回，
     * 避免 {@link ReadOnlyException} 逃逸为 500。组织始终由 Keycloak 管理，故仅用户分支需要此守卫。
     */
    protected SubjectManagementResult readOnlySubject(String operation, String callerClientId, UserModel user) {
        log.debugf("SSF subject %s: user %s is read-only; ssf.notify state cannot be persisted. "
                + "This requires writable user storage. clientId=%s",
                operation, user.getId(), callerClientId);
        return SubjectManagementResult.SUBJECT_READ_ONLY;
    }

    /**
     * 将 {@link SubjectId} 解析为 Keycloak 实体。protected 以便子类接入自定义解析逻辑
     *（例如额外主题格式或替代查找策略）。
     */
    protected SubjectResolution resolveSubject(SubjectId subjectId) {
        RealmModel realm = session.getContext().getRealm();
        return SubjectResolver.resolve(session, realm, subjectId);
    }

    /**
     * 管理员驱动添加：按管理员简写类型（user-id、user-email、org-alias）解析并设置 notify 属性。
     * 跳过归属检查——管理员操作可信。
     *
     * @return 含已解析实体类型与 id 的结果，或失败指示
     */
    public AdminSubjectResult addSubjectByAdmin(String clientId, String type, String value) {
        String clientClientId = resolveClientClientId(clientId);
        SubjectResolution resolution = resolveByAdminType(type, value);
        SubjectManagementResult result = registerSubjectForNotification(clientClientId, resolution);
        return toAdminResult(result, resolution);
    }

    /**
     * 管理员驱动忽略：按管理员简写类型解析并将 notify 属性设为 {@code false}（显式排除）。
     */
    public AdminSubjectResult ignoreSubjectByAdmin(String clientId, String type, String value) {
        String clientClientId = resolveClientClientId(clientId);
        SubjectResolution resolution = resolveByAdminType(type, value);
        SubjectManagementResult result = excludeSubjectFromNotification(clientClientId, resolution);
        return toAdminResult(result, resolution);
    }

    /**
     * 管理员驱动移除：按管理员简写类型解析并清除 notify 属性。
     */
    public AdminSubjectResult removeSubjectByAdmin(String clientId, String type, String value) {
        String clientClientId = resolveClientClientId(clientId);
        SubjectResolution resolution = resolveByAdminType(type, value);
        // 管理员移除有意跳过 SSF §9.3 宽限墓碑——操作员操作可信且立即生效。
        // 被攻破接收方防护仅适用于接收方驱动的 remove 路径。
        SubjectManagementResult result = unregisterSubjectForNotification(clientClientId, resolution, false);
        return toAdminResult(result, resolution);
    }

    /**
     * 只读检查主体对某接收方客户端的有效通知状态——若此刻分发该主体事件，
     * 分发器主题门控会作出的决定。驱动管理 UI「检查」按钮，使展示状态反映真实门控逻辑。
     *
     * <p>与分发器决策顺序一致：按用户显式设置始终优先于组织继承及
     * {@code default_subjects} 回退。故 {@code ssf.notify.<clientId>=false} 的用户
     * 读作 {@code "ignored"}，即使其某组织为 notify=true——管理员对特定用户点击
     * 「忽略」时期望该决定不受成员资格默认值影响。
     *
     * <p>解析顺序，返回首个匹配：
     * <ol>
     *   <li>用户上 {@code ssf.notify.<clientId>=true} → {@code "notified"}</li>
     *   <li>用户上 {@code ssf.notify.<clientId>=false} → {@code "ignored"}</li>
     *   <li>仅用户：任一所属组织 {@code ssf.notify.<clientId>=true} →
     *       {@code "notified_via_org"}</li>
     *   <li>{@code default_subjects=ALL} 分支：
     *     <ol type="a">
     *       <li>仅用户：任一组织 {@code ssf.notify.<clientId>=false} →
     *           {@code "ignored_via_org"}</li>
     *       <li>否则 → {@code "implicitly_included"}</li>
     *     </ol>
     *   </li>
     *   <li>{@code default_subjects=NONE} 且无包含信号 → {@code "not_notified"}</li>
     * </ol>
     *
     * <p>SSF §9.3 移除宽限墓碑及可插拔 {@link SsfSubjectInclusionResolver} 扩展
     * 尚未在此反映；后续跟进文档化。
     */
    public AdminSubjectStatus inspectSubjectByAdmin(ClientModel client, String type, String value) {
        SubjectResolution resolution = resolveByAdminType(type, value);
        if (resolution == SubjectResolution.NOT_FOUND) {
            return new AdminSubjectStatus("not_found", null, null, null);
        }
        if (resolution == SubjectResolution.UNSUPPORTED_FORMAT) {
            return new AdminSubjectStatus("unsupported_format", null, null, null);
        }

        String receiverClientId = client.getClientId();
        DefaultSubjects defaultSubjects = DefaultSubjects.parseOrDefault(
                client.getAttribute(ClientStreamStore.SSF_DEFAULT_SUBJECTS_KEY), null);

        if (resolution instanceof SubjectResolution.User u) {
            UserModel user = u.user();

            // 1. 按用户显式设置始终优先。
            if (SsfNotifyAttributes.isUserNotified(user, receiverClientId)) {
                return new AdminSubjectStatus("notified", "user", user.getId(), null);
            }
            if (SsfNotifyAttributes.isUserExcluded(user, receiverClientId)) {
                return new AdminSubjectStatus("ignored", "user", user.getId(), null);
            }

            // 2. 组织级包含（任一组织 notify=true）。
            OrganizationModel notifyingOrg = firstOrgNotifying(user, receiverClientId);
            if (notifyingOrg != null) {
                return new AdminSubjectStatus("notified_via_org", "user", user.getId(), notifyingOrg.getAlias());
            }

            // 3. ALL 模式：检查组织级排除，否则隐式包含。
            if (defaultSubjects == DefaultSubjects.ALL) {
                OrganizationModel excludingOrg = firstOrgExcluding(user, receiverClientId);
                if (excludingOrg != null) {
                    return new AdminSubjectStatus("ignored_via_org", "user", user.getId(), excludingOrg.getAlias());
                }
                return new AdminSubjectStatus("implicitly_included", "user", user.getId(), null);
            }

            // 4. NONE 模式且无包含信号。
            return new AdminSubjectStatus("not_notified", "user", user.getId(), null);
        }
        if (resolution instanceof SubjectResolution.Organization o) {
            OrganizationModel org = o.organization();
            if (SsfNotifyAttributes.isOrganizationNotified(org, receiverClientId)) {
                return new AdminSubjectStatus("notified", "organization", org.getId(), null);
            }
            if (SsfNotifyAttributes.isOrganizationExcluded(org, receiverClientId)) {
                return new AdminSubjectStatus("ignored", "organization", org.getId(), null);
            }
            if (defaultSubjects == DefaultSubjects.ALL) {
                return new AdminSubjectStatus("implicitly_included", "organization", org.getId(), null);
            }
            return new AdminSubjectStatus("not_notified", "organization", org.getId(), null);
        }
        return new AdminSubjectStatus("not_notified", null, null, null);
    }

    /**
     * 返回用户所属、且对该接收方已订阅的第一个组织，无则 {@code null}。
     * 「第一个」组织为 {@link OrganizationProvider} 流返回的首个——通常对给定存储确定但规范未保证顺序。
     * 别名仅作管理 UI 信息上下文；分发器门控不关心是哪个组织触发了决定。
     */
    protected OrganizationModel firstOrgNotifying(UserModel user, String receiverClientId) {
        if (!Organizations.isEnabled(session)) {
            return null;
        }
        return session.getProvider(OrganizationProvider.class).getByMember(user)
                .filter(org -> SsfNotifyAttributes.isOrganizationNotified(org, receiverClientId))
                .findFirst()
                .orElse(null);
    }

    /** 返回用户所属、且对该接收方已排除的第一个组织。 */
    protected OrganizationModel firstOrgExcluding(UserModel user, String receiverClientId) {
        if (!Organizations.isEnabled(session)) {
            return null;
        }
        return session.getProvider(OrganizationProvider.class).getByMember(user)
                .filter(org -> SsfNotifyAttributes.isOrganizationExcluded(org, receiverClientId))
                .findFirst()
                .orElse(null);
    }

    /**
     * {@link #inspectSubjectByAdmin} 返回的状态。
     *
     * @param status 规范状态名（如 {@code "notified"}、{@code "ignored_via_org"}）
     * @param entityType {@code "user"} 或 {@code "organization"}，主体未解析时为 {@code null}
     * @param entityId 已解析实体 id，未解析时为 {@code null}
     * @param sourceOrgAlias 对 {@code *_via_org} 状态驱动决定的组织别名，否则 {@code null}；
     *                       供管理 UI 展示是哪个成员资格触发了门控
     */
    public record AdminSubjectStatus(String status, String entityType, String entityId, String sourceOrgAlias) {}

    /** 将 {@link SubjectManagementResult} 与解析结果转换为管理员 API 响应。 */
    protected AdminSubjectResult toAdminResult(SubjectManagementResult result, SubjectResolution resolution) {
        if (result == SubjectManagementResult.OK) {
            if (resolution instanceof SubjectResolution.User u) {
                return new AdminSubjectResult(result, "user", u.user().getId());
            }
            if (resolution instanceof SubjectResolution.Organization o) {
                return new AdminSubjectResult(result, "organization", o.organization().getId());
            }
        }
        return new AdminSubjectResult(result, null, null);
    }

    /** 按管理员简写类型（user-id、user-email 等）解析主体。 */
    public SubjectResolution resolveByAdminType(String type, String value) {
        RealmModel realm = session.getContext().getRealm();

        if ("user-id".equals(type)) {
            UserModel user = session.users().getUserById(realm, value);
            return user != null ? new SubjectResolution.User(user) : SubjectResolution.NOT_FOUND;
        }
        if ("user-email".equals(type)) {
            UserModel user = session.users().getUserByEmail(realm, value);
            return user != null ? new SubjectResolution.User(user) : SubjectResolution.NOT_FOUND;
        }
        if ("user-username".equals(type)) {
            UserModel user = session.users().getUserByUsername(realm, value);
            return user != null ? new SubjectResolution.User(user) : SubjectResolution.NOT_FOUND;
        }
        if ("org-alias".equals(type)) {
            if (!Organizations.isEnabled(session)) {
                return SubjectResolution.UNSUPPORTED_FORMAT;
            }
            var org = session.getProvider(OrganizationProvider.class).getByAlias(value);
            return org != null ? new SubjectResolution.Organization(org) : SubjectResolution.NOT_FOUND;
        }

        return SubjectResolution.UNSUPPORTED_FORMAT;
    }

    /**
     * 将管理员简写 {@code (type, value)} 解析为可直接交给合成事件发射器的
     * {@link SubjectId}。用户主体（{@code user-id} / {@code user-email} /
     * {@code user-username}）通过
     * {@link org.keycloak.ssf.transmitter.event.SecurityEventTokenMapper#buildSubjectForReceiver
     * buildSubjectForReceiver} 构建 sub_id，以遵循接收方配置的
     * {@code ssf.userSubjectFormat}。{@code org-alias} 返回仅含 {@code tenant} 面的
     * {@link ComplexSubjectId}（发射器按组织范围事件路由）。
     *
     * <p>每种失败抛出带操作员友好消息的 {@link SsfException}——无法解析的主体、未知类型，
     * 或映射器 fail-loud 场景（缺少邮箱、{@code +tenant} 格式无组织等）。管理端点捕获并以 400 返回。
     */
    public SubjectId resolveSubjectForEmit(StreamConfig stream,
                                           String subjectType,
                                           String subjectValue) {
        SubjectResolution resolution = resolveByAdminType(subjectType, subjectValue);
        if (resolution instanceof SubjectResolution.User userRes) {
            SsfTransmitterProvider transmitter = session.getProvider(SsfTransmitterProvider.class);
            return transmitter.securityEventTokenMapper()
                    .buildSubjectForReceiver(stream, userRes.user().getId());
        }
        if (resolution instanceof SubjectResolution.Organization orgRes) {
            ComplexSubjectId complex = new ComplexSubjectId();
            OpaqueSubjectId tenant = new OpaqueSubjectId();
            tenant.setId(orgRes.organization().getAlias());
            complex.setTenant(tenant);
            return complex;
        }
        if (resolution instanceof SubjectResolution.NotFound) {
            throw new SubjectNotFoundException(subjectType, subjectValue);
        }
        // UNSUPPORTED_FORMAT（未知类型或组织功能未启用）。
        throw new SsfException("Unsupported subjectType: " + subjectType);
    }

    /**
     * 将内部客户端 UUID 解析为用作 {@code ssf.notify} 属性键的可读 OAuth {@code client_id}。
     * 无法解析时（例如请求中途客户端被删）回退为输入值。
     */
    protected String resolveClientClientId(String clientUuid) {
        RealmModel realm = session.getContext().getRealm();
        var client = realm.getClientById(clientUuid);
        return client != null ? client.getClientId() : clientUuid;
    }

    /** 校验调用方是否拥有指定流；失败返回对应 {@link SubjectManagementResult}，成功返回 {@code null}。 */
    protected SubjectManagementResult checkOwnership(String callerClientId, String streamId) {
        ClientStreamStore streamStore = new ClientStreamStore(session);
        var client = session.getContext().getRealm().getClientById(callerClientId);
        if (client == null) {
            return SubjectManagementResult.STREAM_NOT_FOUND;
        }
        StreamConfig stream = streamStore.getStreamForClient(client);
        if (stream == null || !streamId.equals(stream.getStreamId())) {
            return SubjectManagementResult.STREAM_NOT_FOUND;
        }
        return null;
    }
}
