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

package org.apache.commons.net.telnet;

/***
 * Telnet 选项协商通知接口：远端发送 DO/DONT/WILL/WONT 等命令时回调。
 * <p>
 * 实现本接口并通过 {@link TelnetClient#registerNotificationHandler} 注册即可监听协商过程。
 ***/

public interface TelnetNotificationHandler
{
    /** 远端请求本地启用某选项（DO） */
    /***
     * The remote party sent a DO command.
     ***/
    public static final int RECEIVED_DO =   1;

    /** 远端请求本地关闭某选项（DONT） */
    /***
     * The remote party sent a DONT command.
     ***/
    public static final int RECEIVED_DONT = 2;

    /** 远端声明将启用某选项（WILL） */
    /***
     * The remote party sent a WILL command.
     ***/
    public static final int RECEIVED_WILL = 3;

    /** 远端声明将关闭某选项（WONT） */
    /***
     * The remote party sent a WONT command.
     ***/
    public static final int RECEIVED_WONT = 4;

    /***
     * The remote party sent a COMMAND.
     * @since 2.2
     ***/
    public static final int RECEIVED_COMMAND = 5;

    /***
     * 收到协商或 Telnet 命令时的回调。
     *
     * @param negotiation_code 协商类型（RECEIVED_DO 等）
     * @param option_code 选项码或命令码（如 NOP）
     ***/
    public void receivedNegotiation(int negotiation_code, int option_code);
}
