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
package org.apache.rocketmq.common;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONField;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.rocketmq.common.attribute.TopicMessageType;
import org.apache.rocketmq.common.constant.PermName;

import static org.apache.rocketmq.common.TopicAttributes.LITE_EXPIRATION_ATTRIBUTE;
import static org.apache.rocketmq.common.TopicAttributes.TOPIC_MESSAGE_TYPE_ATTRIBUTE;

/**
 * Topic 元数据配置：读写队列数、权限、过滤类型、顺序标志及扩展属性。
 * 支持空格分隔字符串编解码，属性 Map 以 JSON 附加在末尾字段。
 */
public class TopicConfig {
    /** encode/decode 字段分隔符（属性 JSON 内不得含空格）。 */
    private static final String SEPARATOR = " ";
    /** 默认读队列数量。 */
    public static int defaultReadQueueNums = 16;
    /** 默认写队列数量。 */
    public static int defaultWriteQueueNums = 16;
    private static final TypeReference<Map<String, String>> ATTRIBUTES_TYPE_REFERENCE = new TypeReference<Map<String, String>>() {
    };
    /** Topic 名称。 */
    private String topicName;
    /** 读队列数量。 */
    private int readQueueNums = defaultReadQueueNums;
    /** 写队列数量。 */
    private int writeQueueNums = defaultWriteQueueNums;
    /** 权限位（读/写/继承等，见 {@link PermName}）。 */
    private int perm = PermName.PERM_READ | PermName.PERM_WRITE;
    /** Tag 过滤类型（单 Tag / 多 Tag）。 */
    private TopicFilterType topicFilterType = TopicFilterType.SINGLE_TAG;
    /** Topic 系统标志位。 */
    private int topicSysFlag = 0;
    /** 是否为顺序 Topic。 */
    private boolean order = false;
    // 属性键值不得含空格，否则 decode 时 split 会失败
    /** 扩展属性（message.type、lite.topic.expiration 等）。 */
    private Map<String, String> attributes = new HashMap<>();

    public TopicConfig() {
    }

    public TopicConfig(String topicName) {
        this.topicName = topicName;
    }

    public TopicConfig(String topicName, int readQueueNums, int writeQueueNums) {
        this.topicName = topicName;
        this.readQueueNums = readQueueNums;
        this.writeQueueNums = writeQueueNums;
    }

    public TopicConfig(String topicName, int readQueueNums, int writeQueueNums, int perm) {
        this.topicName = topicName;
        this.readQueueNums = readQueueNums;
        this.writeQueueNums = writeQueueNums;
        this.perm = perm;
    }

    public TopicConfig(String topicName, int readQueueNums, int writeQueueNums, int perm, int topicSysFlag) {
        this.topicName = topicName;
        this.readQueueNums = readQueueNums;
        this.writeQueueNums = writeQueueNums;
        this.perm = perm;
        this.topicSysFlag = topicSysFlag;
    }

    public TopicConfig(TopicConfig other) {
        this.topicName = other.topicName;
        this.readQueueNums = other.readQueueNums;
        this.writeQueueNums = other.writeQueueNums;
        this.perm = other.perm;
        this.topicFilterType = other.topicFilterType;
        this.topicSysFlag = other.topicSysFlag;
        this.order = other.order;
        this.attributes = other.attributes;
    }

    /** 编码为「topic read write perm filterType [attributesJson]」空格分隔串。 */
    public String encode() {
        StringBuilder sb = new StringBuilder();
        //[0] topicName
        sb.append(this.topicName);
        sb.append(SEPARATOR);
        //[1] readQueueNums
        sb.append(this.readQueueNums);
        sb.append(SEPARATOR);
        //[2] writeQueueNums
        sb.append(this.writeQueueNums);
        sb.append(SEPARATOR);
        //[3] perm
        sb.append(this.perm);
        sb.append(SEPARATOR);
        //[4] topicFilterType
        sb.append(this.topicFilterType);
        sb.append(SEPARATOR);
        //[5] attributes JSON（可选）
        if (attributes != null) {
            sb.append(JSON.toJSONString(attributes));
        }

        return sb.toString();
    }

