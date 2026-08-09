/*
 * Copyright 2020  Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.arthas.grpcweb.proxy;

import com.taobao.arthas.grpcweb.proxy.MessageUtils.ContentType;
import com.taobao.arthas.common.IOUtils;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

/**
 * gRPC-Web 入站解帧器：从 HTTP 请求体读取一个或多个 DATA 帧，合并为单条 protobuf 消息字节。
 *
 * <p>帧格式：1 字节类型（DATA=0x00）+ 4 字节大端长度 + payload。
 * {@link ContentType#GRPC_WEB_TEXT} 时先 Base64 解码再解帧。</p>
 */
public class MessageDeframer {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
    /** DATA 帧类型标识字节 */
    static final byte DATA_BYTE = (byte) 0x00;

    // TODO: 当前实现无法处理超过 4GB 的输入
    /** 已解析 payload 总长度 */
    private int mLength = 0;
    /** 当前在 inBytes 中的读偏移 */
    private int mReadSoFar = 0;

    /** 各 DATA 帧 payload 列表（多帧时再拼接） */
    private ArrayList<byte[]> mFrames = new ArrayList<>();
    /** 合并后的完整消息字节 */
    private byte[] mMsg = null;
    /** DATA 帧数量 */
    private int mNumFrames;

    /** 返回解帧后的 protobuf 消息字节。 */
    byte[] getMessageBytes() {
        return mMsg;
    }

    /** 返回 payload 总长度。 */
    int getLength() {
        return mLength;
    }

    /** 返回解析到的 DATA 帧个数。 */
    int getNumberOfFrames() {
        return mNumFrames;
    }

    /**
     * 从输入流读取并解帧，结果写入 {@link #mMsg}。
     *
     * @param in 请求体输入流
     * @param contentType gRPC-Web 内容类型（决定是否 Base64 解码）
     * @return 解帧成功返回 true
     */
    public boolean processInput(InputStream in, MessageUtils.ContentType contentType) {
        byte[] inBytes;
        try {
            InputStream inStream = (contentType == ContentType.GRPC_WEB_TEXT) ? Base64.getDecoder().wrap(in) : in;
            inBytes = IOUtils.getBytes(inStream);
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("invalid input");
            return false;
        }
        if (inBytes.length < 5) {
            logger.debug("invalid input. Expected minimum of 5 bytes");
            return false;
        }

        while (getNextFrameBytes(inBytes)) {
        }
        mNumFrames = mFrames.size();

        // 常见情况仅一帧，直接引用避免拷贝
        if (mNumFrames == 1) {
            mMsg = mFrames.get(0);
        } else {
            // 多帧时拼接为连续字节数组（当前实现效率一般）
            // TODO: 大消息时可改为流式处理
            mMsg = new byte[mLength];
            int offset = 0;
            for (byte[] f : mFrames) {
                System.arraycopy(f, 0, mMsg, offset, f.length);
                offset += f.length;
            }
            mFrames = null;
        }
        return true;
    }

    /**
     * 尝试从当前偏移解析下一 DATA 帧。
     *
     * @return 若成功解析且仍有未读字节则 true，否则 false
     */
    private boolean getNextFrameBytes(byte[] inBytes) {
        // 首字节须为 0x00 表示 DATA 帧
        int firstByteValue = inBytes[mReadSoFar] | DATA_BYTE;
        if (firstByteValue != 0) {
            logger.debug("done with DATA bytes");
            return false;
        }

        // 随后 4 字节为大端 payload 长度
        int offset = mReadSoFar + 1;
        int len = ByteBuffer.wrap(inBytes, offset, 4).getInt();

        // 空消息：长度为 0 的特殊处理
        // TODO: 评估是否可移除此分支
        if (len == 0) {
            mFrames.add(new byte[0]);
            return false;
        }

        // 校验缓冲区是否包含完整帧
        int expectedNumBytes = len + 5 + mReadSoFar;
        if (inBytes.length < expectedNumBytes) {
            logger.warn(String.format("input doesn't have enough bytes. expected: %d, found %d", expectedNumBytes,
                    inBytes.length));
            return false;
        }

        // 拷贝 len 字节 payload
        mLength += len;
        offset += 4;
        byte[] inputBytes = Arrays.copyOfRange(inBytes, offset, len + offset);
        mFrames.add(inputBytes);
        mReadSoFar += (len + 5);
        // 若还有剩余字节，可能还有后续帧
        return inBytes.length > mReadSoFar;
    }
}
