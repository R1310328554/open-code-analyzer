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
package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.util.List;

/**
 * 聚合监控指标仓库接口，定义保存与按应用/资源/时间区间查询能力。
 *
 * @param <T> 指标实体类型
 * @author Eric Zhao
 */
public interface MetricsRepository<T> {

    /**
     * 保存单条指标到仓库。
     *
     * @param metric 待保存的指标数据
     */
    void save(T metric);

    /**
     * 批量保存指标到仓库。
     *
     * @param metrics 待保存的指标集合
     */
    void saveAll(Iterable<T> metrics);

    /**
     * 按应用名、资源名与时间区间查询全部指标。
     *
     * @param app       Sentinel 应用名
     * @param resource  资源名
     * @param startTime 起始时间戳
     * @param endTime   结束时间戳
     * @return 满足条件的指标列表
     */
    List<T> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime);

    /**
     * 列出指定应用下的资源名，通常按近期限流量排序。
     *
     * @param app 应用名
     * @return 资源名列表
     */
    List<String> listResourcesOfApp(String app);
}
