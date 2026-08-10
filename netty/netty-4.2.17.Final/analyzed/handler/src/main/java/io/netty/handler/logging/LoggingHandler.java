/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.logging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.SocketAddress;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * A {@link ChannelHandler} that logs all events using a logging framework.
 * By default, all events are logged at <tt>DEBUG</tt> level and full hex dumps are recorded for ByteBufs.
 *
 * <p>可共享的双向 {@link ChannelHandler}：在 pipeline 入站/出站各阶段记录 Channel 生命周期与 I/O 事件。
 * 默认 DEBUG 级别，{@link ByteBuf} 以十六进制转储输出；可通过 {@link LogLevel} 与 {@link ByteBufFormat} 调整。</p>
 */
@Sharable
@SuppressWarnings({ "StringConcatenationInsideStringBufferAppend", "StringBufferReplaceableByString" })
public class LoggingHandler extends ChannelDuplexHandler {

    /** 未指定级别时的默认日志级别。 */
    private static final LogLevel DEFAULT_LEVEL = LogLevel.DEBUG;

    /** 底层 InternalLogger 实例。 */
    protected final InternalLogger logger;
    /** 与 {@link LogLevel} 对应的内部级别，避免重复转换。 */
    protected final InternalLogLevel internalLevel;

    /** 对外暴露的日志级别。 */
    private final LogLevel level;
    /** ByteBuf 日志格式（简要或十六进制转储）。 */
    private final ByteBufFormat byteBufFormat;

    /**
     * Creates a new instance whose logger name is the fully qualified class
     * name of the instance with hex dump enabled.
     *
     * <p>使用本类全限定名作为 logger 名，默认 DEBUG + HEX_DUMP。</p>
     */
    public LoggingHandler() {
        this(DEFAULT_LEVEL);
    }
    /**
     * Creates a new instance whose logger name is the fully qualified class
     * name of the instance.
     *
     * @param format Format of ByteBuf dumping
     *
     * <p>指定 ByteBuf 格式，级别仍为 DEBUG。</p>
     */
    public LoggingHandler(ByteBufFormat format) {
        this(DEFAULT_LEVEL, format);
    }

    /**
     * Creates a new instance whose logger name is the fully qualified class
     * name of the instance.
     *
     * @param level the log level
     *
     * <p>指定日志级别，ByteBuf 默认 HEX_DUMP。</p>
     */
    public LoggingHandler(LogLevel level) {
        this(level, ByteBufFormat.HEX_DUMP);
    }

    /**
     * Creates a new instance whose logger name is the fully qualified class
     * name of the instance.
     *
     * @param level the log level
     * @param byteBufFormat the ByteBuf format
     */
    public LoggingHandler(LogLevel level, ByteBufFormat byteBufFormat) {
        this.level = ObjectUtil.checkNotNull(level, "level");
        this.byteBufFormat = ObjectUtil.checkNotNull(byteBufFormat, "byteBufFormat");
        logger = InternalLoggerFactory.getInstance(getClass());
        internalLevel = level.toInternalLevel();
    }

    /**
     * Creates a new instance with the specified logger name and with hex dump
     * enabled.
     *
     * @param clazz the class type to generate the logger for
     *
     * <p>以指定 Class 名创建 logger。</p>
     */
    public LoggingHandler(Class<?> clazz) {
        this(clazz, DEFAULT_LEVEL);
    }

    /**
     * Creates a new instance with the specified logger name.
     *
     * @param clazz the class type to generate the logger for
     * @param level the log level
     */
    public LoggingHandler(Class<?> clazz, LogLevel level) {
        this(clazz, level, ByteBufFormat.HEX_DUMP);
    }

    /**
     * Creates a new instance with the specified logger name.
     *
     * @param clazz the class type to generate the logger for
     * @param level the log level
     * @param byteBufFormat the ByteBuf format
     */
    public LoggingHandler(Class<?> clazz, LogLevel level, ByteBufFormat byteBufFormat) {
        ObjectUtil.checkNotNull(clazz, "clazz");
        this.level = ObjectUtil.checkNotNull(level, "level");
        this.byteBufFormat = ObjectUtil.checkNotNull(byteBufFormat, "byteBufFormat");
        logger = InternalLoggerFactory.getInstance(clazz);
        internalLevel = level.toInternalLevel();
    }

    /**
     * Creates a new instance with the specified logger name using the default log level.
     *
     * @param name the name of the class to use for the logger
     */
    public LoggingHandler(String name) {
        this(name, DEFAULT_LEVEL);
    }

