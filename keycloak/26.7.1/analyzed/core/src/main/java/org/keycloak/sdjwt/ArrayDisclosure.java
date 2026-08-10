/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.sdjwt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * 顶层数组声明的选择性披露处理器，支持可见元素、未披露元素与诱饵元素的混合编排。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 *
 */
public class ArrayDisclosure extends AbstractSdJwtClaim {
    /** 数组元素列表（可见与未披露）。 */
    private final List<SdJwtArrayElement> elements;
    /** 缓存的可见声明值 JSON 数组。 */
    private JsonNode visibleClaimValue = null;
    /** 诱饵数组元素列表。 */
    private final List<DecoyArrayElement> decoyElements;

    private ArrayDisclosure(SdJwtClaimName claimName, List<SdJwtArrayElement> elements,
            List<DecoyArrayElement> decoyElements) {
        super(claimName);
        this.elements = elements;
        this.decoyElements = decoyElements;
    }

    /**
     * 组装包含可见与不可见元素的数组 JSON 表示。
     *
     * @param hashAlgo 哈希算法名称
     */
    @Override
    public JsonNode getVisibleClaimValue(String hashAlgo) {
        if (visibleClaimValue != null)
            return visibleClaimValue;

        List<JsonNode> visibleElts = new ArrayList<>();
        elements.stream()
                .filter(Objects::nonNull)
                .forEach(e -> visibleElts.add(e.getVisibleValue(hashAlgo)));

        decoyElements.stream()
                .filter(Objects::nonNull)
                .forEach(e -> {
                    if (e.getIndex() < visibleElts.size())
                        visibleElts.add(e.getIndex(), e.getVisibleValue(hashAlgo));
                    else
                        visibleElts.add(e.getVisibleValue(hashAlgo));
                });

        final ArrayNode n = SdJwtUtils.mapper.createArrayNode();
        visibleElts.forEach(n::add);
        visibleClaimValue = n;
        return visibleClaimValue;
    }

    @Override
    public List<String> getDisclosureStrings() {
        final List<String> disclosureStrings = new ArrayList<>();
        elements.stream()
                .filter(Objects::nonNull)
                .forEach(e -> {
                    String disclosureString = e.getDisclosureString();
                    if (disclosureString != null)
                        disclosureStrings.add(disclosureString);
                });
        return disclosureStrings;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ArrayDisclosure)) {
            return false;
        }

        ArrayDisclosure that = (ArrayDisclosure) o;
        return Objects.equals(elements, that.elements) && //
            Objects.equals(visibleClaimValue, that.visibleClaimValue) && //
            Objects.equals(decoyElements, that.decoyElements);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(elements);
        result = 31 * result + Objects.hashCode(visibleClaimValue);
        result = 31 * result + Objects.hashCode(decoyElements);
        return result;
    }

    /** 构建 {@link ArrayDisclosure} 的流式建造者。 */
    public static class Builder {
        private SdJwtClaimName claimName;
        private final List<SdJwtArrayElement> elements = new ArrayList<>();
        private final List<DecoyArrayElement> decoyElements = new ArrayList<>();

        /** @param claimName 数组声明名称 */
        public Builder withClaimName(String claimName) {
            this.claimName = new SdJwtClaimName(claimName);
            return this;
        }

        /** @param elementValue 直接可见的数组元素值 */
        public Builder withVisibleElement(JsonNode elementValue) {
            this.elements.add(new VisibleArrayElement(elementValue));
            return this;
        }

        /** @param salt 盐值，为 {@code null} 时自动生成 */
        public Builder withUndisclosedElement(SdJwtSalt salt, JsonNode elementValue) {
            SdJwtSalt sdJwtSalt = salt == null ? new SdJwtSalt(SdJwtUtils.randomSalt()) : salt;
            this.elements.add(UndisclosedArrayElement.builder()
                    .withSalt(sdJwtSalt)
                    .withArrayElement(elementValue)
                    .build());
            return this;
        }

        /** @param position 诱饵元素插入位置 */
        public void withDecoyElt(Integer position, SdJwtSalt salt) {
            SdJwtSalt sdJwtSalt = salt == null ? new SdJwtSalt(SdJwtUtils.randomSalt()) : salt;
            DecoyArrayElement decoyElement = DecoyArrayElement.builder().withSalt(sdJwtSalt).atIndex(position).build();
            this.decoyElements.add(decoyElement);
        }

        /** @return 构建完成的数组披露对象 */
        public ArrayDisclosure build() {
            return new ArrayDisclosure(claimName, Collections.unmodifiableList(elements),
                    Collections.unmodifiableList(decoyElements));
        }
    }

    /** @return 新的建造者实例 */
    public static Builder builder() {
        return new Builder();
    }
}
