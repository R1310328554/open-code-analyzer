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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/***
 * DatagramSocket 工厂接口：由 {@link DatagramSocketClient} 调用以创建可插拔的 UDP 套接字。
 * <p>
 * 便于在创建前做权限检查或注入自定义 Socket 实现。
 ***/

public interface DatagramSocketFactory
{

    /***
     * Creates a DatagramSocket on the local host at the first available port.
     * @return the socket
     *
     * @exception SocketException If the socket could not be created.
     ***/
    /** 在本地主机首个可用端口创建 DatagramSocket。 */
    public DatagramSocket createDatagramSocket() throws SocketException;

    /***
     * Creates a DatagramSocket on the local host at a specified port.
     *
     * @param port The port to use for the socket.
     * @return the socket
     * @exception SocketException If the socket could not be created.
     ***/
    public DatagramSocket createDatagramSocket(int port) throws SocketException;

    /***
     * Creates a DatagramSocket at the specified address on the local host
     * at a specified port.
     *
     * @param port The port to use for the socket.
     * @param laddr  The local address to use.
     * @return the socket
     * @exception SocketException If the socket could not be created.
     ***/
    public DatagramSocket createDatagramSocket(int port, InetAddress laddr)
    throws SocketException;
}
