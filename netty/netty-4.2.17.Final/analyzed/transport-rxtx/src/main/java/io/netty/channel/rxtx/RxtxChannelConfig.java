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

import gnu.io.SerialPort;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;

/**
 * A configuration class for RXTX device connections.
 * <p>RXTX 串口通道配置：在通用 {@link ChannelConfig} 之上暴露波特率、 数据位/停止位/校验位、DTR/RTS 流控及打开后等待时间等串口参数。 基于 gnu.io RXTX 库，下一主版本将移除。</p>
 *
 * <h3>Available options</h3>
 *
 * In addition to the options provided by {@link ChannelConfig},
 * {@link DefaultRxtxChannelConfig} allows the following options in the option map:
 *
 * <table border="1" cellspacing="0" cellpadding="6">
 * <tr>
 * <th>Name</th><th>Associated setter method</th>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#BAUD_RATE}</td><td>{@link #setBaudrate(int)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#DTR}</td><td>{@link #setDtr(boolean)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#RTS}</td><td>{@link #setRts(boolean)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#STOP_BITS}</td><td>{@link #setStopbits(Stopbits)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#DATA_BITS}</td><td>{@link #setDatabits(Databits)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#PARITY_BIT}</td><td>{@link #setParitybit(Paritybit)}</td>
 * </tr><tr>
 * <td>{@link RxtxChannelOption#WAIT_TIME}</td><td>{@link #setWaitTimeMillis(int)}</td>
 * </tr>
 * </table>
 *
 * @deprecated this transport will be removed in the next major version.
 */
@Deprecated
public interface RxtxChannelConfig extends ChannelConfig {
    enum Stopbits {
        /**
         * 1 stop bit will be sent at the end of every character
         * <p>每字符末尾发送 1 个停止位（最常见）。</p>
         */
        STOPBITS_1(SerialPort.STOPBITS_1),
        /**
         * 2 stop bits will be sent at the end of every character
         * <p>每字符末尾发送 2 个停止位，便于慢速链路同步。</p>
         */
        STOPBITS_2(SerialPort.STOPBITS_2),
        /**
         * 1.5 stop bits will be sent at the end of every character
         * <p>每字符末尾发送 1.5 个停止位（部分 UART 支持）。</p>
         */
        STOPBITS_1_5(SerialPort.STOPBITS_1_5);

        private final int value;

        Stopbits(int value) {
            this.value = value;
        }

        /** 返回 RXTX {@link SerialPort} 常量值 */
        public int value() {
            return value;
        }

        /** 按底层整型值解析停止位枚举 */
        public static Stopbits valueOf(int value) {
            for (Stopbits stopbit : Stopbits.values()) {
                if (stopbit.value == value) {
                    return stopbit;
                }
            }
            throw new IllegalArgumentException("unknown " + Stopbits.class.getSimpleName() + " value: " + value);
        }
    }

    enum Databits {
        /**
         * 5 data bits will be used for each character (ie. Baudot code)
         * <p>每字符 5 数据位（如 Baudot 电码）。</p>
         */
        DATABITS_5(SerialPort.DATABITS_5),
        /**
         * 6 data bits will be used for each character
         * <p>每字符 6 数据位。</p>
         */
        DATABITS_6(SerialPort.DATABITS_6),
        /**
         * 7 data bits will be used for each character (ie. ASCII)
         * <p>每字符 7 数据位（传统 ASCII）。</p>
         */
        DATABITS_7(SerialPort.DATABITS_7),
        /**
         * 8 data bits will be used for each character (ie. binary data)
         * <p>每字符 8 数据位（二进制/现代默认）。</p>
         */
        DATABITS_8(SerialPort.DATABITS_8);

        private final int value;

        Databits(int value) {
            this.value = value;
        }

        /** 返回 RXTX 数据位常量值 */
        public int value() {
            return value;
        }

        /** 按底层整型值解析数据位枚举 */
        public static Databits valueOf(int value) {
            for (Databits databit : Databits.values()) {
                if (databit.value == value) {
                    return databit;
                }
            }
            throw new IllegalArgumentException("unknown " + Databits.class.getSimpleName() + " value: " + value);
        }
    }

    enum Paritybit {
        /**
         * No parity bit will be sent with each data character at all
         * <p>无校验位（默认）。</p>
         */
        NONE(SerialPort.PARITY_NONE),
        /**
         * An odd parity bit will be sent with each data character, ie. will be set
         * to 1 if the data character contains an even number of bits set to 1.
         * <p>奇校验：数据位中 1 的个数为偶数时校验位为 1。</p>
         */
        ODD(SerialPort.PARITY_ODD),
        /**
         * An even parity bit will be sent with each data character, ie. will be set
         * to 1 if the data character contains an odd number of bits set to 1.
         * <p>偶校验：数据位中 1 的个数为奇数时校验位为 1。</p>
         */
        EVEN(SerialPort.PARITY_EVEN),
        /**
         * A mark parity bit (ie. always 1) will be sent with each data character
         * <p>标记校验：校验位恒为 1。</p>
         */
        MARK(SerialPort.PARITY_MARK),
        /**
         * A space parity bit (ie. always 0) will be sent with each data character
         * <p>空格校验：校验位恒为 0。</p>
         */
        SPACE(SerialPort.PARITY_SPACE);

