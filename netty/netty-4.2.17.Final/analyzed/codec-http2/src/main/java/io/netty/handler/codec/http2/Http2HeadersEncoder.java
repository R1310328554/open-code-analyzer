/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;

/**
 * HPACK 头块编码器：将 {@link Http2Headers} 序列化为二进制头块写入 {@link ByteBuf}。
 */
public interface Http2HeadersEncoder {
    /**
     * {@link Http2HeadersEncoder} 的配置项。
     */
    interface Configuration {
        /**
         * 设置 HPACK 动态表最大容量，对应 SETTINGS_HEADER_TABLE_SIZE。
         * This method should only be called by Netty (not users) as a result of a receiving a {@code SETTINGS} frame.
         */
        void maxHeaderTableSize(long max) throws Http2Exception;

        /**
         * 返回当前 HPACK 动态表最大容量；初始值须为 {@link Http2CodecUtil#DEFAULT_HEADER_TABLE_SIZE}。
         */
        long maxHeaderTableSize();

        /**
         * 设置单组头的最大字节数，对应 SETTINGS_MAX_HEADER_LIST_SIZE。
         * This method should only be called by Netty (not users) as a result of a receiving a {@code SETTINGS} frame.
         */
        void maxHeaderListSize(long max) throws Http2Exception;

        /**
         * 返回 SETTINGS_MAX_HEADER_LIST_SIZE 当前值。
         */
        long maxHeaderListSize();
    }

    /**
     * 敏感头检测器：判定头名/值对应否以 HPACK "Never Index" 方式编码（RFC 7541 §7.1.3）。
     * <p>若实例跨连接共享且可动态修改，实现须保证线程安全。
     */
    interface SensitivityDetector {
        /**
         * 判定 {@code name}/{@code value} 是否应视为敏感头（禁止索引）。
         *
         * @param name The name for the header.
         * @param value The value of the header.
         * @return {@code true} if a header {@code name}/{@code value} pair should be treated as
         * <a href="https://tools.ietf.org/html/rfc7541#section-7.1.3">sensitive</a>.
         * {@code false} otherwise.
         */
        boolean isSensitive(CharSequence name, CharSequence value);
    }

    /**
     * 将头列表 HPACK 编码并写入输出缓冲。
     *
     * @param streamId  所属流 ID
     * @param headers 待编码的头集合
     * @param buffer 接收编码结果的输出缓冲
     */
    void encodeHeaders(int streamId, Http2Headers headers, ByteBuf buffer) throws Http2Exception;

    /**
     * 获取本编码器的配置对象。
     */
    Configuration configuration();

    /** 始终返回 {@code false} 的敏感头检测器（所有头均可索引）。 */
    SensitivityDetector NEVER_SENSITIVE = new SensitivityDetector() {
        @Override
        public boolean isSensitive(CharSequence name, CharSequence value) {
            return false;
        }
    };

    /** 始终返回 {@code true} 的敏感头检测器（所有头均禁止索引）。 */
    SensitivityDetector ALWAYS_SENSITIVE = new SensitivityDetector() {
        @Override
        public boolean isSensitive(CharSequence name, CharSequence value) {
            return true;
        }
    };
}
