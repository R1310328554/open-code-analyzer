/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import org.redisson.api.tdigest.TDigestMergeArgs;

import java.util.List;

/**
 * t-digest RxJava3 API。
 *
 * @author Nikita Koksharov
 *
 */
public interface RTDigestRx extends RExpirableRx {

    /**
     * 使用默认压缩因子初始化新的 t-digest 草图。
     * <p>
     * Equivalent to {@code TDIGEST.CREATE key}.
     *
     * @return 无返回值
     */
    Completable create();

    /**
     * 使用指定压缩因子初始化 t-digest 草图；压缩越高，内存占用越大但精度越高。
     * <p>
     * Equivalent to {@code TDIGEST.CREATE key COMPRESSION compression}.
     *
     * @param compression 压缩因子
     * @return 无返回值
     */
    Completable create(int compression);

    /**
     * 向草图添加单个观测值。
     * <p>
     * Equivalent to {@code TDIGEST.ADD key value}.
     *
     * @param value 观测值
     * @return 无返回值
     */
    Completable add(double value);

    /**
     * 向草图添加一个或多个观测值。
     * <p>
     * Equivalent to {@code TDIGEST.ADD key value [value ...]}.
     *
     * @param values 观测值列表
     * @return 无返回值
     */
    Completable add(double... values);

    /**
     * 将指定键上的草图合并到当前草图（当前草图为合并目标）。
     * <p>
     * Equivalent to {@code TDIGEST.MERGE this numkeys key [key ...]}.
     *
     * @param keys 源草图的键名
     * @return 无返回值
     */
    Completable mergeWith(String... keys);

    /**
     * 按 {@code args} 指定的压缩与覆盖选项，将源草图合并到当前草图（当前草图为合并目标）。
     * <p>
     * Equivalent to {@code TDIGEST.MERGE this numkeys key [key ...] [COMPRESSION c] [OVERRIDE]}.
     *
     * @param args 合并参数
     * @return 无返回值
     */
    Completable mergeWith(TDigestMergeArgs args);

    /**
     * 返回草图中的最小观测值；草图为空时返回 {@code NaN}。
     * <p>
     * Equivalent to {@code TDIGEST.MIN key}.
     *
     * @return 最小观测值
     */
    Single<Double> getMin();

    /**
     * 返回草图中的最大观测值；草图为空时返回 {@code NaN}。
     * <p>
     * Equivalent to {@code TDIGEST.MAX key}.
     *
     * @return 最大观测值
     */
    Single<Double> getMax();

    /**
     * 对每个输入分位数，估算低于该比例观测值的分位点估计值。
     * <p>
     * Equivalent to {@code TDIGEST.QUANTILE key quantile [quantile ...]}.
     *
     * @param quantiles 输入分位数（0 至 1，含端点）
     * @return 各分位数对应的估计值
     */
    Single<List<Double>> quantile(double... quantiles);

    /**
     * 对每个输入值，估算小于等于该值的观测值所占比例（累积分布）。
     * <p>
     * Equivalent to {@code TDIGEST.CDF key value [value ...]}.
     *
     * @param values 输入值列表
     * @return 各输入值对应的估计比例
     */
    Single<List<Double>> cumulativeProbability(double... values);

    /**
     * 估算截尾均值，忽略 {@code [lowCutQuantile, highCutQuantile]} 范围外的观测值。
     * <p>
     * Equivalent to {@code TDIGEST.TRIMMED_MEAN key lowCutQuantile highCutQuantile}.
     *
     * @param lowCutQuantile 低端截尾分位数（0 至 1）
     * @param highCutQuantile 高端截尾分位数（0 至 1）
     * @return 截尾均值估计值
     */
    Single<Double> trimmedMean(double lowCutQuantile, double highCutQuantile);

    /**
     * 对每个输入值返回估计秩（严格小于该值的观测数）；若小于全部观测则返回 {@code -1}。
     * <p>
     * Equivalent to {@code TDIGEST.RANK key value [value ...]}.
     *
     * @param values 输入值列表
     * @return 各输入值对应的估计秩
     */
    Single<List<Long>> rank(double... values);

    /**
     * 对每个输入值返回估计逆秩（严格大于该值的观测数）；若大于全部观测则返回 {@code -1}。
     * <p>
     * Equivalent to {@code TDIGEST.REVRANK key value [value ...]}.
     *
     * @param values 输入值列表
     * @return 各输入值对应的估计逆秩
     */
    Single<List<Long>> revRank(double... values);

    /**
     * 对每个输入秩，从最小观测起估算对应分位点的值。
     * <p>
     * Equivalent to {@code TDIGEST.BYRANK key rank [rank ...]}.
     *
     * @param ranks 输入秩列表
     * @return 各秩对应的估计值
     */
    Single<List<Double>> byRank(long... ranks);

    /**
     * 对每个输入逆秩，从最大观测起估算对应分位点的值。
     * <p>
     * Equivalent to {@code TDIGEST.BYREVRANK key rank [rank ...]}.
     *
     * @param ranks 输入逆秩列表
     * @return 各逆秩对应的估计值
     */
    Single<List<Double>> byRevRank(long... ranks);

    /**
     * 返回草图的信息与统计摘要。
     * <p>
     * Equivalent to {@code TDIGEST.INFO key}.
     *
     * @return 草图信息
     */
    Single<TDigestInfo> getInfo();

    /**
     * 重置草图：清空并重新初始化。
     * <p>
     * Equivalent to {@code TDIGEST.RESET key}.
     *
     * @return 无返回值
     */
    Completable reset();

}