    /**
     * Creates a new instance with the specified logger name.
     *
     * @param name the name of the class to use for the logger
     * @param level the log level
     */
    public LoggingHandler(String name, LogLevel level) {
        this(name, level, ByteBufFormat.HEX_DUMP);
    }

    /**
     * Creates a new instance with the specified logger name.
     *
     * @param name the name of the class to use for the logger
     * @param level the log level
     * @param byteBufFormat the ByteBuf format
     */
    public LoggingHandler(String name, LogLevel level, ByteBufFormat byteBufFormat) {
        ObjectUtil.checkNotNull(name, "name");

        this.level = ObjectUtil.checkNotNull(level, "level");
        this.byteBufFormat = ObjectUtil.checkNotNull(byteBufFormat, "byteBufFormat");
        logger = InternalLoggerFactory.getInstance(name);
        internalLevel = level.toInternalLevel();
    }

    /**
     * Returns the {@link LogLevel} that this handler uses to log
     *
     * <p>返回当前配置的日志级别。</p>
     */
    public LogLevel level() {
        return level;
    }

    /**
     * Returns the {@link ByteBufFormat} that this handler uses to log
     *
     * <p>返回 ByteBuf 的日志格式。</p>
     */
    public ByteBufFormat byteBufFormat() {
        return byteBufFormat;
    }

