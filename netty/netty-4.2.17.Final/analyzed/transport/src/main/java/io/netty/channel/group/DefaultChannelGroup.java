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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import io.netty.channel.ServerChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The default {@link ChannelGroup} implementation.
 *
 * <p>{@link ChannelGroup} 的默认实现：线程安全地管理 {@link ServerChannel} 与普通 channel，
 * 支持按 {@link ChannelMatcher} 批量写/刷/关闭，并在 channel 关闭时自动移除。</p>
 */
public class DefaultChannelGroup extends AbstractSet<Channel> implements ChannelGroup {

    /** 自动生成组名时使用的递增 ID。 */
    private static final AtomicInteger nextId = new AtomicInteger();
    private final String name;
    /** 用于通知 {@link ChannelGroupFuture} 监听器的事件执行器。 */
    private final EventExecutor executor;
    /** 服务端 channel 映射（按 {@link ChannelId}）。 */
    private final ConcurrentMap<ChannelId, Channel> serverChannels = new ConcurrentHashMap<>();
    /** 非服务端 channel 映射。 */
    private final ConcurrentMap<ChannelId, Channel> nonServerChannels = new ConcurrentHashMap<>();
    /** channel 关闭完成后自动从组中移除。 */
    private final ChannelFutureListener remover = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            remove(future.channel());
        }
    };
    /** void promise 写操作返回的占位 future。 */
    private final VoidChannelGroupFuture voidFuture = new VoidChannelGroupFuture(this);
    /** 为 true 时组一旦关闭则保持关闭，后续 add 会立即关闭新 channel。 */
    private final boolean stayClosed;
    private volatile boolean closed;

    /**
     * Creates a new group with a generated name and the provided {@link EventExecutor} to notify the
     * {@link ChannelGroupFuture}s.
     * <p>使用自动生成的组名与指定 {@link EventExecutor} 创建组。</p>
     */
    public DefaultChannelGroup(EventExecutor executor) {
        this(executor, false);
    }

    /**
     * Creates a new group with the specified {@code name} and {@link EventExecutor} to notify the
     * {@link ChannelGroupFuture}s.  Please note that different groups can have the same name, which means no
     * duplicate check is done against group names.
     * <p>指定组名创建；不同组可重名，不做唯一性校验。</p>
     */
    public DefaultChannelGroup(String name, EventExecutor executor) {
        this(name, executor, false);
    }

    /**
     * Creates a new group with a generated name and the provided {@link EventExecutor} to notify the
     * {@link ChannelGroupFuture}s. {@code stayClosed} defines whether or not, this group can be closed
     * more than once. Adding channels to a closed group will immediately close them, too. This makes it
     * easy, to shutdown server and child channels at once.
     * <p>{@code stayClosed} 为 true 时关闭后保持关闭状态，便于一次性关停服务端与子 channel。</p>
     */
    public DefaultChannelGroup(EventExecutor executor, boolean stayClosed) {
        this("group-0x" + Integer.toHexString(nextId.incrementAndGet()), executor, stayClosed);
    }

    /**
     * Creates a new group with the specified {@code name} and {@link EventExecutor} to notify the
     * {@link ChannelGroupFuture}s. {@code stayClosed} defines whether or not, this group can be closed
     * more than once. Adding channels to a closed group will immediately close them, too. This makes it
     * easy, to shutdown server and child channels at once. Please note that different groups can have
     * the same name, which means no duplicate check is done against group names.
     * <p>完整构造：组名、执行器与 stayClosed 标志。</p>
     */
    public DefaultChannelGroup(String name, EventExecutor executor, boolean stayClosed) {
        ObjectUtil.checkNotNull(name, "name");
        this.name = name;
        this.executor = executor;
        this.stayClosed = stayClosed;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Channel find(ChannelId id) {
        Channel c = nonServerChannels.get(id);
        if (c != null) {
            return c;
        } else {
            return serverChannels.get(id);
        }
    }

    @Override
    public boolean isEmpty() {
        return nonServerChannels.isEmpty() && serverChannels.isEmpty();
    }

    @Override
    public int size() {
        return nonServerChannels.size() + serverChannels.size();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof ServerChannel) {
            return serverChannels.containsValue(o);
        } else if (o instanceof Channel) {
            return nonServerChannels.containsValue(o);
        }
        return false;
    }

    @Override
    public boolean add(Channel channel) {
        ConcurrentMap<ChannelId, Channel> map =
            channel instanceof ServerChannel? serverChannels : nonServerChannels;

        boolean added = map.putIfAbsent(channel.id(), channel) == null;
        if (added) {
            channel.closeFuture().addListener(remover);
        }

        if (stayClosed && closed) {

            // 先加入再检查 closed，利用 volatile 保证线程安全（见 issue #4020）
            // First add channel, than check if closed.
            // Seems inefficient at first, but this way a volatile
            // gives us enough synchronization to be thread-safe.
            //
            // If true: Close right away.
            // (Might be closed a second time by ChannelGroup.close(), but this is ok)
            //
            // If false: Channel will definitely be closed by the ChannelGroup.
            // (Because closed=true always happens-before ChannelGroup.close())
            //
            // See https://github.com/netty/netty/issues/4020
            channel.close();
        }

        return added;
    }

    @Override
    public boolean remove(Object o) {
        Channel c = null;
        if (o instanceof ChannelId) {
            c = nonServerChannels.remove(o);
            if (c == null) {
                c = serverChannels.remove(o);
            }
        } else if (o instanceof Channel) {
            c = (Channel) o;
            if (c instanceof ServerChannel) {
                c = serverChannels.remove(c.id());
            } else {
                c = nonServerChannels.remove(c.id());
            }
        }

        if (c == null) {
            return false;
        }

        c.closeFuture().removeListener(remover);
        return true;
    }

    @Override
    public void clear() {
        nonServerChannels.clear();
        serverChannels.clear();
    }

    @Override
    public Iterator<Channel> iterator() {
        return new CombinedIterator<Channel>(
                serverChannels.values().iterator(),
                nonServerChannels.values().iterator());
    }

    @Override
    public Object[] toArray() {
        Collection<Channel> channels = new ArrayList<Channel>(size());
        channels.addAll(serverChannels.values());
        channels.addAll(nonServerChannels.values());
        return channels.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Collection<Channel> channels = new ArrayList<Channel>(size());
        channels.addAll(serverChannels.values());
        channels.addAll(nonServerChannels.values());
        return channels.toArray(a);
    }

    @Override
    public ChannelGroupFuture close() {
        return close(ChannelMatchers.all());
    }

    @Override
    public ChannelGroupFuture disconnect() {
        return disconnect(ChannelMatchers.all());
    }

    @Override
    public ChannelGroupFuture deregister() {
        return deregister(ChannelMatchers.all());
    }

    @Override
    public ChannelGroupFuture write(Object message) {
        return write(message, ChannelMatchers.all());
    }

    // 为每个 channel 安全复制消息，避免多路写共享同一 buffer（见 issue #1461）
    // Create a safe duplicate of the message to write it to a channel but not affect other writes.
    // See https://github.com/netty/netty/issues/1461
    private static Object safeDuplicate(Object message) {
        if (message instanceof ByteBuf) {
            return ((ByteBuf) message).retainedDuplicate();
        } else if (message instanceof ByteBufHolder) {
            return ((ByteBufHolder) message).retainedDuplicate();
        } else {
            return ReferenceCountUtil.retain(message);
        }
    }

    @Override
    public ChannelGroupFuture write(Object message, ChannelMatcher matcher) {
        return write(message, matcher, false);
    }

    @Override
    public ChannelGroupFuture write(Object message, ChannelMatcher matcher, boolean voidPromise) {
        ObjectUtil.checkNotNull(message, "message");
        ObjectUtil.checkNotNull(matcher, "matcher");

        final ChannelGroupFuture future;
        if (voidPromise) {
            for (Channel c: nonServerChannels.values()) {
                if (matcher.matches(c)) {
                    c.write(safeDuplicate(message), c.voidPromise());
                }
            }
            future = voidFuture;
        } else {
            Map<Channel, ChannelFuture> futures = new LinkedHashMap<Channel, ChannelFuture>(nonServerChannels.size());
            for (Channel c: nonServerChannels.values()) {
                if (matcher.matches(c)) {
                    futures.put(c, c.write(safeDuplicate(message)));
                }
            }
            future = new DefaultChannelGroupFuture(this, futures, executor);
        }
        ReferenceCountUtil.release(message);
        return future;
    }

    @Override
    public ChannelGroup flush() {
        return flush(ChannelMatchers.all());
    }

    @Override
    public ChannelGroupFuture flushAndWrite(Object message) {
        return writeAndFlush(message);
    }

    @Override
    public ChannelGroupFuture writeAndFlush(Object message) {
        return writeAndFlush(message, ChannelMatchers.all());
    }

    @Override
    public ChannelGroupFuture disconnect(ChannelMatcher matcher) {
        ObjectUtil.checkNotNull(matcher, "matcher");

        Map<Channel, ChannelFuture> futures =
                new LinkedHashMap<Channel, ChannelFuture>(size());

        for (Channel c: serverChannels.values()) {
            if (matcher.matches(c)) {
                futures.put(c, c.disconnect());
            }
        }
        for (Channel c: nonServerChannels.values()) {
            if (matcher.matches(c)) {
                futures.put(c, c.disconnect());
            }
        }

        return new DefaultChannelGroupFuture(this, futures, executor);
    }

    @Override
    public ChannelGroupFuture close(ChannelMatcher matcher) {
        ObjectUtil.checkNotNull(matcher, "matcher");

        Map<Channel, ChannelFuture> futures =
                new LinkedHashMap<Channel, ChannelFuture>(size());

        if (stayClosed) {
            // 关闭前先置 closed=true，保证 add 与 close 的 happens-before 顺序（见 issue #4020）
            // It is important to set the closed to true, before closing channels.
            // Our invariants are:
            // closed=true happens-before ChannelGroup.close()
            // ChannelGroup.add() happens-before checking closed==true
            //
            // See https://github.com/netty/netty/issues/4020
            closed = true;
        }

        for (Channel c: serverChannels.values()) {
            if (matcher.matches(c)) {
                futures.put(c, c.close());
            }
        }
        for (Channel c: nonServerChannels.values()) {
            if (matcher.matches(c)) {
                futures.put(c, c.close());
            }
        }

        return new DefaultChannelGroupFuture(this, futures, executor);
    }

    @Override
    public ChannelGroupFuture deregister(ChannelMatcher matcher) {
        ObjectUtil.checkNotNull(matcher, "matcher");

        Map<Channel, ChannelFuture> futures =
                new LinkedHashMap<Channel, ChannelFuture>(size());

        for (Channel c: serverChannels.values()) {
            if (matcher.matches(c)) {
                futures.put(c, c.deregister());
            }
        }
        for (Channel c: nonServerChannels.values()) {
            if (matcher.matches(c)) {
                futures.put(c, c.deregister());
            }
        }

        return new DefaultChannelGroupFuture(this, futures, executor);
    }

    @Override
    public ChannelGroup flush(ChannelMatcher matcher) {
        for (Channel c: nonServerChannels.values()) {
            if (matcher.matches(c)) {
                c.flush();
            }
        }
        return this;
    }

    @Override
    public ChannelGroupFuture flushAndWrite(Object message, ChannelMatcher matcher) {
        return writeAndFlush(message, matcher);
    }

    @Override
    public ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher) {
        return writeAndFlush(message, matcher, false);
    }

    @Override
    public ChannelGroupFuture writeAndFlush(Object message, ChannelMatcher matcher, boolean voidPromise) {
        ObjectUtil.checkNotNull(message, "message");

        final ChannelGroupFuture future;
        if (voidPromise) {
            for (Channel c: nonServerChannels.values()) {
                if (matcher.matches(c)) {
                    c.writeAndFlush(safeDuplicate(message), c.voidPromise());
                }
            }
            future = voidFuture;
        } else {
            Map<Channel, ChannelFuture> futures = new LinkedHashMap<Channel, ChannelFuture>(nonServerChannels.size());
            for (Channel c: nonServerChannels.values()) {
                if (matcher.matches(c)) {
                    futures.put(c, c.writeAndFlush(safeDuplicate(message)));
                }
            }
            future = new DefaultChannelGroupFuture(this, futures, executor);
        }
        ReferenceCountUtil.release(message);
        return future;
    }

    @Override
    public ChannelGroupFuture newCloseFuture() {
        return newCloseFuture(ChannelMatchers.all());
    }

    @Override
    public ChannelGroupFuture newCloseFuture(ChannelMatcher matcher) {
        Map<Channel, ChannelFuture> futures =
                new LinkedHashMap<Channel, ChannelFuture>(size());

        for (Channel c: serverChannels.values()) {
            if (matcher.matches(c)) {
                futures.put(c, c.closeFuture());
            }
        }
        for (Channel c: nonServerChannels.values()) {
            if (matcher.matches(c)) {
                futures.put(c, c.closeFuture());
            }
        }

        return new DefaultChannelGroupFuture(this, futures, executor);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int compareTo(ChannelGroup o) {
        int v = name().compareTo(o.name());
        if (v != 0) {
            return v;
        }

        return System.identityHashCode(this) - System.identityHashCode(o);
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(this) + "(name: " + name() + ", size: " + size() + ')';
    }
}
