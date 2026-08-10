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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 披露规格管理器，描述 SD-JWT 中未披露声明、诱饵声明及数组元素的选择性披露配置。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 *
 */
public class DisclosureSpec {

    // 未披露声明及其对应盐值（盐值可为 null）
    private final Map<SdJwtClaimName, DisclosureData> undisclosedClaims;

    // 诱饵声明列表，摘要由披露数据（盐值）生成
    private final List<DisclosureData> decoyClaims;

    // 键为声明名称，值为未披露数组元素（索引 → 披露数据）
    private final Map<SdJwtClaimName, Map<Integer, DisclosureData>> undisclosedArrayElts;

    // 键为声明名称，值为诱饵数组元素（索引 → 披露数据）
    private final Map<SdJwtClaimName, Map<Integer, DisclosureData>> decoyArrayElts;

    private DisclosureSpec(Map<SdJwtClaimName, DisclosureData> undisclosedClaims,
                           List<DisclosureData> decoyClaims,
                           Map<SdJwtClaimName, Map<Integer, DisclosureData>> undisclosedArrayElts,
                           Map<SdJwtClaimName, Map<Integer, DisclosureData>> decoyArrayElts) {
        this.undisclosedClaims = undisclosedClaims;
        this.decoyClaims = decoyClaims;
        this.undisclosedArrayElts = undisclosedArrayElts;
        this.decoyArrayElts = decoyArrayElts;
    }

    /** @param arrayClaimName 数组声明名称 */
    public Map<Integer, DisclosureData> getUndisclosedArrayElts(SdJwtClaimName arrayClaimName) {
        return undisclosedArrayElts.get(arrayClaimName);
    }

    /** @param arrayClaimName 数组声明名称 */
    public Map<Integer, DisclosureData> getDecoyArrayElts(SdJwtClaimName arrayClaimName) {
        return decoyArrayElts.get(arrayClaimName);
    }

    /** @return 所有未披露声明的映射 */
    public Map<SdJwtClaimName, DisclosureData> getUndisclosedClaims() {
        return undisclosedClaims;
    }

    /** @return 诱饵声明的披露数据列表 */
    public List<DisclosureData> getDecoyClaims() {
        return decoyClaims;
    }

    /** @param claimName 声明名称，若为未披露声明则返回对应披露数据 */
    public DisclosureData getUndisclosedClaim(SdJwtClaimName claimName) {
        return undisclosedClaims.get(claimName);
    }

    /** @param claimName 声明名称 */
    public boolean hasUndisclosedArrayElts(SdJwtClaimName claimName) {
        return undisclosedArrayElts.containsKey(claimName);
    }

    /** @return 根据披露规格创建的 {@link DecoyClaim} 列表 */
    public List<DecoyClaim> createDecoyClaims() {
        return this.getDecoyClaims().stream()
                   .map(disclosureData -> {
                       return DecoyClaim.builder().withSalt(disclosureData.getSalt()).build();
                   })
                   .collect(Collectors.toList());
    }

    /** 构建 {@link DisclosureSpec} 的流式建造者。 */
    public static class Builder {
        private final Map<SdJwtClaimName, DisclosureData> undisclosedClaims = new HashMap<>();
        private final List<DisclosureData> decoyClaims = new ArrayList<>();
        private final Map<SdJwtClaimName, Map<Integer, DisclosureData>> undisclosedArrayElts = new HashMap<>();
        private final Map<SdJwtClaimName, Map<Integer, DisclosureData>> decoyArrayElts = new HashMap<>();
        private DisclosureRedList redListedClaimNames;

        /** @param claimName 声明名称 */
        public Builder withUndisclosedClaim(String claimName, String salt) {
            this.undisclosedClaims.put(SdJwtClaimName.of(claimName), DisclosureData.of(salt));
            return this;
        }

        /** @param claimName 声明名称，盐值自动生成 */
        public Builder withUndisclosedClaim(String claimName) {
            return withUndisclosedClaim(claimName, null);
        }

