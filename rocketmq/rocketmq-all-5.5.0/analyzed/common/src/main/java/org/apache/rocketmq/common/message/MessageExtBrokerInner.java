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
package org.apache.rocketmq.common.message;

import com.google.common.base.Strings;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicFilterType;
import org.apache.rocketmq.common.utils.MessageUtils;

/**
 * Broker 内部写入 CommitLog 前的消息：含 properties 字符串、tagsCode、
 * 编码缓存及 {@link MessageVersion} 等 Broker 专用字段。
 */
public class MessageExtBrokerInner extends MessageExt {
    private static final long serialVersionUID = 7256001576878700634L;
    /** 属性键值对序列化字符串（落盘格式）。 */
    private String propertiesString;
    /** 标签 hashCode，用于索引过滤。 */
    private long tagsCode;

    /** 编码后的 ByteBuffer 缓存。 */
    private ByteBuffer encodedBuff;

    /** 是否已完成编码。 */
    private volatile boolean encodeCompleted;

    /** 消息协议版本，默认 V1。 */
    private MessageVersion version = MessageVersion.MESSAGE_VERSION_V1;

    public ByteBuffer getEncodedBuff() {
        return encodedBuff;
    }

    public void setEncodedBuff(ByteBuffer encodedBuff) {
        this.encodedBuff = encodedBuff;
    }

    /** 将 tags 字符串转为 tagsCode（空串为 0）。 */
    public static long tagsString2tagsCode(final TopicFilterType filter, final String tags) {
        if (Strings.isNullOrEmpty(tags)) { return 0; }

        return tags.hashCode();
    }

    public static long tagsString2tagsCode(final String tags) {
        return tagsString2tagsCode(null, tags);
    }

    public String getPropertiesString() {
        return propertiesString;
    }

    public void setPropertiesString(String propertiesString) {
        this.propertiesString = propertiesString;
    }


    /** 删除属性并同步更新 propertiesString。 */
    public void deleteProperty(String name) {
        super.clearProperty(name);
        if (propertiesString != null) {
            this.setPropertiesString(MessageUtils.deleteProperty(propertiesString, name));
        }
    }

    public long getTagsCode() {
        return tagsCode;
    }

    public void setTagsCode(long tagsCode) {
        this.tagsCode = tagsCode;
    }

    public MessageVersion getVersion() {
        return version;
    }

    public void setVersion(MessageVersion version) {
        this.version = version;
    }

    /** 从 propertiesString 中移除 WAIT 以节省存储，properties Map 仍保留 WAIT。 */
    public void removeWaitStorePropertyString() {
        if (this.getProperties().containsKey(MessageConst.PROPERTY_WAIT_STORE_MSG_OK)) {
            // WAIT=true 无需落盘，从 propertiesString 移除以省 9 字节
            // It works for most case. In some cases msgInner.setPropertiesString invoked later and replace it.
            String waitStoreMsgOKValue = this.getProperties().remove(MessageConst.PROPERTY_WAIT_STORE_MSG_OK);
            this.setPropertiesString(MessageDecoder.messageProperties2String(this.getProperties()));
            // 仍写回 properties，后续 isWaitStoreMsgOK() 会读取
            this.getProperties().put(MessageConst.PROPERTY_WAIT_STORE_MSG_OK, waitStoreMsgOKValue);
        } else {
            this.setPropertiesString(MessageDecoder.messageProperties2String(this.getProperties()));
        }
    }

    public boolean isEncodeCompleted() {
        return encodeCompleted;
    }

    public void setEncodeCompleted(boolean encodeCompleted) {
        this.encodeCompleted = encodeCompleted;
    }

    /** 是否需向 LMQ（逻辑多队列）分发。 */
    public boolean needDispatchLMQ() {
        return StringUtils.isNoneBlank(getProperty(MessageConst.PROPERTY_INNER_MULTI_DISPATCH))
            && MixAll.topicAllowsLMQ(getTopic());
    }
}
