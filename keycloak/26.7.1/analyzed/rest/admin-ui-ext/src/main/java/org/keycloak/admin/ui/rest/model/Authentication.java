package org.keycloak.admin.ui.rest.model;

import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 认证流（Authentication Flow）在管理 UI 中的展示模型。
 */
public class Authentication {

    /** 认证流内部 ID。 */
    @Schema(required = true)
    private String id;

    /** 认证流别名，领域内唯一标识。 */
    @Schema(required = true)
    private String alias;

    /** 是否为 Keycloak 内置流，内置流不可删除。 */
    @Schema(required = true)
    private boolean builtIn;

    /** 该流被哪些身份提供方、客户端或默认绑定引用。 */
    private UsedBy usedBy;

    /** 人类可读的描述文本。 */
    private String description;

    public  UsedBy getUsedBy() {
        return usedBy;
    }

    public void setUsedBy( UsedBy usedBy) {
        this.usedBy = usedBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Authentication that = (Authentication) o;
        return builtIn == that.builtIn && Objects.equals(usedBy, that.usedBy) && Objects.equals(id, that.id) && Objects.equals(alias,
                that.alias) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usedBy, id, builtIn, alias, description);
    }

    @Override public String toString() {
        return "Authentication{" + "usedBy=" + usedBy + ", id='" + id + '\'' + ", buildIn=" + builtIn + ", alias='" + alias + '\'' + ", description='" + description + '\'' + '}';
    }
}
