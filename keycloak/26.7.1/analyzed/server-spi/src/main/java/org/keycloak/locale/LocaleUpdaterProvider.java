package org.keycloak.locale;

import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 区域更新提供者：持久化用户区域偏好并管理区域 Cookie。
 * <p>扩展 {@link Provider}，在用户切换语言时更新存储与响应 Cookie。</p>
 */
public interface LocaleUpdaterProvider extends Provider {

    /** 更新用户的区域偏好。
     * @param user 目标用户
     * @param locale 区域字符串（如 {@code en}） */
    void updateUsersLocale(UserModel user, String locale);

    /** 在 HTTP 响应中设置区域 Cookie。
     * @param locale 区域字符串 */
    void updateLocaleCookie(String locale);

    /** 使区域 Cookie 过期（清除用户语言偏好 Cookie）。 */
    void expireLocaleCookie();

}
