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
package org.keycloak.adapters.spi;

/**
 * 更新 {@link SessionIdMapper} 条目的策略 SPI。
 *
 * <p>集群或外部会话存储场景下，映射的写入方式可能与单机内存直接更新不同；
 * 本接口抽象 {@link #DIRECT} 与 {@link #EXTERNAL} 等更新模式。</p>
 *
 * @author hmlnarik
 */
public interface SessionIdMapperUpdater {
    /**
     * 直接更新 {@link SessionIdMapper} 条目（委托至 mapper 方法）。
     */
    public static final SessionIdMapperUpdater DIRECT = new SessionIdMapperUpdater() {
        @Override public void clear(SessionIdMapper idMapper) {
            idMapper.clear();
        }

        @Override public void map(SessionIdMapper idMapper, String sso, String principal, String httpSessionId) {
            idMapper.map(sso, principal, httpSessionId);
        }

        @Override public void removeSession(SessionIdMapper idMapper, String httpSessionId) {
            idMapper.removeSession(httpSessionId);
        }

        @Override public boolean refreshMapping(SessionIdMapper idMapper, String httpSessionId) {
            return false;
        }
    };

    /**
     * 仅操作 HTTP 会话，不直接更新 {@link SessionIdMapper}；
     * 映射需由 HTTP 会话监听器等其他机制同步维护。
     */
    public static final SessionIdMapperUpdater EXTERNAL = new SessionIdMapperUpdater() {
        @Override public void clear(SessionIdMapper idMapper) { }

        @Override public void map(SessionIdMapper idMapper, String sso, String principal, String httpSessionId) { }

        @Override public void removeSession(SessionIdMapper idMapper, String httpSessionId) { }

        @Override public boolean refreshMapping(SessionIdMapper idMapper, String httpSessionId) { return false; }
    };

    /**
     * 委托至 {@link SessionIdMapper#clear}。
     */
    void clear(SessionIdMapper idMapper);

    /**
     * 委托至 {@link SessionIdMapper#map}。
     * @param idMapper 会话 ID 映射器
     * @param sso SSO 用户会话 ID
     * @param principal 用户主体
     * @param session HTTP 会话 ID
     */
    void map(SessionIdMapper idMapper, String sso, String principal, String session);

    /**
     * 委托至 {@link SessionIdMapper#removeSession}。
     * @param idMapper 会话 ID 映射器
     * @param session HTTP 会话 ID
     */
    void removeSession(SessionIdMapper idMapper, String session);

    /**
     * 从本 updater 的内部数据源刷新映射，并通过 {@link SessionIdMapper#map} 写回。
     * @param idMapper 会话 ID 映射器
     * @param session HTTP 会话 ID
     * @return 若内部源存在该会话且已成功刷新则返回 {@code true}，否则 {@code false}
     */
    boolean refreshMapping(SessionIdMapper idMapper, String httpSessionId);
}
