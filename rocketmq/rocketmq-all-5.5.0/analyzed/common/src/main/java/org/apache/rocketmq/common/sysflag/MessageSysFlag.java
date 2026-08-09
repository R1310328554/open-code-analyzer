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
package org.apache.rocketmq.common.sysflag;

import org.apache.rocketmq.common.compression.CompressionType;

/**
 * 消息系统标志位：压缩、事务、多 Tag、IPv6 地址及批处理等属性编码在 int 标志中。
 */
public class MessageSysFlag {

    /**
     * 系统标志各位含义：
     *
     * | bit    | 7 | 6 | 5         | 4        | 3           | 2                | 1                | 0                |
     * |--------|---|---|-----------|----------|-------------|------------------|------------------|------------------|
     * | byte 1 |   |   | STOREHOST | BORNHOST | TRANSACTION | TRANSACTION      | MULTI_TAGS       | COMPRESSED       |
     * | byte 2 |   |   |           |          |             | COMPRESSION_TYPE | COMPRESSION_TYPE | COMPRESSION_TYPE |
     * | byte 3 |   |   |           |          |             |                  |                  |                  |
     * | byte 4 |   |   |           |          |             |                  |                  |                  |
     */
    /** 消息体已压缩。 */
    public final static int COMPRESSED_FLAG = 0x1;
    /** 消息含多个 Tag。 */
    public final static int MULTI_TAGS_FLAG = 0x1 << 1;
    /** 非事务消息。 */
    public final static int TRANSACTION_NOT_TYPE = 0;
    /** 事务半消息（Prepared）。 */
    public final static int TRANSACTION_PREPARED_TYPE = 0x1 << 2;
    /** 事务已提交。 */
    public final static int TRANSACTION_COMMIT_TYPE = 0x2 << 2;
    /** 事务已回滚。 */
    public final static int TRANSACTION_ROLLBACK_TYPE = 0x3 << 2;
    /** bornHost 为 IPv6 地址。 */
    public final static int BORNHOST_V6_FLAG = 0x1 << 4;
    /** storeHost 为 IPv6 地址。 */
    public final static int STOREHOSTADDRESS_V6_FLAG = 0x1 << 5;
    // 批处理标志，避免与其他位冲突
    /** 批消息需要解包。 */
    public final static int NEED_UNWRAP_FLAG = 0x1 << 6;
    /** 内部批消息标志。 */
    public final static int INNER_BATCH_FLAG = 0x1 << 7;

    // 压缩算法类型（占 bit 8-10）
    /** LZ4 压缩。 */
    public final static int COMPRESSION_LZ4_TYPE = 0x1 << 8;
    /** Zstd 压缩。 */
    public final static int COMPRESSION_ZSTD_TYPE = 0x2 << 8;
    /** Zlib 压缩。 */
    public final static int COMPRESSION_ZLIB_TYPE = 0x3 << 8;
    /** 压缩类型位掩码。 */
    public final static int COMPRESSION_TYPE_COMPARATOR = 0x7 << 8;

    /** 提取事务状态位（Prepared/Commit/Rollback）。 */
    public static int getTransactionValue(final int flag) {
        return flag & TRANSACTION_ROLLBACK_TYPE;
    }

    /** 重置事务状态位为指定 type。 */
    public static int resetTransactionValue(final int flag, final int type) {
        return (flag & (~TRANSACTION_ROLLBACK_TYPE)) | type;
    }

    /** 清除压缩标志位。 */
    public static int clearCompressedFlag(final int flag) {
        return flag & (~COMPRESSED_FLAG);
    }

    // 从标志位解析压缩算法
    /** 根据标志位返回 {@link CompressionType}。 */
    public static CompressionType getCompressionType(final int flag) {
        return CompressionType.findByValue((flag & COMPRESSION_TYPE_COMPARATOR) >> 8);
    }

    /** 检测 flag 是否包含 expectedFlag 指定位。 */
    public static boolean check(int flag, int expectedFlag) {
        return (flag & expectedFlag) != 0;
    }

}
