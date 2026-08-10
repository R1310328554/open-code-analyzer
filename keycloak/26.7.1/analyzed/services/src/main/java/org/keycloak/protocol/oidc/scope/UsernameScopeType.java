package org.keycloak.protocol.oidc.scope;

import jakarta.annotation.Nonnull;

import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.utils.StringUtil;

/**
 * 用户名参数化 scope 类型。
 * <p>校验参数为领域内已存在且已启用的用户名（或邮箱，若领域允许邮箱登录）。</p>
 */
public class UsernameScopeType implements ParameterizedScopeTypeProvider {

    /** 类型标识：username */
    public static final String TYPE = "username";

    /** Keycloak 会话（解析用户时必需） */
    protected final KeycloakSession session;

    /** 无参构造，供 SPI 反射实例化 */
    public UsernameScopeType() {
        this.session = null;
    }

    /** @param session Keycloak 会话 */
    public UsernameScopeType(KeycloakSession session) {
        this.session = session;
    }

    /** @return 类型名称 {@link #TYPE} */
    @Override
    public String getTypeName() {
        return TYPE;
    }

    /** @param session Keycloak 会话 @return 带会话的实例 */
    @Override
    public ParameterizedScopeTypeProvider create(KeycloakSession session) {
        return new UsernameScopeType(session);
    }

    /** 校验参数为非空且对应已启用用户 @throws InvalidScopeParameterException 用户不存在或已禁用时 */
    @Override
    public void validateParameter(@Nonnull ClientScopeModel scope, @Nonnull String parameter) throws InvalidScopeParameterException {
        if (StringUtil.isBlank(parameter)) {
            throw new InvalidScopeParameterException("Username parameter must not be blank");
        }
        resolveUser(scope, parameter);
    }

    /** 带当前用户上下文校验：禁止用户指定自身为目标 @param currentUser 当前用户 */
    @Override
    public void validateParameterWithUser(@Nonnull UserModel currentUser, @Nonnull ClientScopeModel scope, @Nonnull String parameter) throws InvalidScopeParameterException {
        UserModel targetUser = resolveUser(scope, parameter);
        if (targetUser.getId().equals(currentUser.getId())) {
            throw new InvalidScopeParameterException("User cannot target themselves");
        }
    }

    /**
     * 按用户名或邮箱解析目标用户。
     * @param scope 客户端范围（用于获取领域）
     * @param parameter 用户名或邮箱
     * @return 已启用的用户模型
     * @throws InvalidScopeParameterException 用户不存在或已禁用时
     */
    protected UserModel resolveUser(ClientScopeModel scope, String parameter) throws InvalidScopeParameterException {
        RealmModel realm = scope.getRealm();
        UserModel targetUser = session.users().getUserByUsername(realm, parameter);
        if (targetUser == null && realm.isLoginWithEmailAllowed() && parameter.contains("@")) {
            targetUser = session.users().getUserByEmail(realm, parameter);
        }
        if (targetUser == null) {
            throw new InvalidScopeParameterException(String.format("User '%s' not found in realm '%s'", parameter, realm.getName()));
        }
        if (!targetUser.isEnabled()) {
            throw new InvalidScopeParameterException(String.format("User '%s' is disabled in realm '%s'", parameter, scope.getRealm().getName()));
        }
        return targetUser;
    }
}
