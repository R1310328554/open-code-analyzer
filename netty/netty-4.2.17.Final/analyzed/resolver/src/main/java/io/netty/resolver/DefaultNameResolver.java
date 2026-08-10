/*
 * Copyright 2015 The Netty Project
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

package io.netty.resolver;

import io.netty.util.internal.SocketUtils;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link InetNameResolver} that resolves using JDK's built-in domain name lookup mechanism.
 * Note that this resolver performs a blocking name lookup from the caller thread.
 * <p>使用 JDK 内置 {@code InetAddress} 机制解析主机名的默认实现。
 * 解析在调用线程中同步阻塞执行，适合作为通用后备解析器。</p>
 */
public class DefaultNameResolver extends InetNameResolver {

    public DefaultNameResolver(EventExecutor executor) {
        super(executor);
    }

    @Override
    protected void doResolve(String inetHost, Promise<InetAddress> promise) throws Exception {
        try {
            // SocketUtils 封装 JDK 调用，便于测试与平台差异隔离
            promise.setSuccess(SocketUtils.addressByName(inetHost));
        } catch (UnknownHostException e) {
            promise.setFailure(e);
        }
    }

    @Override
    protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) throws Exception {
        try {
            promise.setSuccess(Arrays.asList(SocketUtils.allAddressesByName(inetHost)));
        } catch (UnknownHostException e) {
            promise.setFailure(e);
        }
    }
}
