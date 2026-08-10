package org.keycloak.admin.ui.rest.model;

import java.util.List;
import java.util.Objects;

/**
 * 描述某配置项（如认证流程、执行器）的适用范围。
 * <p>
 * 通过 {@link UsedByType} 区分是绑定特定客户端、特定提供者还是使用默认范围，
 * {@code values} 列出具体的目标标识。
 */
public class UsedBy {
    public UsedBy(UsedByType type, List<String> values) {
        this.type = type;
        this.values = values;
    }

    /**
     * 适用范围类型枚举。
     */
    public enum UsedByType {
        /** 绑定到指定的客户端列表。 */
        SPECIFIC_CLIENTS,
        /** 绑定到指定的身份提供者或类似组件。 */
        SPECIFIC_PROVIDERS,
        /** 使用默认/global 范围。 */
        DEFAULT
    }

    /** 适用范围类型。 */
    private UsedByType type;
    /** 具体目标标识列表（客户端 ID、提供者 ID 等，取决于 type）。 */
    private List<String> values;

    public UsedByType getType() {
        return type;
    }

    public void setType(UsedByType type) {
        this.type = type;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UsedBy usedBy = (UsedBy) o;
        return type == usedBy.type && Objects.equals(values, usedBy.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, values);
    }
}
