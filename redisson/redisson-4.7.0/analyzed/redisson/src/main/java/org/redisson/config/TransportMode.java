/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.config;

/**
 * Netty 底层 I/O 传输模式，影响事件循环与 socket 实现。
 * <p>
 * 通过 {@link Config#setTransportMode(TransportMode)} 设置；
 * 非 NIO 模式需对应 native 依赖在 classpath 中。
 *
 * @author Nikita Koksharov
 *
 */
public enum TransportMode {

    /** 使用 Java NIO 传输（跨平台默认选项）。 */
    NIO,

    /**
     * 使用 Linux EPOLL 传输；服务器绑定回环地址时可启用 Unix Domain Socket。
     * 需要 classpath 中包含 <b>netty-transport-native-epoll</b>。
     */
    EPOLL,

    /** 使用 BSD/macOS KQUEUE 传输，需 <b>netty-transport-native-kqueue</b> 依赖。 */
    KQUEUE,

    /** 使用 Linux io_uring 传输，需 <b>netty-transport-io_uring</b> 依赖。 */
    IO_URING,

}
