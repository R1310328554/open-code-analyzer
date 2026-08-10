package org.keycloak.userprofile;

import java.util.List;
import java.util.Map;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * 服务账户专用的 {@link Attributes} 实现。
 * <p>服务账户非普通用户；仅在 {@link UserProfileContext#USER_API} 下允许管理未托管属性，避免管理员必须为服务账户单独配置 {@link org.keycloak.representations.userprofile.config.UPConfig.UnmanagedAttributePolicy} 或额外托管属性。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ServiceAccountAttributes extends DefaultAttributes {

    /** 构造服务账户属性容器。 */
    public ServiceAccountAttributes(UserProfileContext context, Map<String, ?> attributes, UserModel user,
                                    UserProfileMetadata profileMetadata, KeycloakSession session) {
        super(context, attributes, user, profileMetadata, session);
    }

    /** 用户名为只读；非 USER_API 上下文下其余属性只读。 */
    @Override
    public boolean isReadOnly(String name) {
        if (UserModel.USERNAME.equals(name)) {
            return true;
        }

        return !UserProfileContext.USER_API.equals(context);
    }

    /** 未托管属性仅在 USER_API 上下文可查看/编辑。 */
    @Override
    protected AttributeMetadata createUnmanagedAttributeMetadata(String name) {
        return new AttributeMetadata(name, Integer.MAX_VALUE) {
            @Override
            public boolean canView(AttributeContext context) {
                return UserProfileContext.USER_API.equals(context.getContext());
            }

            @Override
            public boolean canEdit(AttributeContext context) {
                return UserProfileContext.USER_API.equals(context.getContext());
            }
        };
    }

    /** 仅 USER_API 允许未托管属性。 */
    @Override
    protected boolean isAllowUnmanagedAttribute() {
        return UserProfileContext.USER_API.equals(context);
    }

    @Override
    protected void setUserName(Map<String, List<String>> newAttributes, List<String> values) {
        // 服务账户不允许更新用户名
    }
}
