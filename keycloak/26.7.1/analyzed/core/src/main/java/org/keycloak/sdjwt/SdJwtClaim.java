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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 表示 JWT 载荷中的顶层声明。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public interface SdJwtClaim {

    /** @return 声明名称 */
    public SdJwtClaimName getClaimName();

    /** @return 声明名称字符串形式 */
    public String getClaimNameAsString();

    /**
     * @param hashAlgo 哈希算法
     * @return 在 JWT 中可见的声明值
     */
    public JsonNode getVisibleClaimValue(String hashAlgo);

    /** @return 关联的披露字符串列表 */
    public List<String> getDisclosureStrings();

}
