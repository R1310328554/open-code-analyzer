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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SD-JWT 披露红名单，列出禁止选择性披露的声明名称（如 {@code iss}、{@code exp} 等元数据声明）。
 */
public class DisclosureRedList {
    /** 默认禁止披露的声明名称字符串列表。 */
    public static final List<String> redList = Collections
            .unmodifiableList(Arrays.asList("iss", "iat", "nbf", "exp", "cnf", "vct", "status"));

    /** 红名单声明名称集合。 */
    private final Set<SdJwtClaimName> redListClaimNames;
    /** 使用默认红名单的静态实例。 */
    public static final DisclosureRedList defaultList = defaultList();

    /** @param redListClaimNames 自定义红名单声明名称集合 */
    public static DisclosureRedList of(Set<SdJwtClaimName> redListClaimNames) {
        return new DisclosureRedList(redListClaimNames);
    }

    private static DisclosureRedList defaultList() {
        return new DisclosureRedList(redList.stream().map(SdJwtClaimName::of).collect(Collectors.toSet()));
    }

    private DisclosureRedList(Set<SdJwtClaimName> redListClaimNames) {
        this.redListClaimNames = Collections.unmodifiableSet(redListClaimNames);
    }

    /** @param claimName 待检查的声明名称 */
    public boolean isRedListedClaimName(SdJwtClaimName claimName) {
        return redListClaimNames.contains(claimName);
    }

    /** @param claimNames 待检查的声明名称集合 */
    public boolean containsRedListedClaimNames(Collection<SdJwtClaimName> claimNames) {
        return !redListClaimNames.isEmpty() && !claimNames.isEmpty()
                && !Collections.disjoint(redListClaimNames, claimNames);
    }
}
