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

package org.keycloak.sdjwt.consumer;

import org.keycloak.common.VerificationException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 演示要求接口，用于约束所期望凭证的类型与内容。
 *
 * <p>
 * 借鉴 DIF Presentation Exchange 中
 * <a href="https://identity.foundation/presentation-exchange/#presentation-definition">Presentation Definition</a>
 * 的表达力，同时支持更简单的替代方案。
 * </p>
 *
 * @author <a href="mailto:Ingrid.Kamga@adorsys.com">Ingrid Kamga</a>
 */
public interface PresentationRequirements {

    /**
     * 确保已配置的演示要求被当前演示满足。
     *
     * @param disclosedPayload 已完全披露的签发者签名 JWT 载荷
     * @throws VerificationException 未满足配置要求时
     */
    void checkIfSatisfiedBy(JsonNode disclosedPayload) throws VerificationException;
}
