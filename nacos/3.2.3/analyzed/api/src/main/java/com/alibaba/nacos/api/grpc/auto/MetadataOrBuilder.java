/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.grpc.auto;

/**
 * {@link Metadata} 的 Protobuf 构建器接口。
 *
 * <p>提供 type、clientIp 与 headers 等字段的只读访问。</p>
 */
public interface MetadataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Metadata)
    com.google.protobuf.MessageOrBuilder {

  /**
   * 获取消息类型标识。
   *
   * <p>对应 Protobuf 字段 {@code type = 3}。</p>
   */
  String getType();
  /**
   * <code>string type = 3;</code>
    * <p>Nacos API；详见上方说明。</p>
   */
  com.google.protobuf.ByteString
      getTypeBytes();

  /**
   * 获取客户端 IP 地址。
   *
   * <p>对应 Protobuf 字段 {@code clientIp = 8}。</p>
   */
  String getClientIp();
  /**
   * <code>string clientIp = 8;</code>
    * <p>Nacos API；详见上方说明。</p>
   */
  com.google.protobuf.ByteString
      getClientIpBytes();

  /**
   * 获取扩展头数量。
   *
   * <p>对应 Protobuf map 字段 {@code headers = 7}。</p>
   */
  int getHeadersCount();
  /**
   * <code>map&lt;string, string&gt; headers = 7;</code>
    * <p>Nacos API；详见上方说明。</p>
   */
  boolean containsHeaders(
      String key);
  /**
   * Use {@link #getHeadersMap()} instead.
    * <p>Nacos API；详见上方说明。</p>
   */
  @Deprecated
  java.util.Map<String, String>
  getHeaders();
  /**
   * <code>map&lt;string, string&gt; headers = 7;</code>
    * <p>Nacos API；详见上方说明。</p>
   */
  java.util.Map<String, String>
  getHeadersMap();
  /**
   * <code>map&lt;string, string&gt; headers = 7;</code>
    * <p>Nacos API；详见上方说明。</p>
   */

  String getHeadersOrDefault(
      String key,
      String defaultValue);
  /**
   * <code>map&lt;string, string&gt; headers = 7;</code>
    * <p>Nacos API；详见上方说明。</p>
   */

  String getHeadersOrThrow(
      String key);
}
