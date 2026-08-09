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

/**
 * $Id: SubscriptionData.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */
package org.apache.rocketmq.remoting.protocol.heartbeat;

import com.alibaba.fastjson2.annotation.JSONField;
import org.apache.rocketmq.common.filter.ExpressionType;

import java.util.HashSet;
import java.util.Set;

/**
 * 订阅数据：描述 Topic、过滤表达式、Tag/SQL 集合及订阅版本。
 */
public class SubscriptionData implements Comparable<SubscriptionData> {
    /** 订阅全部 Tag 的通配符常量。 */
    public final static String SUB_ALL = "*";
    /** 是否启用类过滤模式。 */
    private boolean classFilterMode = false;
    /** 订阅 Topic 名称。 */
    private String topic;
    /** 订阅表达式串（Tag 或 SQL92）。 */
    private String subString;
    /** 解析后的 Tag 集合。 */
    private Set<String> tagsSet = new HashSet<>();
    /** Tag 哈希码集合（加速匹配）。 */
    private Set<Integer> codeSet = new HashSet<>();
    /** 订阅版本号（变更时递增）。 */
    private long subVersion = System.currentTimeMillis();
    /** 表达式类型（Tag/SQL92 等）。 */
    private String expressionType = ExpressionType.TAG;

    /** 类过滤源码（不参与 JSON 序列化）。 */
    @JSONField(serialize = false)
    private String filterClassSource;

    /** 默认构造。 */
    public SubscriptionData() {

    }

    /** 指定 Topic 与订阅串的构造。 */
    public SubscriptionData(String topic, String subString) {
        super();
        this.topic = topic;
        this.subString = subString;
    }

    /** 返回类过滤源码。 */
    public String getFilterClassSource() {
        return filterClassSource;
    }

    /** 设置类过滤源码。 */
    public void setFilterClassSource(String filterClassSource) {
        this.filterClassSource = filterClassSource;
    }

    /** 返回 Topic 名称。 */
    public String getTopic() {
        return topic;
    }

    /** 设置 Topic 名称。 */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /** 返回订阅表达式串。 */
    public String getSubString() {
        return subString;
    }

    /** 设置订阅表达式串。 */
    public void setSubString(String subString) {
        this.subString = subString;
    }

    /** 返回 Tag 集合。 */
    public Set<String> getTagsSet() {
        return tagsSet;
    }

    /** 设置 Tag 集合。 */
    public void setTagsSet(Set<String> tagsSet) {
        this.tagsSet = tagsSet;
    }

    /** 返回订阅版本号。 */
    public long getSubVersion() {
        return subVersion;
    }

    /** 设置订阅版本号。 */
    public void setSubVersion(long subVersion) {
        this.subVersion = subVersion;
    }

    /** 返回 Tag 哈希码集合。 */
    public Set<Integer> getCodeSet() {
        return codeSet;
    }

    /** 设置 Tag 哈希码集合。 */
    public void setCodeSet(Set<Integer> codeSet) {
        this.codeSet = codeSet;
    }

    /** 返回是否类过滤模式。 */
    public boolean isClassFilterMode() {
        return classFilterMode;
    }

    /** 设置类过滤模式标志。 */
    public void setClassFilterMode(boolean classFilterMode) {
        this.classFilterMode = classFilterMode;
    }

    /** 返回表达式类型。 */
    public String getExpressionType() {
        return expressionType;
    }

    /** 设置表达式类型。 */
    public void setExpressionType(String expressionType) {
        this.expressionType = expressionType;
    }

    /** 基于 Topic、表达式与 Tag 集合计算哈希码。 */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (classFilterMode ? 1231 : 1237);
        result = prime * result + ((codeSet == null) ? 0 : codeSet.hashCode());
        result = prime * result + ((subString == null) ? 0 : subString.hashCode());
        result = prime * result + ((tagsSet == null) ? 0 : tagsSet.hashCode());
        result = prime * result + ((topic == null) ? 0 : topic.hashCode());
        result = prime * result + ((expressionType == null) ? 0 : expressionType.hashCode());
        return result;
    }

    /** 比较订阅 Topic、表达式、版本与 Tag 集合是否相等。 */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SubscriptionData other = (SubscriptionData) obj;
        if (classFilterMode != other.classFilterMode)
            return false;
        if (codeSet == null) {
            if (other.codeSet != null)
                return false;
        } else if (!codeSet.equals(other.codeSet))
            return false;
        if (subString == null) {
            if (other.subString != null)
                return false;
        } else if (!subString.equals(other.subString))
            return false;
        if (subVersion != other.subVersion)
            return false;
        if (tagsSet == null) {
            if (other.tagsSet != null)
                return false;
        } else if (!tagsSet.equals(other.tagsSet))
            return false;
        if (topic == null) {
            if (other.topic != null)
                return false;
        } else if (!topic.equals(other.topic))
            return false;
        if (expressionType == null) {
            if (other.expressionType != null)
                return false;
        } else if (!expressionType.equals(other.expressionType))
            return false;
        return true;
    }

    /** 返回含 Topic、表达式与 Tag 集合的调试字符串。 */
    @Override
    public String toString() {
        return "SubscriptionData [classFilterMode=" + classFilterMode + ", topic=" + topic + ", subString="
            + subString + ", tagsSet=" + tagsSet + ", codeSet=" + codeSet + ", subVersion=" + subVersion
            + ", expressionType=" + expressionType + "]";
    }

    /** 按 topic@subString 字典序比较。 */
    @Override
    public int compareTo(SubscriptionData other) {
        String thisValue = this.topic + "@" + this.subString;
        String otherValue = other.topic + "@" + other.subString;
        return thisValue.compareTo(otherValue);
    }
}
