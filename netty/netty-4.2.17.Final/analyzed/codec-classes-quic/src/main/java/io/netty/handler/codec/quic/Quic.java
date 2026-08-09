/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.quic;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * QUIC 模块入口工具类：可用性检测、常量定义及 Channel 选项/属性辅助方法。
 */
public final class Quic {
    @SuppressWarnings("unchecked")
    static final Map.Entry<ChannelOption<?>, Object>[] EMPTY_OPTION_ARRAY = new Map.Entry[0];
    @SuppressWarnings("unchecked")
    static final Map.Entry<AttributeKey<?>, Object>[] EMPTY_ATTRIBUTE_ARRAY = new Map.Entry[0];

    /** 推荐的最大 UDP 数据报负载（字节）。 */
    static final int MAX_DATAGRAM_SIZE = 1350;

    /** Stateless Reset Token 固定长度（字节）。 */
    static final int RESET_TOKEN_LEN = 16;

    /** 若 native Quiche 库加载失败则保存原因，否则为 {@code null}。 */
    private static final Throwable UNAVAILABILITY_CAUSE;

    static {
        Throwable cause = null;

        try {
            String version = Quiche.quiche_version();
            assert version != null;
        } catch (Throwable error) {
            cause = error;
        }

        UNAVAILABILITY_CAUSE = cause;
    }

    /**
     * <a href="https://www.rfc-editor.org/rfc/rfc9000.html#section-17.2">连接 ID</a> 的最大长度。
     */
    public static final int MAX_CONN_ID_LEN = 20;

    /**
     * 判断指定 QUIC 协议版本是否受支持。
     *
     * @param version   协议版本号。
     * @return          支持则 {@code true}，否则 {@code false}。
     */
    public static boolean isVersionSupported(int version) {
        return isAvailable() && Quiche.quiche_version_is_supported(version);
    }

    /**
     * 当前平台是否可使用本 QUIC 实现（native 库是否加载成功）。
     *
     * @return 可用则 {@code true}，否则 {@code false}。
     */
    public static boolean isAvailable() {
        return UNAVAILABILITY_CAUSE == null;
    }

    /**
     * 断言 QUIC 实现可用；不可用时抛出 {@link UnsatisfiedLinkError}。
     *
     * @throws UnsatisfiedLinkError 若 native 库不可用
     */
    public static void ensureAvailability() {
        if (UNAVAILABILITY_CAUSE != null) {
            throw (Error) new UnsatisfiedLinkError(
                    "failed to load the required native library").initCause(UNAVAILABILITY_CAUSE);
        }
    }

    /**
     * 返回不可用的原因。
     *
     * @return 不可用时为异常原因；可用时为 {@code null}。
     */
    @Nullable
    public static Throwable unavailabilityCause() {
        return UNAVAILABILITY_CAUSE;
    }

    /** 将选项 Map 转为数组，供 native/内部 API 使用。 */
    static Map.Entry<ChannelOption<?>, Object>[] toOptionsArray(Map<ChannelOption<?>, Object> opts) {
        return new HashMap<>(opts).entrySet().toArray(EMPTY_OPTION_ARRAY);
    }

    /** 将属性 Map 转为数组，供 native/内部 API 使用。 */
    static Map.Entry<AttributeKey<?>, Object>[] toAttributesArray(Map<AttributeKey<?>, Object> attributes) {
        return new LinkedHashMap<>(attributes).entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY);
    }

    private static void setAttributes(Channel channel, Map.Entry<AttributeKey<?>, Object>[] attrs) {
        for (Map.Entry<AttributeKey<?>, Object> e: attrs) {
            @SuppressWarnings("unchecked")
            AttributeKey<Object> key = (AttributeKey<Object>) e.getKey();
            channel.attr(key).set(e.getValue());
        }
    }

    private static void setChannelOptions(
            Channel channel, Map.Entry<ChannelOption<?>, Object>[] options, InternalLogger logger) {
        for (Map.Entry<ChannelOption<?>, Object> e: options) {
            setChannelOption(channel, e.getKey(), e.getValue(), logger);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setChannelOption(
            Channel channel, ChannelOption<?> option, Object value, InternalLogger logger) {
        try {
            if (!channel.config().setOption((ChannelOption<Object>) option, value)) {
                logger.warn("Unknown channel option '{}' for channel '{}'", option, channel);
            }
        } catch (Throwable t) {
            logger.warn(
                    "Failed to set channel option '{}' with value '{}' for channel '{}'", option, value, channel, t);
        }
    }

    /**
     * 为新建 {@link QuicStreamChannel} 指定 {@link ChannelOption}；{@code null} 值表示移除先前设置。
     */
    static <T> void updateOptions(Map<ChannelOption<?>, Object> options, ChannelOption<T> option, @Nullable T value) {
        ObjectUtil.checkNotNull(option, "option");
        if (value == null) {
            options.remove(option);
        } else {
            options.put(option, value);
        }
    }

    /**
     * 为新建 {@link QuicStreamChannel} 指定初始 {@link AttributeKey}；{@code null} 值表示移除。
     */
    static <T> void updateAttributes(Map<AttributeKey<?>, Object> attributes, AttributeKey<T> key, @Nullable T value) {
        ObjectUtil.checkNotNull(key, "key");
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    /** 应用选项、属性并在 pipeline 末尾添加 handler（若提供）。 */
    static void setupChannel(Channel ch, Map.Entry<ChannelOption<?>, Object>[] options,
                             Map.Entry<AttributeKey<?>, Object>[] attrs, @Nullable ChannelHandler handler,
                             InternalLogger logger) {
        Quic.setChannelOptions(ch, options, logger);
        Quic.setAttributes(ch, attrs);
        if (handler != null) {
            ch.pipeline().addLast(handler);
        }
    }

    private Quic() { }
}
