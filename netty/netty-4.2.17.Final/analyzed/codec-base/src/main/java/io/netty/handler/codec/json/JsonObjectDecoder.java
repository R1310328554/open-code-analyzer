/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.handler.codec.json;

import static io.netty.util.internal.ObjectUtil.checkPositive;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.channel.ChannelPipeline;

import java.util.List;

/**
 * 将 JSON 对象/数组字节流按顶层边界切分，逐个向上游 {@link ChannelPipeline} 传递。
 * <p>
 * 输入流应为 UTF-8 或 ASCII。实现通过 {@code byte} 到 {@code char} 的直接转换，
 * 并与 {@code '{'}、{@code '['}、{@code '"'} 等低范围 ASCII 比较；
 * UTF-8 多字节码点不使用 [0..0x7F] 范围，因此本实现完全支持 UTF-8。
 * <p>
 * 本类不做完整 JSON 解析或校验：仅当开闭括号/方括号数量匹配时即视为一个 JSON 对象/数组。
 * 后续 {@link io.netty.channel.ChannelHandler} 负责将 JSON 文本解析为 POJO 等可用形式。
 */
public class JsonObjectDecoder extends ByteToMessageDecoder {

    /** 状态：流已损坏，丢弃后续输入。 */
    /** 状态：流已损坏，丢弃后续输入。 */
    private static final int ST_CORRUPTED = -1;
    /** 状态：初始，等待 JSON 起始符。 */
    /** 状态：初始，等待 JSON 起始符。 */
    private static final int ST_INIT = 0;
    /** 状态：正常解码单个 JSON 对象/数组。 */
    /** 状态：正常解码单个 JSON 对象/数组。 */
    private static final int ST_DECODING_NORMAL = 1;
    /** 状态：流式解码 JSON 数组元素。 */
    /** 状态：流式解码 JSON 数组元素。 */
    private static final int ST_DECODING_ARRAY_STREAM = 2;

    /** 未闭合的括号/方括号计数。 */
    /** 未闭合的括号/方括号计数。 */
    private int openBraces;
    /** 当前扫描到的字节索引。 */
    /** 当前扫描到的字节索引。 */
    private int idx;

    /** 上次 {@link ByteBuf#readerIndex()}，用于缓冲区压缩后校正 idx。 */
    /** 上次 {@link ByteBuf#readerIndex()}，用于缓冲区压缩后校正 idx。 */
    private int lastReaderIndex;

    /** 当前解码状态。 */
    /** 当前解码状态。 */
    private int state;
    /** 是否处于 JSON 字符串内部。 */
    /** 是否处于 JSON 字符串内部。 */
    private boolean insideString;

    /** 单个 JSON 对象/数组允许的最大字节数（含括号等）。 */
    /** 单个 JSON 对象/数组允许的最大字节数（含括号等）。 */
    private final int maxObjectLength;
    /** 顶层为数组时是否逐元素流式输出。 */
    /** 顶层为数组时是否逐元素流式输出。 */
    private final boolean streamArrayElements;

    /** 默认最大对象长度 1 MB。 */
    /** 默认最大对象长度 1 MB。 */
    public JsonObjectDecoder() {
        // 1 MB
        this(1024 * 1024);
    }

    public JsonObjectDecoder(int maxObjectLength) {
        this(maxObjectLength, false);
    }

    public JsonObjectDecoder(boolean streamArrayElements) {
        this(1024 * 1024, streamArrayElements);
    }

