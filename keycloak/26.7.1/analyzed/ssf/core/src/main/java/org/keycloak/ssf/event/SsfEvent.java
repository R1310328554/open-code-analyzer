package org.keycloak.ssf.event;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.keycloak.ssf.subject.SubjectId;
import org.keycloak.ssf.subject.SubjectIdJsonDeserializer;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 通用 SSF（Security Event Token）事件抽象基类。
 * <p>参见 RFC 8417：https://datatracker.ietf.org/doc/html/rfc8417</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class SsfEvent {

    /** 事件类型的内部（较短）别名。 */
    @JsonIgnore
    protected String alias;

    @JsonProperty("subject")
    @JsonDeserialize(using = SubjectIdJsonDeserializer.class)
    protected SubjectId subjectId;

    @JsonIgnore
    protected String eventType;

    /**
     * 事件发生时间（UNIX 时间戳）。可为 null，以便不含时间戳的事件
     * （如 {@code ssf/event-type/verification} 及流管理事件）
     * 在 JSON 中省略该字段，而非序列化为 {@code "event_timestamp": 0}。
     */
    @JsonProperty("event_timestamp")
    protected Long eventTimestamp;

    /** 发起该事件的实体。 */
    @JsonProperty("initiating_entity")
    protected InitiatingEntity initiatingEntity;

    /** 面向日志与审计的本地化管理员消息，键为语言代码，值为消息文本。 */
    @JsonProperty("reason_admin")
    protected Map<String, String> reasonAdmin;

    /** 面向终端用户的本地化消息，键为语言代码，值为消息文本。 */
    @JsonProperty("reason_user")
    protected Map<String, String> reasonUser;

    @JsonIgnore
    protected Map<String, Object> attributes = new HashMap<>();

    public SsfEvent(String eventType) {
        this.eventType = eventType;

        // use the simple class name as the default alias
        this.alias = getClass().getSimpleName();
    }

    public SubjectId getSubjectId() {
        return subjectId;
    }

    public Long getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public InitiatingEntity getInitiatingEntity() {
        return initiatingEntity;
    }

    public void setInitiatingEntity(InitiatingEntity initiatingEntity) {
        this.initiatingEntity = initiatingEntity;
    }

    public Map<String, String> getReasonAdmin() {
        return reasonAdmin;
    }

    public void setReasonAdmin(Map<String, String> reasonAdmin) {
        this.reasonAdmin = reasonAdmin;
    }

    public Map<String, String> getReasonUser() {
        return reasonUser;
    }

    public void setReasonUser(Map<String, String> reasonUser) {
        this.reasonUser = reasonUser;
    }

    public String getEventType() {
        return eventType;
    }

    /**
     * 将 {@link #attributes} 作为 Jackson any-getter 暴露，
     * 使各条目作为事件对象的顶层 JSON 字段（与 {@code @JsonProperty} 字段平铺），
     * 符合 SSF §4.2.3 扩展字段布局，而非嵌套在 {@code "attributes"} 下。
     */
    @JsonAnyGetter
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @JsonAnySetter
    public void setAttributeValue(String key, Object value) {
        if (declaredJsonPropertyNames(getClass()).contains(key)) {
            throw new IllegalArgumentException(
                    "Custom attribute key '" + key + "' collides with a declared @JsonProperty on "
                            + getClass().getName());
        }
        attributes.put(key, value);
    }

    private static final ConcurrentMap<Class<?>, Set<String>> DECLARED_JSON_PROPERTIES = new ConcurrentHashMap<>();

    private static Set<String> declaredJsonPropertyNames(Class<?> type) {
        return DECLARED_JSON_PROPERTIES.computeIfAbsent(type, t -> {
            Set<String> names = new HashSet<>();
            for (Class<?> c = t; c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field f : c.getDeclaredFields()) {
                    JsonProperty ann = f.getAnnotation(JsonProperty.class);
                    if (ann != null && !ann.value().isEmpty()) {
                        names.add(ann.value());
                    }
                }
            }
            return Set.copyOf(names);
        });
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setSubjectId(SubjectId subjectId) {
        this.subjectId = subjectId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * 校验本事件实例是否包含 SSF / CAEP / RISC 规范标记为 REQUIRED 的字段。
     * 合成发射管道在 Jackson 从调用方 JSON 实例化事件后调用，
     * 以便在 SET 签名与分发前以明确错误拒绝缺失字段。
     * <p>默认实现为空操作；含必填字段的子类（如 {@code CaepCredentialChange.change_type}）
     * 应覆盖并抛出 {@link SsfEventValidationException}。</p>
     * <p>原生事件生产路径始终提供必填字段；该钩子主要作用于合成发射与自定义扩展事件。</p>
     */
    public void validate() {
        // no-op — overridden by event subclasses that have spec-required fields
    }
}
