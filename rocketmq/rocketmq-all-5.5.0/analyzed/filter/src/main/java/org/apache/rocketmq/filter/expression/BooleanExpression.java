/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.filter.expression;

/**
 * 布尔表达式接口：求值结果恒为布尔类型。
 * <p>
 * 源自 ActiveMQ，求值上下文改为 {@link EvaluationContext} 接口。
 * </p>
 *
 * @see org.apache.rocketmq.filter.expression.EvaluationContext
 */
public interface BooleanExpression extends Expression {

    /**
     * 在给定上下文中求值，结果为 {@link Boolean#TRUE} 时返回 true。
     * @param context 表达式求值上下文
     */
    boolean matches(EvaluationContext context) throws Exception;

}
