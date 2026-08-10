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

import io.netty.util.collection.CharObjectHashMap;

import static io.netty.handler.codec.http2.Http2CodecUtil.DEFAULT_HEADER_LIST_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.DEFAULT_MAX_CONCURRENT_STREAMS;
import static io.netty.handler.codec.http2.Http2CodecUtil.MAX_CONCURRENT_STREAMS;
import static io.netty.handler.codec.http2.Http2CodecUtil.MAX_FRAME_SIZE_LOWER_BOUND;
import static io.netty.handler.codec.http2.Http2CodecUtil.MAX_FRAME_SIZE_UPPER_BOUND;
import static io.netty.handler.codec.http2.Http2CodecUtil.MAX_HEADER_LIST_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.MAX_HEADER_TABLE_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.MAX_INITIAL_WINDOW_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.MAX_UNSIGNED_INT;
import static io.netty.handler.codec.http2.Http2CodecUtil.MIN_CONCURRENT_STREAMS;
import static io.netty.handler.codec.http2.Http2CodecUtil.MIN_HEADER_LIST_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.MIN_HEADER_TABLE_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.MIN_INITIAL_WINDOW_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.NUM_STANDARD_SETTINGS;
import static io.netty.handler.codec.http2.Http2CodecUtil.SETTINGS_ENABLE_CONNECT_PROTOCOL;
import static io.netty.handler.codec.http2.Http2CodecUtil.SETTINGS_ENABLE_PUSH;
import static io.netty.handler.codec.http2.Http2CodecUtil.SETTINGS_HEADER_TABLE_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.SETTINGS_INITIAL_WINDOW_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.SETTINGS_MAX_CONCURRENT_STREAMS;
import static io.netty.handler.codec.http2.Http2CodecUtil.SETTINGS_MAX_FRAME_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.SETTINGS_MAX_HEADER_LIST_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.isMaxFrameSizeValid;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static java.lang.Integer.toHexString;

/**
 * HTTP/2 连接端点参数容器：以 char 键（SETTINGS 标识符）映射 32 位无符号值。
 * <p>规范定义的六项均为可选；同时支持扩展键值。{@link #put(char, Long)} 对标准项做范围校验。
 */
public final class Http2Settings extends CharObjectHashMap<Long> {
    /**
     * 默认容量：按规范标准 SETTINGS 数量推算，避免一次性 put 全部标准项时触发 rehash。
     */
    private static final int DEFAULT_CAPACITY = (int) (NUM_STANDARD_SETTINGS / DEFAULT_LOAD_FACTOR) + 1;
    private static final Long FALSE = 0L;
    private static final Long TRUE = 1L;

    public Http2Settings() {
        this(DEFAULT_CAPACITY);
    }

