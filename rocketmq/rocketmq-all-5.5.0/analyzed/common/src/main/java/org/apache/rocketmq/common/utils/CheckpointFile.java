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

package org.apache.rocketmq.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;

/**
 * 条目 Checkpoint 文件工具。
 * <p>文件格式：
 * <li>第一行：条目数量
 * <li>第二行：条目内容 crc32
 * <li>后续：每行一条序列化后的条目数据
 * <p>示例：
 * <li>2（数量）
 * <li>773307083（crc32）
 * <li>7-7000（条目）
 * <li>8-8000（条目）
 */
public class CheckpointFile<T> {

    /** crc32 为 0 时不校验完整性。 */
    private static final int NOT_CHECK_CRC_MAGIC_CODE = 0;
    private final String filePath;
    private final CheckpointSerializer<T> serializer;

    /** Checkpoint 条目与文本行之间的序列化/反序列化接口。 */
    public interface CheckpointSerializer<T> {
        /** 将条目序列化为单行文本。 */
        String toLine(final T entry);

        /** 从单行文本反序列化为条目。 */
        T fromLine(final String line);
    }

    public CheckpointFile(final String filePath, final CheckpointSerializer<T> serializer) {
        this.filePath = filePath;
        this.serializer = serializer;
    }

    /** 返回备份文件路径（主文件路径 + {@code .bak}）。 */
    public String getBackFilePath() {
        return this.filePath + ".bak";
    }

    /** 将条目列表写入 Checkpoint 文件（含数量与 crc32 头）。 */
    public void write(final List<T> entries) throws IOException {
        if (entries.isEmpty()) {
            return;
        }
        synchronized (this) {
            StringBuilder entryContent = new StringBuilder();
            for (T entry : entries) {
                final String line = this.serializer.toLine(entry);
                if (line != null && !line.isEmpty()) {
                    entryContent.append(line);
                    entryContent.append(System.lineSeparator());
                }
            }
            int crc32 = UtilAll.crc32(entryContent.toString().getBytes(StandardCharsets.UTF_8));

            String content = entries.size() + System.lineSeparator() +
                crc32 + System.lineSeparator() + entryContent;
            MixAll.string2File(content, this.filePath);
        }
    }

    private List<T> read(String filePath) throws IOException {
        final ArrayList<T> result = new ArrayList<>();
        synchronized (this) {
            final File file = new File(filePath);
            if (!file.exists()) {
                return result;
            }
            try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
                // 读取条目数量
                int expectedLines = Integer.parseInt(reader.readLine());

                // 读取整块内容的 crc32
                int expectedCrc32 = Integer.parseInt(reader.readLine());

                // 逐行读取条目数据
                StringBuilder sb = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line).append(System.lineSeparator());
                    final T entry = this.serializer.fromLine(line);
                    if (entry != null) {
                        result.add(entry);
                    }
                    line = reader.readLine();
                }
                int truthCrc32 = UtilAll.crc32(sb.toString().getBytes(StandardCharsets.UTF_8));

                if (result.size() != expectedLines) {
                    final String err = String.format(
                        "Expect %d entries, only found %d entries", expectedLines, result.size());
                    throw new IOException(err);
                }

                if (NOT_CHECK_CRC_MAGIC_CODE != expectedCrc32 && truthCrc32 != expectedCrc32) {
                    final String err = String.format(
                        "Entries crc32 not match, file=%s, truth=%s", expectedCrc32, truthCrc32);
                    throw new IOException(err);
                }
                return result;
            }
        }
    }

    /** 从主文件读取条目；主文件为空或失败时回退到备份文件。 */
    public List<T> read() throws IOException {
        try {
            List<T> result = this.read(this.filePath);
            if (CollectionUtils.isEmpty(result)) {
                result = this.read(this.getBackFilePath());
            }
            return result;
        } catch (IOException e) {
            return this.read(this.getBackFilePath());
        }
    }
}
