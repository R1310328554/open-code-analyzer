/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.rxtx;

import io.netty.channel.ChannelOption;
import io.netty.channel.rxtx.RxtxChannelConfig.Databits;
import io.netty.channel.rxtx.RxtxChannelConfig.Paritybit;
import io.netty.channel.rxtx.RxtxChannelConfig.Stopbits;

/**
 * Option for configuring a serial port connection
 * <p>RXTX 串口 {@link ChannelOption} 键：波特率、DTR/RTS、帧格式、 打开等待时间与读超时等，与 {@link RxtxChannelConfig} 一一对应。</p>
 *
 * @deprecated this transport will be removed in the next major version.
 */
@Deprecated
public final class RxtxChannelOption<T> extends ChannelOption<T> {

    /** 波特率（比特/秒） */
    public static final ChannelOption<Integer> BAUD_RATE = valueOf(RxtxChannelOption.class, "BAUD_RATE");
    /** 是否启用 DTR 流控信号 */
    public static final ChannelOption<Boolean> DTR = valueOf(RxtxChannelOption.class, "DTR");
    /** 是否启用 RTS 流控信号 */
    public static final ChannelOption<Boolean> RTS = valueOf(RxtxChannelOption.class, "RTS");
    /** 停止位枚举 {@link Stopbits} */
    public static final ChannelOption<Stopbits> STOP_BITS = valueOf(RxtxChannelOption.class, "STOP_BITS");
    /** 数据位枚举 {@link Databits} */
    public static final ChannelOption<Databits> DATA_BITS = valueOf(RxtxChannelOption.class, "DATA_BITS");
    /** 校验位枚举 {@link Paritybit} */
    public static final ChannelOption<Paritybit> PARITY_BIT = valueOf(RxtxChannelOption.class, "PARITY_BIT");
    /** 打开串口后的等待毫秒数 */
    public static final ChannelOption<Integer> WAIT_TIME = valueOf(RxtxChannelOption.class, "WAIT_TIME");
    /** 读操作最大阻塞毫秒数 */
    public static final ChannelOption<Integer> READ_TIMEOUT = valueOf(RxtxChannelOption.class, "READ_TIMEOUT");

    /** 禁止实例化，仅通过静态常量引用 */
    @SuppressWarnings({ "unused", "deprecation" })
    private RxtxChannelOption() {
        super(null);
    }
}
