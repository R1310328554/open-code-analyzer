/*
 * Copyright 2016 The Netty Project
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
package io.netty.handler.codec.smtp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.AsciiString;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.UnstableApi;

import java.util.HashMap;
import java.util.Map;

/**
 * The command part of a {@link SmtpRequest}.
 * <p>SMTP 命令字的不可变值对象，预置 RFC 常用命令（EHLO、MAIL、DATA 等）单例。
 * 比较时忽略大小写；{@link #DATA} 是唯一 {@link #isContentExpected()} 为真的命令，
 * 表示后续须发送 {@link SmtpContent} 序列而非普通命令行。</p>
 */
@UnstableApi
public final class SmtpCommand {
    public static final SmtpCommand EHLO = new SmtpCommand(AsciiString.cached("EHLO"));
    public static final SmtpCommand HELO = new SmtpCommand(AsciiString.cached("HELO"));
    public static final SmtpCommand AUTH = new SmtpCommand(AsciiString.cached("AUTH"));
    public static final SmtpCommand MAIL = new SmtpCommand(AsciiString.cached("MAIL"));
    public static final SmtpCommand RCPT = new SmtpCommand(AsciiString.cached("RCPT"));
    /** 发送 DATA 后进入邮件正文传输阶段。 */
    public static final SmtpCommand DATA = new SmtpCommand(AsciiString.cached("DATA"));
    public static final SmtpCommand NOOP = new SmtpCommand(AsciiString.cached("NOOP"));
    /** 重置会话状态，可在 DATA 传输中途取消正文发送。 */
    public static final SmtpCommand RSET = new SmtpCommand(AsciiString.cached("RSET"));
    public static final SmtpCommand EXPN = new SmtpCommand(AsciiString.cached("EXPN"));
    public static final SmtpCommand VRFY = new SmtpCommand(AsciiString.cached("VRFY"));
    public static final SmtpCommand HELP = new SmtpCommand(AsciiString.cached("HELP"));
    public static final SmtpCommand QUIT = new SmtpCommand(AsciiString.cached("QUIT"));
    /** 空命令名，仅携带参数（扩展用法）。 */
    public static final SmtpCommand EMPTY = new SmtpCommand(AsciiString.cached(""));

    /** 标准命令名到单例的查找表，供 {@link #valueOf(CharSequence)} 快速命中。 */
    private static final Map<String, SmtpCommand> COMMANDS = new HashMap<String, SmtpCommand>();
    static {
        COMMANDS.put(EHLO.name().toString(), EHLO);
        COMMANDS.put(HELO.name().toString(), HELO);
        COMMANDS.put(AUTH.name().toString(), AUTH);
        COMMANDS.put(MAIL.name().toString(), MAIL);
        COMMANDS.put(RCPT.name().toString(), RCPT);
        COMMANDS.put(DATA.name().toString(), DATA);
        COMMANDS.put(NOOP.name().toString(), NOOP);
        COMMANDS.put(RSET.name().toString(), RSET);
        COMMANDS.put(EXPN.name().toString(), EXPN);
        COMMANDS.put(VRFY.name().toString(), VRFY);
        COMMANDS.put(HELP.name().toString(), HELP);
        COMMANDS.put(QUIT.name().toString(), QUIT);
        COMMANDS.put(EMPTY.name().toString(), EMPTY);
    }

    /**
     * Returns the {@link SmtpCommand} for the given command name.
     * <p>已知标准命令返回预置单例；未知命令按需创建新实例（仍可与同名实例 equals）。</p>
     */
    public static SmtpCommand valueOf(CharSequence commandName) {
        ObjectUtil.checkNotNull(commandName, "commandName");
        SmtpCommand command = COMMANDS.get(commandName.toString());
        return command != null ? command : new SmtpCommand(AsciiString.of(commandName));
    }

    private final AsciiString name;

    private SmtpCommand(AsciiString name) {
        this.name = name;
    }

    /**
     * Return the command name.
     * @return 命令 ASCII 名称（如 {@code "MAIL"}）。
     */
    public AsciiString name() {
        return name;
    }

    /** 将命令名写入缓冲区，不含空格与 CRLF。 */
    void encode(ByteBuf buffer) {
        ByteBufUtil.writeAscii(buffer, name);
    }

    /** 仅 {@link #DATA} 为 true：编码器发送 DATA 行后进入正文期待状态。 */
    boolean isContentExpected() {
        return this.equals(DATA);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SmtpCommand)) {
            return false;
        }
        return name.contentEqualsIgnoreCase(((SmtpCommand) obj).name());
    }

    @Override
    public String toString() {
        return "SmtpCommand{name=" + name + '}';
    }
}
