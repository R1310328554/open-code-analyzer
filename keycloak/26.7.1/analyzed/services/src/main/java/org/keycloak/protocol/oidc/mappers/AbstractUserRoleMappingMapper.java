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

package org.keycloak.protocol.oidc.mappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;

import static org.keycloak.utils.JsonUtils.splitClaimPath;

/**
 * 用户角色映射协议映射器抽象基类：将用户角色集合写入 ID/Access Token 声明。
 * <p>支持 realm_access/resource_access 特殊路径及嵌套 JSON 声明路径。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public abstract class AbstractUserRoleMappingMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper, TokenIntrospectionTokenMapper {

    @Override
    public int getPriority() {
        return ProtocolMapperUtils.PRIORITY_ROLE_MAPPER;
    }


    /**
     * 将角色名集合写入令牌声明（支持前缀与 ${client_id} 占位符）。
     * @param token 目标令牌
     * @param mappingModel 映射器配置
     * @param rolesToAdd 待写入的角色名集合
     * @param clientId 客户端 ID（用于 resource_access 路径）
     * @param prefix 角色名前缀
     */
    protected static void setClaim(IDToken token, ProtocolMapperModel mappingModel, Set<String> rolesToAdd,
                                   String clientId, String prefix) {

        Set<String> realmRoleNames;
        if (prefix != null && !prefix.isEmpty()) {
            realmRoleNames = rolesToAdd.stream()
                    .map(roleName -> prefix + roleName)
                    .collect(Collectors.toSet());
        } else {
            realmRoleNames = rolesToAdd;
        }

        mapClaim(token, mappingModel, realmRoleNames, clientId);
    }


    /** 声明名中 ${client_id} 占位符模式 */
    protected static final Pattern CLIENT_ID_PATTERN = Pattern.compile(Pattern.quote("${client_id}"));

    private static void mapClaim(IDToken token, ProtocolMapperModel mappingModel, Object attributeValue, String clientId) {
        attributeValue = OIDCAttributeMapperHelper.mapAttributeValue(mappingModel, attributeValue);
        if (attributeValue == null) return;

        String protocolClaim = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);
        if (protocolClaim == null) {
            return;
        }

        if (clientId != null) {
            Matcher matcher = CLIENT_ID_PATTERN.matcher(protocolClaim);
            if (matcher.find()) {
                // clientId 中的点与反斜杠需先转义再写入声明路径
                protocolClaim = matcher.replaceAll(Matcher.quoteReplacement(clientId.replace("\\", "\\\\").replace(".", "\\.")));
            }
        }

        List<String> split = splitClaimPath(protocolClaim);

        // 特殊路径：realm_access / resource_access
        if (checkAccessToken(token, split, attributeValue)) {
            return;
        }

        final int length = split.size();
        int i = 0;
        Map<String, Object> jsonObject = token.getOtherClaims();
        for (String component : split) {
            i++;
            if (i == length) {
                // 合并至已有角色集合
                Object last = jsonObject.get(component);
                if (last instanceof Collection && attributeValue instanceof Collection) {
                    ((Collection) last).addAll((Collection) attributeValue);
                } else {
                    jsonObject.put(component, attributeValue);
                }

            } else {
                Map<String, Object> nested = (Map<String, Object>)jsonObject.get(component);

                if (nested == null) {
                    nested = new HashMap<>();
                    jsonObject.put(component, nested);
                }

                jsonObject = nested;
            }
        }
    }


    // 特殊处理：通过 realm_access / resource_access 结构写入 Access Token
    private static boolean checkAccessToken(IDToken idToken, List<String> path, Object attributeValue) {
        if (!(idToken instanceof AccessToken)) {
            return false;
        }

        if (!(attributeValue instanceof Collection)) {
            return false;
        }

        Collection<String> roles = (Collection<String>) attributeValue;

        AccessToken token = (AccessToken) idToken;
        AccessToken.Access access = null;
        if (path.size() == 2 && "realm_access".equals(path.get(0)) && "roles".equals(path.get(1))) {
            access = token.getRealmAccess();
            if (access == null) {
                access = new AccessToken.Access();
                token.setRealmAccess(access);
            }
        } else if (path.size() == 3 && "resource_access".equals(path.get(0)) && "roles".equals(path.get(2))) {
            String clientId = path.get(1);
            access = token.addAccess(clientId);
        } else {
            return false;
        }

        for (String role : roles) {
            access.addRole(role);
        }
        return true;
    }
}
