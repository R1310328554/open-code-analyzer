package org.keycloak.scim.resource.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SCIM 多值属性（Multi-valued Attribute）的通用表示。
 * <p>常用于 emails、phoneNumbers 等复合多值字段，子类可扩展特定语义。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MultiValuedAttribute {

    /** 属性值。 */
    @JsonProperty("value")
    private String value;

    /** 面向用户的显示名称。 */
    @JsonProperty("display")
    private String display;

    /** 属性类型标签（如 work、home）。 */
    @JsonProperty("type")
    private String type;

    /** 是否为该类型的主值。 */
    @JsonProperty("primary")
    private Boolean primary;

    /** 关联资源的 URI 引用。 */
    @JsonProperty("$ref")
    private String ref;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
