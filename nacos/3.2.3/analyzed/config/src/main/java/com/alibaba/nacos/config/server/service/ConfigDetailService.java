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

package com.alibaba.nacos.config.server.service;

import com.alibaba.nacos.api.exception.runtime.NacosRuntimeException;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.config.server.constant.Constants;
import com.alibaba.nacos.config.server.constant.PropertiesConstant;
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.api.model.Page;
import com.alibaba.nacos.config.server.service.repository.ConfigInfoPersistService;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 配置分页检索服务：通过有界队列 + 工作线程池异步执行 DB 查询，调用方阻塞等待结果。
 * 支持精确/模糊搜索，队列满或超时时抛出 503 限流异常。
 * config detail service.
 *
 * @author 985492783@qq.com
 * @date 2023/2/9 5:25
 */
@Service
public class ConfigDetailService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigDetailService.class);
    
    private final ConfigInfoPersistService configInfoPersistService;
    
    private BlockingQueue<SearchEvent> eventLinkedBlockingQueue;
    
    private ScheduledExecutorService clientEventExecutor;
    
    /**
     * 搜索任务队列最大容量，可通过 {@link PropertiesConstant#SEARCH_MAX_CAPACITY} 配置。
     */
    private static int maxCapacity = 4;
    
    private static final int MAX_CAPACITY = 32;
    
    /**
     * 调用方等待搜索结果的超时毫秒数，可通过 {@link PropertiesConstant#SEARCH_WAIT_TIMEOUT} 配置。
     */
    private static long waitTimeout = 8000L;
    
    /**
     * 搜索工作线程数上限，可通过 {@link PropertiesConstant#SEARCH_MAX_THREAD} 配置。
     */
    private static int maxThread = 2;
    
    private static final int MAX_THREAD = 16;
    
    public ConfigDetailService(ConfigInfoPersistService configInfoPersistService) {
        this.configInfoPersistService = configInfoPersistService;
        loadSetting();
        initWorker();
    }
    
    private void loadSetting() {
        setMaxCapacity(
            Math.min(Integer.parseInt(EnvUtil.getProperty(PropertiesConstant.SEARCH_MAX_CAPACITY,
                String.valueOf(getMaxCapacity()))), MAX_CAPACITY));
        setMaxThread(
            Math.min(Integer.parseInt(EnvUtil.getProperty(PropertiesConstant.SEARCH_MAX_THREAD,
                String.valueOf(getMaxThread()))), MAX_THREAD));
        setWaitTimeout(Integer.parseInt(EnvUtil.getProperty(PropertiesConstant.SEARCH_WAIT_TIMEOUT,
            String.valueOf(getWaitTimeout()))));
    }
    
    /**
     * 初始化有界队列与固定数量搜索工作线程（长期循环 take 任务）。
     */
    private void initWorker() {
        this.eventLinkedBlockingQueue = new LinkedBlockingQueue<>(maxCapacity);
        
        clientEventExecutor = new ScheduledThreadPoolExecutor(maxThread,
            new NameThreadFactory("com.alibaba.nacos.config.search.worker"));
        
        for (int i = 0; i < maxThread; i++) {
            clientEventExecutor.submit(() -> {
                while (true) {
                    try {
                        SearchEvent event = eventLinkedBlockingQueue.take();
                        Page<ConfigInfo> result = null;
                        if (Constants.CONFIG_SEARCH_BLUR.equals(event.getType())) {
                            result = configInfoPersistService.findConfigInfoLike4Page(event.pageNo,
                                event.pageSize,
                                event.dataId, event.group, event.tenant, event.configAdvanceInfo);
                        } else {
                            result = configInfoPersistService.findConfigInfo4Page(event.pageNo,
                                event.pageSize,
                                event.dataId, event.group, event.tenant, event.configAdvanceInfo);
                        }
                        synchronized (event) {
                            event.setResponse(result);
                            event.notifyAll();
                        }
                    } catch (Exception e) {
                        LOGGER.error("catch search worker error: {}", e.getMessage());
                    }
                }
            });
        }
    }
    
    /**
     * 阻塞当前线程，将搜索任务入队并由 worker 执行 DB 分页查询后唤醒返回。
     */
    public Page<ConfigInfo> findConfigInfoPage(String search, int pageNo, int pageSize,
        String dataId, String group,
        String tenant, Map<String, Object> configAdvanceInfo) throws NacosRuntimeException {
        SearchEvent searchEvent = new SearchEvent(search, pageNo, pageSize, dataId, group, tenant,
            configAdvanceInfo);
        Page<ConfigInfo> result = null;
        try {
            synchronized (searchEvent) {
                boolean offer = eventLinkedBlockingQueue.offer(searchEvent);
                if (!offer) {
                    throw new NacosRuntimeException(503, "server limit match.");
                }
                searchEvent.wait(waitTimeout);
                result = searchEvent.getResponse();
            }
        } catch (InterruptedException e) {
            LOGGER.error("get config detail timeout: {}.", e.getMessage());
            throw new NacosRuntimeException(503, "server limit match.");
        }
        if (result == null) {
            throw new NacosRuntimeException(503, "server limit match.");
        }
        return result;
    }
    
    public static int getMaxCapacity() {
        return maxCapacity;
    }
    
    public static void setMaxCapacity(int maxCapacity) {
        ConfigDetailService.maxCapacity = maxCapacity;
    }
    
    public static long getWaitTimeout() {
        return waitTimeout;
    }
    
    public static void setWaitTimeout(long waitTimeout) {
        ConfigDetailService.waitTimeout = waitTimeout;
    }
    
    public static int getMaxThread() {
        return maxThread;
    }
    
    public static void setMaxThread(int maxThread) {
        ConfigDetailService.maxThread = maxThread;
    }
    
    public static class SearchEvent {
        
        private String type;
        
        private int pageNo;
        
        private int pageSize;
        
        private String dataId;
        
        private String group;
        
        private String tenant;
        
        private Map<String, Object> configAdvanceInfo;
        
        private Page<ConfigInfo> response;
        
        public SearchEvent() {
        }
        
        public SearchEvent(String type, int pageNo, int pageSize, String dataId, String group,
            String tenant,
            Map<String, Object> configAdvanceInfo) {
            this.type = type;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            this.dataId = dataId;
            this.group = group;
            this.tenant = tenant;
            this.configAdvanceInfo = configAdvanceInfo;
        }
        
        public String getType() {
            return type;
        }
        
        public int getPageNo() {
            return pageNo;
        }
        
        public int getPageSize() {
            return pageSize;
        }
        
        public String getDataId() {
            return dataId;
        }
        
        public String getGroup() {
            return group;
        }
        
        public String getTenant() {
            return tenant;
        }
        
        public Map<String, Object> getConfigAdvanceInfo() {
            return configAdvanceInfo;
        }
        
        public Page<ConfigInfo> getResponse() {
            return response;
        }
        
        public void setResponse(Page<ConfigInfo> response) {
            this.response = response;
        }
    }
}
