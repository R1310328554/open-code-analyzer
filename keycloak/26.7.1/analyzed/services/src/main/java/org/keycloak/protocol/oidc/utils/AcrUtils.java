/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.protocol.oidc.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.keycloak.authentication.authenticators.util.LoAUtil;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.representations.ClaimsRepresentation;
import org.keycloak.representations.IDToken;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jboss.logging.Logger;

/**
 * ACR（Authentication Context Class Reference）工具类：解析 claims/acr_values 参数、执行最低 ACR 约束，并在 ACR 与 LoA（认证保证级别）之间映射。
 */
public class AcrUtils {

    private static final Logger LOGGER = Logger.getLogger(AcrUtils.class);

    /**
     * 从 claims 参数中提取 essential 的 ACR 值列表。
     * @param claimsParam OIDC claims JSON 参数字符串
     * @return 必需的 ACR 值列表
     */
        return getAcrValues(claimsParam, null, true);
    }


    /**
     * 合并 claims 与 acr_values 参数中的 ACR，并在为空时回退到客户端默认值，最后执行最低 ACR 约束。
     * @param claimsParam claims JSON 参数
     * @param acrValuesParam acr_values 请求参数
     * @param client 客户端模型
     * @return 经最低 ACR 过滤后的 ACR 列表
     */
        List<String> acrValues = getAcrValues(claimsParam, acrValuesParam, false);

        if (acrValues.isEmpty()) {
            // 回退到客户端配置的默认 ACR 值
            acrValues = getDefaultAcrValues(client);
        }
        return enforceMinimumAcr(acrValues, client);
    }

    /**
     * 按客户端配置的最低 ACR 过滤列表：移除 LoA 低于最低值的 ACR；若过滤后为空则注入最低 ACR。
     * @param acrValues 待过滤的 ACR 列表
     * @param client 客户端模型
     * @return 满足最低 ACR 要求的列表
     */
        String minimumAcr = getMinimumAcrValue(client);

        // 若配置了最低 ACR，须确保客户端未请求更低级别
        if (minimumAcr != null) {
            List<String> acrCopy = new ArrayList<>(acrValues);
            Map<String, Integer> acrMap = getAcrLoaMap(client);
            Integer minimumLoa = getLoaForAcr(minimumAcr, acrMap, client);
            if (minimumLoa == null) {
                LOGGER.warnf("ACR '%s' can not be mapped to a LoA value.", minimumAcr);
            } else {
                // 移除所有低于最低 LoA 的 ACR
                Iterator<String> iterator = acrCopy.iterator();
                while (iterator.hasNext()) {
                    String acrValue = iterator.next();
                    Integer loa = getLoaForAcr(acrValue, acrMap, client);
                    if (loa == null) {
                        LOGGER.warnf("ACR '%s' can not be mapped to a LoA value.", acrValue);
                        iterator.remove();
                    } else if (loa < minimumLoa) {
                        iterator.remove();
                    }
                }
                // 过滤后若列表为空，则添加最低 ACR
                if (acrCopy.isEmpty()) {
                    acrCopy.add(minimumAcr);
                }
            }
            return acrCopy;
        }
        return acrValues;
    }

    private static Integer getLoaForAcr(String acr, Map<String, Integer> acrMap, ClientModel client) {
        Integer loa = acrMap.get(acr);
        if (loa == null) {
            Optional<Integer> loaFromFlows = LoAUtil.getLoAConfiguredInRealmBrowserFlow(client.getRealm())
                    .filter(l -> acr.equals(String.valueOf(l)))
                    .findFirst();
            if (loaFromFlows.isPresent()) {
                loa = loaFromFlows.get();
            }
        }
        return loa;
    }

    private static List<String> getAcrValues(String claimsParam, String acrValuesParam, boolean essential) {
        List<String> acrValues = new ArrayList<>();
        if (acrValuesParam != null && !essential) {
            acrValues.addAll(Arrays.asList(acrValuesParam.split(" ")));
        }
        if (claimsParam != null) {
            try {
                ClaimsRepresentation claims = JsonSerialization.readValue(claimsParam, ClaimsRepresentation.class);
                if (claims == null) {
                    LOGGER.warnf("Invalid claims parameter. Claims parameter should be JSON");
                } else {
                    ClaimsRepresentation.ClaimValue<String> acrClaim = claims.getClaimValue(IDToken.ACR, ClaimsRepresentation.ClaimContext.ID_TOKEN, String.class);
                    if (acrClaim != null) {
                        if (!essential || acrClaim.isEssential()) {
                            if (acrClaim.getValues() != null) {
                                acrValues.addAll(acrClaim.getValues());
                            } else if (acrClaim.getValue() != null) {
                                acrValues.add(acrClaim.getValue());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Invalid claims parameter", e);
            }
        }
        return acrValues;
    }

    /**
     * @param client 客户端模型
     * @return 对应客户端 "acr-to-loa" 属性的映射；未配置时回退到 realm
     */
    public static Map<String, Integer> getAcrLoaMap(ClientModel client) {
        Map<String, Integer> result = getAcrLoaMapForClientOnly(client);
        if (result.isEmpty()) {
            // 回退到 realm 级映射
            return getAcrLoaMap(client.getRealm());
        } else {
            return result;
        }
    }

    /**
     * 获取 URI 到 LoA 的映射：优先使用客户端配置，否则通过 realm 的 acr/uri 映射组合。
     * @param client 客户端模型
     * @return URI → LoA 映射
     */
        Map<String, Integer> result = getAcrLoaMapForClientOnly(client);
        if (!result.isEmpty()) {
            // 客户端已配置正确的 uri 或 acr 映射
            return result;
        }

        // 回退到 realm，经 acr → uri → loa 两级映射
        Map<String, Integer> acrLoaMap = getAcrLoaMap(client.getRealm());
        Map<String, String> acrUriMap = getAcrUriMap(client.getRealm());
        return acrLoaMap.entrySet().stream()
                .filter(e -> acrUriMap.containsKey(e.getKey()))
                .collect(Collectors.toMap(e -> acrUriMap.get(e.getKey()), Map.Entry::getValue, (a, b) -> Math.max(a, b)));
    }

    private static Map<String, Integer> getAcrLoaMapForClientOnly(ClientModel client) {
        String map = client.getAttribute(Constants.ACR_LOA_MAP);
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return parseAcrLoaMap(map);
        } catch (IOException e) {
            LOGGER.warnf("Invalid client configuration (ACR-LOA map) for client '%s'. Error details: %s", client.getClientId(), e.getMessage());
            return Collections.emptyMap();
        }
    }

    /** 解析 JSON 格式的 ACR→LoA 映射字符串 */
    public static Map<String, Integer> parseAcrLoaMap(String map) throws IOException {
        return JsonSerialization.readValue(map, new TypeReference<Map<String, Integer>>() {});
    }

    /** 解析 JSON 格式的 ACR→URI 映射字符串 */
    public static Map<String, String> parseAcrUriMap(String map) throws IOException {
        return JsonSerialization.readValue(map, new TypeReference<Map<String, String>>() {});
    }

    /**
     * @param realm 领域模型
     * @return 对应 realm "acr-to-loa" 属性的映射
     */
    public static Map<String, Integer> getAcrLoaMap(RealmModel realm) {
        String map = realm.getAttribute(Constants.ACR_LOA_MAP);
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return parseAcrLoaMap(map);
        } catch (IOException e) {
            LOGGER.warnf("Invalid realm configuration (ACR-LOA map). Details: %s", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 返回 realm 中的 ACR 到 URI 映射。
     * @param realm 领域模型
     * @return ACR → URI 映射
     */
    public static Map<String, String> getAcrUriMap(RealmModel realm) {
        String map = realm.getAttribute(Constants.ACR_URI_MAP);
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return parseAcrUriMap(map);
        } catch (IOException e) {
            LOGGER.warnf("Invalid realm configuration (ACR-URI map). Details: %s", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 将给定 LoA 映射到 acrValues 中 LoA 不超过 loa 的最大 ACR 值。
     * @param loa 目标认证保证级别
     * @param acrLoaMap ACR→LoA 映射
     * @param acrValues 候选 ACR 值集合
     * @return 匹配的 ACR，若无则 null
     */
        String acr = null;
        if (!acrLoaMap.isEmpty() && !acrValues.isEmpty()) {
            int maxLoa = -1;
            for (String acrValue : acrValues) {
                Integer mappedLoa = acrLoaMap.get(acrValue);
                // 若无映射，acrValue 本身可能是整数 LoA
                if (mappedLoa == null) {
                    try {
                        mappedLoa = Integer.parseInt(acrValue);
                    } catch (NumberFormatException e) {
                        // 无法将 acrValue 映射为整数
                        LOGGER.warnf("Acr value '%s' cannot be mapped to int", acrValue);
                    }
                }
                if (mappedLoa != null && mappedLoa > maxLoa && loa >= mappedLoa) {
                    acr = acrValue;
                    maxLoa = mappedLoa;
                }
            }
        }
        return acr;
    }


    /** 获取客户端配置的默认 ACR 值列表 */
    public static List<String> getDefaultAcrValues(ClientModel client) {
        return OIDCAdvancedConfigWrapper.fromClientModel(client).getAttributeMultivalued(Constants.DEFAULT_ACR_VALUES);
    }

    /** 获取客户端配置的最低 ACR 值 */
    public static String getMinimumAcrValue(ClientModel client) {
        return OIDCAdvancedConfigWrapper.fromClientModel(client).getMinimumAcrValue();
    }
}
