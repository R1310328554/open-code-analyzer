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
package com.alibaba.csp.sentinel.dashboard.repository.rule;

import java.util.List;

import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;

/**
 * 规则存储与查询接口，支持按 ID、机器与应用维度访问。
 *
 * @author leyou
 */
public interface RuleRepository<T, ID> {

    /**
     * 保存单条规则。
     *
     * @param entity 规则实体
     * @return 保存后的实体
     */
    T save(T entity);

    /**
     * 全量保存规则（先清空再写入）。
     *
     * @param rules 规则列表
     * @return 已保存的规则列表
     */
    List<T> saveAll(List<T> rules);

    /**
     * 按 ID 删除规则。
     *
     * @param id 规则 ID
     * @return 被删除的实体
     */
    T delete(ID id);

    /**
     * 按 ID 查询规则。
     *
     * @param id 规则 ID
     * @return 规则实体，不存在时返回 null
     */
    T findById(ID id);

    /**
     * 查询指定机器上的全部规则。
     *
     * @param machineInfo 机器信息
     * @return 规则列表
     */
    List<T> findAllByMachine(MachineInfo machineInfo);

    /**
     * 查询指定应用下的全部规则。
     *
     * @param appName 有效应用名
     * @return 该应用的全部规则
     * @since 1.4.0
     */
    List<T> findAllByApp(String appName);

    ///**
    // * Find all by app and enable switch.
    // * @param app
    // * @param enable
    // * @return
    // */
    //List<T> findAllByAppAndEnable(String app, boolean enable);
}
