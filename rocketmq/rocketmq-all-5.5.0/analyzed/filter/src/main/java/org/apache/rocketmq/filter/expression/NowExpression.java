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
 * 当前时间表达式：求值时返回 {@link System#currentTimeMillis()}。
 * <p>主要用于测试场景，常量名为 {@code "now"}。</p>
 */
public class NowExpression extends ConstantExpression {
    /** 构造固定符号 {@code now} 的时间表达式。 */
    /** 构造固定符号 {@code now} 的时间表达式。 */
    public NowExpression() {
        super("now");
    }

    /** 在求值上下文中返回当前毫秒时间戳。 */
    /** 在求值上下文中返回当前毫秒时间戳。 */
    @Override
    public Object evaluate(EvaluationContext context) throws Exception {
        return new Long(System.currentTimeMillis());
    }

    /** 直接获取当前毫秒时间戳，不依赖上下文。 */
    /** 直接获取当前毫秒时间戳，不依赖上下文。 */
    public Object getValue() {
        return new Long(System.currentTimeMillis());
    }
}