    /** 从 encode 字符串解析；至少 5 段，第 6 段为 attributes JSON。 */
    public boolean decode(final String in) {
        String[] strs = in.split(SEPARATOR);
        if (strs.length >= 5) {
            this.topicName = strs[0];

            this.readQueueNums = Integer.parseInt(strs[1]);

            this.writeQueueNums = Integer.parseInt(strs[2]);

            this.perm = Integer.parseInt(strs[3]);

            this.topicFilterType = TopicFilterType.valueOf(strs[4]);

            if (strs.length >= 6) {
                try {
                    this.attributes = JSON.parseObject(strs[5], ATTRIBUTES_TYPE_REFERENCE.getType());
                } catch (Exception e) {
                    // 解析失败时忽略，因旧数据或键值可能含空格导致 JSON 段不完整
                }
            }

            return true;
        }

        return false;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public int getReadQueueNums() {
        return readQueueNums;
    }

    public void setReadQueueNums(int readQueueNums) {
        this.readQueueNums = readQueueNums;
    }

    public int getWriteQueueNums() {
        return writeQueueNums;
    }

    public void setWriteQueueNums(int writeQueueNums) {
        this.writeQueueNums = writeQueueNums;
    }

    public int getPerm() {
        return perm;
    }

    public void setPerm(int perm) {
        this.perm = perm;
    }

    public TopicFilterType getTopicFilterType() {
        return topicFilterType;
    }

    public void setTopicFilterType(TopicFilterType topicFilterType) {
        this.topicFilterType = topicFilterType;
    }

    public int getTopicSysFlag() {
        return topicSysFlag;
    }

    public void setTopicSysFlag(int topicSysFlag) {
        this.topicSysFlag = topicSysFlag;
    }

    public boolean isOrder() {
        return order;
    }

    public void setOrder(boolean isOrder) {
        this.order = isOrder;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /** 从 attributes 读取 {@link TopicMessageType}，缺省为 NORMAL。 */
    @JSONField(serialize = false, deserialize = false)
    public TopicMessageType getTopicMessageType() {
        if (attributes == null) {
            return TopicMessageType.NORMAL;
        }
        String content = attributes.get(TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName());
        if (content == null) {
            return TopicMessageType.NORMAL;
        }
        return TopicMessageType.valueOf(content);
    }

    /** 写入 message.type 属性。 */
    @JSONField(serialize = false, deserialize = false)
    public void setTopicMessageType(TopicMessageType topicMessageType) {
        attributes.put(TOPIC_MESSAGE_TYPE_ATTRIBUTE.getName(), topicMessageType.getValue());
    }

    /** 仅 LITE 类型 Topic 设置 lite.topic.expiration（分钟）。 */
    @JSONField(serialize = false, deserialize = false)
    public void setLiteTopicExpiration(int liteTopicExpiration) {
        if (!TopicMessageType.LITE.equals(getTopicMessageType())) {
            return;
        }
        attributes.put(LITE_EXPIRATION_ATTRIBUTE.getName(), String.valueOf(liteTopicExpiration));
    }

    /** 读取 LITE Topic 过期分钟数，非 LITE 或缺失时返回 -1。 */
    @JSONField(serialize = false, deserialize = false)
    public int getLiteTopicExpiration() {
        if (!TopicMessageType.LITE.equals(getTopicMessageType())) {
            return -1;
        }
        String content = attributes.get(LITE_EXPIRATION_ATTRIBUTE.getName());
        if (content == null) {
            return -1;
        }
        return NumberUtils.toInt(content, -1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TopicConfig that = (TopicConfig) o;

        if (readQueueNums != that.readQueueNums) {
            return false;
        }
        if (writeQueueNums != that.writeQueueNums) {
            return false;
        }
        if (perm != that.perm) {
            return false;
        }
        if (topicSysFlag != that.topicSysFlag) {
            return false;
        }
        if (order != that.order) {
            return false;
        }
        if (!Objects.equals(topicName, that.topicName)) {
            return false;
        }
        if (topicFilterType != that.topicFilterType) {
            return false;
        }
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        int result = topicName != null ? topicName.hashCode() : 0;
        result = 31 * result + readQueueNums;
        result = 31 * result + writeQueueNums;
        result = 31 * result + perm;
        result = 31 * result + (topicFilterType != null ? topicFilterType.hashCode() : 0);
        result = 31 * result + topicSysFlag;
        result = 31 * result + (order ? 1 : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TopicConfig [topicName=" + topicName + ", readQueueNums=" + readQueueNums
            + ", writeQueueNums=" + writeQueueNums + ", perm=" + PermName.perm2String(perm)
            + ", topicFilterType=" + topicFilterType + ", topicSysFlag=" + topicSysFlag + ", order=" + order
            + ", attributes=" + attributes + "]";
    }
}
