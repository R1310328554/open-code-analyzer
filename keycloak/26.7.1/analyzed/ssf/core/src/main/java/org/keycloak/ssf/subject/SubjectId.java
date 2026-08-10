package org.keycloak.ssf.subject;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 主体标识符（Subject Identifier）是描述与安全事件相关主体的结构化信息，
 * 通过命名格式定义其在安全事件令牌（SET）中作为 JSON 对象的编码方式。
 *
 * <p>本类刻意不在类级别标注 {@code @JsonDeserialize}：否则会经 Jackson 注解继承
 * 传播到每个具体子类，而 {@link SubjectIdJsonDeserializer} 本身通过 {@code treeToValue}
 * 分派到具体子类，将形成循环。需要反序列化抽象 {@code SubjectId} 类型的调用点
 * 要么在字段级别使用 {@code @JsonDeserialize(using = SubjectIdJsonDeserializer.class)}
 * （例如 {@code SsfEmitEventRequest.sub_id}），要么通过 {@code SubjectIds.fromTree(...)} 调用反序列化器。
 *
 * <p>参见 https://datatracker.ietf.org/doc/html/rfc9493</p>
 */
public abstract class SubjectId {

    @JsonProperty("format")
    protected String format;

    @JsonIgnore
    protected Map<String, Object> attributes = new HashMap<>();

    public SubjectId(String format) {
        this.format = format;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @JsonAnySetter
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
