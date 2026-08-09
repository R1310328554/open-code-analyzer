/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.message;

import java.nio.ByteBuffer;

/**
 * Broker 侧批量扩展消息：body 为多条子消息编码，
 * 支持 inner batch（无需 Broker 再拆包）。
 */
public class MessageExtBatch extends MessageExtBrokerInner {

    private static final long serialVersionUID = -2353110995348498537L;

    /** true 表示 inner batch，Broker 无需再拆包。 */
    /** 是否为 inner batch 模式。 */
    private boolean isInnerBatch = false;

    /** 将消息体 body 包装为 ByteBuffer 视图。 */
    public ByteBuffer wrap() {
        assert getBody() != null;
        return ByteBuffer.wrap(getBody(), 0, getBody().length);
    }

    /** 是否 inner batch。 */
    public boolean isInnerBatch() {
        return isInnerBatch;
    }

    /** 设置 inner batch 标志。 */
    public void setInnerBatch(boolean innerBatch) {
        isInnerBatch = innerBatch;
    }

    /** 预编码后的 ByteBuffer 缓存。 */
    private ByteBuffer encodedBuff;

    public ByteBuffer getEncodedBuff() {
        return encodedBuff;
    }

    public void setEncodedBuff(ByteBuffer encodedBuff) {
        this.encodedBuff = encodedBuff;
    }
}
