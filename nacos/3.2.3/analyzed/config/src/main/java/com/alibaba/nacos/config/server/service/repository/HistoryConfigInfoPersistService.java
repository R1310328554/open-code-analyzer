/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.config.server.service.repository;

import com.alibaba.nacos.config.server.model.ConfigHistoryInfo;
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.ConfigInfoStateWrapper;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.persistence.repository.PaginationHelper;

import java.sql.Timestamp;
import java.util.List;

/**
 * 配置历史持久化服务接口：访问 {@code his_config_info} 表，记录配置变更轨迹并支持历史查询与清理。
 * Database service, providing access to his_config_info in the database.
 *
 * @author lixiaoshuang
 */
public interface HistoryConfigInfoPersistService {
    
    /**
     * 创建分页查询助手。
     *
     * @param <E> Generic object
     * @return {@link PaginationHelper}
     */
    <E> PaginationHelper<E> createPaginationHelper();
    
    //------------------------------------------insert 插入区---------------------------------------------//
    
    /**
     * 原子写入一条配置变更历史；仅做最小 SQL 封装，不含业务逻辑。
     *
     * @param id          id
     * @param configInfo  config info
     * @param srcIp       ip
     * @param srcUser     user
     * @param time        time
     * @param ops         ops type
     * @param publishType publish type
     * @param grayName    gray name
     * @param extInfo     extra config info
     */
    void insertConfigHistoryAtomic(long id, ConfigInfo configInfo, String srcIp, String srcUser,
        final Timestamp time,
        String ops, String publishType, String grayName, String extInfo);
    //------------------------------------------delete 删除区---------------------------------------------//
    
    /**
     * 按时间批量清理历史记录（运维归档）。
     *
     * @param startTime start time
     * @param limitSize limit size
     */
    void removeConfigHistory(final Timestamp startTime, final int limitSize);
    //------------------------------------------update 更新区---------------------------------------------//
    //------------------------------------------select 查询区---------------------------------------------//
    
    /**
     * 查询已删除配置的元数据，供 Dump 与审计使用。
     *
     * @param startTime   start time
     * @param startId     last max id
     * @param size        page size
     * @param publishType publish type
     * @return {@link ConfigInfoStateWrapper} list
     */
    List<ConfigInfoStateWrapper> findDeletedConfig(final Timestamp startTime, final long startId,
        int size,
        String publishType);
    
    /**
     * 分页查询指定配置的历史变更列表。
     *
     * @param dataId   data Id
     * @param group    group
     * @param tenant   tenant
     * @param pageNo   no
     * @param pageSize size
     * @return {@link Page} with {@link ConfigHistoryInfo} generation
     */
    Page<ConfigHistoryInfo> findConfigHistory(String dataId, String group, String tenant,
        int pageNo, int pageSize);
    
    /**
     * 按 nid 获取单条历史详情（含内容）。
     *
     * @param nid nid
     * @return {@link ConfigHistoryInfo}
     */
    ConfigHistoryInfo detailConfigHistory(Long nid);
    
    /**
     * 获取指定 id 的上一条历史快照。
     *
     * @param id id
     * @return {@link ConfigHistoryInfo}
     */
    ConfigHistoryInfo detailPreviousConfigHistory(Long id);
    
    /**
     * 统计指定时间之前的历史记录数量（已废弃）。
     *
     * @param startTime start time
     * @return count of history config that meet the conditions
     */
    @Deprecated
    int findConfigHistoryCountByTime(final Timestamp startTime);
    
    /**
     * 获取 startNid 之后的下一条历史记录，用于增量同步。
     *
     * @param dataId      data Id
     * @param group       group
     * @param tenant      tenant
     * @param publishType publish type
     * @param grayName    gray name
     * @param startNid    start nid
     * @return the next history config detail of the history config
     */
    ConfigHistoryInfo getNextHistoryInfo(String dataId, String group, String tenant,
        String publishType, String grayName,
        long startNid);
}
