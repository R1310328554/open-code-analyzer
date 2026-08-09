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
 * Telnet 输入监听器：当 {@link TelnetClient} 输入流有新数据可读时触发回调。
 * <p>
 * 配合 {@link TelnetClient#registerInputListener} 注册，用于异步通知上层读取。
 *
 * @see TelnetClient
 * @since 3.0
 ***/
public interface TelnetInputListener
{

    /***
     * 输入流有新数据到达时的回调；调用方应在此方法中读取 {@link TelnetClient#getInputStream}。
     *
     * @see TelnetClient#registerInputListener
     ***/
    public void telnetInputAvailable();
}
