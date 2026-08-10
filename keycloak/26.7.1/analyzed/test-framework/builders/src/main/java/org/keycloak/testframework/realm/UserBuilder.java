package org.keycloak.testframework.realm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.common.util.Time;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.oid4vc.UserVerifiableCredentialRepresentation;

/**
 * {@link UserRepresentation} 的流式构建器，用于在测试中构造用户、凭据、角色与联邦链接。
 */
public class UserBuilder extends Builder<UserRepresentation> {

    /** 基于已有用户表示对象构造构建器。 */
    protected UserBuilder(UserRepresentation rep) {
        super(rep);
    }

    /** 创建默认启用的用户构建器。 */
    public static UserBuilder create() {
        return new UserBuilder(new UserRepresentation()).enabled(true);
    }

    /** 创建并设置用户名的用户构建器。 */
    public static UserBuilder create(String username) {
        return create().username(username);
    }

    /** 基于已有用户表示对象创建更新用构建器。 */
    public static UserBuilder update(UserRepresentation rep) {
        return new UserBuilder(rep);
    }

    /** 设置用户 ID。 */
    public UserBuilder id(String id) {
        rep.setId(id);
        return this;
    }

    /** 设置用户是否启用。 */
    public UserBuilder enabled(boolean enabled) {
        rep.setEnabled(enabled);
        return this;
    }

    /** 设置用户名。 */
    public UserBuilder username(String username) {
        rep.setUsername(username);
        return this;
    }

    /** 同时设置名与姓。 */
    public UserBuilder name(String firstName, String lastName) {
        return firstName(firstName).lastName(lastName);
    }

    /** 设置邮箱地址。 */
    public UserBuilder email(String email) {
        rep.setEmail(email);
        return this;
    }

    /** 设置名。 */
    public UserBuilder firstName(String firstName) {
        rep.setFirstName(firstName);
        return this;
    }

    /** 设置姓。 */
    public UserBuilder lastName(String lastName) {
        rep.setLastName(lastName);
        return this;
    }

    /** 设置邮箱是否已验证。 */
    public UserBuilder emailVerified(boolean verified) {
        rep.setEmailVerified(verified);
        return this;
    }

    /** 追加凭据（表示对象或构建器）。 */
    public UserBuilder credential(CredentialRepresentation credential) {
        rep.setCredentials(combine(rep.getCredentials(), credential));
        return this;
    }

    /** 追加凭据（表示对象或构建器）。 */
    public UserBuilder credential(CredentialBuilder credential) {
        return credential(credential.build());
    }

    /** 设置密码凭据。 */
    public UserBuilder password(String password) {
        return credential(CredentialBuilder.password(password));
    }

    /** 设置 TOTP 密钥凭据。 */
    public UserBuilder totpSecret(String totpSecret) {
        return credential(CredentialBuilder.totp(totpSecret));
    }

    /** 设置 HOTP 密钥凭据。 */
    public UserBuilder hotpSecret(String hotpSecret) {
        return credential(CredentialBuilder.hotp(hotpSecret));
    }

    /** 追加领域角色。 */
    public UserBuilder realmRoles(String... roles) {
        rep.setRealmRoles(combine(rep.getRealmRoles(), roles));
        return this;
    }

    /** 为指定客户端追加客户端角色。 */
    public UserBuilder clientRoles(String client, String... roles) {
        rep.setClientRoles(combine(rep.getClientRoles(), client, roles));
        return this;
    }

    /** 追加必需操作。 */
    public UserBuilder requiredActions(String... requiredActions) {
        rep.setRequiredActions(combine(rep.getRequiredActions(), requiredActions));
        return this;
    }

    /** 追加用户所属组路径。 */
    public UserBuilder groups(String... groups) {
        rep.setGroups(combine(rep.getGroups(), groups));
        return this;
    }

    /** 添加用户属性（多值）。 */
    public UserBuilder attribute(String key, String... value) {
        rep.setAttributes(combine(rep.getAttributes(), key, value));
        return this;
    }

    /** 合并用户属性映射。 */
    public UserBuilder attributes(Map<String, List<String>> attributes) {
        rep.setAttributes(combine(rep.getAttributes(), attributes));
        return this;
    }

    /** 追加联邦身份链接。 */
    public UserBuilder federatedLink(FederatedIdentityRepresentation federatedIdentity) {
        rep.setFederatedIdentities(combine(rep.getFederatedIdentities(), federatedIdentity));
        return this;
    }

    /** 追加联邦身份链接。 */
    public UserBuilder federatedLink(FederatedIdentityBuilder federatedIdentity) {
        return federatedLink(federatedIdentity.build());
    }

    /** 追加联邦身份链接。 */
    public UserBuilder federatedLink(String identityProvider, String federatedUserId, String federatedUsername) {
        return federatedLink(FederatedIdentityBuilder.create(identityProvider, federatedUserId, federatedUsername));
    }

    /** 标记为指定客户端的服务账户用户。 */
    public UserBuilder serviceAccountId(String serviceAccountClientId) {
        rep.setServiceAccountClientId(serviceAccountClientId);
        return this;
    }

    /** 追加可验证凭据（OID4VC）条目。 */
    public UserBuilder verifiableCredential(String credentialScopeName) {
        UserVerifiableCredentialRepresentation newCred = new UserVerifiableCredentialRepresentation();
        newCred.setCredentialScopeName(credentialScopeName);
        newCred.setRevision(SecretGenerator.getInstance().generateSecureID());
        newCred.setCreatedDate(Time.currentTimeMillis());

        List<UserVerifiableCredentialRepresentation> creds = Optional.ofNullable(rep.getVerifiableCredentials())
                .orElseGet(() -> {
            List<UserVerifiableCredentialRepresentation> newCreds = new ArrayList<>();
            rep.setVerifiableCredentials(newCreds);
            return newCreds;
        });
        creds.add(newCred);
        return this;
    }

}
