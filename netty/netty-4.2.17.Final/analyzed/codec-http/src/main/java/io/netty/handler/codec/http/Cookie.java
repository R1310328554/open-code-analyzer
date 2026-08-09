/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.http;

import java.util.Set;

/**
 * 定义 <a href="https://en.wikipedia.org/wiki/HTTP_cookie">HTTP Cookie</a> 的旧版接口。
 * <p>
 * 含 RFC6265 未收录的 comment、version、ports 等字段；新代码请用 {@link io.netty.handler.codec.http.cookie.Cookie}。
 * @deprecated Use {@link io.netty.handler.codec.http.cookie.Cookie} instead.
 */
@Deprecated
public interface Cookie extends io.netty.handler.codec.http.cookie.Cookie {

    /**
     * @deprecated Use {@link #name()} instead.
     */
    @Deprecated
    String getName();

    /**
     * @deprecated Use {@link #value()} instead.
     */
    @Deprecated
    String getValue();

    /**
     * @deprecated Use {@link #domain()} instead.
     */
    @Deprecated
    String getDomain();

    /**
     * @deprecated Use {@link #path()} instead.
     */
    @Deprecated
    String getPath();

    /**
     * @deprecated Use {@link #comment()} instead.
     */
    @Deprecated
    String getComment();

    /**
     * 返回本 Cookie 的 comment 属性。
     *
     * @return The comment of this {@link Cookie}
     *
     * @deprecated Not part of RFC6265
     */
    @Deprecated
    String comment();

    /**
     * 设置 comment 属性。
     *
     * @param comment The comment to use
     *
     * @deprecated Not part of RFC6265
     */
    @Deprecated
    void setComment(String comment);

    /**
     * @deprecated Use {@link #maxAge()} instead.
     */
    @Deprecated
    long getMaxAge();

    /**
     * 返回最大存活秒数；未指定时为 {@link Long#MIN_VALUE}。
     *
     * @return The maximum age of this {@link Cookie}
     *
     * @deprecated Not part of RFC6265
     */
    @Deprecated
    @Override
    long maxAge();

    /**
     * 设置最大存活秒数：0 表示立即过期，{@link Long#MIN_VALUE} 表示会话 Cookie。
     *
     * @param maxAge The maximum age of this {@link Cookie} in seconds
     *
     * @deprecated Not part of RFC6265
     */
    @Deprecated
    @Override
    void setMaxAge(long maxAge);

    /**
     * @deprecated Use {@link #version()} instead.
     */
    @Deprecated
    int getVersion();

    /**
     * 返回 Cookie 版本号（旧版 Set-Cookie 语法）。
     *
     * @return The version of this {@link Cookie}
     *
     * @deprecated Not part of RFC6265
     */
    @Deprecated
    int version();

    /** 设置 Cookie 版本号。 @deprecated Not part of RFC6265 */
    @Deprecated
    void setVersion(int version);

    /**
     * @deprecated Use {@link #commentUrl()} instead.
     */
    @Deprecated
    String getCommentUrl();

    /** 返回 comment URL。 @deprecated Not part of RFC6265 */
    @Deprecated
    String commentUrl();

    /** 设置 comment URL。 @deprecated Not part of RFC6265 */
    @Deprecated
    void setCommentUrl(String commentUrl);

    /** 是否为会话结束时丢弃的 Cookie。 @deprecated Not part of RFC6265 */
    @Deprecated
    boolean isDiscard();

    /** 设置 discard 标志。 @deprecated Not part of RFC6265 */
    @Deprecated
    void setDiscard(boolean discard);

    /**
     * @deprecated Use {@link #ports()} instead.
     */
    @Deprecated
    Set<Integer> getPorts();

    /** 返回 Cookie 可访问的端口集合。 @deprecated Not part of RFC6265 */
    @Deprecated
    Set<Integer> ports();

    /** 设置可访问端口（可变参数）。 @deprecated Not part of RFC6265 */
    @Deprecated
    void setPorts(int... ports);

    /** 设置可访问端口（Iterable）。 @deprecated Not part of RFC6265 */
    @Deprecated
    void setPorts(Iterable<Integer> ports);
}
