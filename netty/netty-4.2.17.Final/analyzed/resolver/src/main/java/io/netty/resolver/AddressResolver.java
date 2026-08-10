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

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;

import java.io.Closeable;
import java.net.SocketAddress;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.List;

/**
 * 将可能未解析的 {@link SocketAddress} 解析为已解析地址。
 */
public interface AddressResolver<T extends SocketAddress> extends Closeable {

  /**
   * 当且仅当本解析器支持指定地址类型时返回 {@code true}。
   */
  boolean isSupported(SocketAddress address);

  /**
   * 当且仅当指定地址已完成解析时返回 {@code true}。
   *
   * @throws UnsupportedAddressTypeException 若地址类型不受本解析器支持
   */
  boolean isResolved(SocketAddress address);

  /**
   * 解析指定地址。若已解析则直接返回原地址。
   *
   * @param address 待解析地址
   *
   * @return 解析结果的 {@link SocketAddress}
   */
  Future<T> resolve(SocketAddress address);

  /**
   * 解析指定地址。若已解析则直接返回原地址。
   *
   * @param address 待解析地址
   * @param promise 解析完成时会被通知的 {@link Promise}
   *
   * @return 解析结果的 {@link SocketAddress}
   */
  Future<T> resolve(SocketAddress address, Promise<T> promise);

  /**
   * 解析指定地址并返回全部候选结果。若已解析则直接返回原地址。
   *
   * @param address 待解析地址
   *
   * @return 解析得到的 {@link SocketAddress} 列表
   */
  Future<List<T>> resolveAll(SocketAddress address);

  /**
   * 解析指定地址并返回全部候选结果。若已解析则直接返回原地址。
   *
   * @param address 待解析地址
   * @param promise 解析完成时会被通知的 {@link Promise}
   *
   * @return 解析得到的 {@link SocketAddress} 列表
   */
  Future<List<T>> resolveAll(SocketAddress address, Promise<List<T>> promise);

  /**
   * 关闭本解析器分配并使用的全部资源。
   */
  @Override
  void close();
}
