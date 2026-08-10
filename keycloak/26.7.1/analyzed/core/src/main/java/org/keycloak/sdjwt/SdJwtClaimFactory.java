/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.sdjwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * {@link SdJwtClaim} 工厂，根据载荷 JSON 与 {@link DisclosureSpec} 解析并构建声明对象。
 *
 * @author Pascal Knueppel
 * @since 13.11.2025
 */
public class SdJwtClaimFactory {

    /**
     * 解析载荷对象节点，为每个属性创建对应的 {@link SdJwtClaim}。
     *
     * @param objectNode 载荷 JSON 对象
     * @param disclosureSpec 披露规格
     * @return 解析后的声明列表
     */
    public static List<SdJwtClaim> parsePayload(ObjectNode objectNode, DisclosureSpec disclosureSpec) {
        List<SdJwtClaim> claims = new ArrayList<>();
        objectNode.properties().forEach(entry -> {
            claims.add(createClaim(entry.getKey(), entry.getValue(), disclosureSpec));
        });
        return claims;
    }

    /** 根据披露规格创建单个声明。 */
    private static SdJwtClaim createClaim(String claimName, JsonNode claimValue, DisclosureSpec disclosureSpec) {
        DisclosureSpec.DisclosureData disclosureData = disclosureSpec.getUndisclosedClaim(SdJwtClaimName.of(claimName));

        if (disclosureData != null) {
            return createUndisclosedClaim(claimName, claimValue, disclosureData.getSalt());
        }
        else {
            return createArrayOrVisibleClaim(claimName, claimValue, disclosureSpec);
        }
    }

    /** 创建未披露的顶层声明。 */
    private static SdJwtClaim createUndisclosedClaim(String claimName, JsonNode claimValue, SdJwtSalt salt) {
        return UndisclosedClaim.builder()
                               .withClaimName(claimName)
                               .withClaimValue(claimValue)
                               .withSalt(salt)
                               .build();
    }

    /** 创建数组披露声明或明文可见声明。 */
    private static SdJwtClaim createArrayOrVisibleClaim(String claimName, JsonNode claimValue, DisclosureSpec disclosureSpec) {
        SdJwtClaimName sdJwtClaimName = SdJwtClaimName.of(claimName);
        Map<Integer, DisclosureSpec.DisclosureData> undisclosedArrayElts = //
            disclosureSpec.getUndisclosedArrayElts(sdJwtClaimName);
        Map<Integer, DisclosureSpec.DisclosureData> decoyArrayElts = disclosureSpec.getDecoyArrayElts(sdJwtClaimName);

        if (undisclosedArrayElts != null || decoyArrayElts != null) {
            return createArrayDisclosure(claimName, claimValue, undisclosedArrayElts, decoyArrayElts);
        }
        else {
            return VisibleSdJwtClaim.builder()
                                    .withClaimName(claimName)
                                    .withClaimValue(claimValue)
                                    .build();
        }
    }

    /** 创建数组选择性披露声明。 */
    private static SdJwtClaim createArrayDisclosure(String claimName, JsonNode claimValue,
                                                    Map<Integer, DisclosureSpec.DisclosureData> undisclosedArrayElts,
                                                    Map<Integer, DisclosureSpec.DisclosureData> decoyArrayElts) {
        ArrayNode arrayNode = validateArrayNode(claimName, claimValue);
        ArrayDisclosure.Builder arrayDisclosureBuilder = ArrayDisclosure.builder().withClaimName(claimName);

        if (undisclosedArrayElts != null) {
            IntStream.range(0, arrayNode.size())
                     .forEach(i -> processArrayElement(arrayDisclosureBuilder, arrayNode.get(i),
                                                       undisclosedArrayElts.get(i)));
        }

        if (decoyArrayElts != null) {
            decoyArrayElts.forEach((key, value) ->
                                       arrayDisclosureBuilder.withDecoyElt(key, value.getSalt()));
        }

        return arrayDisclosureBuilder.build();
    }

    /** 校验声明值为数组类型。 */
    private static ArrayNode validateArrayNode(String claimName, JsonNode claimValue) {
        return Optional.of(claimValue)
                       .filter(v -> v.getNodeType() == JsonNodeType.ARRAY)
                       .map(v -> (ArrayNode) v)
                       .orElseThrow(
                           () -> new IllegalArgumentException("Expected array for claim with name: " + claimName));
    }

    /** 处理单个数组元素，决定明文或选择性披露。 */
    private static void processArrayElement(ArrayDisclosure.Builder builder, JsonNode elementValue,
                                            DisclosureSpec.DisclosureData disclosureData) {
        if (disclosureData != null) {
            builder.withUndisclosedElement(disclosureData.getSalt(), elementValue);
        }
        else {
            builder.withVisibleElement(elementValue);
        }
    }
}