        /** @param salt 诱饵声明盐值，可为 {@code null} */
        public Builder withDecoyClaim(String salt) {
            this.decoyClaims.add(DisclosureData.of(salt));
            return this;
        }

        /** @param claimName 数组声明名称 */
        public Builder withUndisclosedArrayElt(String claimName, Integer undisclosedEltIndex, String salt) {
            Map<Integer, DisclosureData> indexes = this.undisclosedArrayElts.computeIfAbsent(
                    SdJwtClaimName.of(claimName),
                    k -> new HashMap<>());
            indexes.put(undisclosedEltIndex, DisclosureData.of(salt));
            return this;
        }

        /** @param claimName 数组声明名称 */
        public Builder withDecoyArrayElt(String claimName, Integer decoyEltIndex, String salt) {
            Map<Integer, DisclosureData> indexes = this.decoyArrayElts.computeIfAbsent(SdJwtClaimName.of(claimName),
                    k -> new HashMap<>());

            indexes.put(decoyEltIndex, DisclosureData.of(salt));
            return this;
        }

        /** @param redListedClaimNames 披露红名单 */
        public Builder withRedListedClaimNames(DisclosureRedList redListedClaimNames) {
            this.redListedClaimNames = redListedClaimNames;
            return this;
        }

        /** @return 构建完成的披露规格，构建时校验红名单约束 */
        public DisclosureSpec build() {
            // 校验红名单
            validateRedList();

            Map<SdJwtClaimName, Map<Integer, DisclosureData>> undisclosedArrayEltMap = new HashMap<>();
            undisclosedArrayElts.forEach((k, v) -> {
                undisclosedArrayEltMap.put(k, Collections.unmodifiableMap((v)));
            });

            Map<SdJwtClaimName, Map<Integer, DisclosureData>> decoyArrayEltMap = new HashMap<>();
            decoyArrayElts.forEach((k, v) -> {
                decoyArrayEltMap.put(k, Collections.unmodifiableMap((v)));
            });

            return new DisclosureSpec(Collections.unmodifiableMap(undisclosedClaims),
                    Collections.unmodifiableList(decoyClaims),
                    Collections.unmodifiableMap(undisclosedArrayEltMap),
                    Collections.unmodifiableMap(decoyArrayEltMap));
        }

        /** 校验未披露与诱饵声明不违反红名单规则。 */
        private void validateRedList() {
            // 未设置时使用默认红名单
            if (redListedClaimNames == null) {
                redListedClaimNames = DisclosureRedList.defaultList;
            }

            // 校验未披露声明
            if (redListedClaimNames.containsRedListedClaimNames(undisclosedClaims.keySet())) {
                throw new IllegalArgumentException("UndisclosedClaims contains red listed claim names");
            }

            // 校验未披露数组声明
            if (redListedClaimNames.containsRedListedClaimNames(undisclosedArrayElts.keySet())) {
                throw new IllegalArgumentException("UndisclosedArrays with red listed claim names");
            }

            // 校验诱饵数组声明
            if (redListedClaimNames.containsRedListedClaimNames(decoyArrayElts.keySet())) {
                throw new IllegalArgumentException("decoyArrayElts contains red listed claim names");
            }
        }
    }

    /** @return 新的建造者实例 */
    public static Builder builder() {
        return new Builder();
    }

    /** 披露数据，封装用于生成披露字符串的盐值。 */
    public static class DisclosureData {
        protected final SdJwtSalt salt;

        public DisclosureData() {
            this.salt = null;
        }

        public DisclosureData(String salt) {
            this.salt = salt == null ? null : SdJwtSalt.of(salt);
        }

        public DisclosureData(SdJwtSalt salt) {
            this.salt = salt;
        }

        /** @param salt 盐值字符串，{@code null} 表示无盐 */
        public static DisclosureData of(String salt) {
            return salt == null ? new DisclosureData() : new DisclosureData(salt);
        }

        /** @return 盐值对象 */
        public SdJwtSalt getSalt() {
            return salt;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof DisclosureData)) {
                return false;
            }

            DisclosureData that = (DisclosureData) o;
            return Objects.equals(salt, that.salt);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(salt);
        }
    }
}
