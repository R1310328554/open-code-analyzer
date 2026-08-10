package org.keycloak.scim.resource.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SCIM 资源元数据（meta 属性），包含资源类型、时间戳、位置与版本信息。
 * <p>对应 RFC 7643 中 Resource 的 meta 复合属性。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Meta {

    /** 资源类型名称（如 User、Group）。 */
    @JsonProperty("resourceType")
    private String resourceType;

    /** 资源创建时间（ISO-8601 字符串）。 */
    @JsonProperty("created")
    private String created;

    /** 资源最后修改时间（ISO-8601 字符串）。 */
    @JsonProperty("lastModified")
    private String lastModified;

    /** 资源的 canonical URI 位置。 */
    @JsonProperty("location")
    private String location;

    /** 资源版本标识，通常用于 ETag/乐观并发控制。 */
    @JsonProperty("version")
    private String version;

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
