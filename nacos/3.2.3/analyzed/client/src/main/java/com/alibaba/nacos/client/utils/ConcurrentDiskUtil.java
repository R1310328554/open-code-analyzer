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

package com.alibaba.nacos.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Concurrent Disk util.
 * <p>并发磁盘读写工具：通过 {@link java.nio.channels.FileLock} 在多读单写场景下安全访问本地文件，锁冲突时按指数退避重试，供客户端缓存快照等持久化场景使用。</p>
 *
 * @author nkorange
 */
public class ConcurrentDiskUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentDiskUtil.class);
    
    /** RandomAccessFile 只读模式 */
    private static final String READ_ONLY = "r";
    
    /** RandomAccessFile 读写模式 */
    private static final String READ_WRITE = "rw";
    
    /** 获取文件锁最大重试次数 */
    private static final int RETRY_COUNT = 10;
    
    /** 锁冲突重试基础休眠毫秒数（实际为 base * 重试序号） */
    private static final int SLEEP_BASETIME = 10;
    
    /**
     * get file content.
     * <p>以共享读锁读取指定路径文件全文并解码为字符串。</p>
     *
     * @param path        file path
     * @param charsetName charsetName
     * @return content
     * @throws IOException IOException
     */
    public static String getFileContent(String path, String charsetName) throws IOException {
        File file = new File(path);
        return getFileContent(file, charsetName);
    }
    
    /**
     * get file content.
     *
     * @param file        file
     * @param charsetName charsetName
     * @return content
     * @throws IOException IOException
      * <p>并发磁盘读写；详见类级说明。</p>
     */
    public static String getFileContent(File file, String charsetName) throws IOException {
        try (RandomAccessFile fis = new RandomAccessFile(file, READ_ONLY);
            FileChannel fcin = fis.getChannel();
            FileLock rlock = tryLock(file, fcin, true)) {
            int fileSize = (int) fcin.size();
            ByteBuffer byteBuffer = ByteBuffer.allocate(fileSize);
            fcin.read(byteBuffer);
            byteBuffer.flip();
            return byteBufferToString(byteBuffer, charsetName);
        }
    }
    
    /**
     * write file content.
     * <p>以独占写锁写入内容并截断至新长度；文件不存在时尝试创建。</p>
     *
     * @param path        file path
     * @param content     content
     * @param charsetName charsetName
     * @return whether write ok
     * @throws IOException IOException
     */
    public static Boolean writeFileContent(String path, String content, String charsetName)
        throws IOException {
        File file = new File(path);
        return writeFileContent(file, content, charsetName);
    }
    
    /**
     * write file content.
     *
     * @param file        file
     * @param content     content
     * @param charsetName charsetName
     * @return whether write ok
     * @throws IOException IOException
      * <p>并发磁盘读写；详见类级说明。</p>
     */
    public static Boolean writeFileContent(File file, String content, String charsetName)
        throws IOException {
        
        if (!file.exists() && !file.createNewFile()) {
            return false;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file, READ_WRITE);
            FileChannel channel = raf.getChannel();
            FileLock lock = tryLock(file, channel, false)) {
            byte[] contentBytes = content.getBytes(charsetName);
            ByteBuffer sendBuffer = ByteBuffer.wrap(contentBytes);
            while (sendBuffer.hasRemaining()) {
                channel.write(sendBuffer);
            }
            channel.truncate(contentBytes.length);
        } catch (FileNotFoundException e) {
            throw new IOException("file not exist");
        }
        return true;
    }
    
    /**
     * transfer ByteBuffer to String.
     * <p>按指定字符集将 {@link ByteBuffer} 解码为字符串。</p>
     *
     * @param buffer      buffer
     * @param charsetName charsetName
     * @return String
     * @throws IOException IOException
     */
    public static String byteBufferToString(ByteBuffer buffer, String charsetName)
        throws IOException {
        Charset charset = Charset.forName(charsetName);
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
        return charBuffer.toString();
    }
    
    /** 锁重试间隔休眠，中断时恢复线程中断标志 */
    private static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            LOGGER.warn("sleep wrong", e);
            // set the interrupted flag
            Thread.currentThread().interrupt();
        }
    }
    
    /** 非阻塞尝试获取文件锁，失败则退避重试直至成功或超过 {@link #RETRY_COUNT} */
    private static FileLock tryLock(File file, FileChannel channel, boolean shared)
        throws IOException {
        FileLock result = null;
        int i = 0;
        do {
            try {
                result = channel.tryLock(0L, Long.MAX_VALUE, shared);
            } catch (Exception e) {
                ++i;
                if (i > RETRY_COUNT) {
                    LOGGER.error("[NA] lock " + file.getName() + " fail;retryed time: " + i, e);
                    throw new IOException("lock " + file.getAbsolutePath() + " conflict");
                }
                sleep(SLEEP_BASETIME * i);
                LOGGER.warn("lock " + file.getName() + " conflict;retry time: " + i);
            }
        } while (null == result);
        return result;
    }
    
}
