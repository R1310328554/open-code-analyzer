/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Function;

import java.util.Collection;

/**
 * <p>
 * 结合前序 Slot（{@link com.alibaba.csp.sentinel.slots.nodeselector.NodeSelectorSlot}、
 * {@link com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot} 与
 * {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot}）采集的运行时统计信息，
 * FlowSlot 依据预设规则判定入站请求是否应被阻断。
 * </p>
 *
 * <p>
 * 若任意规则被触发，{@code SphU.entry(resourceName)} 将抛出 {@code FlowException}。
 * 用户可通过捕获 {@code FlowException} 自定义处理逻辑。
 * </p>
 *
 * <p>
 * 一个资源可配置多条流控规则。FlowSlot 依次遍历这些规则，直到某条被触发或全部遍历完毕。
 * </p>
 *
 * <p>
 * 每条 {@link FlowRule} 主要由 grade、strategy、path 等因素组成，
 * 可组合这些因素实现不同的流控效果。
 * </p>
 *
 * <p>
 * grade 由 {@link FlowRule} 的 {@code grade} 字段定义：0 表示线程隔离，1 表示请求数整形（QPS）。
 * 线程数与请求数均在运行时实时采集，可通过以下命令查看统计：
 * </p>
 *
 * <pre>
 * curl http://localhost:8719/tree
 *
 * idx id    thread pass  blocked   success total aRt   1m-pass   1m-block   1m-all   exception
 * 2   abc647 0      460    46          46   1    27      630       276        897      0
 * </pre>
 *
 * <ul>
 * <li>{@code thread}：当前正在处理该资源的线程数</li>
 * <li>{@code pass}：一秒内通过的请求数</li>
 * <li>{@code blocked}：一秒内被阻断的请求数</li>
 * <li>{@code success}：一秒内由 Sentinel 成功处理的请求数</li>
 * <li>{@code RT}：一秒内请求的平均响应时间</li>
 * <li>{@code total}：一秒内入站请求与被阻断请求的总和</li>
 * <li>{@code 1m-pass}：一分钟内通过的请求数</li>
 * <li>{@code 1m-block}：一分钟内被阻断的请求数</li>
 * <li>{@code 1m-all}：一分钟内入站与被阻断请求的总和</li>
 * <li>{@code exception}：一秒内业务（自定义）异常数</li>
 * </ul>
 *
 * 该阶段通常用于防止资源被长时间占用。若资源处理耗时较长，线程会持续占用；
 * 响应越慢，占用的线程越多。
 *
 * 除计数器外，也可使用线程池或信号量实现并发控制：
 *
 * - 线程池：为资源分配专用线程池；池中无空闲线程时直接拒绝，不影响其他资源。
 *
 * - 信号量：用信号量控制该资源的并发线程数。
 *
 * 线程池的优势是超时可优雅退出，但会带来上下文切换与额外线程开销。
 * 若入站请求已在独立线程中处理（如 Servlet HTTP 请求），使用线程池几乎会使线程数翻倍。
 *
 * <h3>流量整形</h3>
 * <p>
 * 当 QPS 超过阈值时，Sentinel 将按流控规则中的 {@code controlBehavior} 字段对入站请求采取控制动作。
 * </p>
 * <ol>
 * <li>直接拒绝（{@code RuleConstant.CONTROL_BEHAVIOR_DEFAULT}）</li>
 * <p>
 * 默认行为：超限请求立即被拒绝并抛出 FlowException。
 * </p>
 *
 * <li>预热（{@code RuleConstant.CONTROL_BEHAVIOR_WARM_UP}）</li>
 * <p>
 * 若系统长期低负载后突然涌入大量请求，可能无法一次性全部处理。
 * 通过逐步增加入站请求，系统可预热并最终承载全部流量。
 * 预热时长可通过流控规则中的 {@code warmUpPeriodSec} 配置。
 * </p>
 *
 * <li>匀速排队（{@code RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER}）</li>
 * <p>
 * 该策略严格控制请求间隔，以稳定、均匀的速率放行请求。
 * </p>
 * <img src="https://raw.githubusercontent.com/wiki/alibaba/Sentinel/image/uniform-speed-queue.png" style="max-width:
 * 60%;"/>
 * <p>
 * 该策略是<a href="https://en.wikipedia.org/wiki/Leaky_bucket">漏桶算法</a>的实现，
 * 以恒定速率处理请求，常用于突发流量（如消息处理）场景。
 * 当超出系统容量的大量请求同时到达时，系统以固定速率处理，
 * 直至全部完成或超时。
 * </p>
 * </ol>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
@Spi(order = Constants.ORDER_FLOW_SLOT)
public class FlowSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    private final FlowRuleChecker checker;

    public FlowSlot() {
        this(new FlowRuleChecker());
    }

    /**
     * 包内可见，供测试使用。
     *
     * @param checker 流控规则校验器
     * @since 1.6.1
     */
    FlowSlot(FlowRuleChecker checker) {
        AssertUtil.notNull(checker, "flow checker should not be null");
        this.checker = checker;
    }

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count,
                      boolean prioritized, Object... args) throws Throwable {
        checkFlow(resourceWrapper, context, node, count, prioritized);

        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    void checkFlow(ResourceWrapper resource, Context context, DefaultNode node, int count, boolean prioritized)
        throws BlockException {
        checker.checkFlow(ruleProvider, resource, context, node, count, prioritized);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        fireExit(context, resourceWrapper, count, args);
    }

    private final Function<String, Collection<FlowRule>> ruleProvider = new Function<String, Collection<FlowRule>>() {
        @Override
        public Collection<FlowRule> apply(String resource) {
            return FlowRuleManager.getFlowRules(resource);
        }
    };
}
