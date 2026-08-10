package org.keycloak.testsuite.user.profile;

import java.util.Map;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.userprofile.DeclarativeUserProfileProvider;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;

/**
 * 自定义用户配置提供者：在集成测试中覆盖默认必填属性（名、姓、邮箱），
 * 以便验证非必填场景下的用户配置行为。
 */
public class CustomUserProfileProvider extends DeclarativeUserProfileProvider {

    /**
     * 构造提供者并清除名、姓、邮箱的必填约束。
     *
     * @param session Keycloak 会话
     * @param factory 关联的工厂实例
     */
    public CustomUserProfileProvider(KeycloakSession session, CustomUserProfileProviderFactory factory) {
        super(session, factory);
        UPConfig upConfig = getConfiguration();

        upConfig.getAttribute(UserModel.FIRST_NAME).setRequired(null);
        upConfig.getAttribute(UserModel.LAST_NAME).setRequired(null);
        upConfig.getAttribute(UserModel.EMAIL).setRequired(null);

        setConfiguration(upConfig);
    }

    /** 基于已有用户属性创建 {@link UserProfile}。 */
    @Override
    public UserProfile create(UserProfileContext context, UserModel user) {
        return this.create(context, user.getAttributes(), user);
    }

    /** 基于显式属性映射创建 {@link UserProfile}。 */
    @Override
    public UserProfile create(UserProfileContext context, Map<String, ?> attributes, UserModel user) {
        return super.create(context, attributes, user);
    }

    /** 基于属性映射创建 {@link UserProfile}（无关联用户模型）。 */
    @Override
    public UserProfile create(UserProfileContext context, Map<String, ?> attributes) {
        return this.create(context, attributes, (UserModel) null);
    }

}
