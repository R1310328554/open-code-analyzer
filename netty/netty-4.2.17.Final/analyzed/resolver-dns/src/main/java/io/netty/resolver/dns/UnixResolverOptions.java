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
package io.netty.resolver.dns;

/**
 * 表示 <a href=https://linux.die.net/man/5/resolver>etc/resolv.conf</a> 格式文件中定义的解析选项。
 */
final class UnixResolverOptions {

    /** 触发绝对查询前 hostname 中所需的最少点数。 */
    private final int ndots;
    /** 单次 DNS 查询超时（秒）。 */
    private final int timeout;
    /** 解析主机名时允许发送的最大查询次数。 */
    private final int attempts;

    UnixResolverOptions(int ndots, int timeout, int attempts) {
        this.ndots = ndots;
        this.timeout = timeout;
        this.attempts = attempts;
    }

    static UnixResolverOptions.Builder newBuilder() {
        return new UnixResolverOptions.Builder();
    }

    /**
     * 在发起首次绝对查询前，名称中必须出现的点数。
     * 默认值为 {@code 1}。
     */
    int ndots() {
        return ndots;
    }

    /**
     * 本解析器每次 DNS 查询的超时时间（秒）。
     * 默认值为 {@code 5}。
     */
    int timeout() {
        return timeout;
    }

    /**
     * 解析主机名时允许发送的 DNS 查询次数上限。
     * 默认值为 {@code 16}。
     */
    int attempts() {
        return attempts;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{ndots=" + ndots +
                ", timeout=" + timeout +
                ", attempts=" + attempts +
                '}';
    }

    /** 可变构建器，默认值与 glibc 常见 resolv.conf 缺省一致。 */
    static final class Builder {

        private int ndots = 1;
        private int timeout = 5;
        private int attempts = 16;

        private Builder() {
        }

        void setNdots(int ndots) {
            this.ndots = ndots;
        }

        void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        void setAttempts(int attempts) {
            this.attempts = attempts;
        }

        UnixResolverOptions build() {
            return new UnixResolverOptions(ndots, timeout, attempts);
        }
    }
}
