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
 * {@link Payload} 的 Protobuf 构建器接口。
 *
 * <p>提供 metadata 与 body 字段的只读访问。</p>
 */
public interface PayloadOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Payload)
    com.google.protobuf.MessageOrBuilder {

  /** 是否已设置 metadata 字段。 */
  boolean hasMetadata();
  /** 获取消息元数据。 */
  Metadata getMetadata();
  /**
   * <code>.Metadata metadata = 2;</code>
    * <p>Nacos API；详见上方说明。</p>
   */
  MetadataOrBuilder getMetadataOrBuilder();

  /** 是否已设置 body 字段。 */
  boolean hasBody();
  /** 获取业务消息体。 */
  com.google.protobuf.Any getBody();
  /**
   * <code>.google.protobuf.Any body = 3;</code>
    * <p>Nacos API；详见上方说明。</p>
   */
  com.google.protobuf.AnyOrBuilder getBodyOrBuilder();
}
