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
 * Telnet 选项处理器基类：管理本地/远端 WILL/DO 的初始值、接受策略及子协商响应。
 * <p>
 * 子类覆盖 {@link #answerSubnegotiation} 等以处理 TERMINAL-TYPE、NAWS 等子选项。
 ***/
public abstract class TelnetOptionHandler
{
    /** 本处理器对应的选项码 */
    private int optionCode = -1;

    /** 连接建立后是否主动发送 WILL（本地启用） */
    private boolean initialLocal = false;

    /** 连接建立后是否主动发送 DO（要求远端启用） */
    private boolean initialRemote = false;

    /***
     * true if the option should be accepted on the local side
     ***/
    private boolean acceptLocal = false;

    /***
     * true if the option should be accepted on the remote side
     ***/
    private boolean acceptRemote = false;

    /***
     * true if the option is active on the local side
     ***/
    private boolean doFlag = false;

    /***
     * true if the option is active on the remote side
     ***/
    private boolean willFlag = false;

    /***
     * 构造选项处理器，配置连接时的主动协商与对端请求的默认应答策略。
     * <p>
     * @param optcode 选项码
     * @param initlocal 为 true 时连接后发送 WILL
     * @param initremote 为 true 时连接后发送 DO
     * @param acceptlocal 为 true 时接受对端 DO
     * @param acceptremote 为 true 时接受对端 WILL
     ***/
    public TelnetOptionHandler(int optcode,
                                boolean initlocal,
                                boolean initremote,
                                boolean acceptlocal,
                                boolean acceptremote)
    {
        optionCode = optcode;
        initialLocal = initlocal;
        initialRemote = initremote;
        acceptLocal = acceptlocal;
        acceptRemote = acceptremote;
    }


    /***
     * Returns the option code for this option.
     * <p>
     * @return Option code.
     ***/
    public int getOptionCode()
    {
        return (optionCode);
    }

    /***
     * Returns a boolean indicating whether to accept a DO
     * request coming from the other end.
     * <p>
     * @return true if a DO request shall be accepted.
     ***/
    public boolean getAcceptLocal()
    {
        return (acceptLocal);
    }

    /***
     * Returns a boolean indicating whether to accept a WILL
     * request coming from the other end.
     * <p>
     * @return true if a WILL request shall be accepted.
     ***/
    public boolean getAcceptRemote()
    {
        return (acceptRemote);
    }

    /***
     * Set behaviour of the option for DO requests coming from
     * the other end.
     * <p>
     * @param accept - if true, subsequent DO requests will be accepted.
     ***/
    public void setAcceptLocal(boolean accept)
    {
        acceptLocal = accept;
    }

    /***
     * Set behaviour of the option for WILL requests coming from
     * the other end.
     * <p>
     * @param accept - if true, subsequent WILL requests will be accepted.
     ***/
    public void setAcceptRemote(boolean accept)
    {
        acceptRemote = accept;
    }

    /***
     * Returns a boolean indicating whether to send a WILL request
     * to the other end upon connection.
     * <p>
     * @return true if a WILL request shall be sent upon connection.
     ***/
    public boolean getInitLocal()
    {
        return (initialLocal);
    }

    /***
     * Returns a boolean indicating whether to send a DO request
     * to the other end upon connection.
     * <p>
     * @return true if a DO request shall be sent upon connection.
     ***/
    public boolean getInitRemote()
    {
        return (initialRemote);
    }

    /***
     * Tells this option whether to send a WILL request upon connection.
     * <p>
     * @param init - if true, a WILL request will be sent upon subsequent
     * connections.
     ***/
    public void setInitLocal(boolean init)
    {
        initialLocal = init;
    }

    /***
     * Tells this option whether to send a DO request upon connection.
     * <p>
     * @param init - if true, a DO request will be sent upon subsequent
     * connections.
     ***/
    public void setInitRemote(boolean init)
    {
        initialRemote = init;
    }

    /***
     * 收到对端子协商（IAC SB … IAC SE 内载荷）时调用；默认无响应。
     * <p>
     * @param suboptionData 子协商字节序列（不含 SB/SE）
     * @param suboptionLength 有效长度
     * @return 应答载荷，由 TelnetClient 包裹 SB/SE；null 表示不回复
     ***/
    public int[] answerSubnegotiation(int suboptionData[], int suboptionLength) {
        return null;
    }

    /***
     * 本地选项被确认激活（已发 WILL 且对端 DO）后，可返回主动发起的子协商序列。
     * @return 子协商载荷；null 表示不发送
     ***/
    public int[] startSubnegotiationLocal() {
        return null;
    }

    /***
     * 远端选项被确认激活（已发 DO 且对端 WILL）后，可返回主动发起的子协商序列。
     * @return 子协商载荷；null 表示不发送
     ***/
    public int[] startSubnegotiationRemote() {
        return null;
    }

    /***
     * Returns a boolean indicating whether a WILL request sent to the other
     * side has been acknowledged.
     * <p>
     * @return true if a WILL sent to the other side has been acknowledged.
     ***/
    boolean getWill()
    {
        return willFlag;
    }

    /***
     * Tells this option whether a WILL request sent to the other
     * side has been acknowledged (invoked by TelnetClient).
     * <p>
     * @param state - if true, a WILL request has been acknowledged.
     ***/
    void setWill(boolean state)
    {
        willFlag = state;
    }

    /***
     * Returns a boolean indicating whether a DO request sent to the other
     * side has been acknowledged.
     * <p>
     * @return true if a DO sent to the other side has been acknowledged.
     ***/
    boolean getDo()
    {
        return doFlag;
    }


    /***
     * Tells this option whether a DO request sent to the other
     * side has been acknowledged (invoked by TelnetClient).
     * <p>
     * @param state - if true, a DO request has been acknowledged.
     ***/
    void setDo(boolean state)
    {
        doFlag = state;
    }
}
