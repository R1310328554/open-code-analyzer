package org.keycloak.authentication.requiredactions;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.component.ComponentModel;
import org.keycloak.cookie.CookieProvider;
import org.keycloak.cookie.CookieType;
import org.keycloak.locale.LocaleSelectorProvider;
import org.keycloak.locale.LocaleUpdaterProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.UserModel;
import org.keycloak.storage.UserStorageProvider;

/**
 * 更新用户语言环境必需操作：在认证流程中同步用户 locale 与 cookie。
 * <p>优先使用用户请求 locale，其次用户属性，只读联邦用户可继承 cookie locale。</p>
 */
public class UpdateUserLocaleAction implements RequiredActionProvider, RequiredActionFactory {

    @Override
    public String getDisplayText() {
        return "Update User Locale";
    }

    /** 根据请求 locale、用户属性或 cookie 更新 locale 设置。 */
    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        String userRequestedLocale = context.getAuthenticationSession().getAuthNote(LocaleSelectorProvider.USER_REQUEST_LOCALE);
        if (userRequestedLocale != null) {
            LocaleUpdaterProvider updater = context.getSession().getProvider(LocaleUpdaterProvider.class);
            updater.updateUsersLocale(context.getUser(), userRequestedLocale);
        } else {
            String userLocale = context.getUser().getFirstAttribute(UserModel.LOCALE);

            if (userLocale != null) {
                LocaleUpdaterProvider updater = context.getSession().getProvider(LocaleUpdaterProvider.class);
                updater.updateLocaleCookie(userLocale);
            } else {
                CookieProvider cookies = context.getSession().getProvider(CookieProvider.class);
                String cookieLocale = cookies.get(CookieType.LOCALE);
                if (cookieLocale != null && !cookieLocale.isEmpty() && isReadOnlyFederatedUser(context)) {
                    context.getSession().getProvider(LocaleUpdaterProvider.class).updateLocaleCookie(cookieLocale);
                } else {
                    context.getSession().getProvider(LocaleUpdaterProvider.class).expireLocaleCookie();
                }
            }
        }
    }

    /** @return 用户是否为只读模式的联邦用户 */
    private boolean isReadOnlyFederatedUser(RequiredActionContext context) {
        String federationLink = context.getUser().getFederationLink();
        if (federationLink == null) {
            return false;
        }

        ComponentModel component = context.getRealm().getComponent(federationLink);
        if (component == null) {
            return false;
        }

        String editMode = component.getConfig().getFirst(LDAPConstants.EDIT_MODE);
        return UserStorageProvider.EditMode.READ_ONLY.toString().equals(editMode);
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
    }

    @Override
    public void processAction(RequiredActionContext context) {
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    /** @return 提供者标识符 update_user_locale */
    @Override
    public String getId() {
        return "update_user_locale";
    }

}
