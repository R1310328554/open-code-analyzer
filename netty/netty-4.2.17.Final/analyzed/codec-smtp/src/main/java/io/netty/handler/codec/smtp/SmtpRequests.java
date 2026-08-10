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

import io.netty.util.AsciiString;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.UnstableApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides utility methods to create {@link SmtpRequest}s.
 * <p>常用 SMTP 客户端命令的工厂方法：无参命令（NOOP、DATA、RSET、QUIT）复用单例；
 * MAIL/RCPT 自动拼装 {@code FROM:<...>} / {@code TO:<...>} 并支持 ESMTP 扩展参数。</p>
 */
@UnstableApi
public final class SmtpRequests {

    private static final SmtpRequest DATA = new DefaultSmtpRequest(SmtpCommand.DATA);
    private static final SmtpRequest NOOP = new DefaultSmtpRequest(SmtpCommand.NOOP);
    private static final SmtpRequest RSET = new DefaultSmtpRequest(SmtpCommand.RSET);
    private static final SmtpRequest HELP_NO_ARG = new DefaultSmtpRequest(SmtpCommand.HELP);
    private static final SmtpRequest QUIT = new DefaultSmtpRequest(SmtpCommand.QUIT);
    /** null 发件人时 MAIL 命令使用的 {@code FROM:<>} 字面量。 */
    private static final AsciiString FROM_NULL_SENDER = AsciiString.cached("FROM:<>");

    /**
     * Creates a {@code HELO} request.
     * @param hostname 客户端标识主机名。
     */
    public static SmtpRequest helo(CharSequence hostname) {
        return new DefaultSmtpRequest(SmtpCommand.HELO, hostname);
    }

    /**
     * Creates a {@code EHLO} request.
     * @param hostname 客户端标识主机名，服务端应答可含 ESMTP 扩展列表。
     */
    public static SmtpRequest ehlo(CharSequence hostname) {
        return new DefaultSmtpRequest(SmtpCommand.EHLO, hostname);
    }

    /**
     * Creates a {@code EMPTY} request.
     * @param parameter 无命令字时的纯参数字段。
     */
    public static SmtpRequest empty(CharSequence... parameter) {
        return new DefaultSmtpRequest(SmtpCommand.EMPTY, parameter);
    }

    /**
     * Creates a {@code AUTH} request.
     * @param parameter 认证机制及凭证（如 {@code PLAIN}、Base64 载荷等）。
     */
    public static SmtpRequest auth(CharSequence... parameter) {
        return new DefaultSmtpRequest(SmtpCommand.AUTH, parameter);
    }

    /**
     * Creates a {@code NOOP} request.
     */
    public static SmtpRequest noop() {
        return NOOP;
    }

    /**
     * Creates a {@code DATA} request.
     * <p>发送后须跟随 {@link SmtpContent}/{@link LastSmtpContent} 序列。</p>
     */
    public static SmtpRequest data() {
        return DATA;
    }

    /**
     * Creates a {@code RSET} request.
     */
    public static SmtpRequest rset() {
        return RSET;
    }

    /**
     * Creates a {@code HELP} request.
     * @param cmd 可选子命令；{@code null} 时请求通用帮助。
     */
    public static SmtpRequest help(String cmd) {
        return cmd == null ? HELP_NO_ARG : new DefaultSmtpRequest(SmtpCommand.HELP, cmd);
    }

    /**
     * Creates a {@code QUIT} request.
     */
    public static SmtpRequest quit() {
        return QUIT;
    }

    /**
     * Creates a {@code MAIL} request.
     * @param sender 发件人地址；{@code null} 时使用 {@code FROM:<>}（空反向路径）。
     * @param mailParameters 可选 ESMTP MAIL 参数（SIZE、BODY 等）。
     */
    public static SmtpRequest mail(CharSequence sender, CharSequence... mailParameters) {
        if (mailParameters == null || mailParameters.length == 0) {
            return new DefaultSmtpRequest(SmtpCommand.MAIL,
                                          sender != null ? "FROM:<" + sender + '>' : FROM_NULL_SENDER);
        } else {
            List<CharSequence> params = new ArrayList<CharSequence>(mailParameters.length + 1);
            params.add(sender != null? "FROM:<" + sender + '>' : FROM_NULL_SENDER);
            Collections.addAll(params, mailParameters);
            return new DefaultSmtpRequest(SmtpCommand.MAIL, params);
        }
    }

    /**
     * Creates a {@code RCPT} request.
     * @param recipient 收件人地址（必填）。
     * @param rcptParameters 可选 ESMTP RCPT 参数。
     */
    public static SmtpRequest rcpt(CharSequence recipient, CharSequence... rcptParameters) {
        ObjectUtil.checkNotNull(recipient, "recipient");
        if (rcptParameters == null || rcptParameters.length == 0) {
            return new DefaultSmtpRequest(SmtpCommand.RCPT, "TO:<" + recipient + '>');
        } else {
            List<CharSequence> params = new ArrayList<CharSequence>(rcptParameters.length + 1);
            params.add("TO:<" + recipient + '>');
            Collections.addAll(params, rcptParameters);
            return new DefaultSmtpRequest(SmtpCommand.RCPT, params);
        }
    }

    /**
     * Creates a {@code EXPN} request.
     * @param mailingList 邮件列表名。
     */
    public static SmtpRequest expn(CharSequence mailingList) {
        return new DefaultSmtpRequest(SmtpCommand.EXPN, ObjectUtil.checkNotNull(mailingList, "mailingList"));
    }

    /**
     * Creates a {@code VRFY} request.
     * @param user 待验证的用户名或地址。
     */
    public static SmtpRequest vrfy(CharSequence user) {
        return new DefaultSmtpRequest(SmtpCommand.VRFY, ObjectUtil.checkNotNull(user, "user"));
    }

    private SmtpRequests() { }
}