        private final int value;

        Paritybit(int value) {
            this.value = value;
        }

        /** 返回 RXTX 校验位常量值 */
        public int value() {
            return value;
        }

        /** 按底层整型值解析校验位枚举 */
        public static Paritybit valueOf(int value) {
            for (Paritybit paritybit : Paritybit.values()) {
                if (paritybit.value == value) {
                    return paritybit;
                }
            }
            throw new IllegalArgumentException("unknown " + Paritybit.class.getSimpleName() + " value: " + value);
        }
    }
    /**
     * Sets the baud rate (ie. bits per second) for communication with the serial device.
     * The baud rate will include bits for framing (in the form of stop bits and parity),
     * such that the effective data rate will be lower than this value.
     * <p>设置波特率（含帧格式开销，有效净荷速率低于标称值）。</p>
     *
     * @param baudrate The baud rate (in bits per second)
     */
    RxtxChannelConfig setBaudrate(int baudrate);

    /**
     * Sets the number of stop bits to include at the end of every character to aid the
     * serial device in synchronising with the data.
     * <p>设置停止位数量，帮助接收端字符同步。</p>
     *
     * @param stopbits The number of stop bits to use
     */
    RxtxChannelConfig setStopbits(Stopbits stopbits);

    /**
     * Sets the number of data bits to use to make up each character sent to the serial
     * device.
     * <p>设置每字符数据位数。</p>
     *
     * @param databits The number of data bits to use
     */
    RxtxChannelConfig setDatabits(Databits databits);

    /**
     * Sets the type of parity bit to be used when communicating with the serial device.
     * <p>设置校验方式（无/奇/偶/标记/空格）。</p>
     *
     * @param paritybit The type of parity bit to be used
     */
    RxtxChannelConfig setParitybit(Paritybit paritybit);

    /**
     * @return The configured baud rate, defaulting to 115200 if unset
     * <p>返回当前波特率，未设置时默认 115200。</p>
     */
    int getBaudrate();

    /**
     * @return The configured stop bits, defaulting to {@link Stopbits#STOPBITS_1} if unset
     * <p>返回停止位配置，默认 {@link Stopbits#STOPBITS_1}。</p>
     */
    Stopbits getStopbits();

    /**
     * @return The configured data bits, defaulting to {@link Databits#DATABITS_8} if unset
     * <p>返回数据位配置，默认 {@link Databits#DATABITS_8}。</p>
     */
    Databits getDatabits();

    /**
     * @return The configured parity bit, defaulting to {@link Paritybit#NONE} if unset
     * <p>返回校验位配置，默认 {@link Paritybit#NONE}。</p>
     */
    Paritybit getParitybit();

    /**
     * @return true if the serial device should support the Data Terminal Ready signal
     * <p>是否启用 DTR（数据终端就绪）硬件流控信号。</p>
     */
    boolean isDtr();

    /**
     * Sets whether the serial device supports the Data Terminal Ready signal, used for
     * flow control
     * <p>设置是否驱动 DTR 信号用于流控。</p>
     *
     * @param dtr true if DTR is supported, false otherwise
     */
    RxtxChannelConfig setDtr(boolean dtr);

    /**
     * @return true if the serial device should support the Ready to Send signal
     * <p>是否启用 RTS（请求发送）硬件流控信号。</p>
     */
    boolean isRts();

    /**
     * Sets whether the serial device supports the Request To Send signal, used for flow
     * control
     * <p>设置是否驱动 RTS 信号用于流控。</p>
     *
     * @param rts true if RTS is supported, false otherwise
     */
    RxtxChannelConfig setRts(boolean rts);

    /**
     * @return The number of milliseconds to wait between opening the serial port and
     *     initialising.
     * <p>返回打开串口后、下发配置前的等待毫秒数。</p>
     */
    int getWaitTimeMillis();

    /**
     * Sets the time to wait after opening the serial port and before sending it any
     * configuration information or data. A value of 0 indicates that no waiting should
     * occur.
     * <p>设置打开后延迟；0 表示不等待。部分 USB 转串口芯片需要短暂 settle 时间。</p>
     *
     * @param waitTimeMillis The number of milliseconds to wait, defaulting to 0 (no
     *     wait) if unset
     * @throws IllegalArgumentException if the supplied value is &lt; 0
     */
    RxtxChannelConfig setWaitTimeMillis(int waitTimeMillis);

    /**
     * Sets the maximal time (in ms) to block while try to read from the serial port. Default is 1000ms
     * <p>设置读串口时的最大阻塞毫秒数，默认 1000ms。</p>
     */
    RxtxChannelConfig setReadTimeout(int readTimeout);

    /**
     * Return the maximal time (in ms) to block and wait for something to be ready to read.
     * <p>返回读超时配置（毫秒）。</p>
     */
    int getReadTimeout();

    @Override
    RxtxChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    @Deprecated
    RxtxChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    RxtxChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    RxtxChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    RxtxChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    RxtxChannelConfig setAutoRead(boolean autoRead);

    @Override
    RxtxChannelConfig setAutoClose(boolean autoClose);

    @Override
    RxtxChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    RxtxChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    @Override
    RxtxChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark);

    @Override
    RxtxChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);
}
