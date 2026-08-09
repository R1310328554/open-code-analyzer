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

/**
 * 客户端侧 {@link MessageExt}：{@link #getMsgId()} 优先返回客户端唯一 ID（UNIQ_KEY），
 * 无 UNIQ_KEY 时回退 Broker 偏移 msgId。
 */
public class MessageClientExt extends MessageExt {

    /** 返回 Broker 侧基于 CommitLog 偏移的 msgId。 */
    public String getOffsetMsgId() {
        return super.getMsgId();
    }

    /** 设置 Broker 偏移 msgId。 */
    public void setOffsetMsgId(String offsetMsgId) {
        super.setMsgId(offsetMsgId);
    }

    /** 优先返回 {@link MessageClientIDSetter} 生成的 UNIQ_KEY，否则为 offsetMsgId。 */
    @Override
    public String getMsgId() {
        String uniqID = MessageClientIDSetter.getUniqID(this);
        if (uniqID == null) {
            return this.getOffsetMsgId();
        } else {
            return uniqID;
        }
    }

    /** 客户端 msgId 由 UNIQ_KEY 决定，此方法 intentionally 空实现。 */
    public void setMsgId(String msgId) {
        // 客户端 msgId 由 UNIQ_KEY 属性维护，此处不写入
        //MessageClientIDSetter.setUniqID(this);
    }
}
