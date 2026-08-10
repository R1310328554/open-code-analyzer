/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.xml;

import static io.netty.util.internal.ObjectUtil.checkPositive;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

/**
 * A frame decoder for single separate XML based message streams.
 * <p/>
 * A couple examples will better help illustrate
 * what this decoder actually does.
 * <p/>
 * Given an input array of bytes split over 3 frames like this:
 * <pre>
 * +-----+-----+-----------+
 * | &lt;an | Xml | Element/&gt; |
 * +-----+-----+-----------+
 * </pre>
 * <p/>
 * this decoder would output a single frame:
 * <p/>
 * <pre>
 * +-----------------+
 * | &lt;anXmlElement/&gt; |
 * +-----------------+
 * </pre>
 *
 * Given an input array of bytes split over 5 frames like this:
 * <pre>
 * +-----+-----+-----------+-----+----------------------------------+
 * | &lt;an | Xml | Element/&gt; | &lt;ro | ot&gt;&lt;child&gt;content&lt;/child&gt;&lt;/root&gt; |
 * +-----+-----+-----------+-----+----------------------------------+
 * </pre>
 * <p/>
 * this decoder would output two frames:
 * <p/>
 * <pre>
 * +-----------------+-------------------------------------+
 * | &lt;anXmlElement/&gt; | &lt;root&gt;&lt;child&gt;content&lt;/child&gt;&lt;/root&gt; |
 * +-----------------+-------------------------------------+
 * </pre>
 *
 * <p/>
 * The byte stream is expected to be in UTF-8 character encoding or ASCII. The current implementation
 * uses direct {@code byte} to {@code char} cast and then compares that {@code char} to a few low range
 * ASCII characters like {@code '<'}, {@code '>'} or {@code '/'}. UTF-8 is not using low range [0..0x7F]
 * byte values for multibyte codepoint representations therefore fully supported by this implementation.
 * <p/>
 * Please note that this decoder is not suitable for
 * xml streaming protocols such as
 * <a href="https://xmpp.org/rfcs/rfc6120.html">XMPP</a>,
 * where an initial xml element opens the stream and only
 * gets closed at the end of the session, although this class
 * could probably allow for such type of message flow with
 * minor modifications.
 *
 * <p>按「顶层 XML 元素」切分的帧解码器：通过统计 {@code <}/{@code >} 与 CDATA、注释、
 * 处理指令等结构的嵌套深度，在根元素闭合且括号计数归零时输出完整 {@link ByteBuf} 帧。
 * 适用于短消息、RPC 式 XML，不适用于 XMPP 等长连接流式 XML 协议。</p>
 */
public class XmlFrameDecoder extends ByteToMessageDecoder {

    /** 单帧允许的最大字节长度，超出则丢弃并抛 {@link TooLongFrameException}。 */
    private final int maxFrameLength;

