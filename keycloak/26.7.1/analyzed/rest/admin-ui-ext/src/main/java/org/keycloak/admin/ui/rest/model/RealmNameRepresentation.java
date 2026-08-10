package org.keycloak.admin.ui.rest.model;

/**
 * 领域名称与显示名的轻量 DTO，供领域下拉列表等 UI 组件使用。
 */
public class RealmNameRepresentation {
    /** 领域内部名称（realm name）。 */
    private String name;
    /** 面向用户的显示名称。 */
    private String displayName;

    public RealmNameRepresentation(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
