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

package org.keycloak.protocol.saml;

import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Pattern;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.saml.preprocessor.SamlAuthenticationPreprocessor;

/**
 * SAML 会话索引工具：在 SessionIndex 与用户/客户端会话之间转换，并提供认证预处理器迭代器。
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SamlSessionUtils {

    private static final String DELIMITER = "::";

    // 预编译分隔符正则以优化性能
    private static final Pattern PATTERN = Pattern.compile(DELIMITER);


    /**
     * 生成 SAML SessionIndex：{@code userSessionId::clientUUID}。
     * @param clientSession 已认证客户端会话
     * @return SessionIndex 字符串
     */
        UserSessionModel userSession = clientSession.getUserSession();
        ClientModel client = clientSession.getClient();

        return userSession.getId() + DELIMITER + client.getId();
    }


    /**
     * 从 SessionIndex 解析并加载对应的客户端会话。
     * @param session Keycloak 会话
     * @param realm 领域模型
     * @param sessionIndex SAML SessionIndex
     * @return 匹配的客户端会话，无效时 null
     */
        if (sessionIndex == null) {
            return null;
        }

        String[] parts = PATTERN.split(sessionIndex);
        if (parts.length != 2) {
            return null;
        }

        String userSessionId = parts[0];
        String clientUUID = parts[1];
        UserSessionModel userSession = session.sessions().getUserSessionIfClientExists(realm, userSessionId, false, clientUUID);
        if (userSession == null) {
            return null;
        }

        return userSession.getAuthenticatedClientSessionByClient(clientUUID);
    }

    /** 返回已注册的 {@link SamlAuthenticationPreprocessor} 迭代器 */
    public static Iterator<SamlAuthenticationPreprocessor> getSamlAuthenticationPreprocessorIterator(KeycloakSession session) {
        return session.getKeycloakSessionFactory().getProviderFactoriesStream(SamlAuthenticationPreprocessor.class)
                .filter(Objects::nonNull)
                .map(SamlAuthenticationPreprocessor.class::cast)
                .iterator();
    }

}
