package org.keycloak.theme;

import org.keycloak.Config;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;

/**
 * 默认主题选择器 Provider。
 * <p>按 {@link Theme.Type} 从配置、客户端属性或 Realm 设置解析主题名称。</p>
 */
public class DefaultThemeSelectorProvider implements ThemeSelectorProvider {

    /** 客户端属性键：覆盖登录主题。 */
    public static final String LOGIN_THEME_KEY = "login_theme";

    private final KeycloakSession session;

    /** 绑定当前 Keycloak 会话。 */
    public DefaultThemeSelectorProvider(KeycloakSession session) {
        this.session = session;
    }

    /** 按类型解析生效主题名，缺失时回退到默认主题。 */
    @Override
    public String getThemeName(Theme.Type type) {
        String name = null;

        switch (type) {
            case WELCOME:
                name = Config.scope("theme").get("welcomeTheme");
                break;
            case LOGIN:
                ClientModel client = session.getContext().getClient();
                if (client != null) {
                    name = client.getAttribute(LOGIN_THEME_KEY);
                }

                if (name == null || name.isEmpty()) {
                    name = session.getContext().getRealm().getLoginTheme();
                }
                
                break;
            case ACCOUNT:
                name = session.getContext().getRealm().getAccountTheme();
                break;
            case EMAIL:
                name = session.getContext().getRealm().getEmailTheme();
                break;
            case ADMIN:
                name = session.getContext().getRealm().getAdminTheme();
                break;
        }

        if (name == null || name.isEmpty()) {
            name = getDefaultThemeName(type);
        }

        return name;
    }

    @Override
    public void close() {
    }

}
