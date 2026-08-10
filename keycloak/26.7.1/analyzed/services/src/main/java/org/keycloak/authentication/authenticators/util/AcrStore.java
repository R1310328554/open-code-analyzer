/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.authentication.authenticators.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.keycloak.authentication.AuthenticatorUtil;
import org.keycloak.authentication.CredentialAction;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.Time;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

import static org.keycloak.models.Constants.NO_LOA;

/**
 * 认证会话中与阶梯认证（Step-up Authentication）相关的 LoA 数据读写存储。
 * <p>管理请求的认证级别、已认证级别及其有效期映射。</p>
 * CRUD data in the authentication session, which are related to step-up authentication
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AcrStore {

    private static final Logger logger = Logger.getLogger(AcrStore.class);

    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 当前认证会话。 */
    private final AuthenticationSessionModel authSession;

    /** @param session Keycloak 会话 @param authSession 认证会话 */
    public AcrStore(KeycloakSession session, AuthenticationSessionModel authSession) {
        this.session = session;
        this.authSession = authSession;
    }


    /** @return 客户端是否强制要求重新认证 LoA */
    public boolean isLevelOfAuthenticationForced() {
        return Boolean.parseBoolean(authSession.getClientNote(Constants.FORCE_LEVEL_OF_AUTHENTICATION));
    }


    /** 取客户端与 kc_action 请求 LoA 中的较高值。 */
    public int getRequestedLevelOfAuthentication(AuthenticationFlowModel executionModel) {
        String requiredLoa = authSession.getClientNote(Constants.REQUESTED_LEVEL_OF_AUTHENTICATION);
        int requestedLoaByClient = requiredLoa == null ? NO_LOA : Integer.parseInt(requiredLoa);
        int requestedLoaByKcAction = getRequestedLevelOfAuthenticationByKcAction(executionModel);
        logger.tracef("Level requested by client: %d, level requested by kc_action parameter: %d", requestedLoaByClient, requestedLoaByKcAction);
        return Math.max(requestedLoaByClient, requestedLoaByKcAction);
    }

    /** 从 kc_action 凭证操作推断请求的 LoA 级别。 */
    private int getRequestedLevelOfAuthenticationByKcAction(AuthenticationFlowModel topLevelFlow) {
        RealmModel realm = authSession.getRealm();
        UserModel user = authSession.getAuthenticatedUser();
        String kcAction = authSession.getClientNote(Constants.KC_ACTION);
        if (user != null && kcAction != null) {
            RequiredActionProvider reqAction = session.getProvider(RequiredActionProvider.class, kcAction);
            if (reqAction instanceof CredentialAction) {
                String credentialType = ((CredentialAction) reqAction).getCredentialType(session, authSession);
                if (credentialType != null) {
                    Map<String, Integer> credentialTypesToLoa = LoAUtil.getCredentialTypesToLoAMap(session, realm, topLevelFlow);

                    Integer credentialTypeLevel = credentialTypesToLoa.get(credentialType);
                    if (credentialTypeLevel != null) {
                        // 若用户尚未配置对应凭证类型，则不要求更高 LoA（如无 OTP 时不强制 level2）
                        MultivaluedHashMap<Integer, String> loaToCredentialTypes = reverse(credentialTypesToLoa);
                        return getHighestLevelAvailableForUser(user, loaToCredentialTypes, credentialTypeLevel);
                    }
                }
            }
        }
        return NO_LOA;
    }

    private MultivaluedHashMap<Integer, String> reverse(Map<String, Integer> orig) {
        MultivaluedHashMap<Integer, String> reverse = new MultivaluedHashMap<>();
        orig.forEach((key, value) -> reverse.add(value, key));
        return reverse;
    }

    private Integer getHighestLevelAvailableForUser(UserModel user, MultivaluedHashMap<Integer, String> loaToCredentialTypes, int levelToTry) {
        if (levelToTry <= NO_LOA) return levelToTry;

        List<String> currentLevelCredentialTypes = loaToCredentialTypes.get(levelToTry);
        if (currentLevelCredentialTypes == null || currentLevelCredentialTypes.isEmpty()) {
            // No credentials required for authentication on this level
            return levelToTry;
        }

        boolean hasCredentialOfLevel = user.credentialManager().getStoredCredentialsStream()
                .anyMatch(credentialModel -> currentLevelCredentialTypes.contains(credentialModel.getType()));
        if (hasCredentialOfLevel) {
            logger.tracef("User %s has credential of level %d available", user.getUsername(), levelToTry);
            return levelToTry;
        } else {
            // 回退到更低 LoA 级别
            return getHighestLevelAvailableForUser(user, loaToCredentialTypes, levelToTry - 1);
        }
    }

    /** @return 当前认证是否已满足请求的 LoA */
    public boolean isLevelOfAuthenticationSatisfiedFromCurrentAuthentication(AuthenticationFlowModel topFlow) {
        return getRequestedLevelOfAuthentication(topFlow)
                <= getAuthenticatedLevelCurrentAuthentication();
    }


    /** @return 客户端会话 note 中记录的当前 LoA */
    public static int getCurrentLevelOfAuthentication(AuthenticatedClientSessionModel clientSession) {
        String clientSessionLoaNote = clientSession.getNote(Constants.LEVEL_OF_AUTHENTICATION);
        return clientSessionLoaNote == null ? NO_LOA : Integer.parseInt(clientSessionLoaNote);
    }


    /**
     * @param level 认证级别
     * @param maxAge 该级别视为有效的最大时长（秒）
     * @return 该级别在先前认证中已完成且仍在 maxAge 有效期内
     */
    public boolean isLevelAuthenticatedInPreviousAuth(int level, int maxAge) {
        // 客户端强制重新认证（如 prompt=login、max_age=0）时不复用先前 LoA
        // considered. User needs to re-authenticate all requested levels again.
        if (AuthenticatorUtil.isForcedReauthentication(authSession)) return false;

        Map<Integer, Integer> levels = getCurrentAuthenticatedLevelsMap();
        if (levels == null) return false;

        Integer levelAuthTime = levels.get(level);
        if (levelAuthTime == null) return false;

        int currentTime = Time.currentTime();
        return levelAuthTime + maxAge >= currentTime;
    }


    /**
     * 返回当前认证中已达成或可复用的 LoA 级别。
     *
     *
     * @return see above
     */
    public int getLevelOfAuthenticationFromCurrentAuthentication() {
        String authSessionLoaNote = authSession.getAuthNote(Constants.LEVEL_OF_AUTHENTICATION);
        return authSessionLoaNote == null ? NO_LOA : Integer.parseInt(authSessionLoaNote);
    }


    /**
     * 将已认证 LoA 写入当前认证会话及 LoA 映射（供后续认证复用）
     *
     * @param level level to save
     */
    public void setLevelAuthenticated(int level) {
        setLevelAuthenticatedToCurrentRequest(level);
        setLevelAuthenticatedToMap(level);
    }

    /**
     * 将 LoA 写入当前认证会话 auth note
     *
     * @param level, which was authenticated by user
     */
    public void setLevelAuthenticatedToCurrentRequest(int level) {
        authSession.setAuthNote(Constants.LEVEL_OF_AUTHENTICATION, String.valueOf(level));
    }

    /**
     * 若认证流程 LoA 高于当前值，则更新当前认证会话 LoA
     */
    public void setAuthFlowLevelAuthenticatedToCurrentRequest() {
        if (authSession.getAuthNote(Constants.AUTHENTICATION_FLOW_LEVEL_OF_AUTHENTICATION) != null) {
            int authFlowLoa = Integer.parseInt(authSession.getAuthNote(Constants.AUTHENTICATION_FLOW_LEVEL_OF_AUTHENTICATION));
            if (getLevelOfAuthenticationFromCurrentAuthentication() < authFlowLoa) {
                setLevelAuthenticatedToCurrentRequest(authFlowLoa);
            }
        }
    }

    private void setLevelAuthenticatedToMap(int level) {
        Map<Integer, Integer> levels = getCurrentAuthenticatedLevelsMap();
        if (levels == null) levels = new HashMap<>();

        levels.put(level, Time.currentTime());

        saveCurrentAuthenticatedLevelsMap(levels);
    }


    private int getAuthenticatedLevelCurrentAuthentication() {
        String authSessionLoaNote = authSession.getAuthNote(Constants.LEVEL_OF_AUTHENTICATION);
        return authSessionLoaNote == null ? NO_LOA : Integer.parseInt(authSessionLoaNote);
    }

    /**
     * @return 先前认证中仍在有效期内的最高 LoA
     */
    public int getHighestAuthenticatedLevelFromPreviousAuthentication(String flowId) {
        // 无 LoA 映射：用户在本会话中尚未完成认证
        Map<Integer, Integer> levels = getCurrentAuthenticatedLevelsMap();
        if (levels == null || levels.isEmpty()) return NO_LOA;

        // 已有映射表示至少 SSO 认证，最低 LoA 为 0
        int maxLevel = Constants.MINIMUM_LOA;
        int currentTime = Time.currentTime();

        Map<Integer, Integer> configuredMaxAges = LoAUtil.getLoaMaxAgesConfiguredInRealmFlow(authSession.getRealm(), flowId);
        levels = new TreeMap<>(levels);

        for (Map.Entry<Integer, Integer> entry : levels.entrySet()) {
            Integer levelMaxAge = configuredMaxAges.get(entry.getKey());
            if (levelMaxAge == null) {
                logger.warnf("No condition found for level '%d' in the authentication flow", entry.getKey());
                levelMaxAge = 0;
            }
            int levelAuthTime = entry.getValue();
            int levelExpiration = levelAuthTime + levelMaxAge;
            if (currentTime <= levelExpiration) {
                maxLevel = entry.getKey();
            } else {
                break;
            }
        }

        logger.tracef("Highest authenticated level from previous authentication of client '%s' in authentication '%s' was: %d",
                authSession.getClient().getClientId(), authSession.getParentSession().getId(), maxLevel);
        return maxLevel;
    }

    // 键为 LoA 级别，值为该级别认证时间戳
    private Map<Integer, Integer> getCurrentAuthenticatedLevelsMap() {
        String loaMap = authSession.getAuthNote(Constants.LOA_MAP);
        if (loaMap == null) {
            return null;
        }
        try {
            return JsonSerialization.readValue(loaMap, new TypeReference<Map<Integer, Integer>>() {});
        } catch (IOException e) {
            logger.warnf("Invalid format of the LoA map. Saved value was: %s", loaMap);
            throw new IllegalStateException(e);
        }
    }

    private void saveCurrentAuthenticatedLevelsMap(Map<Integer, Integer> levelInfoMap) {
        try {
            String note = JsonSerialization.writeValueAsString(levelInfoMap);
            authSession.setAuthNote(Constants.LOA_MAP, note);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
