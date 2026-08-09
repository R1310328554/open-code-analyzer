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
package com.alibaba.csp.sentinel.dashboard.domain.vo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;

/**
 * 监控指标视图对象，展示单条资源在某时刻的 QPS、RT 等统计数据。
 * <p>可由 {@link MetricEntity} 或客户端上报的管道分隔行解析得到。
 *
 * @author leyou
 */
public class MetricVo implements Comparable<MetricVo> {
    /** 指标记录主键。 */
    private Long id;
    /** 所属应用名。 */
    private String app;
    /** 指标统计时刻（毫秒时间戳）。 */
    private Long timestamp;
    /** 记录创建时间（毫秒）。 */
    private Long gmtCreate = System.currentTimeMillis();
    /** 受监控资源名。 */
    private String resource;
    /** 通过 QPS。 */
    private Long passQps;
    /** 被限流 QPS。 */
    private Long blockQps;
    /** 成功 QPS。 */
    private Long successQps;
    /** 异常 QPS。 */
    private Long exceptionQps;
    /** 平均响应时间（毫秒）。 */
    private Double rt;
    /** 聚合样本数量。 */
    private Integer count;

    /** 无参构造。 */
    public MetricVo() {
    }

    /** 将 {@link MetricEntity} 集合批量转换为 VO 列表。 */
    public static List<MetricVo> fromMetricEntities(Collection<MetricEntity> entities) {
        List<MetricVo> list = new ArrayList<>();
        if (entities != null) {
            for (MetricEntity entity : entities) {
                list.add(fromMetricEntity(entity));
            }
        }
        return list;
    }

    /**
     * 保留资源名为identity的结果。
     *
     * @param entities 通过hashCode查找到的MetricEntities
     * @param identity 真正需要查找的资源名
     * @return 匹配 identity 的指标 VO 列表
     */
    public static List<MetricVo> fromMetricEntities(Collection<MetricEntity> entities, String identity) {
        List<MetricVo> list = new ArrayList<>();
        if (entities != null) {
            for (MetricEntity entity : entities) {
                if (entity.getResource().equals(identity)) {
                    list.add(fromMetricEntity(entity));
                }
            }
        }
        return list;
    }

    /** 从单条 {@link MetricEntity} 构建 VO，RT 按 successQps 加权平均。 */
    public static MetricVo fromMetricEntity(MetricEntity entity) {
        MetricVo vo = new MetricVo();
        vo.id = entity.getId();
        vo.app = entity.getApp();
        vo.timestamp = entity.getTimestamp().getTime();
        vo.gmtCreate = entity.getGmtCreate().getTime();
        vo.resource = entity.getResource();
        vo.passQps = entity.getPassQps();
        vo.blockQps = entity.getBlockQps();
        vo.successQps = entity.getSuccessQps();
        vo.exceptionQps = entity.getExceptionQps();
        if (entity.getSuccessQps() != 0) {
            vo.rt = entity.getRt() / entity.getSuccessQps();
        } else {
            vo.rt = 0D;
        }
        vo.count = entity.getCount();
        return vo;
    }

    /** 解析客户端上报的管道分隔指标行（timestamp|resource|pass|block|exception|rt|success）。 */
    public static MetricVo parse(String line) {
        String[] strs = line.split("\\|");
        long timestamp = Long.parseLong(strs[0]);
        String identity = strs[1];
        long passQps = Long.parseLong(strs[2]);
        long blockQps = Long.parseLong(strs[3]);
        long exception = Long.parseLong(strs[4]);
        double rt = Double.parseDouble(strs[5]);
        long successQps = Long.parseLong(strs[6]);
        MetricVo vo = new MetricVo();
        vo.timestamp = timestamp;
        vo.resource = identity;
        vo.passQps = passQps;
        vo.blockQps = blockQps;
        vo.successQps = successQps;
        vo.exceptionQps = exception;
        vo.rt = rt;
        vo.count = 1;
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Long getPassQps() {
        return passQps;
    }

    public void setPassQps(Long passQps) {
        this.passQps = passQps;
    }

    public Long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(Long blockQps) {
        this.blockQps = blockQps;
    }

    public Long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(Long successQps) {
        this.successQps = successQps;
    }

    public Long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public Double getRt() {
        return rt;
    }

    public void setRt(Double rt) {
        this.rt = rt;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /** 按 timestamp 升序排序。 */
    @Override
    public int compareTo(MetricVo o) {
        return Long.compare(this.timestamp, o.timestamp);
    }
}
