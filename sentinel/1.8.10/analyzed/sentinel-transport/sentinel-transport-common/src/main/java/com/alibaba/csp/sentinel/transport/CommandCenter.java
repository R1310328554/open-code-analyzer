/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.transport;

/**
 * 传输层命令中心 SPI：负责注册命令、启动 HTTP/Netty 等服务端并优雅停止。
 *
 * @author Eric Zhao
 */
public interface CommandCenter {

    /**
     * 启动前初始化（如注册 {@link com.alibaba.csp.sentinel.command.CommandHandler}）。
     *
     * @throws Exception if error occurs
     */
    void beforeStart() throws Exception;

    /**
     * 在后台启动命令中心，本方法不得阻塞调用线程。
     *
     * @throws Exception if error occurs
     */
    void start() throws Exception;

    /**
     * 停止命令中心并释放资源。
     *
     * @throws Exception if error occurs
     */
    void stop() throws Exception;
}
