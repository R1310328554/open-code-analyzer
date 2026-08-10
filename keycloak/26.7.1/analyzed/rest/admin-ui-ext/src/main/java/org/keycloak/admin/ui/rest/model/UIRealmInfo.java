package org.keycloak.admin.ui.rest.model;

/**
 * 领域信息摘要，对该领域的每位管理员可见，不限于有权查看完整领域配置的管理员。
 * <p>
 * 用于在管理 UI 中展示与当前登录管理员相关的领域级能力开关等信息。
 */
public class UIRealmInfo {
    /** 是否启用了用户配置文件（User Profile）相关提供者。 */
    private boolean userProfileProvidersEnabled;

    public boolean isUserProfileProvidersEnabled() {
        return userProfileProvidersEnabled;
    }

    public void setUserProfileProvidersEnabled(final boolean userProfileProvidersEnabled) {
        this.userProfileProvidersEnabled = userProfileProvidersEnabled;
    }
}
