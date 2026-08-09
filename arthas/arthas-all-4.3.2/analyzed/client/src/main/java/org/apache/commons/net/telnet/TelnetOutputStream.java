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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Telnet 输出流：按 ASCII/二进制模式转换换行并转义 IAC（双写 255）。
 * <p>
 * ASCII 模式下 CR→CRLF、裸 LF→CRLF、裸 CR→CR\0，符合 RFC 854。
 * <p>
 ***/


final class TelnetOutputStream extends OutputStream
{
    /** 所属 Telnet 客户端，发送经其同步 */
    private final TelnetClient __client;
    // TODO there does not appear to be any way to change this value - should it be a ctor parameter?
    private final boolean __convertCRtoCRLF = true;
    private boolean __lastWasCR = false;

    /** 绑定 Telnet 客户端 */
    TelnetOutputStream(TelnetClient client)
    {
        __client = client;
    }


    /***
     * 写入一字节；ASCII 模式下处理 CR/LF 与 IAC 转义。
     * <p>
     * @param ch 待写字节
     * @exception IOException 底层写失败
     ***/
    @Override
    public void write(int ch) throws IOException
    {

        synchronized (__client)
        {
            ch &= 0xff;

            // 非二进制即 ASCII 模式：按 RFC854 转换行结束符
            if (__client._requestedWont(TelnetOption.BINARY)) // i.e. ASCII
            {
                if (__lastWasCR)
                {
                    if (__convertCRtoCRLF)
                    {
                        __client._sendByte('\n');
                        if (ch == '\n') // i.e. was CRLF anyway
                        {
                            __lastWasCR = false;
                            return ;
                        }
                    } // __convertCRtoCRLF
                    else if (ch != '\n')
                     {
                        __client._sendByte('\0'); // RFC854 requires CR NUL for bare CR
                    }
                }

                switch (ch)
                {
                case '\r':
                    __client._sendByte('\r');
                    __lastWasCR = true;
                    break;
                case '\n':
                    if (!__lastWasCR) { // convert LF to CRLF
                        __client._sendByte('\r');
                    }
                    __client._sendByte(ch);
                    __lastWasCR = false;
                    break;
                case TelnetCommand.IAC:
                    __client._sendByte(TelnetCommand.IAC);
                    __client._sendByte(TelnetCommand.IAC);
                    __lastWasCR = false;
                    break;
                default:
                    __client._sendByte(ch);
                    __lastWasCR = false;
                    break;
                }
            } // end ASCII
            else if (ch == TelnetCommand.IAC)
            {
                __client._sendByte(ch);
                __client._sendByte(TelnetCommand.IAC);
            } else {
                __client._sendByte(ch);
            }
        }
    }


    /***
     * Writes a byte array to the stream.
     * <p>
     * @param buffer  The byte array to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            stream.
     ***/
    @Override
    public void write(byte buffer[]) throws IOException
    {
        write(buffer, 0, buffer.length);
    }


    /***
     * Writes a number of bytes from a byte array to the stream starting from
     * a given offset.
     * <p>
     * @param buffer  The byte array to write.
     * @param offset  The offset into the array at which to start copying data.
     * @param length  The number of bytes to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            stream.
     ***/
    @Override
    public void write(byte buffer[], int offset, int length) throws IOException
    {
        synchronized (__client)
        {
            while (length-- > 0) {
                write(buffer[offset++]);
            }
        }
    }

    /** 刷新 Telnet 客户端输出缓冲 */
    /*** Flushes the stream. ***/
    @Override
    public void flush() throws IOException
    {
        __client._flushOutputStream();
    }

    /** 关闭 Telnet 客户端输出流 */
    /*** Closes the stream. ***/
    @Override
    public void close() throws IOException
    {
        __client._closeOutputStream();
    }
}
