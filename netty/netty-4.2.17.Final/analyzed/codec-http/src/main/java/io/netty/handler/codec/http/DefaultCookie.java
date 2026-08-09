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

import io.netty.util.internal.ObjectUtil;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * 默认 {@link Cookie} 实现（已废弃）。
 * <p>
 * 继承 {@link io.netty.handler.codec.http.cookie.DefaultCookie}，保留旧版 getter/setter API 及 RFC 2965 扩展字段（comment、ports、version 等）。
 * @deprecated Use {@link io.netty.handler.codec.http.cookie.DefaultCookie} instead.
 */
@Deprecated
public class DefaultCookie extends io.netty.handler.codec.http.cookie.DefaultCookie implements Cookie {

    /** RFC 2965 注释字段。 */
    private String comment;
    /** RFC 2965 注释 URL。 */
    private String commentUrl;
    /** 是否丢弃（不持久化）会话 Cookie。 */
    private boolean discard;
    /** 限定 Cookie 生效的端口集合。 */
    private Set<Integer> ports = Collections.emptySet();
    /** ports 的只读视图缓存。 */
    private Set<Integer> unmodifiablePorts = ports;
    /** Cookie 协议版本（0/1）。 */
    private int version;

    /** 以指定名称与值创建 Cookie。 */
    public DefaultCookie(String name, String value) {
        super(name, value);
    }

    @Override
    @Deprecated
    public String getName() {
        return name();
    }

    @Override
    @Deprecated
    public String getValue() {
        return value();
    }

    @Override
    @Deprecated
    public String getDomain() {
        return domain();
    }

    @Override
    @Deprecated
    public String getPath() {
        return path();
    }

    @Override
    @Deprecated
    public String getComment() {
        return comment();
    }

    @Override
    @Deprecated
    public String comment() {
        return comment;
    }

    @Override
    @Deprecated
    public void setComment(String comment) {
        this.comment = validateValue("comment", comment);
    }

    @Override
    @Deprecated
    public String getCommentUrl() {
        return commentUrl();
    }

    @Override
    @Deprecated
    public String commentUrl() {
        return commentUrl;
    }

    @Override
    @Deprecated
    public void setCommentUrl(String commentUrl) {
        this.commentUrl = validateValue("commentUrl", commentUrl);
    }

    @Override
    @Deprecated
    public boolean isDiscard() {
        return discard;
    }

    @Override
    @Deprecated
    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    @Override
    @Deprecated
    public Set<Integer> getPorts() {
        return ports();
    }

    @Override
    @Deprecated
    public Set<Integer> ports() {
        if (unmodifiablePorts == null) {
            unmodifiablePorts = Collections.unmodifiableSet(ports);
        }
        return unmodifiablePorts;
    }

    @Override
    @Deprecated
    public void setPorts(int... ports) {
        ObjectUtil.checkNotNull(ports, "ports");

        int[] portsCopy = ports.clone();
        if (portsCopy.length == 0) {
            // 空端口列表表示不限制端口
            unmodifiablePorts = this.ports = Collections.emptySet();
        } else {
            Set<Integer> newPorts = new TreeSet<Integer>();
            for (int p: portsCopy) {
                if (p <= 0 || p > 65535) {
                    // 端口必须在 1–65535 范围内
                    throw new IllegalArgumentException("port out of range: " + p);
                }
                newPorts.add(Integer.valueOf(p));
            }
            this.ports = newPorts;
            unmodifiablePorts = null;
        }
    }

    @Override
    @Deprecated
    public void setPorts(Iterable<Integer> ports) {
        Set<Integer> newPorts = new TreeSet<Integer>();
        for (int p: ports) {
            if (p <= 0 || p > 65535) {
                throw new IllegalArgumentException("port out of range: " + p);
            }
            newPorts.add(Integer.valueOf(p));
        }
        if (newPorts.isEmpty()) {
            unmodifiablePorts = this.ports = Collections.emptySet();
        } else {
            this.ports = newPorts;
            unmodifiablePorts = null;
        }
    }

    @Override
    @Deprecated
    public long getMaxAge() {
        return maxAge();
    }

    @Override
    @Deprecated
    public int getVersion() {
        return version();
    }

    @Override
    @Deprecated
    public int version() {
        return version;
    }

    @Override
    @Deprecated
    public void setVersion(int version) {
        this.version = version;
    }
}
