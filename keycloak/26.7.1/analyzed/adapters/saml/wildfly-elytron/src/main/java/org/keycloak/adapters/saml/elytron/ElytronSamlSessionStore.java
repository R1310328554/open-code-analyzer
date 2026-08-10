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

package org.keycloak.adapters.saml.elytron;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlSession;
import org.keycloak.adapters.saml.SamlSessionStore;
import org.keycloak.adapters.saml.SamlUtil;
import org.keycloak.adapters.spi.SessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapperUpdater;
import org.keycloak.common.util.KeycloakUriBuilder;

import org.jboss.logging.Logger;
import org.wildfly.security.http.HttpScope;
import org.wildfly.security.http.Scope;

/**
 * Elytron HTTP 作用域下的 SAML 会话存储实现。
 *
 * <p>基于 WildFly Elytron 的 {@link HttpScope} 管理 {@link SamlSession}、
 * 登录/登出状态及原始请求 URI，并同步 {@link SessionIdMapper} 映射。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ElytronSamlSessionStore implements SamlSessionStore, ElytronTokeStore {
    /** 本类日志记录器。 */
    protected static Logger log = Logger.getLogger(SamlSessionStore.class);
    /** 会话中保存登录前原始 URI 的附件键名。 */
    public static final String SAML_REDIRECT_URI = "SAML_REDIRECT_URI";

    /** SSO SessionIndex 与容器 Session ID 的映射器。 */
    private final SessionIdMapper idMapper;
    /** 映射器的集群同步更新器。 */
    private final SessionIdMapperUpdater idMapperUpdater;
    /** SAML 部署配置。 */
    protected final SamlDeployment deployment;
    /** Elytron HTTP 门面。 */
    private final ElytronHttpFacade exchange;


    /**
     * 构造 Elytron SAML 会话存储。
     *
     * @param exchange         HTTP 门面
     * @param idMapper         会话 ID 映射器
     * @param idMapperUpdater  映射更新器
     * @param deployment       SAML 部署配置
     */
    public ElytronSamlSessionStore(ElytronHttpFacade exchange, SessionIdMapper idMapper, SessionIdMapperUpdater idMapperUpdater, SamlDeployment deployment) {
        this.exchange = exchange;
        this.idMapper = idMapper;
        this.idMapperUpdater = idMapperUpdater;
        this.deployment = deployment;
    }

    /** 设置当前 SAML 流程动作（登录中/登出中/无）。 */
    @Override
    public void setCurrentAction(CurrentAction action) {
        if (action == CurrentAction.NONE && !exchange.getScope(Scope.SESSION).exists()) return;
        exchange.getScope(Scope.SESSION).setAttachment(CURRENT_ACTION, action);
    /** @return 当前会话是否处于 SAML 登录流程中 */
    @Override
    public boolean isLoggingIn() {
        HttpScope session = exchange.getScope(Scope.SESSION);
        if (!session.exists()) return false;
        CurrentAction action = (CurrentAction) session.getAttachment(CURRENT_ACTION);
        return action == CurrentAction.LOGGING_IN;
    /** @return 当前会话是否处于 SAML 登出流程中 */
    @Override
    public boolean isLoggingOut() {
        HttpScope session = exchange.getScope(Scope.SESSION);
        if (!session.exists()) return false;
        CurrentAction action = (CurrentAction) session.getAttachment(CURRENT_ACTION);
        return action == CurrentAction.LOGGING_OUT;
    /** 注销当前会话中的 SAML 账户并清理映射。 */
    @Override
    public void logoutAccount() {
        HttpScope session = getSession(false);
        if (session.exists()) {
            log.debug("Logging out - current account");
            SamlSession samlSession = (SamlSession)session.getAttachment(SamlSession.class.getName());
            if (samlSession != null) {
                if (samlSession.getSessionIndex() != null) {
                    idMapperUpdater.removeSession(idMapper, session.getID());
                }
                session.setAttachment(SamlSession.class.getName(), null);
            }
            session.setAttachment(SAML_REDIRECT_URI, null);
        }
    /** 按 SAML 主体名注销该用户的所有会话。 */
    @Override
    public void logoutByPrincipal(String principal) {
        Set<String> sessions = idMapper.getUserSessions(principal);
        if (sessions != null) {
            log.debugf("Logging out - by principal: %s", sessions);
            List<String> ids = new LinkedList<>();
            ids.addAll(sessions);
            logoutSessionIds(ids);
            for (String id : ids) {
                idMapperUpdater.removeSession(idMapper, id);
            }
        }

    /** 按 SSO SessionIndex 列表注销对应容器会话。 */
    @Override
    public void logoutBySsoId(List<String> ssoIds) {
        if (ssoIds == null) return;
        log.debugf("Logging out - by session IDs: %s", ssoIds);
        List<String> sessionIds = new LinkedList<>();
        for (String id : ssoIds) {
             String sessionId = idMapper.getSessionFromSSO(id);
             if (sessionId != null) {
                 sessionIds.add(sessionId);
                 idMapperUpdater.removeSession(idMapper, sessionId);
             }

        }
        logoutSessionIds(sessionIds);
    }

    /** 批量使指定 Session ID 的 Elytron 会话失效。 */
    protected void logoutSessionIds(List<String> sessionIds) {
        sessionIds.forEach(id -> {
            HttpScope scope = exchange.getScope(Scope.SESSION, id);

            if (scope.exists()) {
                log.debugf("Invalidating session %s", id);
                scope.setAttachment(SamlSession.class.getName(), null);
                scope.invalidate();
            }
        });
    /**
     * 检查当前会话是否已登录且 SAML 断言仍有效。
     *
     * @return 已登录且断言有效时返回 {@code true}
     */
    @Override
    public boolean isLoggedIn() {
        HttpScope session = getSession(false);
        if (!session.exists()) {
            log.debug("session was null, returning null");
            return false;
        }

        if (! idMapper.hasSession(session.getID()) && ! idMapperUpdater.refreshMapping(idMapper, session.getID())) {
            log.debugf("Session %s has expired on some other node", session.getID());
            session.setAttachment(SamlSession.class.getName(), null);
            return false;
        }

        final SamlSession samlSession = SamlUtil.validateSamlSession(session.getAttachment(SamlSession.class.getName()), deployment);
        if (samlSession == null) {
            return false;
        }

        exchange.authenticationComplete(samlSession);
        restoreRequest();
        return true;
    /** 将会话账户写入 Elytron Session 作用域并更新 SSO 映射。 */
    @Override
    public void saveAccount(SamlSession account) {
        HttpScope session = getSession(true);
        session.setAttachment(SamlSession.class.getName(), account);
        String sessionId = changeSessionId(session);
        idMapperUpdater.map(idMapper, account.getSessionIndex(), account.getPrincipal().getSamlSubject(), sessionId);

    }

    /** 登录成功后按需轮换 Session ID 以防固定攻击。 */
    protected String changeSessionId(HttpScope session) {
        if (!deployment.turnOffChangeSessionIdOnLogin()) {
            if (!session.supportsChangeID() || !session.changeID()) {
                log.debug("Session ID cannot be changed although turnOffChangeSessionIdOnLogin is set to false");
            }
        }
        return session.getID();
    /** @return 当前会话中的 SAML 账户 */
    @Override
    public SamlSession getAccount() {
        HttpScope session = getSession(true);
        return (SamlSession)session.getAttachment(SamlSession.class.getName());
    /** 返回登录前应重定向的 URI（会话附件或从 Referer 推断）。 */
    @Override
    public String getRedirectUri() {
        HttpScope session = exchange.getScope(Scope.SESSION);
        String redirect = (String) session.getAttachment(SAML_REDIRECT_URI);
        if (redirect == null) {
            URI uri = exchange.getURI();
            String path = uri.getPath();
            String relativePath = exchange.getRequest().getRelativePath();
            String contextPath = path.substring(0, path.indexOf(relativePath));

            if (!contextPath.isEmpty()) {
                contextPath = contextPath + "/";
            }

            String baseUri = KeycloakUriBuilder.fromUri(path).replacePath(contextPath).build().toString();
            return SamlUtil.getRedirectTo(exchange, contextPath, baseUri);
        }
        return redirect;
    /** 挂起当前请求并将原始 URI 保存到会话，供登录后恢复。 */
    @Override
    public void saveRequest() {
        exchange.suspendRequest();
        HttpScope scope = exchange.getScope(Scope.SESSION);

        if (!scope.exists()) {
            scope.create();
        }
        scope.setAttachment(SAML_REDIRECT_URI, exchange.getRequest().getURI());
    /** 恢复挂起的原始请求。 */
    @Override
    public boolean restoreRequest() {
        return exchange.restoreRequest();
    }

    /** 获取或创建 Elytron Session 作用域。 */
    protected HttpScope getSession(boolean create) {
        HttpScope scope = exchange.getScope(Scope.SESSION);

        if (!scope.exists() && create) {
            scope.create();
        }

        return scope;
    /** {@link ElytronTokeStore} 登出实现。 */
    @Override
    public void logout(boolean glo) {
        logoutAccount();
    }
}
