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

package org.apache.rocketmq.tieredstore.stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.tieredstore.common.FileSegmentType;

/**
 * 文件段上传输入流：串联多个 ByteBuffer 供组提交读取。
 */
public class FileSegmentInputStream extends InputStream {

    /** 文件段类型：CommitLog、ConsumeQueue 或 Index。 */
    protected final FileSegmentType fileType;

    /** 待上传的 ByteBuffer 列表。 */
    protected final List<ByteBuffer> bufferList;

    /** ByteBuffer 列表的总可读字节数。 */
    protected final int contentLength;

    /** 当前在流中的全局读位置。 */
    protected int readPosition = 0;

    /** 当前正在读取的 bufferList 索引。 */
    protected int curReadBufferIndex = 0;
    /** 当前 buffer 内的读位置。 */
    protected int readPosInCurBuffer = 0;

    /** 当前正在读取的 ByteBuffer，等同于 bufferList.get(curReadBufferIndex)。 */
    protected ByteBuffer curBuffer;

    private int markReadPosition = -1;

    private int markCurReadBufferIndex = -1;

    private int markReadPosInCurBuffer = -1;

    public FileSegmentInputStream(
        FileSegmentType fileType, List<ByteBuffer> bufferList, int contentLength) {
        this.fileType = fileType;
        this.contentLength = contentLength;
        this.bufferList = bufferList;
        if (bufferList != null && bufferList.size() > 0) {
            this.curBuffer = bufferList.get(curReadBufferIndex);
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int ignore) {
        this.markReadPosition = readPosition;
        this.markCurReadBufferIndex = curReadBufferIndex;
        this.markReadPosInCurBuffer = readPosInCurBuffer;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (this.markReadPosition == -1) {
            throw new IOException("mark not set");
        }
        this.readPosition = markReadPosition;
        this.curReadBufferIndex = markCurReadBufferIndex;
        this.readPosInCurBuffer = markReadPosInCurBuffer;
        if (this.curReadBufferIndex < bufferList.size()) {
            this.curBuffer = bufferList.get(curReadBufferIndex);
        }
    }

    /** 重置所有缓冲与读指针到起始位置。 */
    public synchronized void rewind() {
        this.readPosition = 0;
        this.curReadBufferIndex = 0;
        this.readPosInCurBuffer = 0;
        if (CollectionUtils.isNotEmpty(bufferList)) {
            this.curBuffer = bufferList.get(0);
            for (ByteBuffer buffer : bufferList) {
                buffer.rewind();
            }
        }
    }

    /** 返回流总内容长度。 */
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public int available() {
        return contentLength - readPosition;
    }

    public List<ByteBuffer> getBufferList() {
        return bufferList;
    }

    public ByteBuffer getCodaBuffer() {
        return null;
    }

    @Override
    public int read() {
        if (available() <= 0) {
            return -1;
        }
        readPosition++;
        if (readPosInCurBuffer >= curBuffer.remaining()) {
            curReadBufferIndex++;
            if (curReadBufferIndex >= bufferList.size()) {
                return -1;
            }
            curBuffer = bufferList.get(curReadBufferIndex);
            readPosInCurBuffer = 0;
        }
        return curBuffer.get(readPosInCurBuffer++) & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || len > b.length - off");
        }
        if (readPosition >= contentLength) {
            return -1;
        }

        int available = available();
        if (len > available) {
            len = available;
        }
        if (len <= 0) {
            return 0;
        }
        int needRead = len;
        int pos = readPosition;
        int bufIndex = curReadBufferIndex;
        int posInCurBuffer = readPosInCurBuffer;
        ByteBuffer curBuf = curBuffer;
        while (needRead > 0 && bufIndex < bufferList.size()) {
            curBuf = bufferList.get(bufIndex);
            int remaining = curBuf.remaining() - posInCurBuffer;
            int readLen = Math.min(remaining, needRead);
            // 从当前 ByteBuffer 读取
            curBuf.position(posInCurBuffer);
            curBuf.get(b, off, readLen);
            curBuf.position(0);
            // update flags
            off += readLen;
            needRead -= readLen;
            pos += readLen;
            posInCurBuffer += readLen;
            if (posInCurBuffer == curBuf.remaining()) {
                // 切换到下一段缓冲
                bufIndex++;
                posInCurBuffer = 0;
            }
        }
        readPosition = pos;
        curReadBufferIndex = bufIndex;
        readPosInCurBuffer = posInCurBuffer;
        curBuffer = curBuf;
        return len;
    }
}

