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

package org.apache.rocketmq.broker.filter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.rocketmq.filter.expression.Expression;
import org.apache.rocketmq.filter.util.BloomFilterData;

import java.util.Collections;

/**
 * 消费者过滤元数据：保存订阅表达式、编译结果、Bloom 数据及生命周期时间戳。
 */
public class ConsumerFilterData {

    private String consumerGroup;
    private String topic;
    private String expression;
    private String expressionType;
    private transient Expression compiledExpression;
    private long bornTime;
    private long deadTime = 0;
    private BloomFilterData bloomFilterData;
    private long clientVersion;

    /** 判断过滤器是否已失效（deadTime >= bornTime）。 */
    public boolean isDead() {
        return this.deadTime >= this.bornTime;
    }

    /** 返回失效后经过的毫秒数，仍有效时返回 -1。 */
    public long howLongAfterDeath() {
        if (isDead()) {
            return System.currentTimeMillis() - getDeadTime();
        }
        return -1;
    }

    /** 判断消息存储时间是否晚于过滤器创建时间（即是否应参与位图计算）。 */
    public boolean isMsgInLive(long msgStoreTime) {
        return msgStoreTime > getBornTime();
    }

    /** 返回消费者组名。 */
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /** 设置消费者组名。 */
    public void setConsumerGroup(final String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    /** 返回订阅 Topic。 */
    public String getTopic() {
        return topic;
    }

    /** 设置订阅 Topic。 */
    public void setTopic(final String topic) {
        this.topic = topic;
    }

    /** 返回原始过滤表达式字符串。 */
    public String getExpression() {
        return expression;
    }

    /** 设置过滤表达式。 */
    public void setExpression(final String expression) {
        this.expression = expression;
    }

    /** 返回表达式类型（如 TAG、SQL92）。 */
    public String getExpressionType() {
        return expressionType;
    }

    /** 设置表达式类型。 */
    public void setExpressionType(final String expressionType) {
        this.expressionType = expressionType;
    }

    /** 返回已编译的 {@link Expression}。 */
    public Expression getCompiledExpression() {
        return compiledExpression;
    }

    /** 设置编译后的表达式对象。 */
    public void setCompiledExpression(final Expression compiledExpression) {
        this.compiledExpression = compiledExpression;
    }

    /** 返回过滤器注册时间戳。 */
    public long getBornTime() {
        return bornTime;
    }

    /** 设置注册时间戳。 */
    public void setBornTime(final long bornTime) {
        this.bornTime = bornTime;
    }

    /** 返回失效时间戳（0 表示仍有效）。 */
    public long getDeadTime() {
        return deadTime;
    }

    /** 设置失效时间戳。 */
    public void setDeadTime(final long deadTime) {
        this.deadTime = deadTime;
    }

    /** 返回关联的 Bloom 过滤器数据。 */
    public BloomFilterData getBloomFilterData() {
        return bloomFilterData;
    }

    /** 设置 Bloom 过滤器数据。 */
    public void setBloomFilterData(final BloomFilterData bloomFilterData) {
        this.bloomFilterData = bloomFilterData;
    }

    /** 返回客户端版本号。 */
    public long getClientVersion() {
        return clientVersion;
    }

    /** 设置客户端版本号。 */
    public void setClientVersion(long clientVersion) {
        this.clientVersion = clientVersion;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, Collections.<String>emptyList());
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, Collections.<String>emptyList());
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
