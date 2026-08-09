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
package org.apache.rocketmq.tieredstore.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageDecoder;
import org.apache.rocketmq.tieredstore.common.SelectBufferResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息格式工具：解析 CommitLog/ConsumeQueue 字段并按 CQ 切分消息缓冲。
 */
public class MessageFormatUtil {

    private static final Logger log = LoggerFactory.getLogger(MessageStoreUtil.TIERED_STORE_LOGGER_NAME);

    /** MsgId 长度：storeHost(8) + offset(8)。 */
    public static final int MSG_ID_LENGTH = 8 + 8;
    /** 魔数字段在消息头中的偏移。 */
    public static final int MAGIC_CODE_POSITION = 4;
    /** 队列逻辑偏移字段偏移。 */
    public static final int QUEUE_OFFSET_POSITION = 20;
    /** CommitLog 物理偏移字段偏移。 */
    public static final int PHYSICAL_OFFSET_POSITION = 28;
    /** 系统标志字段起始偏移（物理偏移字段结束位置）。 */
    public static final int SYS_FLAG_OFFSET_POSITION = 36;
    /** 存储时间戳字段偏移。 */
    public static final int STORE_TIMESTAMP_POSITION = 56;
    /** storeHost 字段偏移。 */
    public static final int STORE_HOST_POSITION = 64;

    /**
     * CommitLog coda 大小：itemSize(4) + magicCode(4) + maxStoreTimestamp(8)。
     */
    /** CommitLog 段尾 coda 字节长度。 */
    public static final int COMMIT_LOG_CODA_SIZE = 4 + 8 + 4;
    /** 空白消息魔数，用于跳过 coda 占位。 */
    public static final int BLANK_MAGIC_CODE = 0xBBCCDDEE ^ 1880681586 + 8;

    /**
     * ConsumeQueue 单元大小：commitLogOffset(8) + size(4) + tagHash(8)。
     */
    /** 单条 ConsumeQueue 索引条目字节长度。 */
    public static final int CONSUME_QUEUE_UNIT_SIZE = 8 + 4 + 8;

    /** 读取消息总大小（含头部）。 */
    public static int getTotalSize(ByteBuffer message) {
        return message.getInt(message.position());
    }

    public static int getMagicCode(ByteBuffer message) {
        return message.getInt(message.position() + MAGIC_CODE_POSITION);
    }

    public static long getQueueOffset(ByteBuffer message) {
        return message.getLong(message.position() + QUEUE_OFFSET_POSITION);
    }

    /** 读取消息 CommitLog 物理偏移。 */
    public static long getCommitLogOffset(ByteBuffer message) {
        return message.getLong(message.position() + PHYSICAL_OFFSET_POSITION);
    }

    public static long getStoreTimeStamp(ByteBuffer message) {
        return message.getLong(message.position() + STORE_TIMESTAMP_POSITION);
    }

    public static ByteBuffer getOffsetIdBuffer(ByteBuffer message) {
        ByteBuffer buffer = ByteBuffer.allocate(MSG_ID_LENGTH);
        buffer.putLong(message.getLong(message.position() + STORE_HOST_POSITION));
        buffer.putLong(getCommitLogOffset(message));
        buffer.flip();
        return buffer;
    }

    public static String getOffsetId(ByteBuffer message) {
        return UtilAll.bytes2string(getOffsetIdBuffer(message).array());
    }

    public static Map<String, String> getProperties(ByteBuffer message) {
        return MessageDecoder.decodeProperties(message.slice());
    }

    public static long getCommitLogOffsetFromItem(ByteBuffer cqItem) {
        return cqItem.getLong(cqItem.position());
    }

    public static int getSizeFromItem(ByteBuffer cqItem) {
        return cqItem.getInt(cqItem.position() + 8);
    }

    public static long getTagCodeFromItem(ByteBuffer cqItem) {
        return cqItem.getLong(cqItem.position() + 12);
    }

    /** 按 ConsumeQueue 缓冲切分 CommitLog 消息并校验魔数与长度。 */
    public static List<SelectBufferResult> splitMessageBuffer(ByteBuffer cqBuffer, ByteBuffer msgBuffer) {

        if (cqBuffer == null || msgBuffer == null) {
            log.error("MessageFormatUtil split buffer error, cq buffer or msg buffer is null");
            return new ArrayList<>();
        }

        cqBuffer.rewind();
        msgBuffer.rewind();

        List<SelectBufferResult> bufferResultList = new ArrayList<>(
            cqBuffer.remaining() / CONSUME_QUEUE_UNIT_SIZE);

        if (msgBuffer.remaining() == 0) {
            log.error("MessageFormatUtil split buffer error, msg buffer length is 0");
            return bufferResultList;
        }

        if (cqBuffer.remaining() == 0 || cqBuffer.remaining() % CONSUME_QUEUE_UNIT_SIZE != 0) {
            log.error("MessageFormatUtil split buffer error, cq buffer size is {}", cqBuffer.remaining());
            return bufferResultList;
        }

        try {
            long firstCommitLogOffset = MessageFormatUtil.getCommitLogOffsetFromItem(cqBuffer);

            for (int position = cqBuffer.position(); position < cqBuffer.limit();
                position += CONSUME_QUEUE_UNIT_SIZE) {

                cqBuffer.position(position);
                long logOffset = MessageFormatUtil.getCommitLogOffsetFromItem(cqBuffer);
                int bufferSize = MessageFormatUtil.getSizeFromItem(cqBuffer);
                long tagCode = MessageFormatUtil.getTagCodeFromItem(cqBuffer);

                int offset = (int) (logOffset - firstCommitLogOffset);
                if (offset + bufferSize > msgBuffer.limit()) {
                    log.error("MessageFormatUtil split buffer error, message buffer offset exceeded limit. " +
                        "Expect length: {}, Actual length: {}", offset + bufferSize, msgBuffer.limit());
                    break;
                }

                msgBuffer.position(offset);
                int magicCode = getMagicCode(msgBuffer);
                if (magicCode == BLANK_MAGIC_CODE) {
                    offset += COMMIT_LOG_CODA_SIZE;
                    msgBuffer.position(offset);
                    magicCode = getMagicCode(msgBuffer);
                }
                if (magicCode != MessageDecoder.MESSAGE_MAGIC_CODE &&
                    magicCode != MessageDecoder.MESSAGE_MAGIC_CODE_V2) {
                    log.error("MessageFormatUtil split buffer error, found unknown magic code. " +
                        "Message offset: {}, wrong magic code: {}", offset, magicCode);
                    continue;
                }

                if (bufferSize != getTotalSize(msgBuffer)) {
                    log.error("MessageFormatUtil split buffer error, message length not match. " +
                        "CommitLog length: {}, buffer length: {}", getTotalSize(msgBuffer), bufferSize);
                    continue;
                }

                ByteBuffer sliceBuffer = msgBuffer.slice();
                sliceBuffer.limit(bufferSize);
                bufferResultList.add(new SelectBufferResult(sliceBuffer, offset, bufferSize, tagCode));
            }
        } finally {
            cqBuffer.rewind();
            msgBuffer.rewind();
        }
        return bufferResultList;
    }
}
