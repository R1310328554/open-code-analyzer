/*
 * Copyright 2020 The Netty Project
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
package io.netty.buffer.search;

/**
 * 预计算工厂基类，用于创建 {@link SearchProcessor}。
 * <br>
 * 各子类实现不同搜索算法，性能因场景而异，选定前应针对实际负载做基准测试。
 * <br>
 * 每个工厂实例对应固定 {@code needle}，内含预计算表，可在多次搜索同一模式时复用。
 * <br>
 * <b>注意：</b>{@link SearchProcessor} 严格顺序扫描 {@link io.netty.buffer.ByteBuf}，不做随机访问。
 * 配合 {@link io.netty.buffer.ByteBuf#forEachByte} 时，返回的是命中序列<strong>最后一个字节</strong>的索引
 * （与 {@link io.netty.buffer.ByteBufUtil#indexOf} 返回首字节索引不同）。
 * <br>
 * 实现为<a href="https://en.wikipedia.org/wiki/Finite-state_machine">有限状态自动机</a>，每处理一字节更新内部状态。
 * 不同 {@link io.netty.buffer.ByteBuf} 的搜索会话须各自 {@link #newSearchProcessor()}；
 * 同一会话内应复用同一 processor 以支持重叠匹配（如 "ABABAB" 中两个重叠的 "BAB"）。
 * 命中结束于 {@code idx} 后，应从 {@code idx + 1} 继续扫描。
 * <br>
 * 示例（{@code haystack} 含 "ABABAB"，{@code needle} 为 "BAB"）：
 * <pre>
 *     SearchProcessorFactory factory =
 *         SearchProcessorFactory.newKmpSearchProcessorFactory(needle.getBytes(CharsetUtil.UTF_8));
 *     SearchProcessor processor = factory.newSearchProcessor();
 *
 *     int idx1 = haystack.forEachByte(processor);
 *     // idx1 is 3 (index of the last character of the first occurrence of the needle in the haystack)
 *
 *     int continueFrom1 = idx1 + 1;
 *     // continue the search starting from the next character
 *
 *     int idx2 = haystack.forEachByte(continueFrom1, haystack.readableBytes() - continueFrom1, processor);
 *     // idx2 is 5 (index of the last character of the second occurrence of the needle in the haystack)
 *
 *     int continueFrom2 = idx2 + 1;
 *     // continue the search starting from the next character
 *
 *     int idx3 = haystack.forEachByte(continueFrom2, haystack.readableBytes() - continueFrom2, processor);
 *     // idx3 is -1 (no more occurrences of the needle)
 *
 *     // 搜索结束应丢弃 processor；再次搜索同一 needle 时复用工厂创建新实例
 * </pre>
 */
public abstract class AbstractSearchProcessorFactory implements SearchProcessorFactory {

    /**
     * 基于
     * <a href="https://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm">KMP</a>
     * 算法创建 {@link SearchProcessorFactory}，为提供的算法中的合理默认选择。
     * <br>
     * 预计算 {@code O(|needle|)}，保留长度 {@code needle.length + 1} 的 int 数组及 needle 副本。
     * <br>
     * 搜索时对 haystack 每字节顺序处理一次，时间 {@code O(|haystack|)}。
     *
      * @param needle 待搜索字节序列
     * @return 针对给定 {@code needle} 预计算的 {@link KmpSearchProcessorFactory}
     */
    public static KmpSearchProcessorFactory newKmpSearchProcessorFactory(byte[] needle) {
        return new KmpSearchProcessorFactory(needle);
    }

    /**
     * 基于 Bitap 算法创建 {@link SearchProcessorFactory}：无跳跃、性能稳定，输入内容影响小。
     * {@code needle} 长度不得超过 64 字节。
     * <br>
     * 预计算 {@code O(|needle|)}，保留 {@code long[256]}。
     * <br>
     * 搜索时对 haystack 每字节顺序处理一次，时间 {@code O(|haystack|)}。
     *
      * @param needle 待搜索字节序列（<b>不超过 64 字节</b>）
     * @return 针对给定 {@code needle} 预计算的 {@link BitapSearchProcessorFactory}
     */
    public static BitapSearchProcessorFactory newBitapSearchProcessorFactory(byte[] needle) {
        return new BitapSearchProcessorFactory(needle);
    }

}
