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

package com.alibaba.nacos.cmdb.memory;

import com.alibaba.nacos.api.cmdb.pojo.Entity;
import com.alibaba.nacos.api.cmdb.pojo.EntityEvent;
import com.alibaba.nacos.api.cmdb.pojo.Label;
import com.alibaba.nacos.api.cmdb.spi.CmdbService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.cmdb.core.SwitchAndOptions;
import com.alibaba.nacos.cmdb.service.CmdbReader;
import com.alibaba.nacos.cmdb.service.CmdbWriter;
import com.alibaba.nacos.cmdb.utils.CmdbExecutor;
import com.alibaba.nacos.cmdb.utils.Loggers;
import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.common.utils.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * CMDB provider.
 * <p>CMDB 内存数据提供者：通过 SPI 加载 {@link CmdbService}，维护实体与标签缓存，并周期性 dump 全量数据、刷新标签定义、消费实体变更事件。</p>
 *
 * @author nkorange
 * @since 0.7.0
 */
@Component
public class CmdbProvider implements CmdbReader, CmdbWriter {
    
    /** CMDB 开关与任务间隔配置 */
    @Autowired
    private SwitchAndOptions switches;
    
    /** 当前生效的 CMDB SPI 实现（取 {@link NacosServiceLoader} 首个） */
    private CmdbService cmdbService;
    
    /** SPI 发现的全部 CmdbService 实现 */
    private final Collection<CmdbService> services = NacosServiceLoader.load(CmdbService.class);
    
    /** 实体缓存：entityType → (entityName → {@link Entity}) */
    private Map<String, Map<String, Entity>> entityMap = new ConcurrentHashMap<>();
    
    /** 标签定义缓存：labelName → {@link Label} */
    private Map<String, Label> labelMap = new ConcurrentHashMap<>();
    
    /** 已注册的实体类型集合，用于过滤非法更新 */
    private Set<String> entityTypeSet = new HashSet<>();
    
    /** 上次拉取实体变更事件的时间戳（毫秒） */
    private long eventTimestamp = System.currentTimeMillis();
    
    /** 默认构造；{@link #init()} 中完成 SPI 与定时任务注册 */
    public CmdbProvider() throws NacosException {
    }
    
    /** 选取首个 CmdbService；若要求启动加载却无实现则抛 {@link NacosException} */
    private void initCmdbService() throws NacosException {
        Iterator<CmdbService> iterator = services.iterator();
        if (iterator.hasNext()) {
            cmdbService = iterator.next();
        }
        
        if (cmdbService == null && switches.isLoadDataAtStart()) {
            throw new NacosException(NacosException.SERVER_ERROR, "Cannot initialize CmdbService!");
        }
    }
    
    /**
     * load data.
     * <p>启动时全量加载：标签定义、实体类型与实体 map（受 {@link SwitchAndOptions#isLoadDataAtStart()} 控制）。</p>
     */
    public void load() {
        
        if (!switches.isLoadDataAtStart()) {
            return;
        }
        
        // 初始化标签定义缓存
        Set<String> labelNames = cmdbService.getLabelNames();
        if (labelNames == null || labelNames.isEmpty()) {
            Loggers.MAIN.warn("[LOAD] init label names failed!");
        } else {
            for (String labelName : labelNames) {
                // 标签元数据可能暂为空，后续遇到该标签时再尝试加载
                labelMap.put(labelName, cmdbService.getLabel(labelName));
            }
        }
        
        // 初始化支持的实体类型集合
        entityTypeSet = cmdbService.getEntityTypes();
        
        // 初始化全量实体 map
        entityMap = cmdbService.getAllEntities();
    }
    
    /**
     * Init, called by spring.
     * <p>Spring {@link PostConstruct} 入口：初始化 SPI、可选全量 load，并注册 dump/标签/事件三类定时任务。</p>
     *
     * @throws NacosException nacos exception
     */
    @PostConstruct
    public void init() throws NacosException {
        
        initCmdbService();
        load();
        
        CmdbExecutor.scheduleCmdbTask(new CmdbDumpTask(), switches.getDumpTaskInterval(),
            TimeUnit.SECONDS);
        CmdbExecutor.scheduleCmdbTask(new CmdbLabelTask(), switches.getLabelTaskInterval(),
            TimeUnit.SECONDS);
        CmdbExecutor.scheduleCmdbTask(new CmdbEventTask(), switches.getEventTaskInterval(),
            TimeUnit.SECONDS);
    }
    
    /** 按类型与名称从内存 map 查询实体 */
    @Override
    public Entity queryEntity(String entityName, String entityType) {
        if (!entityMap.containsKey(entityType)) {
            return null;
        }
        return entityMap.get(entityType).get(entityName);
    }
    
