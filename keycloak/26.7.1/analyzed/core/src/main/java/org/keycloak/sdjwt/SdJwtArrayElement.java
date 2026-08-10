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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * SD-JWT 数组元素的接口，定义可见值与披露字符串的获取方式。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 *
 */
public interface SdJwtArrayElement {
    /**
     * 返回在签发者签名的 JWT 中作为数组元素可见打印的值。
     *
     * @param hashAlg 哈希算法名称
     */
    public JsonNode getVisibleValue(String hashAlg);

    /** @return Base64Url 编码的披露字符串，无披露时可为 {@code null} */
    public String getDisclosureString();
}
