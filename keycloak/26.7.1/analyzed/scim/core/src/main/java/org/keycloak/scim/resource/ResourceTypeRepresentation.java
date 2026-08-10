package org.keycloak.scim.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.keycloak.scim.resource.common.Meta;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import static org.keycloak.scim.resource.Scim.getCoreSchema;

/**
 * SCIM 资源类型表示的抽象基类，封装 schemas、id、meta 及扩展属性等通用字段。
 * <p>具体资源（User、Group 等）继承此类并通过 Jackson 注解映射 JSON。</p>
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties({"type"})
public abstract class ResourceTypeRepresentation {

    /** SCIM 模式 URN 集合，标识资源遵循的 schema。 */
    @JsonProperty("schemas")
    private Set<String> schemas;
    /** 服务端分配的资源唯一标识。 */
    @JsonProperty("id")
    private String id;

    /** 外部系统提供的资源标识，用于跨系统关联。 */
    @JsonProperty("externalId")
    private String externalId;

    /** 资源元数据（创建时间、位置、版本等）。 */
    @JsonProperty("meta")
    private Meta meta;

    /** 创建时间戳（内部使用，不序列化到 JSON）。 */
    @JsonIgnore
    private Long createdTimestamp;

    /** 最后修改时间戳（内部使用，不序列化到 JSON）。 */
    @JsonIgnore
    private Long lastModifiedTimestamp;

    /** 扩展 schema 属性，通过 {@link JsonAnyGetter}/{@link JsonAnySetter} 映射。 */
    private Map<String, Object> extensions;

    /** 返回 schema URN 集合；若未设置则自动加入当前资源类型的核心 schema。 */
    public Set<String> getSchemas() {
        if (schemas == null) {
            schemas = new HashSet<>();
            schemas.add(getCoreSchema(this.getClass()));
        }
        return schemas;
    }

    public void setSchemas(Set<String> schemas) {
        this.schemas = schemas;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    /** 判断资源是否包含指定 schema URN。 */
    public boolean hasSchema(String schema) {
        return Optional.ofNullable(getSchemas()).orElse(Set.of()).contains(schema);
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    public Long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    /** 向 schemas 集合追加一个 schema URN。 */
    public void addSchema(String schema) {
        if (schemas == null) {
            schemas = new HashSet<>();
        }
        schemas.add(schema);
    }

    /** 返回扩展属性映射，供 Jackson 序列化未知 schema 字段。 */
    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        return extensions;
    }

    /** 反序列化时将未知 JSON 属性写入扩展映射。 */
    @JsonAnySetter
    public void setExtensions(String name, Object value) {
        if (extensions == null) {
            extensions = new HashMap<>();
        }
        this.extensions.put(name, value);
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
    }
}
