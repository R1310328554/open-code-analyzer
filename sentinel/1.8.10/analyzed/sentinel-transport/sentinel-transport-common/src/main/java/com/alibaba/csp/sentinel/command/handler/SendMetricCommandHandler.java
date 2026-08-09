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
package com.alibaba.csp.sentinel.command.handler;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.command.CommandHandler;
import com.alibaba.csp.sentinel.command.CommandRequest;
import com.alibaba.csp.sentinel.command.CommandResponse;
import com.alibaba.csp.sentinel.command.annotation.CommandMapping;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.node.metric.MetricSearcher;
import com.alibaba.csp.sentinel.node.metric.MetricWriter;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * 从本地指标日志检索并聚合 {@link MetricNode}；支持时间窗口、行数上限与资源名过滤。
 * 未指定 {@code identity} 时会附加当前 CPU 使用率与系统负载快照。
 *
 * @author leyou
 * @author Eric Zhao
 */
@CommandMapping(name = "metric", desc = "检索聚合指标，参数 startTime/endTime/maxLines/identify")
public class SendMetricCommandHandler implements CommandHandler<String> {

    /** 本地指标文件搜索器，懒加载。 */
    private volatile MetricSearcher searcher;

    /** 延迟初始化 {@link MetricSearcher} 的锁。 */
    private final Object lock = new Object();

    @Override
    public CommandResponse<String> handle(CommandRequest request) {
        // 懒加载 MetricSearcher，非严格线程安全
        if (searcher == null) {
            synchronized (lock) {
                String appName = SentinelConfig.getAppName();
                if (appName == null) {
                    appName = "";
                }
                if (searcher == null) {
                    searcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR,
                        MetricWriter.formMetricFileName(appName, PidUtil.getPid()));
                }
            }
        }
        String startTimeStr = request.getParam("startTime");
        String endTimeStr = request.getParam("endTime");
        String maxLinesStr = request.getParam("maxLines");
        String identity = request.getParam("identity");
        long startTime = -1;
        int maxLines = 6000;
        if (StringUtil.isNotBlank(startTimeStr)) {
            startTime = Long.parseLong(startTimeStr);
        } else {
            return CommandResponse.ofSuccess("");
        }
        List<MetricNode> list;
        try {
            // 指定 endTime 时按闭区间检索
            if (StringUtil.isNotBlank(endTimeStr)) {
                long endTime = Long.parseLong(endTimeStr);
                list = searcher.findByTimeAndResource(startTime, endTime, identity);
            } else {
                if (StringUtil.isNotBlank(maxLinesStr)) {
                    maxLines = Integer.parseInt(maxLinesStr);
                }
                maxLines = Math.min(maxLines, 12000);
                list = searcher.find(startTime, maxLines);
            }
        } catch (Exception ex) {
            return CommandResponse.ofFailure(new RuntimeException("Error when retrieving metrics", ex));
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        if (StringUtil.isBlank(identity)) {
            addCpuUsageAndLoad(list);
        }
        StringBuilder sb = new StringBuilder();
        for (MetricNode node : list) {
            sb.append(node.toThinString()).append("\n");
        }
        return CommandResponse.ofSuccess(sb.toString());
    }

    /**
     * 将当前 CPU 使用率与系统平均负载追加到指标列表。
     *
     * @param list metric list, should not be null
     */
    private void addCpuUsageAndLoad(List<MetricNode> list) {
        long time = TimeUtil.currentTimeMillis() / 1000 * 1000;
        double load = SystemRuleManager.getCurrentSystemAvgLoad();
        double usage = SystemRuleManager.getCurrentCpuUsage();
        if (load > 0) {
            MetricNode loadNode = toNode(load, time, Constants.SYSTEM_LOAD_RESOURCE_NAME);
            list.add(loadNode);
        }
        if (usage > 0) {
            MetricNode usageNode = toNode(usage, time, Constants.CPU_USAGE_RESOURCE_NAME);
            list.add(usageNode);
        }
    }

    /**
     * 将浮点值乘以 10000 后写入 {@link MetricNode#passQps}，用于承载 CPU/负载等非 QPS 指标。
     * <p>
     * 在统一指标格式落地前的临时编码方案。
     * </p>
     *
     * @param value    value to save.
     * @param ts       timestamp
     * @param resource resource name.
     * @return a MetricNode represents the value.
     */
    private MetricNode toNode(double value, long ts, String resource) {
        MetricNode node = new MetricNode();
        node.setPassQps((long)(value * 10000));
        node.setTimestamp(ts);
        node.setResource(resource);
        return node;
    }
}
