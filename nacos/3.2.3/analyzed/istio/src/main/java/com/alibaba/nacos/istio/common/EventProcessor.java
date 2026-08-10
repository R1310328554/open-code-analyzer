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

package com.alibaba.nacos.istio.common;

import com.alibaba.nacos.istio.mcp.NacosMcpService;
import com.alibaba.nacos.istio.misc.Loggers;
import com.alibaba.nacos.istio.model.PushRequest;
import com.alibaba.nacos.istio.util.IstioExecutor;
import com.alibaba.nacos.istio.xds.NacosXdsService;
import com.alibaba.nacos.sys.utils.ApplicationUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Istio 推送事件处理器：异步消费 {@link PushRequest} 队列，在有客户端连接时触发 XDS/MCP 推送。
 *
 * <p>Spring 容器根上下文刷新后启动守护线程轮询事件；依赖 {@link NacosResourceManager}、{@link NacosXdsService}、{@link NacosMcpService}。</p>
 *
 * @author special.fy
 */
@Component
public class EventProcessor implements ApplicationListener<ContextRefreshedEvent> {
    
    /** 事件队列 poll 最大等待毫秒数。 */
    private static final int MAX_WAIT_EVENT_TIME = 100;
    
    private NacosMcpService nacosMcpService;
    
    private NacosXdsService nacosXdsService;
    
    private NacosResourceManager resourceManager;
    
    /** 待处理的推送请求阻塞队列。 */
    private final BlockingQueue<PushRequest> requests;
    
    public EventProcessor() {
        requests = new ArrayBlockingQueue<>(20);
    }
    
    /**
     * 将推送请求放入队列；队列满时记录警告并恢复中断标志。
     *
     * @param pushRequest 待处理的推送请求
     */
    public void notify(PushRequest pushRequest) {
        try {
            requests.put(pushRequest);
        } catch (InterruptedException e) {
            Loggers.MAIN.warn("There are too many events, this event {} will be ignored.",
                pushRequest.getReason());
            // 恢复线程中断标志
            Thread.currentThread().interrupt();
        }
    }
    
    /** 启动守护线程消费推送事件。 */
    private void handleEvents() {
        Consumer handleEvents = new Consumer("handle events");
        handleEvents.setDaemon(true);
        handleEvents.start();
    }
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (contextRefreshedEvent.getApplicationContext().getParent() == null) {
            checkDependenceReady();
            handleEvents();
        }
    }
    
    private class Consumer extends Thread {
        
        Consumer(String name) {
            setName(name);
        }
        
        @Override
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            Future<Void> task = null;
            boolean hasNewEvent = false;
            PushRequest lastEvent = null;
            while (true) {
                try {
                    // 当前仅关注服务变更事件；上一异步任务未完成前合并等待
                    PushRequest pushRequest =
                        requests.poll(MAX_WAIT_EVENT_TIME, TimeUnit.MILLISECONDS);
                    if (pushRequest != null) {
                        hasNewEvent = true;
                        lastEvent = pushRequest;
                    }
                    if (hasClientConnection() && needNewTask(hasNewEvent, task)) {
                        task = IstioExecutor.asyncHandleEvent(new EventHandleTask(lastEvent));
                        hasNewEvent = false;
                        lastEvent = null;
                    }
                } catch (InterruptedException e) {
                    Loggers.MAIN.warn("Thread {} is be interrupted.", getName());
                    // set the interrupted flag
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /** 是否存在 MCP 或 XDS 客户端连接。 */
    private boolean hasClientConnection() {
        return nacosMcpService.hasClientConnection() || nacosXdsService.hasClientConnection();
    }
    
    /** 有新事件且上一任务已完成（或尚未提交）时需提交新任务。 */
    private boolean needNewTask(boolean hasNewEvent, Future<Void> task) {
        return hasNewEvent && (task == null || task.isDone());
    }
    
    private class EventHandleTask implements Callable<Void> {
        
        private final PushRequest pushRequest;
        
        EventHandleTask(PushRequest pushRequest) {
            this.pushRequest = pushRequest;
        }
        
        /** 创建资源快照并依次交给 XDS（全量/增量）与 MCP 处理。 */
        @Override
        public Void call() throws Exception {
            ResourceSnapshot snapshot = resourceManager.createResourceSnapshot();
            pushRequest.setResourceSnapshot(snapshot);
            nacosXdsService.handleEvent(pushRequest);
            nacosXdsService.handleDeltaEvent(pushRequest);
            nacosMcpService.handleEvent(pushRequest);
            return null;
        }
    }
    
    /** 懒加载并校验 ResourceManager、XdsService、McpService 依赖是否就绪。 */
    private boolean checkDependenceReady() {
        if (null == resourceManager) {
            resourceManager = ApplicationUtils.getBean(NacosResourceManager.class);
        }
        if (null == nacosXdsService) {
            nacosXdsService = ApplicationUtils.getBean(NacosXdsService.class);
        }
        if (null == nacosMcpService) {
            nacosMcpService = ApplicationUtils.getBean(NacosMcpService.class);
        }
        return Objects.nonNull(resourceManager) && Objects.nonNull(nacosMcpService)
            && Objects.nonNull(nacosXdsService);
    }
}
