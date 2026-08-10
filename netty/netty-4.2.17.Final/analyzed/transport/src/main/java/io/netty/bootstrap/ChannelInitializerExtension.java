/*
 * Copyright 2023 The Netty Project
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
import io.netty.channel.ServerChannel;

/**
 * Channel 初始化扩展点：可在同一 JVM 内跨多个互不关联的 Netty 使用场景统一注入规则或修改 Pipeline。
 * <p>
 * 例如应用级防火墙规则可注入到所有 Netty 管道（含第三方库），而无需修改库本身代码。
 * <p>
 * 扩展默认<strong>未启用</strong>，因其能影响跨库、跨框架的全局 Pipeline。
 * 须显式将 {@value #EXTENSIONS_SYSTEM_PROPERTY} 设为 {@code serviceload} 才会加载。
 * <p>
 * classpath 上所有可用扩展将通过 {@linkplain java.util.ServiceLoader#load(Class) ServiceLoader}
 * 加载，并由各 {@link AbstractBootstrap} 子类在初始化后回调。
 * <p>
 * 若 Netty 被 shade 并 relocate 到其他库，本机制无效：relocate 后的类型与原始 Netty 类型不兼容。
 */
public abstract class ChannelInitializerExtension {
    /**
     * 控制初始化扩展行为的系统属性名。
     * <p>
     * 扩展存在潜在安全风险，默认关闭。
     * <p>
     * 设为 {@code serviceload} 时启用 ServiceLoader 发现的所有扩展；
     * 设为 {@code log} 时仅加载并以 INFO 级别记录，不实际执行。
     */
    public static final String EXTENSIONS_SYSTEM_PROPERTY = "io.netty.bootstrap.extensions";

    /**
     * 返回扩展优先级。多个扩展可用时按优先级从低到高依次调用。
     * <p>
     * 建议取值 {@code -100.0} 至 {@code 100.0}，无特殊偏好时返回 {@code 0.0}。
     * <p>
     * 低优先级先执行，高优先级可观察到先执行扩展的效果。
     * 同优先级时顺序不确定，实现应容忍前后均有其他扩展。
     * <p>
     * 默认返回 {@code 0}。
     *
     * @return 优先级数值
     */
    public double priority() {
        return 0;
    }

    /**
     * {@link Bootstrap} 完成客户端 Channel 初始化后调用。
     * <p>
     * 允许修改 Pipeline、Channel 属性或选项；禁止 I/O 或关闭 Channel。
     *
     * @param channel 已初始化的 Channel
     */
    public void postInitializeClientChannel(Channel channel) {
    }

    /**
     * {@link ServerBootstrap} 完成服务端监听 Channel 初始化后调用。
     * 监听 Channel 负责 {@code accept(2)} 并产生子 Channel。
     * <p>
     * 允许修改 Pipeline、属性或选项；禁止 I/O 或关闭 Channel。
     *
     * @param channel 已初始化的监听 Channel
     */
    public void postInitializeServerListenerChannel(ServerChannel channel) {
    }

    /**
     * {@link ServerBootstrap} 完成子 Channel（新接受的客户端连接）初始化后调用。
     * <p>
     * 允许修改 Pipeline、属性或选项；禁止 I/O 或关闭 Channel。
     *
     * @param channel 已初始化的子 Channel
     */
    public void postInitializeServerChildChannel(Channel channel) {
    }
}
