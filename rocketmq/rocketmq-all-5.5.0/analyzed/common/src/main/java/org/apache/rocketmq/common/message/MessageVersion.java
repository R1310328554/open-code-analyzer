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

import java.nio.ByteBuffer;

/**
 * CommitLog 消息编码版本：V1 topic 长度 1 字节，V2 为 2 字节 short。
 */
public enum MessageVersion {

    /** V1：magic {@link MessageDecoder#MESSAGE_MAGIC_CODE}，topic 长度 1 字节。 */
    MESSAGE_VERSION_V1(MessageDecoder.MESSAGE_MAGIC_CODE) {
        @Override
        /** V1 topic 长度字段占 1 字节。 */
        /** V2 topic 长度字段占 2 字节。 */
        public int getTopicLengthSize() {
            return 1;
        }

        @Override
        public int getTopicLength(ByteBuffer buffer) {
            return buffer.get();
        }

        @Override
        public int getTopicLength(ByteBuffer buffer, int index) {
            return buffer.get(index);
        }

        @Override
        public void putTopicLength(ByteBuffer buffer, int topicLength) {
            buffer.put((byte) topicLength);
        }
    },

    /** V2：magic {@link MessageDecoder#MESSAGE_MAGIC_CODE_V2}，topic 长度 2 字节。 */
    MESSAGE_VERSION_V2(MessageDecoder.MESSAGE_MAGIC_CODE_V2) {
        @Override
        public int getTopicLengthSize() {
            return 2;
        }

        @Override
        public int getTopicLength(ByteBuffer buffer) {
            return buffer.getShort();
        }

        @Override
        public int getTopicLength(ByteBuffer buffer, int index) {
            return buffer.getShort(index);
        }

        @Override
        public void putTopicLength(ByteBuffer buffer, int topicLength) {
            buffer.putShort((short) topicLength);
        }
    };

    /** 协议 magic 码。 */
    private final int magicCode;

    MessageVersion(int magicCode) {
        this.magicCode = magicCode;
    }

    /** 按 magic 码解析版本，无效时抛 {@link IllegalArgumentException}。 */
    public static MessageVersion valueOfMagicCode(int magicCode) {
        for (MessageVersion version : MessageVersion.values()) {
            if (version.getMagicCode() == magicCode) {
                return version;
            }
        }

        throw new IllegalArgumentException("Invalid magicCode " + magicCode);
    }

    public int getMagicCode() {
        return magicCode;
    }

    /** topic 长度字段字节数。 */
    public abstract int getTopicLengthSize();

    public abstract int getTopicLength(java.nio.ByteBuffer buffer);
    public abstract int getTopicLength(java.nio.ByteBuffer buffer, int index);
    public abstract void putTopicLength(java.nio.ByteBuffer buffer, int topicLength);
}
