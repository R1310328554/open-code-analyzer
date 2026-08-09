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
 * 预计算工厂基类，用于创建 {@link MultiSearchProcessor}。
 * <br>
 * {@link MultiSearchProcessor} 在 {@code haystack} 中<strong>一次顺序扫描</strong>即可同时匹配多个 {@code needles}；
 * 若只搜单个模式，{@link SearchProcessorFactory} 通常更高效。
 * <br>
 * 通用用法见 {@link AbstractSearchProcessorFactory}；除 {@link SearchProcessor} 能力外，
 * 还提供 {@link MultiSearchProcessor#getFoundNeedleId()} 以标识当前命中的 {@code needle} 下标。
 * <br>
 * <b>注意：</b>某 {@code needle} 可能是另一模式的 suffix（如 {@code {"BC", "ABC"}}），
 * 同一位置可能匹配多个模式；此时 {@link MultiSearchProcessor#getFoundNeedleId()} 返回<strong>最长</strong>匹配在 {@code needles} 数组中的下标。
 * <br>
 * 示例（{@code haystack} 为含 "ABCD" 的 {@link io.netty.buffer.ByteBuf}，{@code needles} 为 "AB"、"BC"、"CD"）：
 * <pre>
 *      MultiSearchProcessorFactory factory = MultiSearchProcessorFactory.newAhoCorasicSearchProcessorFactory(
 *          "AB".getBytes(CharsetUtil.UTF_8), "BC".getBytes(CharsetUtil.UTF_8), "CD".getBytes(CharsetUtil.UTF_8));
 *      MultiSearchProcessor processor = factory.newSearchProcessor();
 *
 *      int idx1 = haystack.forEachByte(processor);
 *      // idx1 is 1 (index of the last character of the occurrence of "AB" in the haystack)
 *      // processor.getFoundNeedleId() is 0 (index of "AB" in needles[])
 *
 *      int continueFrom1 = idx1 + 1;
 *      // continue the search starting from the next character
 *
 *      int idx2 = haystack.forEachByte(continueFrom1, haystack.readableBytes() - continueFrom1, processor);
 *      // idx2 is 2 (index of the last character of the occurrence of "BC" in the haystack)
 *      // processor.getFoundNeedleId() is 1 (index of "BC" in needles[])
 *
 *      int continueFrom2 = idx2 + 1;
 *
 *      int idx3 = haystack.forEachByte(continueFrom2, haystack.readableBytes() - continueFrom2, processor);
 *      // idx3 is 3 (index of the last character of the occurrence of "CD" in the haystack)
 *      // processor.getFoundNeedleId() is 2 (index of "CD" in needles[])
 *
 *      int continueFrom3 = idx3 + 1;
 *
 *      int idx4 = haystack.forEachByte(continueFrom3, haystack.readableBytes() - continueFrom3, processor);
 *      // idx4 is -1 (no more occurrences of any of the needles)
 *
 *      // 本次搜索结束，应丢弃 processor；再次搜索同一组 needles 时复用本工厂创建新实例
 * </pre>
 */
public abstract class AbstractMultiSearchProcessorFactory implements MultiSearchProcessorFactory {

    /**
     * 基于
     * <a href="https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm">Aho–Corasick</a>
     * 算法创建 {@link MultiSearchProcessorFactory}。
     * <br>
     * 预计算时间 {@code O(Σ|needles|)}；工厂保留约 {@code 256×X + X} 个 int，
     * X 为各 needle 长度之和减去重复前缀长度之和。
     * <br>
     * 搜索阶段对每个 {@link io.netty.buffer.ByteBuf} 字节仅顺序处理一次，时间 {@code O(|haystack|)}，
     * 与 {@code needles} 数量无关。
     *
      * @param needles 待搜索的字节序列（可变参数）
     * @return 针对给定 {@code needles} 预计算好的 {@link AhoCorasicSearchProcessorFactory}
     */
    public static AhoCorasicSearchProcessorFactory newAhoCorasicSearchProcessorFactory(byte[] ...needles) {
        return new AhoCorasicSearchProcessorFactory(needles);
    }

}
