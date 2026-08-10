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

package org.keycloak.sessions;

import java.util.regex.Pattern;

/**
 * 认证会话复合 ID：编码/解码根会话 ID、浏览器标签页 ID 与客户端 UUID，用于唯一定位 {@link AuthenticationSessionModel}。
 *
 * Allow to encode compound string to fully lookup authenticationSessionModel
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthenticationSessionCompoundId {

    // 点号分隔符，用于拆分复合 ID
    private static final Pattern DOT = Pattern.compile("\\.");

    /** 从认证会话模型构建复合 ID。 */
    public static AuthenticationSessionCompoundId fromAuthSession(AuthenticationSessionModel authSession) {
        return decoded(authSession.getParentSession().getId(), authSession.getTabId(), authSession.getClient().getId());
    }

    /** 由三部分明文组装并编码为复合 ID。 */
    public static AuthenticationSessionCompoundId decoded(String rootAuthSessionId, String tabId, String clientUUID) {
        String encodedId = rootAuthSessionId + "." + tabId + "." + clientUUID;
        return new AuthenticationSessionCompoundId(rootAuthSessionId, tabId, clientUUID, encodedId);
    }

    /** 解析已编码的复合 ID 字符串。 */
    public static AuthenticationSessionCompoundId encoded(String encodedId) {
        String[] decoded = DOT.split(encodedId, 3);

        String rootAuthSessionId =(decoded.length > 0) ? decoded[0] : null;
        String tabId = (decoded.length > 1) ? decoded[1] : null;
        String clientUUID = (decoded.length > 2) ? decoded[2] : null;

        return new AuthenticationSessionCompoundId(rootAuthSessionId, tabId, clientUUID, encodedId);
    }



    private final String rootSessionId;
    private final String tabId;
    private final String clientUUID;
    private final String encodedId;

    /** @param rootSessionId 根认证会话 ID
     * @param tabId 浏览器标签页 ID
     * @param clientUUID 客户端 UUID
     * @param encodedId 完整编码字符串 */
    public AuthenticationSessionCompoundId(String rootSessionId, String tabId, String clientUUID, String encodedId) {
        this.rootSessionId = rootSessionId;
        this.tabId = tabId;
        this.clientUUID = clientUUID;
        this.encodedId = encodedId;
    }

    /** @return 根认证会话 ID */
    public String getRootSessionId() {
        return rootSessionId;
    }

    /** @return 浏览器标签页 ID */
    public String getTabId() {
        return tabId;
    }

    /** @return 客户端 UUID */
    public String getClientUUID() {
        return clientUUID;
    }

    /** @return 完整编码的复合 ID */
    public String getEncodedId() {
        return encodedId;
    }
}
