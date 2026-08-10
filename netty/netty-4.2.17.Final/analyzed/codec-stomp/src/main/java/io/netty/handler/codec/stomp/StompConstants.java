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
package io.netty.handler.codec.stomp;

/** STOMP 帧编解码使用的 ASCII 控制字符常量。 */
final class StompConstants {

    /** 回车符（CR，0x0D），行结束符的一部分。 */
    static final byte CR = 13;
    /** 换行符（LF，0x0A），命令行与头部行均以 CRLF 结束。 */
    static final byte LF = 10;
    /** 空字节（NUL，0x00），标记帧正文结束。 */
    static final byte NUL = 0;
    /** 冒号（:），分隔头部名与值。 */
    static final byte COLON = 58;

    private StompConstants() { }
}
