/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.plugin.control.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 磁盘文件读写工具类。
 *
 * <p>提供 UTF-8 文本读取、二进制写入及静默删除能力，供管控插件持久化规则等场景使用。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class DiskUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DiskUtils.class);
    
    /** 磁盘空间不足（中文系统消息）。 */
    private static final String NO_SPACE_CN = "设备上没有空间";
    
    /** 磁盘空间不足（英文系统消息）。 */
    private static final String NO_SPACE_EN = "No space left on device";
    
    /** 超出磁盘配额（中文系统消息）。 */
    private static final String DISK_QUOTA_CN = "超出磁盘限额";
    
    /** 超出磁盘配额（英文系统消息）。 */
    private static final String DISK_QUOTA_EN = "Disk quota exceeded";
    
    /** 文件读写统一使用的 UTF-8 字符集。 */
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    
    /**
     * 以 UTF-8 编码读取文件全部文本内容。
     *
     * <p>分块解码并正确处理跨块的多字节字符，避免非 ASCII 内容损坏。</p>
     *
     * @param file 待读取文件
     * @return 文件文本内容，读取失败时返回 {@code null}
     */
    public static String readFile(File file) {
        // CharsetDecoder 非线程安全，每次调用独立创建解码器实例
        CharsetDecoder decoder = CHARSET.newDecoder();
        try (FileChannel fileChannel = new FileInputStream(file).getChannel()) {
            StringBuilder text = new StringBuilder();
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            CharBuffer charBuffer = CharBuffer.allocate(4096);
            while (fileChannel.read(buffer) != -1) {
                buffer.flip();
                decoder.decode(buffer, charBuffer, false);
                charBuffer.flip();
                while (charBuffer.hasRemaining()) {
                    text.append(charBuffer.get());
                }
                // compact() 保留未消费的字节，通常是跨 4096 边界的多字节 UTF-8 字符首部
                buffer.compact();
                charBuffer.clear();
            }
            // 流读完后刷新解码器，输出尾部残留字符
            buffer.flip();
            decoder.decode(buffer, charBuffer, true);
            decoder.flush(charBuffer);
            charBuffer.flip();
            while (charBuffer.hasRemaining()) {
                text.append(charBuffer.get());
            }
            return text.toString();
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * 将字节内容写入目标文件。
     *
     * @param file    目标文件
     * @param content 待写入内容
     * @param append  是否追加模式
     * @return 写入成功返回 {@code true}
     */
    public static boolean writeFile(File file, byte[] content, boolean append) {
        try (FileChannel fileChannel = new FileOutputStream(file, append).getChannel()) {
            ByteBuffer buffer = ByteBuffer.wrap(content);
            fileChannel.write(buffer);
            return true;
        } catch (IOException ioe) {
            if (ioe.getMessage() != null) {
                String errMsg = ioe.getMessage();
                // 磁盘满或超出配额时主动退出进程，避免数据损坏
                if (NO_SPACE_CN.equals(errMsg) || NO_SPACE_EN.equals(errMsg)
                    || errMsg.contains(DISK_QUOTA_CN) || errMsg
                        .contains(DISK_QUOTA_EN)) {
                    LOGGER.warn("磁盘满，自杀退出");
                    System.exit(0);
                }
            }
        }
        return false;
    }
    
    /**
     * 静默删除文件或目录，不抛出异常。
     *
     * @param file 待删除的文件或目录
     */
    public static void deleteQuietly(File file) {
        Objects.requireNonNull(file, "file");
        FileUtils.deleteQuietly(file);
    }
    
}
