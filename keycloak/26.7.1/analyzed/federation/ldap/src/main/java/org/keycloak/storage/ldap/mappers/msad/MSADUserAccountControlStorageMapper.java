/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.storage.ldap.mappers.msad;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.naming.AuthenticationException;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;
import org.keycloak.storage.ldap.mappers.AbstractLDAPStorageMapper;
import org.keycloak.storage.ldap.mappers.LDAPOperationDecorator;
import org.keycloak.storage.ldap.mappers.PasswordUpdateCallback;
import org.keycloak.storage.ldap.mappers.TxAwareLDAPUserModelDelegate;

import org.jboss.logging.Logger;

/**
 * MSAD 专用映射器：读取 userAccountControl 与 pwdLastSet，同步 Keycloak 账户启用状态与必需操作；
 * 并可解析 LDAP 认证异常码（参见 http://www-01.ibm.com/support/docview.wss?uid=swg21290631）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MSADUserAccountControlStorageMapper extends AbstractLDAPStorageMapper implements PasswordUpdateCallback {

    /** 是否在修改密码时使用 LDAP 密码策略提示扩展。 */
    public static final String LDAP_PASSWORD_POLICY_HINTS_ENABLED = "ldap.password.policy.hints.enabled";
    /** 是否始终从 LDAP userAccountControl 读取启用状态。 */
    public static final String ALWAYS_READ_ENABLED_VALUE_FROM_LDAP = "always.read.enabled.value.from.ldap";

    private static final Logger logger = Logger.getLogger(MSADUserAccountControlStorageMapper.class);

    private static final Pattern AUTH_EXCEPTION_REGEX = Pattern.compile(".*AcceptSecurityContext error, data ([0-9a-f]*), v.*");
    private static final Pattern ERROR_CODE_REGEX = Pattern.compile(".*ERROR CODE ([0-9A-F]*) - ([0-9A-F]*).*");

    // 参见 https://datatracker.ietf.org/doc/html/rfc4511#appendix-A.2
    public static final Set<String> PASSWORD_UPDATE_LDAP_ERROR_CODES = Set.of("53", "19");
    // 参见 https://msdn.microsoft.com/en-us/library/windows/desktop/ms681385(v=vs.85).aspx
    public static final Set<String> PASSWORD_UPDATE_MSAD_ERROR_CODES = Set.of("52D");

    private final Function<LDAPObject, UserAccountControl> GET_USER_ACCOUNT_CONTROL = ldapUser -> {
        if (ldapUser == null) {
            return UserAccountControl.empty();
        }
        String userAccountControl = ldapUser.getAttributeAsString(LDAPConstants.USER_ACCOUNT_CONTROL);
        return UserAccountControl.of(userAccountControl);
    };

    public MSADUserAccountControlStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
        super(mapperModel, ldapProvider);
        ldapProvider.setUpdater(this);
    }

    @Override
    public void beforeLDAPQuery(LDAPQuery query) {
        query.addReturningLdapAttribute(LDAPConstants.PWD_LAST_SET);
        query.addReturningLdapAttribute(LDAPConstants.USER_ACCOUNT_CONTROL);

        // pwdLastSet 默认为只读，仅在需要写入时解除
        query.addReturningReadOnlyLdapAttribute(LDAPConstants.PWD_LAST_SET);

        if (ldapProvider.getEditMode() != UserStorageProvider.EditMode.WRITABLE) {
            query.addReturningReadOnlyLdapAttribute(LDAPConstants.USER_ACCOUNT_CONTROL);
        }
    }

    @Override
    public LDAPOperationDecorator beforePasswordUpdate(UserModel user, LDAPObject ldapUser, UserCredentialModel password) {
        // 管理员重置密码时不应用 MSAD 密码策略提示
        if (password.isAdminRequest()) {
            return null;
        }

        boolean applyDecorator = mapperModel.get(LDAP_PASSWORD_POLICY_HINTS_ENABLED, false);
        return applyDecorator ? new LDAPServerPolicyHintsDecorator() : null;
    }

    @Override
    public void passwordUpdated(UserModel user, LDAPObject ldapUser, UserCredentialModel password) {
        logger.debugf("Going to update userAccountControl for ldap user '%s' after successful password update. Keycloak user '%s' in realm '%s'", ldapUser.getDn().toString(),
                user.getUsername(), getRealmName());

        // pwdLastSet 通常只读，写入前需解除只读标记
        ldapUser.removeReadOnlyAttributeName(LDAPConstants.PWD_LAST_SET);

        ldapUser.setSingleAttribute(LDAPConstants.PWD_LAST_SET, "-1");

        UserAccountControl control = getUserAccountControl(ldapUser);
        control.remove(UserAccountControl.PASSWD_NOTREQD);
        control.remove(UserAccountControl.PASSWORD_EXPIRED);

        if (user.isEnabled()) {
            control.remove(UserAccountControl.ACCOUNTDISABLE);
        }

        updateUserAccountControl(true, ldapUser, control);
    }

    @Override
    public void passwordUpdateFailed(UserModel user, LDAPObject ldapUser, UserCredentialModel password, ModelException exception) {
        throw processFailedPasswordUpdateException(exception);
    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        return new MSADUserModelDelegate(delegate, ldapUser, parseBooleanParameter(mapperModel, ALWAYS_READ_ENABLED_VALUE_FROM_LDAP));
    }

    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {

    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {
        // 根据 MSAD userAccountControl 设置 Keycloak 用户启用状态
        user.setEnabled(!getUserAccountControl(ldapUser).has(UserAccountControl.ACCOUNTDISABLE));
    }

    @Override
    public boolean onAuthenticationFailure(LDAPObject ldapUser, UserModel user, AuthenticationException ldapException, RealmModel realm) {
        String exceptionMessage = ldapException.getMessage();
        Matcher m = AUTH_EXCEPTION_REGEX.matcher(exceptionMessage);
        if (m.matches()) {
            String errorCode = m.group(1);
            return processAuthErrorCode(errorCode, user);
        } else {
            return false;
        }
    }

    protected boolean processAuthErrorCode(String errorCode, UserModel user) {
        logger.debugf("MSAD Error code is '%s' after failed LDAP login of user '%s'. Realm is '%s'", errorCode, user.getUsername(), getRealmName());

        if (ldapProvider.getEditMode() == UserStorageProvider.EditMode.WRITABLE) {
            if (errorCode.equals("532") || errorCode.equals("773")) {
                // 用户须更改 MSAD 密码：允许登录并添加 UPDATE_PASSWORD 必需操作
                if (user.getRequiredActionsStream().noneMatch(action -> Objects.equals(action, UserModel.RequiredAction.UPDATE_PASSWORD.name()))) {
                    // 通常对应错误码 532：pwdLastSet 为正且超过 MSAD 密码过期策略
                    AuthenticationSessionModel authSession = getSession().getContext().getAuthenticationSession();
                    if (authSession != null) {
                        if (authSession.getRequiredActions().stream().noneMatch(action -> Objects.equals(action, UserModel.RequiredAction.UPDATE_PASSWORD.name()))) {
                            logger.debugf("Adding requiredAction UPDATE_PASSWORD to the authenticationSession of user %s", user.getUsername());
                            authSession.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
                        }
                    } else {
                        // 兜底：正常认证流程中不应出现
                        logger.debugf("Adding requiredAction UPDATE_PASSWORD to the user %s", user.getUsername());
                        user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
                    }
                } else {
                    // 通常对应错误码 773：pwdLastSet 为 0 且密码被管理员手动设为过期
                    logger.tracef("Skip adding required action UPDATE_PASSWORD. It was already set on user '%s' in realm '%s'", user.getUsername(), getRealmName());
                }
                return true;
            } else if (errorCode.equals("533")) {
                // MSAD 中用户已禁用，同步禁用 Keycloak 用户
                if (user.isEnabled()) {
                    user.setEnabled(false);
                }
                return true;
            } else if (errorCode.equals("775")) {
                logger.warnf("Locked user '%s' attempt to login. Realm is '%s'", user.getUsername(), getRealmName());
            }
        }

        return false;
    }


    /** 解析密码更新失败异常，将 MSAD 密码策略违规转为通用无效密码消息。 */
    protected ModelException processFailedPasswordUpdateException(ModelException e) {
        if (e.getCause() == null || e.getCause().getMessage() == null) {
            return e;
        }

        String exceptionMessage = e.getCause().getMessage().replace('\n', ' ');
        logger.debugf("Failed to update password in Active Directory. Exception message: %s", exceptionMessage);
        exceptionMessage = exceptionMessage.toUpperCase();

        Matcher m = ERROR_CODE_REGEX.matcher(exceptionMessage);

        if (m.matches()) {
            String errorCode = m.group(1);
            String errorCode2 = m.group(2);

            if ((PASSWORD_UPDATE_LDAP_ERROR_CODES.contains(errorCode)) && PASSWORD_UPDATE_MSAD_ERROR_CODES.stream().anyMatch(errorCode2::endsWith)) {
                ModelException me = new ModelException("invalidPasswordGenericMessage", e);
                return me;
            }
        }

        return e;
    }

    /** 获取 userAccountControl；本地对象无值时从 LDAP 重新加载。 */
    protected UserAccountControl getUserAccountControl(LDAPObject ldapUser) {
        UserAccountControl control = GET_USER_ACCOUNT_CONTROL.apply(ldapUser);

        if (control.isAnySet()) {
            return control;
        }

        RealmModel realm = getSession().getContext().getRealm();

        if (realm == null) {
            return control;
        }

        ldapUser = ldapProvider.loadLDAPUserByUuid(realm, ldapUser.getUuid());

        return GET_USER_ACCOUNT_CONTROL.apply(ldapUser);
    }

    /** 更新 userAccountControl；{@code updateInLDAP} 为 false 时延迟到事务结束写入 LDAP。 */
    protected void updateUserAccountControl(boolean updateInLDAP, LDAPObject ldapUser, UserAccountControl accountControl) {
        String userAccountControlValue = String.valueOf(accountControl.getValue());
        logger.debugf("Updating userAccountControl of user '%s' to value '%s'. Realm is '%s'", ldapUser.getDn().toString(), userAccountControlValue, getRealmName());

        ldapUser.setSingleAttribute(LDAPConstants.USER_ACCOUNT_CONTROL, userAccountControlValue);

        if (updateInLDAP) {
            ldapProvider.getLdapIdentityStore().update(ldapUser);
        }
    }

    private String getRealmName() {
        RealmModel realm = getSession().getContext().getRealm();
        return (realm != null) ? realm.getName() : "null";
    }


    /** MSAD 用户代理：同步启用状态、必需操作与 pwdLastSet 到 LDAP。 */
    public class MSADUserModelDelegate extends TxAwareLDAPUserModelDelegate {

        private final LDAPObject ldapUser;
        private final boolean isAlwaysReadEnabledFromLdap;

        public MSADUserModelDelegate(UserModel delegate, LDAPObject ldapUser, boolean isAlwaysReadEnabledFromLdap) {
            super(delegate, ldapProvider, ldapUser);
            this.ldapUser = ldapUser;
            this.isAlwaysReadEnabledFromLdap = isAlwaysReadEnabledFromLdap;
        }

        @Override
        public boolean isEnabled() {
            if (isAlwaysReadEnabledFromLdap) {
                return !getUserAccountControl(ldapUser).has(UserAccountControl.ACCOUNTDISABLE);
            }
            return super.isEnabled();
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (UserStorageProvider.EditMode.WRITABLE.equals(ldapProvider.getEditMode())) {
                UserAccountControl control = getUserAccountControl(ldapUser);

                if (control.isAnySet()) {
                    MSADUserAccountControlStorageMapper.logger.debugf("Going to propagate enabled=%s for ldapUser '%s' to MSAD", enabled, ldapUser.getDn().toString());

                    if (enabled) {
                        control.remove(UserAccountControl.ACCOUNTDISABLE);
                    } else {
                        control.add(UserAccountControl.ACCOUNTDISABLE);
                    }

                    markUpdatedAttributeInTransaction(LDAPConstants.ENABLED);
                    updateUserAccountControl(false, ldapUser, control);
                }
            }
            // 始终同步本地数据库
            super.setEnabled(enabled);
        }

        @Override
        public void addRequiredAction(RequiredAction action) {
            String actionName = action.name();
            addRequiredAction(actionName);
        }

        @Override
        public void addRequiredAction(String action) {
            if (ldapProvider.getEditMode() == UserStorageProvider.EditMode.WRITABLE && RequiredAction.UPDATE_PASSWORD.toString().equals(action)) {
                MSADUserAccountControlStorageMapper.logger.debugf("Going to propagate required action UPDATE_PASSWORD to MSAD for ldap user '%s'. Keycloak user '%s' in realm '%s'",
                        ldapUser.getDn().toString(), getUsername(), getRealmName());

                // Normally it's read-only
                ldapUser.removeReadOnlyAttributeName(LDAPConstants.PWD_LAST_SET);

                ldapUser.setSingleAttribute(LDAPConstants.PWD_LAST_SET, "0");

                markUpdatedRequiredActionInTransaction(action);
            } else {
                // 写入本地数据库
                MSADUserAccountControlStorageMapper.logger.debugf("Going to add required action '%s' of user '%s' in realm '%s' to the DB", action, getUsername(), getRealmName());
                super.addRequiredAction(action);
            }
        }

        @Override
        public void removeRequiredAction(RequiredAction action) {
            String actionName = action.name();
            removeRequiredAction(actionName);
        }

        @Override
        public void removeRequiredAction(String action) {
            // 始终更新本地数据库
            super.removeRequiredAction(action);

            if (ldapProvider.getEditMode() == UserStorageProvider.EditMode.WRITABLE && RequiredAction.UPDATE_PASSWORD.toString().equals(action)) {

                // 新用户不在 MSAD 中设置 pwdLastSet
                UserAccountControl accountControl = getUserAccountControl(ldapUser);
                if (accountControl.getValue() != 0 && !accountControl.has(UserAccountControl.PASSWD_NOTREQD)) {
                    MSADUserAccountControlStorageMapper.logger.debugf("Going to remove required action UPDATE_PASSWORD from MSAD for ldap user '%s'. Account control: %s, Keycloak user '%s' in realm '%s'",
                            ldapUser.getDn().toString(), accountControl.getValue(), getUsername(), getRealmName());

                    // Normally it's read-only
                    ldapUser.removeReadOnlyAttributeName(LDAPConstants.PWD_LAST_SET);

                    ldapUser.setSingleAttribute(LDAPConstants.PWD_LAST_SET, "-1");

                    markUpdatedRequiredActionInTransaction(action);
                } else {
                    MSADUserAccountControlStorageMapper.logger.tracef("It was not required action to remove UPDATE_PASSWORD from MSAD for ldap user '%s' as it was not set on the user. Account control: %s, Keycloak user '%s' in realm '%s'",
                            ldapUser.getDn().toString(), accountControl.getValue(), getUsername(), getRealmName());
                }
            }
        }

        @Override
        public Stream<String> getRequiredActionsStream() {
            if (ldapProvider.getEditMode() == UserStorageProvider.EditMode.WRITABLE) {
                if (getPwdLastSet() == 0 || getUserAccountControl(ldapUser).has(UserAccountControl.PASSWORD_EXPIRED)) {
                    MSADUserAccountControlStorageMapper.logger.tracef("Required action UPDATE_PASSWORD is set in LDAP for user '%s' in realm '%s'", getUsername(), getRealmName());
                    return Stream.concat(super.getRequiredActionsStream(), Stream.of(RequiredAction.UPDATE_PASSWORD.toString()))
                            .distinct();
                }
            }
            return super.getRequiredActionsStream();
        }

        protected long getPwdLastSet() {
            String pwdLastSet = ldapUser.getAttributeAsString(LDAPConstants.PWD_LAST_SET);
            return pwdLastSet == null ? 0 : Long.parseLong(pwdLastSet);
        }


    }

}
