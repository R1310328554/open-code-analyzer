/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.util.Objects;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionIdListener;
import jakarta.servlet.http.HttpSessionListener;

import org.keycloak.adapters.saml.SamlSession;
import org.keycloak.adapters.spi.SessionIdMapper;

import org.jboss.logging.Logger;

/**
 * HTTP 会话生命周期监听器，维护 {@link SessionIdMapper} 与 SAML 会话的映射关系。
 *
 * <p>监听会话创建/销毁、Session ID 变更及 {@link SamlSession} 属性增删改，
 * 确保集群环境下 SSO SessionIndex 与容器 Session ID 同步。</p>
 *
 * @author hmlnarik
 */
public class IdMapperUpdaterSessionListener implements HttpSessionListener, HttpSessionAttributeListener, HttpSessionIdListener {

    /** 本类日志记录器。 */
    private static final Logger LOG = Logger.getLogger(IdMapperUpdaterSessionListener.class);

    /** 会话 ID 映射器。 */
    private final SessionIdMapper idMapper;

    /**
     * 构造监听器。
     *
     * @param idMapper 会话 ID 映射器
     */
    public IdMapperUpdaterSessionListener(SessionIdMapper idMapper) {
        this.idMapper = idMapper;
    }

    /** 会话创建时：若已有 {@link SamlSession} 属性则建立映射。 */
    @Override
    public void sessionCreated(HttpSessionEvent hse) {
        LOG.debugf("Session created");
        HttpSession session = hse.getSession();
        Object value = session.getAttribute(SamlSession.class.getName());
        map(session.getId(), value);
    }

    /** 会话销毁时：移除 SessionIndex 与 Session ID 的映射。 */
    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        LOG.debugf("Session destroyed");
        HttpSession session = hse.getSession();
        unmap(session.getId(), session.getAttribute(SamlSession.class.getName()));
    }

    /** Session ID 变更时：先解除旧 ID 映射，再为新 ID 建立映射。 */
    @Override
    public void sessionIdChanged(HttpSessionEvent hse, String oldSessionId) {
        LOG.debugf("Session changed ID from %s", oldSessionId);
        HttpSession session = hse.getSession();
        Object value = session.getAttribute(SamlSession.class.getName());
        unmap(oldSessionId, value);
        map(session.getId(), value);
    }

    /** {@link SamlSession} 属性新增时建立 SSO 映射。 */
    @Override
    public void attributeAdded(HttpSessionBindingEvent hsbe) {
        HttpSession session = hsbe.getSession();
        if (Objects.equals(hsbe.getName(), SamlSession.class.getName())) {
            LOG.debugf("Attribute added");
            map(session.getId(), hsbe.getValue());
        }
    }

    /** {@link SamlSession} 属性移除时解除 SSO 映射。 */
    @Override
    public void attributeRemoved(HttpSessionBindingEvent hsbe) {
        HttpSession session = hsbe.getSession();
        if (Objects.equals(hsbe.getName(), SamlSession.class.getName())) {
            LOG.debugf("Attribute removed");
            unmap(session.getId(), hsbe.getValue());
        }
    }

    /** {@link SamlSession} 属性被替换时：先解除旧值映射，再映射新值。 */
    @Override
    public void attributeReplaced(HttpSessionBindingEvent hsbe) {
        HttpSession session = hsbe.getSession();
        if (Objects.equals(hsbe.getName(), SamlSession.class.getName())) {
            LOG.debugf("Attribute replaced");
            unmap(session.getId(), hsbe.getValue());
            map(session.getId(), session.getAttribute(SamlSession.class.getName()));
        }
    }

    /** 将 SSO SessionIndex 与容器 Session ID 写入映射器。 */
    private void map(String sessionId, Object value) {
        if (! (value instanceof SamlSession) || sessionId == null) {
            return;
        }
        SamlSession account = (SamlSession) value;

        idMapper.map(account.getSessionIndex(), account.getPrincipal().getSamlSubject(), sessionId);
    }

    /** 从映射器中移除指定 Session ID 的 SSO 映射。 */
    private void unmap(String sessionId, Object value) {
        if (! (value instanceof SamlSession) || sessionId == null) {
            return;
        }

        SamlSession samlSession = (SamlSession) value;
        if (samlSession.getSessionIndex() != null) {
            idMapper.removeSession(sessionId);
        }
    }
}