    /** 查询指定实体上某标签的值 */
    @Override
    public String queryLabel(String entityName, String entityType, String labelName) {
        Entity entity = queryEntity(entityName, entityType);
        if (entity == null) {
            return null;
        }
        return entity.getLabels().get(labelName);
    }
    
    /** 按标签反查实体列表（当前未实现） */
    @Override
    public List<Entity> queryEntitiesByLabel(String labelName, String labelValue) {
        throw new UnsupportedOperationException("Not available now!");
    }
    
    /**
     * Remove CMDB entity.
     * <p>从内存缓存移除指定实体；类型不存在时静默返回。</p>
     *
     * @param entityName entity name
     * @param entityType entity type
     */
    public void removeEntity(String entityName, String entityType) {
        if (!entityMap.containsKey(entityType)) {
            return;
        }
        entityMap.get(entityType).remove(entityName);
    }
    
    /**
     * Update entity.
     * <p>写入或覆盖实体；类型未在 {@link #entityTypeSet} 中则忽略。</p>
     *
     * @param entity entity
     */
    public void updateEntity(Entity entity) {
        if (!entityTypeSet.contains(entity.getType())) {
            return;
        }
        entityMap.get(entity.getType()).put(entity.getName(), entity);
    }
    
    /** 周期性从 CmdbService 刷新标签定义 map */
    public class CmdbLabelTask implements Runnable {
        
        @Override
        public void run() {
            
            Loggers.MAIN.debug("LABEL-TASK {}", "start dump.");
            
            if (cmdbService == null) {
                return;
            }
            
            try {
                
                Map<String, Label> tmpLabelMap = new HashMap<>(16);
                
                Set<String> labelNames = cmdbService.getLabelNames();
                if (labelNames == null || labelNames.isEmpty()) {
                    Loggers.MAIN.warn("CMDB-LABEL-TASK {}", "load label names failed!");
                } else {
                    for (String labelName : labelNames) {
                        // If get null label, it's still ok. We will try it later when we meet this label:
                        tmpLabelMap.put(labelName, cmdbService.getLabel(labelName));
                    }
                    
                    if (Loggers.MAIN.isDebugEnabled()) {
                        Loggers.MAIN.debug("LABEL-TASK {}",
                            "got label map:" + JacksonUtils.toJson(tmpLabelMap));
                    }
                    
                    labelMap = tmpLabelMap;
                }
                
            } catch (Exception e) {
                Loggers.MAIN.error("CMDB-LABEL-TASK {}", "dump failed!", e);
            } finally {
                CmdbExecutor.scheduleCmdbTask(this, switches.getLabelTaskInterval(),
                    TimeUnit.SECONDS);
            }
        }
    }
    
    /** 周期性全量替换 {@link #entityMap} */
    public class CmdbDumpTask implements Runnable {
        
        @Override
        public void run() {
            
            try {
                
                Loggers.MAIN.debug("DUMP-TASK {}", "start dump.");
                
                if (cmdbService == null) {
                    return;
                }
                // 用 SPI 返回的全量实体 map 替换本地缓存
                entityMap = cmdbService.getAllEntities();
            } catch (Exception e) {
                Loggers.MAIN.error("DUMP-TASK {}", "dump failed!", e);
            } finally {
                CmdbExecutor.scheduleCmdbTask(this, switches.getDumpTaskInterval(),
                    TimeUnit.SECONDS);
            }
        }
    }
    
    /** 增量拉取实体变更事件并更新本地缓存 */
    public class CmdbEventTask implements Runnable {
        
        @Override
        public void run() {
            try {
                
                Loggers.MAIN.debug("EVENT-TASK {}", "start dump.");
                
                if (cmdbService == null) {
                    return;
                }
                
                long current = System.currentTimeMillis();
                List<EntityEvent> events = cmdbService.getEntityEvents(eventTimestamp);
                eventTimestamp = current;
                
                if (Loggers.MAIN.isDebugEnabled()) {
                    Loggers.MAIN.debug("EVENT-TASK {}",
                        "got events size:" + ", events:" + JacksonUtils.toJson(events));
                }
                
                if (events != null && !events.isEmpty()) {
                    
                    for (EntityEvent event : events) {
                        switch (event.getType()) {
                            case ENTITY_REMOVE:
                                removeEntity(event.getEntityName(), event.getEntityType());
                                break;
                            case ENTITY_ADD_OR_UPDATE:
                                updateEntity(cmdbService.getEntity(event.getEntityName(),
                                    event.getEntityType()));
                                break;
                            default:
                                break;
                        }
                    }
                }
                
            } catch (Exception e) {
                Loggers.MAIN.error("CMDB-EVENT {}", "event task failed!", e);
            } finally {
                CmdbExecutor.scheduleCmdbTask(this, switches.getEventTaskInterval(),
                    TimeUnit.SECONDS);
            }
        }
    }
}
