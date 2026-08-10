/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.ai.importer.operator;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry for AI resource import operators.
 * <p>AI 资源导入操作器注册表，Spring 注入所有 {@link AiResourceOperator} 实现并按 resourceType 索引。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
@Service
public class AiResourceOperatorRegistry {
    
    /** resourceType -> Operator 不可变映射。 */
    private final Map<String, AiResourceOperator> operators;
    
    public AiResourceOperatorRegistry(Collection<AiResourceOperator> operators) {
        Map<String, AiResourceOperator> loadedOperators = new LinkedHashMap<>();
        for (AiResourceOperator each : operators) {
            if (StringUtils.isBlank(each.resourceType())) {
                throw new IllegalStateException("AI resource import operator type is empty.");
            }
            if (loadedOperators.containsKey(each.resourceType())) {
                throw new IllegalStateException(
                    "Duplicate AI resource import operator type: " + each.resourceType());
            }
            loadedOperators.put(each.resourceType(), each);
        }
        this.operators = Collections.unmodifiableMap(loadedOperators);
    }
    
    /**
     * Resolve the operator for a resource type.
     * <p>按资源类型解析操作器，未注册时抛出 NOT_FOUND。</p>
     *
     * @param resourceType resource type
     * @return resource operator
     * @throws NacosException if no operator is registered
     */
    public AiResourceOperator getOperator(String resourceType) throws NacosException {
        AiResourceOperator operator = operators.get(resourceType);
        if (operator == null) {
            throw new NacosApiException(NacosException.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                "AI resource import operator not found: " + resourceType);
        }
        return operator;
    }
    
    /**
     * Whether an operator exists for the resource type.
     * <p>判断指定资源类型是否已注册操作器。</p>
     *
     * @param resourceType resource type
     * @return true if an operator exists
     */
    public boolean hasOperator(String resourceType) {
        return operators.containsKey(resourceType);
    }
}
