/*
 * Copyright 2020  Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.arthas.grpcweb.proxy;

/**
 * gRPC-Web 出站组帧器：为 payload 生成 5 字节帧头（类型 + 大端长度）。
 *
 * <p>与 {@link MessageDeframer} 对应，用于 {@link SendGrpcWebResponse} 写出 DATA 与 TRAILER 块。</p>
 */
public class MessageFramer {
  /** gRPC-Web 帧类型：DATA 或 TRAILER */
  public enum Type {
    /** 数据帧，首字节 0x00 */
    DATA ((byte) 0x00),
    /** Trailer 帧，首字节 0x80 */
    TRAILER ((byte) 0x80);

    /** 写入帧头的类型字节 */
    public final byte value;
    Type(byte b) {
      value = b;
    }
  }

  /**
   * 生成帧前缀：1 字节类型 + 4 字节大端 payload 长度。
   *
   * <p>TODO: 暂不支持单条消息超过约 2GB 时需拆成多帧。</p>
   *
   * @param in payload 字节
   * @param type DATA 或 TRAILER
   * @return 5 字节帧头
   */
  public byte[] getPrefix(byte[] in, Type type) {
    int len = in.length;
    return new byte[] {
        type.value,
        (byte) ((len >> 24) & 0xff),
        (byte) ((len >> 16) & 0xff),
        (byte) ((len >> 8) & 0xff),
        (byte) ((len >> 0) & 0xff),
    };
  }
}
