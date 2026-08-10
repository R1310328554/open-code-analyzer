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
package org.keycloak.services.resources.admin;

import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Time;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserLoginFailureModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.BruteForceProtector;
import org.keycloak.services.resources.KeycloakOpenAPI;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

/**
 * 攻击检测管理 REST 资源。
 * <p>查询与清除用户暴力破解（Brute Force）登录失败记录。</p>
 *
 * @resource Attack Detection
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Extension(name = KeycloakOpenAPI.Profiles.ADMIN, value = "")
public class AttackDetectionResource {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(AttackDetectionResource.class);
    /** 细粒度权限评估器 */
    protected final AdminPermissionEvaluator auth;
    /** 当前领域 */
    protected final RealmModel realm;
    /** 管理事件构建器 */
    private final AdminEventBuilder adminEvent;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 客户端连接 */
    protected final ClientConnection connection;

    /** HTTP 请求头 */
    protected final HttpHeaders headers;

    /** 构造攻击检测资源并初始化 USER_LOGIN_FAILURE 事件上下文。 */
    public AttackDetectionResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.auth = auth;
        this.realm = session.getContext().getRealm();
        this.connection = session.getContext().getConnection();
        this.adminEvent = adminEvent.realm(realm).resource(ResourceType.USER_LOGIN_FAILURE);
        this.headers = session.getContext().getRequestHeaders();
    }

    /**
     * 获取指定用户在暴力破解检测中的状态。
     *
     * @param userId 用户 ID
     * @return 失败次数、锁定状态等信息的 Map
     */
    @GET
    @Path("brute-force/users/{userId}")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ATTACK_DETECTION)
    @Operation( summary = "Get status of a username in brute force detection")
    public Map<String, Object> bruteForceUserStatus(@PathParam("userId") String userId) {
        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            auth.users().requireView();
        } else {
            auth.users().requireView(user);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("disabled", false);
        data.put("numFailures", 0);
        data.put("numSecondaryAuthFailures", 0);
        data.put("numTemporaryLockouts", 0);
        data.put("lastFailure", 0);
        data.put("lastIPFailure", "n/a");
        data.put("failedLoginNotBefore", 0);
        if (!realm.isBruteForceProtected()) return data;


        UserLoginFailureModel model = session.loginFailures().getUserLoginFailure(realm, userId);
        if (model == null) return data;

        boolean disabled = isUserDisabled(model, user);
        if (disabled) {
            data.put("disabled", true);
            if(session.getProvider(BruteForceProtector.class).isTemporarilyDisabled(session, realm, user)) {
                data.put("failedLoginNotBefore", model.getFailedLoginNotBefore());
            } else {
                data.put("failedLoginNotBefore", Long.MAX_VALUE);
            }
        }

        data.put("numFailures", model.getNumFailures());
        data.put("numSecondaryAuthFailures", model.getNumSecondaryAuthFailures());
        data.put("numTemporaryLockouts", model.getNumTemporaryLockouts());
        data.put("lastFailure", model.getLastFailure());
        data.put("lastIPFailure", model.getLastIPFailure());
        return data;
    }

    /** 判断用户是否因暴力破解被临时或永久禁用。 */
    private boolean isUserDisabled(UserLoginFailureModel model, UserModel user) {
        if(user == null) {
            return Time.currentTime() < model.getFailedLoginNotBefore();
        }

        return isUserDisabledOrLockedByBruteForce(session, realm, user);
    }

    /** 检查用户是否被 {@link BruteForceProtector} 永久或临时锁定。 */
    private boolean isUserDisabledOrLockedByBruteForce(KeycloakSession session, RealmModel realm, UserModel user) {
        return session.getProvider(BruteForceProtector.class).isPermanentlyLockedOut(session, realm, user) 
        || session.getProvider(BruteForceProtector.class).isTemporarilyDisabled(session, realm, user);
    }

    /**
     * 清除指定用户的登录失败记录（可解除临时锁定）。
     *
     * @param userId 用户 ID
     */
    @Path("brute-force/users/{userId}")
    @DELETE
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ATTACK_DETECTION)
    @Operation( summary="Clear any user login failures for the user This can release temporary disabled user")
    public void clearBruteForceForUser(@PathParam("userId") String userId) {
        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            auth.users().requireManage();
        } else {
            auth.users().requireManage(user);
        }
        UserLoginFailureModel model = session.loginFailures().getUserLoginFailure(realm, userId);
        if (model != null) {
            session.loginFailures().removeUserLoginFailure(realm, userId);
            adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).success();
        }
    }

    /**
     * 清除领域内所有用户的登录失败记录（可批量解除临时锁定）。
     */
    @Path("brute-force/users")
    @DELETE
    @Tag(name = KeycloakOpenAPI.Admin.Tags.ATTACK_DETECTION)
    @Operation( summary = "Clear any user login failures for all users This can release temporary disabled users")
    public void clearAllBruteForce() {
        auth.users().requireManage();

        session.loginFailures().removeAllUserLoginFailures(realm);
        adminEvent.operation(OperationType.DELETE).resourcePath(session.getContext().getUri()).success();
    }


}