    /**
      * @param maxObjectLength   单个 JSON 对象/数组允许的最大字节数（含括号等）。
     *                             超出则丢弃并抛出 {@link TooLongFrameException}。
      * @param streamArrayElements   为 {@code true} 且顶层为数组时，每收完一个元素立即向上游传递，
     *                                  可处理“无限长”数组。
     *
     */
    public JsonObjectDecoder(int maxObjectLength, boolean streamArrayElements) {
        this.maxObjectLength = checkPositive(maxObjectLength, "maxObjectLength");
        this.streamArrayElements = streamArrayElements;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (state == ST_CORRUPTED) {
            in.skipBytes(in.readableBytes());
            return;
        }

        if (this.idx > in.readerIndex() && lastReaderIndex != in.readerIndex()) {
            this.idx = in.readerIndex() + (idx - lastReaderIndex);
        }

        // 待处理的下一个字节索引
        int idx = this.idx;
        int wrtIdx = in.writerIndex();

        if (wrtIdx > maxObjectLength) {
            // 缓冲区总长度超限，丢弃并复位
            in.skipBytes(in.readableBytes());
            reset();
            throw new TooLongFrameException(
                            "object length exceeds " + maxObjectLength + ": " + wrtIdx + " bytes discarded");
        }

        for (/* use current idx */; idx < wrtIdx; idx++) {
            byte c = in.getByte(idx);
            if (state == ST_DECODING_NORMAL) {
                decodeByte(c, in, idx);

                // 所有括号已闭合，视为一个完整 JSON 对象/数组
                if (openBraces == 0) {
                    ByteBuf json = extractObject(ctx, in, in.readerIndex(), idx + 1 - in.readerIndex());
                    if (json != null) {
                        out.add(json);
                    }

                    // 已提取，丢弃已消费字节
                    in.readerIndex(idx + 1);
                    // 复位状态，准备下一个 JSON
                    reset();
                }
            } else if (state == ST_DECODING_ARRAY_STREAM) {
                decodeByte(c, in, idx);

                if (!insideString && (openBraces == 1 && c == ',' || openBraces == 0 && c == ']')) {
                    // 跳过元素前导空白
                    for (int i = in.readerIndex(); Character.isWhitespace(in.getByte(i)); i++) {
                        in.skipBytes(1);
                    }

                    // 跳过元素尾部空白
                    int idxNoSpaces = idx - 1;
                    while (idxNoSpaces >= in.readerIndex() && Character.isWhitespace(in.getByte(idxNoSpaces))) {
                        idxNoSpaces--;
                    }

                    ByteBuf json = extractObject(ctx, in, in.readerIndex(), idxNoSpaces + 1 - in.readerIndex());
                    if (json != null) {
                        out.add(json);
                    }

                    in.readerIndex(idx + 1);

                    if (c == ']') {
                        reset();
                    }
                }
            // 检测到 JSON 对象/数组起始，开始累积
            } else if (c == '{' || c == '[') {
                initDecoding(c);

                if (state == ST_DECODING_ARRAY_STREAM) {
                    // 流式模式下丢弃数组开括号
                    in.skipBytes(1);
                }
            // 跳过 JSON 前的空白
            } else if (Character.isWhitespace(c)) {
                in.skipBytes(1);
            } else {
                state = ST_CORRUPTED;
                throw new CorruptedFrameException(
                        "invalid JSON received at byte position " + idx + ": " + ByteBufUtil.hexDump(in));
            }
        }

        if (in.readableBytes() == 0) {
            this.idx = 0;
        } else {
            this.idx = idx;
        }
        this.lastReaderIndex = in.readerIndex();
    }

    /**
     * 子类可覆写以过滤向上游传递的 JSON 对象/数组。
     */
    @SuppressWarnings("UnusedParameters")
    protected ByteBuf extractObject(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.retainedSlice(index, length);
    }

    private void decodeByte(byte c, ByteBuf in, int idx) {
        if ((c == '{' || c == '[') && !insideString) {
            openBraces++;
        } else if ((c == '}' || c == ']') && !insideString) {
            openBraces--;
        } else if (c == '"') {
            // 进入或退出 JSON 字符串；字符串内的括号不计入 openBraces
            if (!insideString) {
                insideString = true;
            } else {
                int backslashCount = 0;
                idx--;
                while (idx >= 0) {
                    if (in.getByte(idx) == '\\') {
                        backslashCount++;
                        idx--;
                    } else {
                        break;
                    }
                }
                // 反斜杠数量为偶数时，该双引号未转义，字符串结束
                if (backslashCount % 2 == 0) {
                    // 字符串结束
                    insideString = false;
                }
            }
        }
    }

    private void initDecoding(byte openingBrace) {
        openBraces = 1;
        if (openingBrace == '[' && streamArrayElements) {
            state = ST_DECODING_ARRAY_STREAM;
        } else {
            state = ST_DECODING_NORMAL;
        }
    }

    private void reset() {
        insideString = false;
        state = ST_INIT;
        openBraces = 0;
    }
}
