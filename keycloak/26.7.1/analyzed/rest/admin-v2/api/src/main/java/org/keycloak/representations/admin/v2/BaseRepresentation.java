package org.keycloak.representations.admin.v2;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Admin API v2 表示对象的基类。
 * <p>
 * 通过 {@link #additionalFields} 支持 Jackson 任意属性读写，并在 PATCH 合并时
 * 允许将字段显式置为 {@code null}。
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class BaseRepresentation {

    // 所有表示类必须支持在 PATCH 合并时将字段置空
    @JsonIgnore
    protected Map<String, Object> additionalFields = new LinkedHashMap<String, Object>();

    /** 返回未在模型中显式声明的附加字段映射。 */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    /** 设置单个附加字段（Jackson {@code @JsonAnySetter}）。 */
    @JsonAnySetter
    public void setAdditionalField(String name, Object value) {
        this.additionalFields.put(name, value);
    }

    /** 批量替换附加字段映射。 */
    public void setAdditionalFields(Map<String, Object> additionalFields) {
        this.additionalFields = additionalFields;
    }
}
