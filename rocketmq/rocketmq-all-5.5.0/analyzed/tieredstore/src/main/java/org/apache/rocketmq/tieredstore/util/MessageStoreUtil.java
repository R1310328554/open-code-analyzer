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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.apache.rocketmq.common.message.MessageQueue;

/**
 * 分层存储通用工具：路径格式化、偏移文件名与人类可读字节数。
 */
public class MessageStoreUtil {

    /** 分层存储 SLF4J Logger 名称。 */
    public static final String TIERED_STORE_LOGGER_NAME = "RocketmqTieredStore";
    /** 系统 Index Topic 名称。 */
    public static final String RMQ_SYS_TIERED_STORE_INDEX_TOPIC = "rmq_sys_INDEX";

    /** 1 字节常量。 */
    public static final long BYTE = 1L;
    /** 1 KB 字节数。 */
    public static final long KB = BYTE << 10;
    /** 1 MB 字节数。 */
    public static final long MB = KB << 10;
    /** 1 GB 字节数。 */
    public static final long GB = MB << 10;
    /** 1 TB 字节数。 */
    public static final long TB = GB << 10;
    /** 1 PB 字节数。 */
    public static final long PB = TB << 10;
    /** 1 EB 字节数。 */
    public static final long EB = PB << 10;

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("#.##");

    private static String formatSize(long size, long divider, String unitName) {
        return DEC_FORMAT.format((double) size / divider) + unitName;
    }

    /** 将字节数格式化为 B/KB/MB… 人类可读字符串。 */
    public static String toHumanReadable(long size) {
        if (size < 0)
            return String.valueOf(size);
        if (size >= EB)
            return formatSize(size, EB, "EB");
        if (size >= PB)
            return formatSize(size, PB, "PB");
        if (size >= TB)
            return formatSize(size, TB, "TB");
        if (size >= GB)
            return formatSize(size, GB, "GB");
        if (size >= MB)
            return formatSize(size, MB, "MB");
        if (size >= KB)
            return formatSize(size, KB, "KB");
        return formatSize(size, BYTE, "B");
    }

    /** 计算字符串 MD5 前 8 位十六进制哈希。 */
    public static String getHash(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return String.format("%032x", new BigInteger(1, digest)).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /** 将 MessageQueue 格式化为 broker/topic/queueId 路径。 */
    public static String toFilePath(MessageQueue mq) {
        return String.format("%s/%s/%s", mq.getBrokerName(), mq.getTopic(), mq.getQueueId());
    }

    /** 返回系统 Index 文件的相对路径。 */
    public static String getIndexFilePath(String brokerName) {
        return toFilePath(new MessageQueue(RMQ_SYS_TIERED_STORE_INDEX_TOPIC, brokerName, 0));
    }

    /** 将逻辑偏移编码为 hash+20 位数字的文件名。 */
    public static String offset2FileName(final long offset) {
        final NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumIntegerDigits(20);
        numberFormat.setMaximumFractionDigits(0);
        numberFormat.setGroupingUsed(false);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Long.toString(offset).getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            String hash = String.format("%032x", new BigInteger(1, digest)).substring(0, 8);
            return hash + numberFormat.format(offset);
        } catch (Exception ignore) {
            return numberFormat.format(offset);
        }
    }

    /** 从文件名解析逻辑偏移（取末 20 位数字）。 */
    public static long fileName2Offset(final String fileName) {
        return Long.parseLong(fileName.substring(fileName.length() - 20));
    }
}
