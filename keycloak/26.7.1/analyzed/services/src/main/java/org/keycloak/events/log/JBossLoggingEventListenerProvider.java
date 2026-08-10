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

package org.keycloak.events.log;

import java.util.Map;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.util.StackUtil;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerTransaction;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.StringUtil;

import org.jboss.logging.Logger;

/**
 * JBoss Logging 事件监听器：将用户事件与管理事件格式化为结构化日志。
 * <p>支持可配置的日志级别、引号包裹与空格/引号清理；TRACE 级别额外输出请求 URI、Cookie 与短栈追踪。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JBossLoggingEventListenerProvider implements EventListenerProvider {

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 目标 JBoss Logger 实例。 */
    private final Logger logger;
    /** 无错误事件使用的日志级别。 */
    private final Logger.Level successLevel;
    /** 含 error 字段事件使用的日志级别。 */
    private final Logger.Level errorLevel;
    /** 是否清理值中的空格与引号。 */
    private final boolean sanitize;
    /** 日志值包裹引号字符，{@code null} 表示不使用引号。 */
    private final Character quotes;
    /** 管理事件日志是否包含 representation JSON。 */
    private final boolean includeRepresentation;
    /** 事务提交后写日志的延迟事务包装。 */
    private final EventListenerTransaction tx = new EventListenerTransaction(this::logAdminEvent, this::logEvent);

    /** @param session 当前会话 @param logger 目标 Logger @param successLevel 成功事件级别 @param errorLevel 错误事件级别 @param quotes 值包裹引号 @param sanitize 是否清理空格/引号 @param includeRepresentation 是否输出 representation */
    public JBossLoggingEventListenerProvider(KeycloakSession session, Logger logger,
            Logger.Level successLevel, Logger.Level errorLevel, Character quotes,
            boolean sanitize, boolean includeRepresentation) {
        this.session = session;
        this.logger = logger;
        this.successLevel = successLevel;
        this.errorLevel = errorLevel;
        this.sanitize = sanitize;
        this.quotes = quotes;
        this.includeRepresentation = includeRepresentation;
        this.session.getTransactionManager().enlistAfterCompletion(tx);
    }

    @Override
    /** 收集用户事件，在事务提交后写日志。 */
    public void onEvent(Event event) {
        tx.addEvent(event);
    }

    @Override
    /** 收集管理事件，在事务提交后写日志。 */
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        tx.addAdminEvent(adminEvent, includeRepresentation);
    }

    /** 按配置为日志值添加引号并可选清理空格/引号。 */
    private void sanitize(StringBuilder sb, String str) {
        if (quotes != null) {
            sb.append(quotes);
        }
        if (sanitize) {
            str = StringUtil.sanitizeSpacesAndQuotes(str, quotes);
        }
        sb.append(str);
        if (quotes != null) {
            sb.append(quotes);
        }
    }

    /** 格式化并输出用户事件日志。 */
    protected void logEvent(Event event) {
        Logger.Level level = event.getError() != null ? errorLevel : successLevel;

        if (logger.isEnabled(level)) {
            StringBuilder sb = new StringBuilder();

            sb.append("type=");
            sanitize(sb, event.getType().toString());
            sb.append(", realmId=");
            sanitize(sb, event.getRealmId());
            sb.append(", realmName=");
            sanitize(sb, event.getRealmName());
            sb.append(", clientId=");
            sanitize(sb, event.getClientId());
            sb.append(", userId=");
            sanitize(sb, event.getUserId());
            if (event.getSessionId() != null) {
                sb.append(", sessionId=");
                sanitize(sb, event.getSessionId());
            }
            sb.append(", ipAddress=");
            sanitize(sb, event.getIpAddress());

            if (event.getError() != null) {
                sb.append(", error=");
                sanitize(sb, event.getError());
            }

            if (event.getDetails() != null) {
                for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
                    sb.append(", ");
                    sb.append(StringUtil.sanitizeSpacesAndQuotes(e.getKey(), null));
                    sb.append("=");
                    sanitize(sb, e.getValue());
                }
            }

            AuthenticationSessionModel authSession = session.getContext().getAuthenticationSession();
            if(authSession!=null) {
                sb.append(", authSessionParentId=");
                sanitize(sb, authSession.getParentSession().getId());
                sb.append(", authSessionTabId=");
                sanitize(sb, authSession.getTabId());
            }

            if(logger.isTraceEnabled()) {
                setKeycloakContext(sb);

                if (StackUtil.isShortStackTraceEnabled()) {
                    sb.append(", stackTrace=").append(StackUtil.getShortStackTrace());
                }
            }

            logger.log(logger.isTraceEnabled() ? Logger.Level.TRACE : level, sb.toString());
        }
    }

    /** 格式化并输出管理事件日志。 */
    protected void logAdminEvent(AdminEvent adminEvent, boolean realmIncludeRepresentation) {
        Logger.Level level = adminEvent.getError() != null ? errorLevel : successLevel;

        if (logger.isEnabled(level)) {
            StringBuilder sb = new StringBuilder();

            sb.append("operationType=");
            sanitize(sb, adminEvent.getOperationType().toString());
            sb.append(", realmId=");
            sanitize(sb, adminEvent.getAuthDetails().getRealmId());
            sb.append(", realmName=");
            sanitize(sb, adminEvent.getAuthDetails().getRealmName());
            sb.append(", clientId=");
            sanitize(sb, adminEvent.getAuthDetails().getClientId());
            sb.append(", userId=");
            sanitize(sb, adminEvent.getAuthDetails().getUserId());
            sb.append(", ipAddress=");
            sanitize(sb, adminEvent.getAuthDetails().getIpAddress());
            sb.append(", resourceType=");
            sanitize(sb, adminEvent.getResourceTypeAsString());
            sb.append(", resourcePath=");
            sanitize(sb, adminEvent.getResourcePath());

            if (adminEvent.getError() != null) {
                sb.append(", error=");
                sanitize(sb, adminEvent.getError());
            }

            if (adminEvent.getDetails() != null) {
                for (Map.Entry<String, String> e : adminEvent.getDetails().entrySet()) {
                    sb.append(", ");
                    sb.append(StringUtil.sanitizeSpacesAndQuotes(e.getKey(), null));
                    sb.append("=");
                    sanitize(sb, e.getValue());
                }
            }

            if (realmIncludeRepresentation && includeRepresentation && adminEvent.getRepresentation() != null) {
                sb.append(", representation=");
                sanitize(sb, adminEvent.getRepresentation());
            }

            if(logger.isTraceEnabled()) {
                setKeycloakContext(sb);
            }

            logger.log(logger.isTraceEnabled() ? Logger.Level.TRACE : level, sb.toString());
        }
    }

    @Override
    /** 关闭监听器（无资源需释放）。 */
    public void close() {
    }

    /** TRACE 级别时追加请求 URI 与 Cookie 信息。 */
    private void setKeycloakContext(StringBuilder sb) {
        KeycloakContext context = session.getContext();
        try {
            UriInfo uriInfo = context.getUri();
            HttpHeaders headers = context.getRequestHeaders();
            if (uriInfo != null) {
                sb.append(", requestUri=");
                sanitize(sb, uriInfo.getRequestUri().toString());
            }

            if (headers != null) {
                sb.append(", cookies=[");
                boolean f = true;
                for (Map.Entry<String, Cookie> e : headers.getCookies().entrySet()) {
                    if (f) {
                        f = false;
                    } else {
                        sb.append(", ");
                    }
                    sb.append(StringUtil.sanitizeSpacesAndQuotes(e.getValue().toString(), null));
                }
                sb.append("]");
            }
        } catch (ContextNotActiveException e) {
            // 无可用请求上下文，跳过 URI/Cookie 追加
        }
    }

}