    public XmlFrameDecoder(int maxFrameLength) {
        this.maxFrameLength = checkPositive(maxFrameLength, "maxFrameLength");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        boolean openingBracketFound = false;
        boolean atLeastOneXmlElementFound = false;
        boolean inCDATASection = false;
        boolean inCommentBlock = false;
        boolean inProcessingInstruction = false;
        boolean inClosingTag = false;
        long openBracketsCount = 0;
        int length = 0;
        int leadingWhiteSpaceCount = 0;
        final int bufferLength = in.writerIndex();

        if (bufferLength > maxFrameLength) {
            // bufferLength exceeded maxFrameLength; dropping frame
            // 缓冲区已超限，丢弃全部可读数据
            in.skipBytes(in.readableBytes());
            fail(bufferLength);
            return;
        }

        for (int i = in.readerIndex(); i < bufferLength; i++) {
            final byte readByte = in.getByte(i);
            if (!openingBracketFound && Character.isWhitespace(readByte)) {
                // xml has not started and whitespace char found
                // XML 尚未开始，跳过前导空白
                leadingWhiteSpaceCount++;
            } else if (!openingBracketFound && readByte != '<') {
                // garbage found before xml start
                // 在首个 '<' 之前出现非空白垃圾字节
                fail(ctx);
                in.skipBytes(in.readableBytes());
                return;
            } else if (inClosingTag && readByte == '<') {
                // 结束标签内部不应再出现 '<'
                fail(ctx);
                in.skipBytes(in.readableBytes());
                return;
            } else if (!inCDATASection && !inCommentBlock && !inProcessingInstruction && readByte == '<') {
                openingBracketFound = true;

                if (i < bufferLength - 1) {
                    final byte peekAheadByte = in.getByte(i + 1);
                    if (peekAheadByte == '/') {
                        // found </, we must check if it is enclosed
                        // 闭合标签开始
                        inClosingTag = true;
                    } else if (isValidStartCharForXmlElement(peekAheadByte)) {
                        atLeastOneXmlElementFound = true;
                        // char after < is a valid xml element start char,
                        // incrementing openBracketsCount
                        // 普通元素开始，嵌套深度 +1
                        openBracketsCount++;
                    } else if (peekAheadByte == '!') {
                        if (isCommentBlockStart(in, i)) {
                            // <!-- comment --> start found
                            openBracketsCount++;
                            inCommentBlock = true;
                        } else if (isCDATABlockStart(in, i)) {
                            // <![CDATA[ start found
                            openBracketsCount++;
                            inCDATASection = true;
                        }
                    } else if (peekAheadByte == '?') {
                        // <?xml ?> start found
                        openBracketsCount++;
                        inProcessingInstruction = true;
                    }
                }
            } else if (!inCDATASection && !inCommentBlock && !inProcessingInstruction && readByte == '/') {
                if (i < bufferLength - 1 && in.getByte(i + 1) == '>') {
                    // found />, decrementing openBracketsCount
                    // 空元素 /> 自闭合，深度 -1
                    openBracketsCount--;
                }
            } else if (readByte == '>') {
                length = i + 1;

                if (i - 1 > -1) {
                    final byte peekBehindByte = in.getByte(i - 1);

                    if (inCommentBlock) {
                        if (peekBehindByte == '-' && i - 2 > -1 && in.getByte(i - 2) == '-') {
                            // a <!-- comment --> was closed
                            openBracketsCount--;
                            inCommentBlock = false;
                        }
                    } else if (inProcessingInstruction) {
                        if (peekBehindByte == '?') {
                            // an <?xml ?> tag was closed
                            openBracketsCount--;
                            inProcessingInstruction = false;
                        }
                    } else if (inClosingTag) {
                        openBracketsCount--;
                        inClosingTag = false;
                    } else if (!inCDATASection) {
                        if (peekBehindByte == '?') {
                            // an <?xml ?> tag was closed
                            openBracketsCount--;
                        } else if (peekBehindByte == '-' && i - 2 > -1 && in.getByte(i - 2) == '-') {
                            // a <!-- comment --> was closed
                            openBracketsCount--;
                        }
                    } else if (inCDATASection && peekBehindByte == ']' && i - 2 > -1 && in.getByte(i - 2) == ']') {
                        // a <![CDATA[...]]> block was closed
                        openBracketsCount--;
                        inCDATASection = false;
                    }
                }

                if (atLeastOneXmlElementFound && openBracketsCount == 0) {
                    // xml is balanced, bailing out
                    // 已找到至少一个元素且括号平衡，可输出一帧
                    break;
                }
            }
        }

        final int readerIndex = in.readerIndex();
        int xmlElementLength = length - readerIndex;

        if (openBracketsCount == 0 && xmlElementLength > 0) {
            if (readerIndex + xmlElementLength >= bufferLength) {
                xmlElementLength = in.readableBytes();
            }
            final ByteBuf frame =
                    extractFrame(in, readerIndex + leadingWhiteSpaceCount, xmlElementLength - leadingWhiteSpaceCount);
            in.skipBytes(xmlElementLength);
            out.add(frame);
        }
    }

    /** 帧超长时构造 {@link TooLongFrameException}。 */
    private void fail(long frameLength) {
        if (frameLength > 0) {
            throw new TooLongFrameException(
                            "frame length exceeds " + maxFrameLength + ": " + frameLength + " - discarded");
        } else {
            throw new TooLongFrameException(
                            "frame length exceeds " + maxFrameLength + " - discarding");
        }
    }

    /** XML 开始前出现非法内容时触发 {@link CorruptedFrameException}。 */
    private static void fail(ChannelHandlerContext ctx) {
        ctx.fireExceptionCaught(new CorruptedFrameException("frame contains content before the xml starts"));
    }

    /** 从输入缓冲区复制一帧（不移动 readerIndex，由调用方 skip）。 */
    private static ByteBuf extractFrame(ByteBuf buffer, int index, int length) {
        return buffer.copy(index, length);
    }

    /**
     * Asks whether the given byte is a valid
     * start char for an xml element name.
     * <p/>
     * Please refer to the
     * <a href="https://www.w3.org/TR/2004/REC-xml11-20040204/#NT-NameStartChar">NameStartChar</a>
     * formal definition in the W3C XML spec for further info.
     *
     * @param b the input char
     * @return true if the char is a valid start char
     *
     * <p>判断字节是否为 XML 元素名的合法起始字符（ASCII 子集：字母、冒号、下划线）。</p>
     */
    private static boolean isValidStartCharForXmlElement(final byte b) {
        return b >= 'a' && b <= 'z' || b >= 'A' && b <= 'Z' || b == ':' || b == '_';
    }

    /** 检测 {@code <!--} 注释块起始。 */
    private static boolean isCommentBlockStart(final ByteBuf in, final int i) {
        return i < in.writerIndex() - 3
                && in.getByte(i + 2) == '-'
                && in.getByte(i + 3) == '-';
    }

    /** 检测 {@code <![CDATA[} 区段起始。 */
    private static boolean isCDATABlockStart(final ByteBuf in, final int i) {
        return i < in.writerIndex() - 8
                && in.getByte(i + 2) == '['
                && in.getByte(i + 3) == 'C'
                && in.getByte(i + 4) == 'D'
                && in.getByte(i + 5) == 'A'
                && in.getByte(i + 6) == 'T'
                && in.getByte(i + 7) == 'A'
                && in.getByte(i + 8) == '[';
    }

}
