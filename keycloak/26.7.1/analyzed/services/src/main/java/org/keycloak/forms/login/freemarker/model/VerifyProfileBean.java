package org.keycloak.forms.login.freemarker.model;

import java.util.stream.Stream;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;

/**
 * 用户资料验证/更新 FreeMarker Bean，用于 {@link UserProfileContext#UPDATE_PROFILE} 场景。
 * <p>继承 {@link AbstractUserProfileBean}，预填用户现有属性值。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class VerifyProfileBean extends AbstractUserProfileBean {

    private final UserModel user;

    /** @param user 当前用户 @param formData 表单回显数据 @param session Keycloak 会话 */
    public VerifyProfileBean(UserModel user, MultivaluedMap<String, String> formData, KeycloakSession session) {
        super(formData);
        this.user = user;
        init(session, false);
    }

    @Override
    /** 创建 UPDATE_PROFILE 上下文的 {@link UserProfile}。 */
    protected UserProfile createUserProfile(UserProfileProvider provider) {
        return provider.create(UserProfileContext.UPDATE_PROFILE, user);
    }

    @Override
    /** 属性默认值取自用户当前属性流。 */
    protected Stream<String> getAttributeDefaultValues(String name){
        return user.getAttributeStream(name);
    }
    
    @Override 
    /** @return 模板上下文名称 {@code UPDATE_PROFILE} */
    public String getContext() {
        return UserProfileContext.UPDATE_PROFILE.name();
    }

}
