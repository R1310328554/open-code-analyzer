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

package org.apache.rocketmq.filter;

import org.apache.rocketmq.common.filter.ExpressionType;
import org.apache.rocketmq.filter.expression.Expression;
import org.apache.rocketmq.filter.expression.MQFilterException;
import org.apache.rocketmq.filter.parser.SelectorParser;

/**
 * SQL92 消息过滤器：封装 {@link org.apache.rocketmq.filter.parser.SelectorParser} 解析逻辑。
 * <p>
 * 请勿直接使用，应通过 {@link FilterFactory#get} 获取过滤器实例。
 * </p>
 */
public class SqlFilter implements FilterSpi {

    @Override
    /** 调用 SelectorParser 将 SQL92 表达式编译为表达式树。 */
    public Expression compile(final String expr) throws MQFilterException {
        return SelectorParser.parse(expr);
    }

    @Override
    /** 返回 {@link ExpressionType#SQL92} 类型标识。 */
    public String ofType() {
        return ExpressionType.SQL92;
    }
}
