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

package com.alibaba.nacos.config.server.service.trace;

import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.config.server.constant.Constants;
import com.alibaba.nacos.config.server.monitor.MetricsMonitor;
import com.alibaba.nacos.config.server.utils.LogUtil;
import com.alibaba.nacos.sys.utils.InetUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 配置全链路追踪服务：将持久化、通知、Dump、Pull 等阶段事件以管道分隔格式写入 trace 日志，并驱动 {@link com.alibaba.nacos.config.server.monitor.MetricsMonitor} 计时指标。
 * Config trace.
 *
 * @author Nacos
 */
@Service
public class ConfigTraceService {
    
    /**
     * 持久化阶段事件标识。
     * persist event.
     */
    public static final String PERSISTENCE_EVENT = "persist";
    
    /** Beta 配置持久化事件 */
    public static final String PERSISTENCE_EVENT_BETA = "persist-beta";
    
    /** Tag 配置持久化事件 */
    public static final String PERSISTENCE_EVENT_TAG = "persist-tag";
    
    /** 元数据持久化事件 */
    public static final String PERSISTENCE_EVENT_METADATA = "persist-metadata";
    
    /**
     * 持久化操作类型常量。
     * persist type.
     */
    /** 发布/写入 */
    public static final String PERSISTENCE_TYPE_PUB = "pub";
    
    /** 删除 */
    public static final String PERSISTENCE_TYPE_REMOVE = "remove";
    
    /** 合并 */
    public static final String PERSISTENCE_TYPE_MERGE = "merge";
    
    /**
     * 长轮询/推送通知阶段事件标识。
     * notify event.
     */
    public static final String NOTIFY_EVENT = "notify";
    
    public static final String NOTIFY_EVENT_BETA = "notify-beta";
    
    public static final String NOTIFY_EVENT_BATCH = "notify-batch";
    
    public static final String NOTIFY_EVENT_TAG = "notify-tag";
    
    /**
     * 通知结果类型常量。
     * notify type.
     */
    /** 通知成功 */
    public static final String NOTIFY_TYPE_OK = "ok";
    
    /** 通知失败 */
    public static final String NOTIFY_TYPE_ERROR = "error";
    
    /** 客户端不健康 */
    public static final String NOTIFY_TYPE_UNHEALTH = "unhealth";
    
    /** 通知过程异常 */
    public static final String NOTIFY_TYPE_EXCEPTION = "exception";
    
    /**
     * 本地缓存 Dump 阶段事件标识。
     * dump event.
     */
    public static final String DUMP_EVENT = "dump";
    
    public static final String DUMP_EVENT_BETA = "dump-beta";
    
    public static final String DUMP_EVENT_BATCH = "dump-batch";
    
    public static final String DUMP_EVENT_TAG = "dump-tag";
    
    /**
     * Dump 结果类型常量。
     * dump type.
     */
    /** Dump 成功 */
    public static final String DUMP_TYPE_OK = "ok";
    
    /** 删除 Dump 成功 */
    public static final String DUMP_TYPE_REMOVE_OK = "remove-ok";
    
    /** Dump 失败 */
    public static final String DUMP_TYPE_ERROR = "error";
    
    /**
     * 客户端拉取配置阶段事件标识。
     * pull event.
     */
    public static final String PULL_EVENT = "pull";
    
    /**
     * 拉取结果类型常量。
     * pull type.
     */
    public static final String PULL_TYPE_OK = "ok";
    
    public static final String PULL_TYPE_NOTFOUND = "not-found";
    
    public static final String PULL_TYPE_CONFLICT = "conflict";
    
    public static final String PULL_TYPE_ERROR = "error";
    
