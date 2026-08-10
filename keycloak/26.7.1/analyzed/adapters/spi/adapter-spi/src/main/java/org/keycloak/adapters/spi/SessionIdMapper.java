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

package org.keycloak.adapters.spi;

import java.util.Set;

/**
 * SSO 用户会话 ID、主体与本地 HTTP 会话 ID 之间的映射 SPI。
 *
 * <p>适配器通过本接口维护 IdP 侧 SSO 标识与容器 HTTP 会话的对应关系，
 * 以支持单点登出与会话查找。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface SessionIdMapper {
    /**
     * 若映射表中存在给定 HTTP 会话 ID 的条目则返回 {@code true}。
     * @param id HTTP 会话 ID
     * @return 是否存在映射
     */
    boolean hasSession(String id);

    /**
     * 清空本映射器中的全部条目。
     */
    void clear();

    /**
     * 返回指定主体关联的全部 HTTP 会话 ID 集合。
     * @param principal 用户主体
     * @return HTTP 会话 ID 集合，若无则返回 {@code null}
     */
    Set<String> getUserSessions(String principal);

    /**
     * 根据 SSO 用户会话 ID 查找对应的 HTTP 会话 ID。
     * @param sso SSO 用户会话 ID
     * @return 对应的 HTTP 会话 ID，若无则返回 {@code null}
     */
    String getSessionFromSSO(String sso);

    /**
     * 建立 SSO 会话 ID、主体与 HTTP 会话 ID 之间的映射。
     * @param sso SSO 用户会话 ID
     * @param principal 用户主体
     * @param session HTTP 会话 ID
     */
    void map(String sso, String principal, String session);

    /**
     * 移除给定 HTTP 会话 ID 相关的全部映射。
     * @param session HTTP 会话 ID
     */
    void removeSession(String session);
}
