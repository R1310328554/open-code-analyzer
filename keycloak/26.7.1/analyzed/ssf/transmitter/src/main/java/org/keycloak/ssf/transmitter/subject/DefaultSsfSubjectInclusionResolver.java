package org.keycloak.ssf.transmitter.subject;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.UserModel;

/**
 * 无状态默认 {@link SsfSubjectInclusionResolver}，委托 {@link SsfNotifyAttributes}
 * 静态辅助方法——即读取用户/组织上的 {@code ssf.notify.<receiverClientId>} 属性。
 *
 * <p>可子类化并重写个别方法，在属性检查之上叠加额外纳入来源
 * （通常 {@code super.isXxx(...) || extraCheck(...)}）。</p>
 */
public class DefaultSsfSubjectInclusionResolver implements SsfSubjectInclusionResolver {

    @Override
    public boolean isUserNotified(KeycloakSession session, UserModel user, String receiverClientId) {
        return SsfNotifyAttributes.isUserNotified(user, receiverClientId);
    }

    @Override
    public boolean isUserExcluded(KeycloakSession session, UserModel user, String receiverClientId) {
        return SsfNotifyAttributes.isUserExcluded(user, receiverClientId);
    }

    @Override
    public boolean isOrganizationNotified(KeycloakSession session, OrganizationModel organization, String receiverClientId) {
        return SsfNotifyAttributes.isOrganizationNotified(organization, receiverClientId);
    }

    @Override
    public boolean isOrganizationExcluded(KeycloakSession session, OrganizationModel organization, String receiverClientId) {
        return SsfNotifyAttributes.isOrganizationExcluded(organization, receiverClientId);
    }
}
