package org.keycloak.ssf.transmitter.admin;

import java.util.Map;

import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.subject.SubjectIdJsonDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 合成 SSF 事件 emit 管理端点的请求体。
 *
 * <p>允许 IAM 管理客户端代表 Keycloak 无法原生观测的上游系统（例如 LDAP 中的密码变更）
 * 推送单个 SSF 事件。发送方将载荷封装为已签名的 SET 并经正常投递管道派发——
 * 接收方的主体订阅与 {@code events_delivered} 过滤仍生效。</p>
 *
 * <p>{@code sub_id} 遵循 RFC 9493（安全事件令牌的主体标识符）。按上游身份模型选择格式——
 * 简单用户主体可用 {@code email}、{@code iss_sub}、{@code opaque}；
 * 携带多个标识的 CAEP 事件可用 {@code complex} 嵌套 {@code user}、{@code session}、{@code tenant}。
 * 发送方原样传递 {@code sub_id}，接收方所见格式与 emit 方一致。</p>
 *
 * <p>示例——以 email 标识用户的凭据变更：
 * <pre>
 * {
 *   "eventType": "CaepCredentialChange",
 *   "sub_id": { "format": "email", "email": "user@example.com" },
 *   "event":  { "credential_type": "password", "change_type": "update" }
 * }
 * </pre></p>
 *
 * <p>示例——含 user + session 的复合主体会话撤销：
 * <pre>
 * {
 *   "eventType": "CaepSessionRevoked",
 *   "sub_id": {
 *     "format":  "complex",
 *     "user":    { "format": "iss_sub", "iss": "https://kc.example.com/realms/foo", "sub": "user-uuid" },
 *     "session": { "format": "opaque",  "id":  "session-id" }
 *   },
 *   "event":  { "event_timestamp": 1713360000, "reason_admin": { "en": "..." } }
 * }
 * </pre></p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SsfEmitEventRequest {

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("sub_id")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    private SubjectId subjectId;

    /**
     * {@link #subjectId} 的管理端简写替代。仅在 admin-emit 路径生效（调用方对接收方具有 manage-clients）；
     * 可信 emit 路径始终原样使用 {@code sub_id}。与管理端 {@code /subjects:add} 端点的
     * {@code type}/{@code value} 对一致：
     *
     * <ul>
     *     <li>{@code user-id}、{@code user-email}、{@code user-username}
     *         → 解析为 Keycloak 用户；发送方通过
     *         {@link org.keycloak.ssf.transmitter.event.SecurityEventTokenMapper#buildSubjectForReceiver
     *         buildSubjectForReceiver} 构造 {@code sub_id}，形状符合接收方配置的 {@code ssf.userSubjectFormat}。</li>
     *     <li>{@code org-alias} → 解析为组织；发送方仅发出含 {@code tenant} 分面的复合主体（无 user），
     *         接收方按组织范围事件路由。</li>
     * </ul>
     *
     * <p>若同时提供两种形式，{@code sub_id} 优先。</p>
     */
    @JsonProperty("subjectType")
    private String subjectType;

    @JsonProperty("subjectValue")
    private String subjectValue;

    @JsonProperty("event")
    private Map<String, Object> event;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public SubjectId getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(SubjectId subjectId) {
        this.subjectId = subjectId;
    }

    public Map<String, Object> getEvent() {
        return event;
    }

    public void setEvent(Map<String, Object> event) {
        this.event = event;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectValue() {
        return subjectValue;
    }

    public void setSubjectValue(String subjectValue) {
        this.subjectValue = subjectValue;
    }
}
