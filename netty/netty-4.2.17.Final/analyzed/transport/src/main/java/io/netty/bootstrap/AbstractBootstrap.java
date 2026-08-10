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

package io.netty.bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.SocketUtils;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link AbstractBootstrap} is a helper class that makes it easy to bootstrap a {@link Channel}. It support
 * method-chaining to provide an easy way to configure the {@link AbstractBootstrap}.
 *
 * <p>When not used in a {@link ServerBootstrap} context, the {@link #bind()} methods are useful for connectionless
 * transports such as datagram (UDP).</p>
 */
public abstract class AbstractBootstrap<B extends AbstractBootstrap<B, C>, C extends Channel> implements Cloneable {

    private static final boolean CLOSE_ON_SET_OPTION_FAILURE = SystemPropertyUtil.getBoolean(
            "io.netty.bootstrap.closeOnSetOptionFailure", true);
    @SuppressWarnings("unchecked")
    private static final Map.Entry<ChannelOption<?>, Object>[] EMPTY_OPTION_ARRAY = new Map.Entry[0];
    @SuppressWarnings("unchecked")
    private static final Map.Entry<AttributeKey<?>, Object>[] EMPTY_ATTRIBUTE_ARRAY = new Map.Entry[0];

    /** 处理 Channel 事件的 {@link EventLoopGroup} */
    volatile EventLoopGroup group;
    /** 创建 Channel 实例的工厂 */
    @SuppressWarnings("deprecation")
    private volatile ChannelFactory<? extends C> channelFactory;
    /** 本地 bind 地址 */
    private volatile SocketAddress localAddress;

    // ChannelOption 应用顺序可能影响校验，使用 LinkedHashMap 保序
    private final Map<ChannelOption<?>, Object> options = new LinkedHashMap<ChannelOption<?>, Object>();
    private final Map<AttributeKey<?>, Object> attrs = new ConcurrentHashMap<AttributeKey<?>, Object>();
    /** 服务端/客户端 Channel 的 Pipeline 入口 Handler */
    private volatile ChannelHandler handler;
    /** 加载 {@link ChannelInitializerExtension} 的 ClassLoader */
    private volatile ClassLoader extensionsClassLoader;

    AbstractBootstrap() {
        // 禁止跨包继承
    }

    AbstractBootstrap(AbstractBootstrap<B, C> bootstrap) {
        group = bootstrap.group;
        channelFactory = bootstrap.channelFactory;
        handler = bootstrap.handler;
        localAddress = bootstrap.localAddress;
        synchronized (bootstrap.options) {
            options.putAll(bootstrap.options);
        }
        attrs.putAll(bootstrap.attrs);
        extensionsClassLoader = bootstrap.extensionsClassLoader;
    }

    /**
     * 设置处理 Channel 全部事件的 {@link EventLoopGroup}。
     */
    public B group(EventLoopGroup group) {
        ObjectUtil.checkNotNull(group, "group");
        if (this.group != null) {
            throw new IllegalStateException("group set already");
        }
        this.group = group;
        return self();
    }

    @SuppressWarnings("unchecked")
    private B self() {
        return (B) this;
    }

    /**
     * 指定用于创建 {@link Channel} 的 {@link Class}；实现类若无无参构造，请改用 {@link #channelFactory}。
     */
    public B channel(Class<? extends C> channelClass) {
        return channelFactory(new ReflectiveChannelFactory<C>(
                ObjectUtil.checkNotNull(channelClass, "channelClass")
        ));
    }

    /**
     * @deprecated 请改用 {@link #channelFactory(io.netty.channel.ChannelFactory)}。
     */
    @Deprecated
    public B channelFactory(ChannelFactory<? extends C> channelFactory) {
        ObjectUtil.checkNotNull(channelFactory, "channelFactory");
        if (this.channelFactory != null) {
            throw new IllegalStateException("channelFactory set already");
        }

        this.channelFactory = channelFactory;
        return self();
    }

    /**
     * 设置 {@link io.netty.channel.ChannelFactory}，在 {@link #bind()} 时创建 {@link Channel}。
     * 通常仅在 {@link #channel(Class)} 无法满足复杂需求时使用；有无参构造时优先用 {@link #channel(Class)}。
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public B channelFactory(io.netty.channel.ChannelFactory<? extends C> channelFactory) {
        return channelFactory((ChannelFactory<C>) channelFactory);
    }

    /**
     * 本地 bind 端使用的 {@link SocketAddress}。
     */
    public B localAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
        return self();
    }

    /**
     * @see #localAddress(SocketAddress)
     */
    public B localAddress(int inetPort) {
        return localAddress(new InetSocketAddress(inetPort));
    }

    /**
     * @see #localAddress(SocketAddress)
     */
    public B localAddress(String inetHost, int inetPort) {
        return localAddress(SocketUtils.socketAddress(inetHost, inetPort));
    }

    /**
     * @see #localAddress(SocketAddress)
     */
    public B localAddress(InetAddress inetHost, int inetPort) {
        return localAddress(new InetSocketAddress(inetHost, inetPort));
    }

    /**
     * 为新建 {@link Channel} 设置 {@link ChannelOption}；{@code null} 移除已有项
     * created. Use a value of {@code null} to remove a previous set {@link ChannelOption}.
     */
    public <T> B option(ChannelOption<T> option, T value) {
        ObjectUtil.checkNotNull(option, "option");
        synchronized (options) {
            if (value == null) {
                options.remove(option);
            } else {
                options.put(option, value);
            }
        }
        return self();
    }

    /**
     * 为新建 {@link Channel} 设置初始 {@link AttributeKey}；{@code null} 移除该属性 {@link Channel}.  If the {@code value} is
     * {@code null}, the attribute of the specified {@code key} is removed.
     */
    public <T> B attr(AttributeKey<T> key, T value) {
        ObjectUtil.checkNotNull(key, "key");
        if (value == null) {
            attrs.remove(key);
        } else {
            attrs.put(key, value);
        }
        return self();
    }

    /**
     * 使用指定 ClassLoader 加载 {@link ChannelInitializerExtension}。
     * <p>
     * 默认使用加载本类的 ClassLoader。
     *
     * @param classLoader 扩展加载器
     * @return 本 Bootstrap
     */
    public B extensionsClassLoader(ClassLoader classLoader) {
        extensionsClassLoader = classLoader;
        return self();
    }

    /**
     * 校验全部参数；子类可覆盖但应调用 {@code super.validate()}。
     */
    public B validate() {
        if (group == null) {
            throw new IllegalStateException("group not set");
        }
        if (channelFactory == null) {
            throw new IllegalStateException("channel or channelFactory not set");
        }
        return self();
    }

    /**
     * 返回配置相同的深拷贝；{@link EventLoopGroup} 为浅拷贝（共享实例）。
     * 适用于批量创建配置相似的 {@link Channel}。
     */
    @Override
    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    public abstract B clone();

    /**
     * 创建 {@link Channel} 并注册到 {@link EventLoop}
     */
    public ChannelFuture register() {
        validate();
        return initAndRegister();
    }

    /**
     * 创建 {@link Channel} 并 bind 到本地地址
     */
    public ChannelFuture bind() {
        validate();
        SocketAddress localAddress = this.localAddress;
        if (localAddress == null) {
            throw new IllegalStateException("localAddress not set");
        }
        return doBind(localAddress);
    }

    /**
     * 创建 {@link Channel} 并 bind 到本地地址
     */
    public ChannelFuture bind(int inetPort) {
        return bind(new InetSocketAddress(inetPort));
    }

    /**
     * 创建 {@link Channel} 并 bind 到本地地址
     */
    public ChannelFuture bind(String inetHost, int inetPort) {
        return bind(SocketUtils.socketAddress(inetHost, inetPort));
    }

    /**
     * 创建 {@link Channel} 并 bind 到本地地址
     */
    public ChannelFuture bind(InetAddress inetHost, int inetPort) {
        return bind(new InetSocketAddress(inetHost, inetPort));
    }

    /**
     * 创建 {@link Channel} 并 bind 到本地地址
     */
    public ChannelFuture bind(SocketAddress localAddress) {
        validate();
        return doBind(ObjectUtil.checkNotNull(localAddress, "localAddress"));
    }

    private ChannelFuture doBind(final SocketAddress localAddress) {
        final ChannelFuture regFuture = initAndRegister();
        final Channel channel = regFuture.channel();
        if (regFuture.cause() != null) {
            return regFuture;
        }

        if (regFuture.isDone()) {
            // 注册已完成且成功，可安全 bind and successful.
            ChannelPromise promise = channel.newPromise();
            doBind0(regFuture, channel, localAddress, promise);
            return promise;
        } else {
            // 注册 Future 通常已完成，处理尚未完成的情况, but just in case it's not.
            final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
            regFuture.addListener(future -> {
                Throwable cause = future.cause();
                if (cause != null) {
                    // EventLoop 注册失败，直接失败 Promise so fail the ChannelPromise directly to not cause an
                    // IllegalStateException once we try to access the EventLoop of the Channel.
                    promise.setFailure(cause);
                } else {
                    // 注册成功，设置正确的 executor（见 netty#2586） to use.
                    // See https://github.com/netty/netty/issues/2586
                    promise.registered();

                    doBind0(regFuture, channel, localAddress, promise);
                }
            });
            return promise;
        }
    }

    final ChannelFuture initAndRegister() {
        Channel channel = null;
        try {
            channel = channelFactory.newChannel();
            init(channel);
        } catch (Throwable t) {
            if (channel != null) {
                // newChannel 可能因资源耗尽等失败，channel 为 null (eg SocketException("too many open files"))
                channel.unsafe().closeForcibly();
                // Channel 尚未注册，强制使用 GlobalEventExecutor 通知 the usage of the GlobalEventExecutor
                return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
            }
            // Channel 尚未注册，强制使用 GlobalEventExecutor 通知 the usage of the GlobalEventExecutor
            return new DefaultChannelPromise(new FailedChannel(), GlobalEventExecutor.INSTANCE).setFailure(t);
        }

        final ChannelFuture regFuture = config().group().register(channel);
        if (regFuture.cause() != null) {
            if (channel.isRegistered()) {
                channel.close();
            } else {
                channel.unsafe().closeForcibly();
            }
        }

        // 注册已成功或已排入 EventLoop 任务队列；bind/connect 将在注册完成后执行, it's one of the following cases:
        // 1) If we attempted registration from the event loop, the registration has been completed at this point.
        //    i.e. It's safe to attempt bind() or connect() now because the channel has been registered.
        // 2) If we attempted registration from the other thread, the registration request has been successfully
        //    added to the event loop's task queue for later execution.
        //    i.e. It's safe to attempt bind() or connect() now:
        //         because bind() or connect() will be executed *after* the scheduled registration task is executed
        //         because register(), bind(), and connect() are all bound to the same thread.

        return regFuture;
    }

    abstract void init(Channel channel) throws Throwable;

    Collection<ChannelInitializerExtension> getInitializerExtensions() {
        ClassLoader loader = extensionsClassLoader;
        if (loader == null) {
            loader = getClass().getClassLoader();
        }
        return ChannelInitializerExtensions.getExtensions().extensions(loader);
    }

    private static void doBind0(
            final ChannelFuture regFuture, final Channel channel,
            final SocketAddress localAddress, final ChannelPromise promise) {

        // This method is invoked before channelRegistered() is triggered.  Give user handlers a chance to set up
        // the pipeline in its channelRegistered() implementation.
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                if (regFuture.isSuccess()) {
                    channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    promise.setFailure(regFuture.cause());
                }
            }
        });
    }

    /**
     * 设置服务请求的 {@link ChannelHandler}
     */
    public B handler(ChannelHandler handler) {
        this.handler = ObjectUtil.checkNotNull(handler, "handler");
        return self();
    }

    /**
     * 返回已配置的 {@link EventLoopGroup}，未配置时为 {@code null} if non is configured yet.
     *
     * @deprecated Use {@link #config()} instead.
     */
    @Deprecated
    public final EventLoopGroup group() {
        return group;
    }

    /**
     * 返回当前 Bootstrap 配置的 {@link AbstractBootstrapConfig} 视图 that can be used to obtain the current config
     * of the bootstrap.
     */
    public abstract AbstractBootstrapConfig<B, C> config();

    final Map.Entry<ChannelOption<?>, Object>[] newOptionsArray() {
        return newOptionsArray(options);
    }

    static Map.Entry<ChannelOption<?>, Object>[] newOptionsArray(Map<ChannelOption<?>, Object> options) {
        synchronized (options) {
            return new LinkedHashMap<ChannelOption<?>, Object>(options).entrySet().toArray(EMPTY_OPTION_ARRAY);
        }
    }

    final Map.Entry<AttributeKey<?>, Object>[] newAttributesArray() {
        return newAttributesArray(attrs0());
    }

    static Map.Entry<AttributeKey<?>, Object>[] newAttributesArray(Map<AttributeKey<?>, Object> attributes) {
        return attributes.entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY);
    }

    final Map<ChannelOption<?>, Object> options0() {
        return options;
    }

    final Map<AttributeKey<?>, Object> attrs0() {
        return attrs;
    }

    final SocketAddress localAddress() {
        return localAddress;
    }

    @SuppressWarnings("deprecation")
    final ChannelFactory<? extends C> channelFactory() {
        return channelFactory;
    }

    final ChannelHandler handler() {
        return handler;
    }

    final Map<ChannelOption<?>, Object> options() {
        synchronized (options) {
            return copiedMap(options);
        }
    }

    final Map<AttributeKey<?>, Object> attrs() {
        return copiedMap(attrs);
    }

    static <K, V> Map<K, V> copiedMap(Map<K, V> map) {
        if (map.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<K, V>(map));
    }

    static void setAttributes(Channel channel, Map.Entry<AttributeKey<?>, Object>[] attrs) {
        for (Map.Entry<AttributeKey<?>, Object> e: attrs) {
            @SuppressWarnings("unchecked")
            AttributeKey<Object> key = (AttributeKey<Object>) e.getKey();
            channel.attr(key).set(e.getValue());
        }
    }

    static void setChannelOptions(
            Channel channel, Map.Entry<ChannelOption<?>, Object>[] options, InternalLogger logger) throws Throwable {
        for (Map.Entry<ChannelOption<?>, Object> e: options) {
            setChannelOption(channel, e.getKey(), e.getValue(), logger);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setChannelOption(
            Channel channel, ChannelOption<?> option, Object value, InternalLogger logger) throws Throwable {
        try {
            if (!channel.config().setOption((ChannelOption<Object>) option, value)) {
                logger.warn("Unknown channel option '{}' for channel '{}' of type '{}'",
                        option, channel, channel.getClass());
            }
        } catch (Throwable t) {
            logger.warn(
                    "Failed to set channel option '{}' with value '{}' for channel '{}' of type '{}'",
                    option, value, channel, channel.getClass(), t);
            if (CLOSE_ON_SET_OPTION_FAILURE) {
                // 仅在配置要求失败时关闭 Channel 的情况下重新抛出 in case of a failure.
                throw t;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder()
            .append(StringUtil.simpleClassName(this))
            .append('(').append(config()).append(')');
        return buf.toString();
    }

    static final class PendingRegistrationPromise extends DefaultChannelPromise {

        // 注册成功后使用 Channel 的 EventExecutor；失败时回退 GlobalEventExecutor Otherwise it will
        // stay null and so the GlobalEventExecutor.INSTANCE will be used for notifications.
        private volatile boolean registered;

        PendingRegistrationPromise(Channel channel) {
            super(channel);
        }

        void registered() {
            registered = true;
        }

        @Override
        protected EventExecutor executor() {
            if (registered) {
                // 注册成功时使用 Channel 关联的 executor（见 netty#2586）
                //
                // See https://github.com/netty/netty/issues/2586
                return super.executor();
            }
            // 注册失败，只能使用 GlobalEventExecutor 通知 as last resort to notify.
            return GlobalEventExecutor.INSTANCE;
        }
    }
}