    /** 记录 REGISTERED 并向下传播。 */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "REGISTERED"));
        }
        ctx.fireChannelRegistered();
    }

    /** 记录 UNREGISTERED 并向下传播。 */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "UNREGISTERED"));
        }
        ctx.fireChannelUnregistered();
    }

    /** 记录 ACTIVE 并向下传播。 */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "ACTIVE"));
        }
        ctx.fireChannelActive();
    }

    /** 记录 INACTIVE 并向下传播。 */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "INACTIVE"));
        }
        ctx.fireChannelInactive();
    }

    /** 记录 EXCEPTION（含堆栈）并向下传播。 */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "EXCEPTION", cause), cause);
        }
        ctx.fireExceptionCaught(cause);
    }

    /** 记录 USER_EVENT 并向下传播。 */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "USER_EVENT", evt));
        }
        ctx.fireUserEventTriggered(evt);
    }

    /** 记录 BIND 并委托 ctx.bind。 */
    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "BIND", localAddress));
        }
        ctx.bind(localAddress, promise);
    }

    /** 记录 CONNECT（远端与本地地址）并委托 ctx.connect。 */
    @Override
    public void connect(
            ChannelHandlerContext ctx,
            SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "CONNECT", remoteAddress, localAddress));
        }
        ctx.connect(remoteAddress, localAddress, promise);
    }

    /** 记录 DISCONNECT 并委托 ctx.disconnect。 */
    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "DISCONNECT"));
        }
        ctx.disconnect(promise);
    }

    /** 记录 CLOSE 并委托 ctx.close。 */
    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "CLOSE"));
        }
        ctx.close(promise);
    }

    /** 记录 DEREGISTER 并委托 ctx.deregister。 */
    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "DEREGISTER"));
        }
        ctx.deregister(promise);
    }

    /** 记录 READ COMPLETE 并向下传播。 */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "READ COMPLETE"));
        }
        ctx.fireChannelReadComplete();
    }

    /** 记录 READ（含 ByteBuf 格式化）并向下传播。 */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "READ", msg));
        }
        ctx.fireChannelRead(msg);
    }

    /** 记录 WRITE 并委托 ctx.write。 */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "WRITE", msg));
        }
        ctx.write(msg, promise);
    }

    /** 记录 WRITABILITY CHANGED 并向下传播。 */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "WRITABILITY CHANGED"));
        }
        ctx.fireChannelWritabilityChanged();
    }

    /** 记录 FLUSH 并委托 ctx.flush。 */
    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, "FLUSH"));
        }
        ctx.flush();
    }

    /**
     * Formats an event and returns the formatted message.
     *
     * @param eventName the name of the event
     *
     * <p>无参数事件：{@code channel.toString() + 事件名}。</p>
     */
    protected String format(ChannelHandlerContext ctx, String eventName) {
        String chStr = ctx.channel().toString();
        return new StringBuilder(chStr.length() + 1 + eventName.length())
            .append(chStr)
            .append(' ')
            .append(eventName)
            .toString();
    }

    /**
     * Formats an event and returns the formatted message.
     *
     * @param eventName the name of the event
     * @param arg       the argument of the event
     *
     * <p>单参数事件：ByteBuf/ByteBufHolder 走专用格式化，其余走简单字符串。</p>
     */
    protected String format(ChannelHandlerContext ctx, String eventName, Object arg) {
        if (arg instanceof ByteBuf) {
            return formatByteBuf(ctx, eventName, (ByteBuf) arg);
        } else if (arg instanceof ByteBufHolder) {
            return formatByteBufHolder(ctx, eventName, (ByteBufHolder) arg);
        } else {
            return formatSimple(ctx, eventName, arg);
        }
    }

    /**
     * Formats an event and returns the formatted message.  This method is currently only used for formatting
     * {@link ChannelOutboundHandler#connect(ChannelHandlerContext, SocketAddress, SocketAddress, ChannelPromise)}.
     *
     * @param eventName the name of the event
     * @param firstArg  the first argument of the event
     * @param secondArg the second argument of the event
     *
     * <p>双参数事件（如 CONNECT）；第二参数为 null 时退化为单参数格式。</p>
     */
    protected String format(ChannelHandlerContext ctx, String eventName, Object firstArg, Object secondArg) {
        if (secondArg == null) {
            return formatSimple(ctx, eventName, firstArg);
        }

        String chStr = ctx.channel().toString();
        String arg1Str = String.valueOf(firstArg);
        String arg2Str = secondArg.toString();
        StringBuilder buf = new StringBuilder(
                chStr.length() + 1 + eventName.length() + 2 + arg1Str.length() + 2 + arg2Str.length());
        buf.append(chStr).append(' ').append(eventName).append(": ").append(arg1Str).append(", ").append(arg2Str);
        return buf.toString();
    }

    /**
     * Generates the default log message of the specified event whose argument is a {@link ByteBuf}.
     *
     * <p>输出可读字节数；{@link ByteBufFormat#HEX_DUMP} 时追加 pretty hex dump。</p>
     */
    private String formatByteBuf(ChannelHandlerContext ctx, String eventName, ByteBuf msg) {
        String chStr = ctx.channel().toString();
        int length = msg.readableBytes();
        if (length == 0) {
            StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 4);
            buf.append(chStr).append(' ').append(eventName).append(": 0B");
            return buf.toString();
        } else {
            int outputLength = chStr.length() + 1 + eventName.length() + 2 + 10 + 1;
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                int rows = length / 16 + (length % 15 == 0? 0 : 1) + 4;
                int hexDumpLength = 2 + rows * 80;
                outputLength += hexDumpLength;
            }
            StringBuilder buf = new StringBuilder(outputLength);
            buf.append(chStr).append(' ').append(eventName).append(": ").append(length).append('B');
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                buf.append(NEWLINE);
                appendPrettyHexDump(buf, msg);
            }

            return buf.toString();
        }
    }

    /**
     * Generates the default log message of the specified event whose argument is a {@link ByteBufHolder}.
     *
     * <p>在 ByteBuf 格式基础上附加 Holder 的 {@code toString()}。</p>
     */
    private String formatByteBufHolder(ChannelHandlerContext ctx, String eventName, ByteBufHolder msg) {
        String chStr = ctx.channel().toString();
        String msgStr = msg.toString();
        ByteBuf content = msg.content();
        int length = content.readableBytes();
        if (length == 0) {
            StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length() + 4);
            buf.append(chStr).append(' ').append(eventName).append(", ").append(msgStr).append(", 0B");
            return buf.toString();
        } else {
            int outputLength = chStr.length() + 1 + eventName.length() + 2 + msgStr.length() + 2 + 10 + 1;
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                int rows = length / 16 + (length % 15 == 0? 0 : 1) + 4;
                int hexDumpLength = 2 + rows * 80;
                outputLength += hexDumpLength;
            }
            StringBuilder buf = new StringBuilder(outputLength);
            buf.append(chStr).append(' ').append(eventName).append(": ")
               .append(msgStr).append(", ").append(length).append('B');
            if (byteBufFormat == ByteBufFormat.HEX_DUMP) {
                buf.append(NEWLINE);
                appendPrettyHexDump(buf, content);
            }

            return buf.toString();
        }
    }

    /**
     * Generates the default log message of the specified event whose argument is an arbitrary object.
     *
     * <p>通用单参数格式：channel + 事件名 + {@code String.valueOf(msg)}。</p>
     */
    private static String formatSimple(ChannelHandlerContext ctx, String eventName, Object msg) {
        String chStr = ctx.channel().toString();
        String msgStr = String.valueOf(msg);
        StringBuilder buf = new StringBuilder(chStr.length() + 1 + eventName.length() + 2 + msgStr.length());
        return buf.append(chStr).append(' ').append(eventName).append(": ").append(msgStr).toString();
    }
}
