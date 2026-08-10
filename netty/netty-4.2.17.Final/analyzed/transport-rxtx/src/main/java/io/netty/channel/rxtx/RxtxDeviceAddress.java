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

import java.net.SocketAddress;

/**
 * A {@link SocketAddress} subclass to wrap the serial port address of a RXTX
 * device (e.g. COM1, /dev/ttyUSB0).
 * <p>RXTX 串口设备地址：以端口名字符串标识端点（Windows {@code COMn}、 Linux {@code /dev/tty*} 等），用于 {@code bind/connect}。</p>
 *
 * @deprecated this transport will be removed in the next major version.
 */
@Deprecated
public class RxtxDeviceAddress extends SocketAddress {

    private static final long serialVersionUID = -2907820090993709523L;

    /** 串口设备路径或端口名 */
    private final String value;

    /**
     * Creates a RxtxDeviceAddress representing the address of the serial port.
     * <p>以端口名字符串构造地址。</p>
     *
     * @param value the address of the device (e.g. COM1, /dev/ttyUSB0, ...)
     */
    public RxtxDeviceAddress(String value) {
        this.value = value;
    }

    /**
     * @return The serial port address of the device (e.g. COM1, /dev/ttyUSB0, ...)
     * <p>返回串口设备名字符串。</p>
     */
    public String value() {
        return value;
    }
}
