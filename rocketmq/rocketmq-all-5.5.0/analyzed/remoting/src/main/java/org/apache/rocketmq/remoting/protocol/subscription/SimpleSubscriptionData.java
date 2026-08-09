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

package org.apache.rocketmq.remoting.protocol.subscription;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * 精简订阅数据：描述单个 Topic 的订阅表达式、类型与版本号。
 */
public class SimpleSubscriptionData {
    /** 订阅 Topic 名称。 */
    private String topic;
    /** 过滤表达式类型（如 TAG、SQL92）。 */
    private String expressionType;
    /** 订阅过滤表达式内容。 */
    private String expression;
    /** 订阅数据版本号，用于增量同步。 */
    private long version;

    /** 构造精简订阅数据。 */
    public SimpleSubscriptionData(String topic, String expressionType, String expression, long version) {
        this.topic = topic;
        this.expressionType = expressionType;
        this.expression = expression;
        this.version = version;
    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回表达式类型。 */
    public String getExpressionType() {
        return expressionType;
    }

    /** 设置表达式类型。 */
    public void setExpressionType(String expressionType) {
        this.expressionType = expressionType;
    }

    /** 返回过滤表达式。 */
    public String getExpression() {
        return expression;
    }

    /** 设置过滤表达式。 */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /** 返回订阅版本号。 */
    public long getVersion() {
        return version;
    }

    /** 设置订阅版本号。 */
    public void setVersion(long version) {
        this.version = version;
    }

    /** 按 topic、表达式类型与表达式比较相等性（不含 version）。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleSubscriptionData that = (SimpleSubscriptionData) o;
        return Objects.equals(topic, that.topic) && Objects.equals(expressionType, that.expressionType) && Objects.equals(expression, that.expression);
    }

    /** 计算哈希（不含 version）。 */
    @Override
    public int hashCode() {
        return Objects.hash(topic, expressionType, expression);
    }

    /** 返回调试字符串。 */
    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("topic", topic)
            .add("expressionType", expressionType)
            .add("expression", expression)
            .add("version", version)
            .toString();
    }
}
