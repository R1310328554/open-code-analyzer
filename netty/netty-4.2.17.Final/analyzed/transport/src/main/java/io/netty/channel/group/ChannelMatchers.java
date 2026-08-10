/*
 * Copyright 2013 The Netty Project
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
package io.netty.channel.group;

import io.netty.channel.Channel;
import io.netty.channel.ServerChannel;

/**
 * Helper class which provides often used {@link ChannelMatcher} implementations.
 *
 * <p>提供常用 {@link ChannelMatcher} 工厂与组合/取反等辅助方法的工具类。</p>
 */
public final class ChannelMatchers {

    /** 匹配全部 channel 的单例匹配器。 */
    private static final ChannelMatcher ALL_MATCHER = new ChannelMatcher() {
        @Override
        public boolean matches(Channel channel) {
            return true;
        }
    };

    private static final ChannelMatcher SERVER_CHANNEL_MATCHER = isInstanceOf(ServerChannel.class);
    private static final ChannelMatcher NON_SERVER_CHANNEL_MATCHER = isNotInstanceOf(ServerChannel.class);

    private ChannelMatchers() {
        // static methods only
    }

    /**
     * Returns a {@link ChannelMatcher} that matches all {@link Channel}s.
     * <p>返回匹配所有 {@link Channel} 的匹配器。</p>
     */
    public static ChannelMatcher all() {
        return ALL_MATCHER;
    }

    /**
     * Returns a {@link ChannelMatcher} that matches all {@link Channel}s except the given.
     * <p>返回匹配除指定 channel 外全部成员的匹配器。</p>
     */
    public static ChannelMatcher isNot(Channel channel) {
        return invert(is(channel));
    }

    /**
     * Returns a {@link ChannelMatcher} that matches the given {@link Channel}.
     * <p>返回仅匹配指定 channel 实例（引用相等）的匹配器。</p>
     */
    public static ChannelMatcher is(Channel channel) {
        return new InstanceMatcher(channel);
    }

    /**
     * Returns a {@link ChannelMatcher} that matches all {@link Channel}s that are an instance of sub-type of
     * the given class.
     * <p>返回匹配给定类型或其子类实例的匹配器。</p>
     */
    public static ChannelMatcher isInstanceOf(Class<? extends Channel> clazz) {
        return new ClassMatcher(clazz);
    }

    /**
     * Returns a {@link ChannelMatcher} that matches all {@link Channel}s that are <strong>not</strong> an
     * instance of sub-type of the given class.
     * <p>返回<strong>不</strong>匹配给定类型或其子类实例的匹配器。</p>
     */
    public static ChannelMatcher isNotInstanceOf(Class<? extends Channel> clazz) {
        return invert(isInstanceOf(clazz));
    }

    /**
     * Returns a {@link ChannelMatcher} that matches all {@link Channel}s that are of type {@link ServerChannel}.
     * <p>返回匹配 {@link ServerChannel} 类型的匹配器。</p>
     */
    public static ChannelMatcher isServerChannel() {
         return SERVER_CHANNEL_MATCHER;
    }

    /**
     * Returns a {@link ChannelMatcher} that matches all {@link Channel}s that are <strong>not</strong> of type
     * {@link ServerChannel}.
     * <p>返回匹配非 {@link ServerChannel} 的匹配器。</p>
     */
    public static ChannelMatcher isNonServerChannel() {
        return NON_SERVER_CHANNEL_MATCHER;
    }

    /**
     * Invert the given {@link ChannelMatcher}.
     * <p>对给定匹配器取逻辑非。</p>
     */
    public static ChannelMatcher invert(ChannelMatcher matcher) {
        return new InvertMatcher(matcher);
    }

    /**
     * Return a composite of the given {@link ChannelMatcher}s. This means all {@link ChannelMatcher} must
     * return {@code true} to match.
     * <p>组合多个匹配器：全部返回 {@code true} 时才算匹配（逻辑与）。</p>
     */
    public static ChannelMatcher compose(ChannelMatcher... matchers) {
        if (matchers.length < 1) {
            throw new IllegalArgumentException("matchers must at least contain one element");
        }
        if (matchers.length == 1) {
            return matchers[0];
        }
        return new CompositeMatcher(matchers);
    }

    /** 多个匹配器全部满足才匹配。 */
    private static final class CompositeMatcher implements ChannelMatcher {
        private final ChannelMatcher[] matchers;

        CompositeMatcher(ChannelMatcher... matchers) {
            this.matchers = matchers;
        }

        @Override
        public boolean matches(Channel channel) {
            for (ChannelMatcher m: matchers) {
                if (!m.matches(channel)) {
                    return false;
                }
            }
            return true;
        }
    }

    /** 对内部匹配器结果取反。 */
    private static final class InvertMatcher implements ChannelMatcher {
        private final ChannelMatcher matcher;

        InvertMatcher(ChannelMatcher matcher) {
            this.matcher = matcher;
        }

        @Override
        public boolean matches(Channel channel) {
            return !matcher.matches(channel);
        }
    }

    /** 按 channel 引用相等匹配。 */
    private static final class InstanceMatcher implements ChannelMatcher {
        private final Channel channel;

        InstanceMatcher(Channel channel) {
            this.channel = channel;
        }

        @Override
        public boolean matches(Channel ch) {
            return channel == ch;
        }
    }

    /** 按运行时类型匹配。 */
    private static final class ClassMatcher implements ChannelMatcher {
        private final Class<? extends Channel> clazz;

        ClassMatcher(Class<? extends Channel> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean matches(Channel ch) {
            return clazz.isInstance(ch);
        }
    }
}
