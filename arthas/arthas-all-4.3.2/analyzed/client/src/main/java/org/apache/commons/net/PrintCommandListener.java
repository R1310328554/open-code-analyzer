/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net;

import java.io.PrintStream;
import java.io.PrintWriter;

/***
 * 协议命令监听器示例实现：将命令/回复流量打印到指定输出流，便于调试。
 * <p>
 * /***
 * This is a support class for some of the example programs.  It is
 * a sample implementation of the ProtocolCommandListener interface
 * which just prints out to a specified stream all command/reply traffic.
 *
 * @since 2.0
 ***/

public class PrintCommandListener implements ProtocolCommandListener
{
    /** 命令与回复的输出目标。 */
    private final PrintWriter __writer;
    /** 为 true 时隐藏 USER/PASS/LOGIN 等凭据内容。 */
    private final boolean __nologin;
    private final char __eolMarker;
    private final boolean __directionMarker;

    /**
     * Create the default instance which prints everything.
     *
     * @param stream where to write the commands and responses
     * e.g. System.out
     * @since 3.0
     */
    public PrintCommandListener(PrintStream stream)
    {
        this(new PrintWriter(stream));
    }

    /**
     * Create an instance which optionally suppresses login command text
     * and indicates where the EOL starts with the specified character.
     *
     * @param stream where to write the commands and responses
     * @param suppressLogin if {@code true}, only print command name for login
     *
     * @since 3.0
     */
    public PrintCommandListener(PrintStream stream, boolean suppressLogin) {
        this(new PrintWriter(stream), suppressLogin);
    }

    /**
     * Create an instance which optionally suppresses login command text
     * and indicates where the EOL starts with the specified character.
     *
     * @param stream where to write the commands and responses
     * @param suppressLogin if {@code true}, only print command name for login
     * @param eolMarker if non-zero, add a marker just before the EOL.
     *
     * @since 3.0
     */
    public PrintCommandListener(PrintStream stream, boolean suppressLogin, char eolMarker) {
        this(new PrintWriter(stream), suppressLogin, eolMarker);
    }

    /**
     * Create an instance which optionally suppresses login command text
     * and indicates where the EOL starts with the specified character.
     *
     * @param stream where to write the commands and responses
     * @param suppressLogin if {@code true}, only print command name for login
     * @param eolMarker if non-zero, add a marker just before the EOL.
     * @param showDirection if {@code true}, add {@code "> "} or {@code "< "} as appropriate to the output
     *
     * @since 3.0
     */
    public PrintCommandListener(PrintStream stream, boolean suppressLogin, char eolMarker, boolean showDirection) {
        this(new PrintWriter(stream), suppressLogin, eolMarker, showDirection);
    }

    /**
     * Create the default instance which prints everything.
     *
     * @param writer where to write the commands and responses
     */
    public PrintCommandListener(PrintWriter writer)
    {
        this(writer, false);
    }

    /**
     * Create an instance which optionally suppresses login command text.
     *
     * @param writer where to write the commands and responses
     * @param suppressLogin if {@code true}, only print command name for login
     *
     * @since 3.0
     */
    public PrintCommandListener(PrintWriter writer, boolean suppressLogin)
    {
        this(writer, suppressLogin, (char) 0);
    }

    /**
     * Create an instance which optionally suppresses login command text
     * and indicates where the EOL starts with the specified character.
     *
     * @param writer where to write the commands and responses
     * @param suppressLogin if {@code true}, only print command name for login
     * @param eolMarker if non-zero, add a marker just before the EOL.
     *
     * @since 3.0
     */
    public PrintCommandListener(PrintWriter writer, boolean suppressLogin, char eolMarker)
    {
        this(writer, suppressLogin, eolMarker, false);
    }

    /**
     * Create an instance which optionally suppresses login command text
     * and indicates where the EOL starts with the specified character.
     *
     * @param writer where to write the commands and responses
     * @param suppressLogin if {@code true}, only print command name for login
     * @param eolMarker if non-zero, add a marker just before the EOL.
     * @param showDirection if {@code true}, add {@code ">} " or {@code "< "} as appropriate to the output
     *
     * @since 3.0
     */
    public PrintCommandListener(PrintWriter writer, boolean suppressLogin, char eolMarker, boolean showDirection)
    {
        __writer = writer;
        __nologin = suppressLogin;
        __eolMarker = eolMarker;
        __directionMarker = showDirection;
    }

    @Override
        // 发送方向：可选打印 "> " 前缀并处理登录脱敏
    public void protocolCommandSent(ProtocolCommandEvent event)
    {
        if (__directionMarker) {
            __writer.print("> ");
        }
        if (__nologin) {
            String cmd = event.getCommand();
            if ("PASS".equalsIgnoreCase(cmd) || "USER".equalsIgnoreCase(cmd)) {
                __writer.print(cmd);
                __writer.println(" *******"); // Don't bother with EOL marker for this!
            } else {
                final String IMAP_LOGIN = "LOGIN";
                if (IMAP_LOGIN.equalsIgnoreCase(cmd)) { // IMAP
                    String msg = event.getMessage();
                    msg=msg.substring(0, msg.indexOf(IMAP_LOGIN)+IMAP_LOGIN.length());
                    __writer.print(msg);
                    __writer.println(" *******"); // Don't bother with EOL marker for this!
                } else {
                    __writer.print(getPrintableString(event.getMessage()));
                }
            }
        } else {
            __writer.print(getPrintableString(event.getMessage()));
        }
        __writer.flush();
    }

    private String getPrintableString(String msg){
        if (__eolMarker == 0) {
            return msg;
        }
        int pos = msg.indexOf(SocketClient.NETASCII_EOL);
        if (pos > 0) {
            return msg.substring(0, pos) +
                    __eolMarker +
                    msg.substring(pos);
        }
        return msg;
    }

    @Override
        // 接收方向：可选打印 "< " 前缀
    public void protocolReplyReceived(ProtocolCommandEvent event)
    {
        if (__directionMarker) {
            __writer.print("< ");
        }
        __writer.print(event.getMessage());
        __writer.flush();
    }
}

