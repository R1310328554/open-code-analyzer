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

package org.apache.rocketmq.client.consumer;

import org.apache.rocketmq.common.filter.ExpressionType;

/**
 * 服务端消息过滤选择器。
 * <p>支持：Tag（{@link org.apache.rocketmq.common.filter.ExpressionType#TAG}）、
 * SQL92（{@link org.apache.rocketmq.common.filter.ExpressionType#SQL92}）。</p>
 */
public class MessageSelector {

    /** 表达式类型，参见 {@link org.apache.rocketmq.common.filter.ExpressionType}。 */
    private String type;

    /** 表达式内容。 */
    private String expression;

    private MessageSelector(String type, String expression) {
        this.type = type;
        this.expression = expression;
    }

    /**
     * 按 SQL92 表达式过滤。
     *
     * @param sql 为 null 或空时表示不过滤
     */
    public static MessageSelector bySql(String sql) {
        return new MessageSelector(ExpressionType.SQL92, sql);
    }

    /**
     * 按 Tag 过滤。
     *
     * @param tag 为 null、空或 "*" 时表示全部
     */
    public static MessageSelector byTag(String tag) {
        return new MessageSelector(ExpressionType.TAG, tag);
    }

    /** 获取表达式类型。 */
    public String getExpressionType() {
        return type;
    }

    /** 获取表达式内容。 */
    public String getExpression() {
        return expression;
    }
}
