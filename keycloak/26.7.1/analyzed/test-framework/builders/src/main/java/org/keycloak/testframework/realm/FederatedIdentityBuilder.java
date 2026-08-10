package org.keycloak.testframework.realm;

import org.keycloak.representations.idm.FederatedIdentityRepresentation;

/**
 * {@link FederatedIdentityRepresentation} 的流式构建器，用于在测试中关联外部身份提供者的联邦身份。
 */
public class FederatedIdentityBuilder extends Builder<FederatedIdentityRepresentation> {

    /** 基于已有联邦身份表示对象构造构建器。 */
    private FederatedIdentityBuilder(FederatedIdentityRepresentation rep) {
        super(rep);
    }

    /** 创建空的联邦身份构建器。 */
    public static FederatedIdentityBuilder create() {
        return new FederatedIdentityBuilder(new FederatedIdentityRepresentation());
    }

    /**
     * 一次性创建并填充 IdP、用户 ID 与用户名的联邦身份。
     *
     * @param identityProvider 身份提供者别名
     * @param userId 外部用户 ID
     * @param userName 外部用户名
     */
    public static FederatedIdentityBuilder create(String identityProvider, String userId, String userName) {
        return new FederatedIdentityBuilder(new FederatedIdentityRepresentation()).identityProvider(identityProvider).userId(userId).userName(userName);
    }

    /** 基于已有联邦身份表示对象创建更新用构建器。 */
    public static FederatedIdentityBuilder update(FederatedIdentityRepresentation rep) {
        return new FederatedIdentityBuilder(rep);
    }

    /** 设置身份提供者别名。 */
    public FederatedIdentityBuilder identityProvider(String identityProvider) {
        rep.setIdentityProvider(identityProvider);
        return this;
    }

    /** 设置外部身份提供者中的用户 ID。 */
    public FederatedIdentityBuilder userId(String userId) {
        rep.setUserId(userId);
        return this;
    }

    /** 设置外部身份提供者中的用户名。 */
    public FederatedIdentityBuilder userName(String userName) {
        rep.setUserName(userName);
        return this;
    }

}