    /**
     * log persistence event.
     *
     * @param dataId           data id
     * @param group            group
     * @param tenant           tenant
     * @param requestIpAppName request ip app name
     * @param ts               ts
     * @param handleIp         remote ip
     * @param type             type
     * @param content          content
      * <p>配置全链路 trace 日志服务；详见类级说明。</p>
     */
    /** 记录持久化 trace：末尾 ext 字段为内容 MD5 */
    public static void logPersistenceEvent(String dataId, String group, String tenant,
        String requestIpAppName, long ts,
        String handleIp, String event, String type, String content) {
        if (!LogUtil.TRACE_LOG.isInfoEnabled()) {
            return;
        }
        // 空 tenant 写 null 便于 tlog 分段解析
        if (StringUtils.isBlank(tenant)) {
            tenant = null;
        }
        //localIp | dataid | group | tenant | requestIpAppName | ts | client ip | event | type | [delayed = -1] | ext
        // (md5)
        String md5 = content == null ? null : MD5Utils.md5Hex(content, Constants.PERSIST_ENCODE);
        LogUtil.TRACE_LOG.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", InetUtils.getSelfIP(), dataId,
            group, tenant,
            requestIpAppName, ts, handleIp, event, type, -1, md5);
    }
    
    /**
     * log notify event.
     *
     * @param dataId           data id
     * @param group            group
     * @param tenant           tenant
     * @param requestIpAppName request ip app name
     * @param ts               ts
     * @param handleIp         handle ip
     * @param type             type
     * @param delayed          delayed
     * @param targetIp         target ip
      * <p>配置全链路 trace 日志服务；详见类级说明。</p>
     */
    /** 记录通知 trace 并上报 notify 耗时到 MetricsMonitor */
    public static void logNotifyEvent(String dataId, String group, String tenant,
        String requestIpAppName, long ts,
        String handleIp, String event, String type, long delayed, String targetIp) {
        if (!LogUtil.TRACE_LOG.isInfoEnabled()) {
            return;
        }
        if (delayed < 0) {
            delayed = 0;
        }
        MetricsMonitor.getNotifyRtTimer().record(delayed, TimeUnit.MILLISECONDS);
        // Convenient tlog segmentation
        if (StringUtils.isBlank(tenant)) {
            tenant = null;
        }
        //localIp | dataid | group | tenant | requestIpAppName | ts | handleIp | event | type | [delayed] | ext
        // (targetIp)
        LogUtil.TRACE_LOG.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", InetUtils.getSelfIP(), dataId,
            group, tenant,
            requestIpAppName, ts, handleIp, event, type, delayed, targetIp);
    }
    
    /**
     * log dump event.
     *
     * @param dataId           data id
     * @param group            group
     * @param tenant           tenant
     * @param requestIpAppName request ip app name
     * @param ts               ts
     * @param handleIp         handle ip
     * @param type             type
     * @param delayed          delayed
     * @param length           length
      * <p>配置全链路 trace 日志服务；详见类级说明。</p>
     */
    /** 记录标准 Dump trace，event 固定为 {@link #DUMP_EVENT} */
    public static void logDumpEvent(String dataId, String group, String tenant,
        String requestIpAppName, long ts,
        String handleIp, String type, long delayed, long length) {
        logDumpEventInner(dataId, group, tenant, requestIpAppName, ts, handleIp,
            ConfigTraceService.DUMP_EVENT, type,
            delayed, length);
    }
    
    /** 记录带灰度名的 Dump trace，event 为 dump-{grayName} */
    public static void logDumpGrayNameEvent(String dataId, String group, String tenant,
        String grayName,
        String requestIpAppName, long ts, String handleIp, String type, long delayed, long length) {
        logDumpEventInner(dataId, group, tenant, requestIpAppName, ts, handleIp,
            ConfigTraceService.DUMP_EVENT + "-" + grayName, type, delayed, length);
    }
    
    private static void logDumpEventInner(String dataId, String group, String tenant,
        String requestIpAppName, long ts,
        String handleIp, String event, String type, long delayed, long length) {
        if (!LogUtil.TRACE_LOG.isInfoEnabled()) {
            return;
        }
        if (delayed < 0) {
            delayed = 0;
        }
        MetricsMonitor.getDumpRtTimer().record(delayed, TimeUnit.MILLISECONDS);
        // Convenient tlog segmentation
        if (StringUtils.isBlank(tenant)) {
            tenant = null;
        }
        //localIp | dataid | group | tenant | requestIpAppName | ts | handleIp | event | type | [delayed] | length
        LogUtil.TRACE_LOG.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", InetUtils.getSelfIP(), dataId,
            group, tenant,
            requestIpAppName, ts, handleIp, event, type, delayed, length);
    }
    
    /**
     * log dump all event.
     *
     * @param dataId           data id
     * @param group            group
     * @param tenant           tenant
     * @param requestIpAppName request ip app name
     * @param ts               ts
     * @param handleIp         handle ip
     * @param type             type
      * <p>配置全链路 trace 日志服务；详见类级说明。</p>
     */
    /** 记录全量 Dump trace，event 固定为 dump-all */
    public static void logDumpAllEvent(String dataId, String group, String tenant,
        String requestIpAppName, long ts,
        String handleIp, String type) {
        if (!LogUtil.TRACE_LOG.isInfoEnabled()) {
            return;
        }
        // Convenient tlog segmentation
        if (StringUtils.isBlank(tenant)) {
            tenant = null;
        }
        //localIp | dataid | group | tenant | requestIpAppName | ts | handleIp | event | type | [delayed = -1]
        LogUtil.TRACE_LOG.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", InetUtils.getSelfIP(), dataId,
            group, tenant,
            requestIpAppName, ts, handleIp, "dump-all", type, -1);
    }
    
    /**
     * log pull event.
     *
     * @param dataId           data id
     * @param group            group
     * @param tenant           tenant
     * @param requestIpAppName request ip app name
     * @param ts               ts
     * @param type             type
     * @param delayed          delayed
     * @param clientIp         clientIp
     * @param isNotify         isNotify
     * @param model            model
      * <p>配置全链路 trace 日志服务；详见类级说明。</p>
     */
    /** 记录客户端拉取 trace，含 delayed、clientIp、isNotify 与协议 model */
    public static void logPullEvent(String dataId, String group, String tenant,
        String requestIpAppName, long ts,
        String event, String type, long delayed, String clientIp, boolean isNotify, String model) {
        if (!LogUtil.TRACE_LOG.isInfoEnabled()) {
            return;
        }
        // Convenient tlog segmentation
        if (StringUtils.isBlank(tenant)) {
            tenant = null;
        }
        if (isNotify && delayed < 0) {
            delayed = 0;
        }
        // localIp | dataid | group | tenant| requestIpAppName| ts | event | type | [delayed] |clientIp| isNotify | mode（http/grpc)
        LogUtil.TRACE_LOG.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", InetUtils.getSelfIP(), dataId,
            group, tenant,
            requestIpAppName, ts, event, type, delayed, clientIp, isNotify, model);
    }
}
