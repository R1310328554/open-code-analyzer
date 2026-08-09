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

import java.io.Closeable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static io.netty.handler.codec.http2.Http2Error.COMPRESSION_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * {@link Http2HeadersEncoder} 默认实现：通过 {@link HpackEncoder} 将 {@link Http2Headers} 序列化为 HPACK 块。
 * <p>动态表尺寸变更会先写入 {@link #tableSizeChangeOutput}，下次 {@link #encodeHeaders} 时 prepend 到输出，
 * 以满足 RFC 要求「表大小变更指令位于使用该尺寸的头块之前」。
 */
public class DefaultHttp2HeadersEncoder implements
    Http2HeadersEncoder, Http2HeadersEncoder.Configuration, Closeable {

    private final HpackEncoder hpackEncoder;
    /** 判定哪些头域不应进入动态表（如 Authorization）。 */
    private final SensitivityDetector sensitivityDetector;
    /** 缓存 HPACK 动态表 resize 产生的字节，在下次 encode 前刷出。 */
    private ByteBuf tableSizeChangeOutput;

    public DefaultHttp2HeadersEncoder() {
        this(NEVER_SENSITIVE);
    }

    public DefaultHttp2HeadersEncoder(SensitivityDetector sensitivityDetector) {
        this(sensitivityDetector, new HpackEncoder());
    }

    public DefaultHttp2HeadersEncoder(SensitivityDetector sensitivityDetector, boolean ignoreMaxHeaderListSize) {
        this(sensitivityDetector, new HpackEncoder(ignoreMaxHeaderListSize));
    }

    public DefaultHttp2HeadersEncoder(SensitivityDetector sensitivityDetector, boolean ignoreMaxHeaderListSize,
                                      int dynamicTableArraySizeHint) {
        this(sensitivityDetector, ignoreMaxHeaderListSize, dynamicTableArraySizeHint, HpackEncoder.HUFF_CODE_THRESHOLD);
    }

    public DefaultHttp2HeadersEncoder(SensitivityDetector sensitivityDetector, boolean ignoreMaxHeaderListSize,
                                      int dynamicTableArraySizeHint, int huffCodeThreshold) {
        this(sensitivityDetector,
                new HpackEncoder(ignoreMaxHeaderListSize, dynamicTableArraySizeHint, huffCodeThreshold));
    }

    /**
     * Exposed Used for testing only! Default values used in the initial settings frame are overridden intentionally
     * for testing but violate the RFC if used outside the scope of testing.
     */
    DefaultHttp2HeadersEncoder(SensitivityDetector sensitivityDetector, HpackEncoder hpackEncoder) {
        this.sensitivityDetector = checkNotNull(sensitivityDetector, "sensitiveDetector");
        this.hpackEncoder = checkNotNull(hpackEncoder, "hpackEncoder");
    }

    @Override
    public void encodeHeaders(int streamId, Http2Headers headers, ByteBuf buffer) throws Http2Exception {
        try {
            // 动态表大小变更产生的 HPACK 指令须先于本帧头块写入同一缓冲区
            if (tableSizeChangeOutput != null && tableSizeChangeOutput.isReadable()) {
                buffer.writeBytes(tableSizeChangeOutput);
                tableSizeChangeOutput.clear();
            }

            hpackEncoder.encodeHeaders(streamId, buffer, headers, sensitivityDetector);
        } catch (Http2Exception e) {
            throw e;
        } catch (Throwable t) {
            throw connectionError(COMPRESSION_ERROR, t, "Failed encoding headers block: %s", t.getMessage());
        }
    }

    @Override
    public void maxHeaderTableSize(long max) throws Http2Exception {
        if (tableSizeChangeOutput == null) {
            tableSizeChangeOutput = Unpooled.buffer();
        }
        hpackEncoder.setMaxHeaderTableSize(tableSizeChangeOutput, max);
    }

    @Override
    public long maxHeaderTableSize() {
        return hpackEncoder.getMaxHeaderTableSize();
    }

    @Override
    public void maxHeaderListSize(long max) throws Http2Exception {
        hpackEncoder.setMaxHeaderListSize(max);
    }

    @Override
    public long maxHeaderListSize() {
        return hpackEncoder.getMaxHeaderListSize();
    }

    @Override
    public Configuration configuration() {
        return this;
    }

    /**
     * Close the encoder and release all its associated data.
     */
    @Override
    public void close() {
        if (tableSizeChangeOutput != null) {
            tableSizeChangeOutput.release();
            tableSizeChangeOutput = null;
        }
    }
}
