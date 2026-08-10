/*
 * Copyright 2016 The Netty Project
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
package io.netty.channel.kqueue;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.UnstableApi;

/**
 * BSD/macOS 监听套接字的 accept 过滤器（SO_ACCEPTFILTER）。
 * <p>将内核层 HTTP 解析等过滤器名称与参数封装为不可变值对象； {@link #PLATFORM_UNSUPPORTED} 表示当前平台不支持。</p>
 */
@UnstableApi
public final class AcceptFilter {
    /** 平台不支持 accept 过滤器时的占位实例（空名称与空参数） */
    static final AcceptFilter PLATFORM_UNSUPPORTED = new AcceptFilter("", "");
    /** 内核 accept 过滤器名称（如 {@code httpready}） */
    private final String filterName;
    /** 过滤器参数字符串 */
    private final String filterArgs;

    /** 构造 accept 过滤器描述 */
    public AcceptFilter(String filterName, String filterArgs) {
        this.filterName = ObjectUtil.checkNotNull(filterName, "filterName");
        this.filterArgs = ObjectUtil.checkNotNull(filterArgs, "filterArgs");
    }

    /** 返回过滤器名称 */
    public String filterName() {
        return filterName;
    }

    /** 返回过滤器参数字符串 */
    public String filterArgs() {
        return filterArgs;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AcceptFilter)) {
            return false;
        }
        AcceptFilter rhs = (AcceptFilter) o;
        return filterName.equals(rhs.filterName) && filterArgs.equals(rhs.filterArgs);
    }

    @Override
    public int hashCode() {
        return 31 * (31 + filterName.hashCode()) + filterArgs.hashCode();
    }

    @Override
    public String toString() {
        return filterName + ", " + filterArgs;
    }
}