    public Http2Settings(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public Http2Settings(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * 写入设置项；标准 SETTINGS 会经 {@link #verifyStandardSetting} 校验取值范围。
     *
     * @throws IllegalArgumentException if verification for a standard HTTP/2 setting fails.
     */
    @Override
    public Long put(char key, Long value) {
        verifyStandardSetting(key, value);
        return super.put(key, value);
    }

    /**
     * 获取 {@code SETTINGS_HEADER_TABLE_SIZE}（HPACK 动态表上限）；未设置时返回 {@code null}。
     */
    public Long headerTableSize() {
        return get(SETTINGS_HEADER_TABLE_SIZE);
    }

    /**
     * 设置 {@code SETTINGS_HEADER_TABLE_SIZE}。
     *
     * @throws IllegalArgumentException if verification of the setting fails.
     */
    public Http2Settings headerTableSize(long value) {
        put(SETTINGS_HEADER_TABLE_SIZE, Long.valueOf(value));
        return this;
    }

    /**
     * 获取 {@code SETTINGS_ENABLE_PUSH}；未设置时返回 {@code null}。
     */
    public Boolean pushEnabled() {
        Long value = get(SETTINGS_ENABLE_PUSH);
        if (value == null) {
            return null;
        }
        return TRUE.equals(value);
    }

    /**
     * 设置 {@code SETTINGS_ENABLE_PUSH}（服务端是否允许 PUSH_PROMISE）。
     */
    public Http2Settings pushEnabled(boolean enabled) {
        put(SETTINGS_ENABLE_PUSH, enabled ? TRUE : FALSE);
        return this;
    }

    /**
     * 获取 {@code SETTINGS_MAX_CONCURRENT_STREAMS}；未设置时返回 {@code null}。
     */
    public Long maxConcurrentStreams() {
        return get(SETTINGS_MAX_CONCURRENT_STREAMS);
    }

    /**
     * 设置 {@code SETTINGS_MAX_CONCURRENT_STREAMS}。
     *
     * @throws IllegalArgumentException if verification of the setting fails.
     */
    public Http2Settings maxConcurrentStreams(long value) {
        put(SETTINGS_MAX_CONCURRENT_STREAMS, Long.valueOf(value));
        return this;
    }

    /**
     * 获取 {@code SETTINGS_INITIAL_WINDOW_SIZE}（流级初始接收窗口）；未设置时返回 {@code null}。
     */
    public Integer initialWindowSize() {
        return getIntValue(SETTINGS_INITIAL_WINDOW_SIZE);
    }

    /**
     * 设置 {@code SETTINGS_INITIAL_WINDOW_SIZE}。
     *
     * @throws IllegalArgumentException if verification of the setting fails.
     */
    public Http2Settings initialWindowSize(int value) {
        put(SETTINGS_INITIAL_WINDOW_SIZE, Long.valueOf(value));
        return this;
    }

    /**
     * 获取 {@code SETTINGS_MAX_FRAME_SIZE}；未设置时返回 {@code null}。
     */
    public Integer maxFrameSize() {
        return getIntValue(SETTINGS_MAX_FRAME_SIZE);
    }

    /**
     * 设置 {@code SETTINGS_MAX_FRAME_SIZE}。
     *
     * @throws IllegalArgumentException if verification of the setting fails.
     */
    public Http2Settings maxFrameSize(int value) {
        put(SETTINGS_MAX_FRAME_SIZE, Long.valueOf(value));
        return this;
    }

    /**
     * 获取 {@code SETTINGS_MAX_HEADER_LIST_SIZE}；未设置时返回 {@code null}。
     */
    public Long maxHeaderListSize() {
        return get(SETTINGS_MAX_HEADER_LIST_SIZE);
    }

    /**
     * 设置 {@code SETTINGS_MAX_HEADER_LIST_SIZE}。
     *
     * @throws IllegalArgumentException if verification of the setting fails.
     */
    public Http2Settings maxHeaderListSize(long value) {
        put(SETTINGS_MAX_HEADER_LIST_SIZE, Long.valueOf(value));
        return this;
    }

    /**
     * 获取 {@code SETTINGS_ENABLE_CONNECT_PROTOCOL}（RFC 8441 Extended CONNECT）；未设置时返回 {@code null}。
     */
    public Boolean connectProtocolEnabled() {
        Long value = get(SETTINGS_ENABLE_CONNECT_PROTOCOL);
        if (value == null) {
            return null;
        }
        return TRUE.equals(value);
    }

    /**
     * 设置 {@code SETTINGS_ENABLE_CONNECT_PROTOCOL}。
     */
    public Http2Settings connectProtocolEnabled(boolean enabled) {
        put(SETTINGS_ENABLE_CONNECT_PROTOCOL, enabled ? TRUE : FALSE);
        return this;
    }

    /** 清空后复制另一份设置，便于就地更新。 */
    public Http2Settings copyFrom(Http2Settings settings) {
        clear();
        putAll(settings);
        return this;
    }

    /**
     * 将 {@link #get(char)} 的 {@link Long} 转为 {@link Integer}；值超出 int 范围时应直接用 {@link #get(char)}。
     */
    public Integer getIntValue(char key) {
        Long value = get(key);
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

    /** 校验标准 SETTINGS 的合法取值；非标准键仅要求 32 位无符号范围。 */
    private static void verifyStandardSetting(int key, Long value) {
        checkNotNull(value, "value");
        switch (key) {
            case SETTINGS_HEADER_TABLE_SIZE:
                if (value < MIN_HEADER_TABLE_SIZE || value > MAX_HEADER_TABLE_SIZE) {
                    throw new IllegalArgumentException("Setting HEADER_TABLE_SIZE is invalid: " + value +
                            ", expected [" + MIN_HEADER_TABLE_SIZE + ", " + MAX_HEADER_TABLE_SIZE + ']');
                }
                break;
            case SETTINGS_ENABLE_PUSH:
                if (value != 0L && value != 1L) {
                    throw new IllegalArgumentException("Setting ENABLE_PUSH is invalid: " + value +
                            ", expected [0, 1]");
                }
                break;
            case SETTINGS_MAX_CONCURRENT_STREAMS:
                if (value < MIN_CONCURRENT_STREAMS || value > MAX_CONCURRENT_STREAMS) {
                    throw new IllegalArgumentException("Setting MAX_CONCURRENT_STREAMS is invalid: " + value +
                            ", expected [" + MIN_CONCURRENT_STREAMS + ", " + MAX_CONCURRENT_STREAMS + ']');
                }
                break;
            case SETTINGS_INITIAL_WINDOW_SIZE:
                if (value < MIN_INITIAL_WINDOW_SIZE || value > MAX_INITIAL_WINDOW_SIZE) {
                    throw new IllegalArgumentException("Setting INITIAL_WINDOW_SIZE is invalid: " + value +
                            ", expected [" + MIN_INITIAL_WINDOW_SIZE + ", " + MAX_INITIAL_WINDOW_SIZE + ']');
                }
                break;
            case SETTINGS_MAX_FRAME_SIZE:
                if (!isMaxFrameSizeValid(value.intValue())) {
                    throw new IllegalArgumentException("Setting MAX_FRAME_SIZE is invalid: " + value +
                            ", expected [" + MAX_FRAME_SIZE_LOWER_BOUND + ", " + MAX_FRAME_SIZE_UPPER_BOUND + ']');
                }
                break;
            case SETTINGS_MAX_HEADER_LIST_SIZE:
                if (value < MIN_HEADER_LIST_SIZE || value > MAX_HEADER_LIST_SIZE) {
                    throw new IllegalArgumentException("Setting MAX_HEADER_LIST_SIZE is invalid: " + value +
                            ", expected [" + MIN_HEADER_LIST_SIZE + ", " + MAX_HEADER_LIST_SIZE + ']');
                }
                break;
            case SETTINGS_ENABLE_CONNECT_PROTOCOL:
                if (value != 0L && value != 1L) {
                    throw new IllegalArgumentException("Setting ENABLE_CONNECT_PROTOCOL is invalid: " + value +
                            ", expected [0, 1]");
                }
                break;
            default:
                // 扩展 SETTINGS：仅校验无符号 32 位
                if (value < 0 || value > MAX_UNSIGNED_INT) {
                    throw new IllegalArgumentException("Non-standard setting 0x" + toHexString(key) + " is invalid: " +
                            value + ", expected unsigned 32-bit value");
                }
                break;
        }
    }

    @Override
    protected String keyToString(char key) {
        switch (key) {
            case SETTINGS_HEADER_TABLE_SIZE:
                return "HEADER_TABLE_SIZE";
            case SETTINGS_ENABLE_PUSH:
                return "ENABLE_PUSH";
            case SETTINGS_MAX_CONCURRENT_STREAMS:
                return "MAX_CONCURRENT_STREAMS";
            case SETTINGS_INITIAL_WINDOW_SIZE:
                return "INITIAL_WINDOW_SIZE";
            case SETTINGS_MAX_FRAME_SIZE:
                return "MAX_FRAME_SIZE";
            case SETTINGS_MAX_HEADER_LIST_SIZE:
                return "MAX_HEADER_LIST_SIZE";
            case SETTINGS_ENABLE_CONNECT_PROTOCOL:
                return "ENABLE_CONNECT_PROTOCOL";
            default:
                // 未知扩展键
                return "0x" + toHexString(key);
        }
    }

    /** Netty 推荐的默认 SETTINGS（最大并发流与头部列表大小）。 */
    public static Http2Settings defaultSettings() {
        return new Http2Settings().maxHeaderListSize(DEFAULT_HEADER_LIST_SIZE)
                .maxConcurrentStreams(DEFAULT_MAX_CONCURRENT_STREAMS);
    }
}
