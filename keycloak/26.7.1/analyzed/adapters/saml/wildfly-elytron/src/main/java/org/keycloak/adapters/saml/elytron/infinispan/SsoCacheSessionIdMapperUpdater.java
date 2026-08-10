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
package org.keycloak.adapters.saml.elytron.infinispan;

import org.keycloak.adapters.spi.SessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapperUpdater;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

/**
 * 将会话 ID 映射写入 Infinispan SSO 缓存的 {@link SessionIdMapperUpdater} 抽象基类。
 *
 * <p>键为 HTTP 会话 ID，值为 {@code [SSO 会话 ID, 主体名]} 字符串数组；
 * 写操作同时委托给底层 {@link SessionIdMapperUpdater}。</p>
 *
 * @author hmlnarik
 */
public abstract class SsoCacheSessionIdMapperUpdater implements SessionIdMapperUpdater, AutoCloseable {

    /** 本类日志记录器。 */
    private static final Logger LOG = Logger.getLogger(SsoCacheSessionIdMapperUpdater.class.getName());

    /** 委托的底层映射更新器。 */
    private final SessionIdMapperUpdater delegate;
    /**
     * SSO 缓存：键为 HTTP 会话 ID，值为 (用户 SSO 会话 ID, 主体名) 字符串对。
     */
    private final Cache<String, String[]> httpSessionToSsoCache;

    /**
     * 创建 SSO 缓存映射更新器。
     *
     * @param httpSessionToSsoCache   HTTP 会话到 SSO 信息的缓存
     * @param previousIdMapperUpdater 底层委托更新器
     */
    public SsoCacheSessionIdMapperUpdater(Cache<String, String[]> httpSessionToSsoCache, SessionIdMapperUpdater previousIdMapperUpdater) {
        this.delegate = previousIdMapperUpdater;
        this.httpSessionToSsoCache = httpSessionToSsoCache;
    }

    // SessionIdMapperUpdater 方法

    /** 清空 SSO 缓存并委托底层清除映射。 */
    @Override
    public void clear(SessionIdMapper idMapper) {
        httpSessionToSsoCache.clear();
        this.delegate.clear(idMapper);
    }

    /** 写入 SSO 缓存并委托底层建立映射。 */
    @Override
    public void map(SessionIdMapper idMapper, String sso, String principal, String httpSessionId) {
        LOG.debugf("Adding mapping (%s, %s, %s)", sso, principal, httpSessionId);

        httpSessionToSsoCache.put(httpSessionId, new String[] {sso, principal});
        this.delegate.map(idMapper, sso, principal, httpSessionId);
    }

    /** 从缓存恢复映射并重新委托底层写入；缓存未命中时返回 false。 */
    @Override
    public boolean refreshMapping(SessionIdMapper idMapper, String httpSessionId) {
        LOG.debugf("Refreshing session %s", httpSessionId);

        String[] ssoAndPrincipal = httpSessionToSsoCache.get(httpSessionId);
        if (ssoAndPrincipal != null) {
            this.delegate.map(idMapper, ssoAndPrincipal[0], ssoAndPrincipal[1], httpSessionId);
            return true;
        }
        return false;
    }

    /** 从 SSO 缓存移除条目并委托底层删除会话映射。 */
    @Override
    public void removeSession(SessionIdMapper idMapper, String httpSessionId) {
        LOG.debugf("Removing session %s", httpSessionId);

        httpSessionToSsoCache.remove(httpSessionId);
        this.delegate.removeSession(idMapper, httpSessionId);
    }
}
