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
 *
 */

package com.alibaba.nacos.istio.common;

import com.alibaba.nacos.istio.misc.IstioConfig;
import com.alibaba.nacos.istio.model.PushRequest;

import java.util.Date;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

/**
 * 推送请求防抖合并器：将短时间内的多次 {@link PushRequest} 合并为一次推送，降低 XDS/MCP 风暴。
 *
 * <p>在 {@link IstioConfig#getDebounceAfter()} 静默窗口或 {@link IstioConfig#getDebounceMax()} 超时后触发合并结果。</p>
 *
 * @author RocketEngine26
 * @date 2022/8/20 9:05
 */
public class Debounce implements Callable<PushRequest> {
    
    /** 当前防抖窗口起始时间。 */
    private Date startDebounce;
    
    /** 最后一次收到配置变更事件的时间。 */
    private Date lastConfigUpdateTime;
    
    private final IstioConfig istioConfig;
    
    /** 待合并的推送请求队列。 */
    private final Queue<PushRequest> pushRequestQueue;
    
    /** 当前防抖周期内累积的合并请求。 */
    private PushRequest pushRequest;
    
    /** 本周期内已合并的事件计数。 */
    private int debouncedEvents = 0;
    
    /** 是否允许调度下一次 pushWorker（未完成推送时为 false）。 */
    private boolean free = true;
    
    /** 合并完成标志，为 true 时 {@link #call()} 返回结果。 */
    private boolean flag = false;
    
    public Debounce(Queue<PushRequest> pushRequestQueue, IstioConfig istioConfig) {
        this.pushRequestQueue = pushRequestQueue;
        this.istioConfig = istioConfig;
    }
    
    @Override
    public PushRequest call() throws Exception {
        while (true) {
            if (flag) {
                return pushRequest;
            }
            
            PushRequest otherRequest = pushRequestQueue.poll();
            
            if (otherRequest != null) {
                lastConfigUpdateTime = new Date();
                if (debouncedEvents == 0) {
                    startDebounce = lastConfigUpdateTime;
                    pushRequest = otherRequest;
                    new Timer().schedule(new TimerTask() {
                        
                        @Override
                        public void run() {
                            if (free) {
                                try {
                                    pushWorker();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }, istioConfig.getDebounceAfter());
                } else {
                    merge(otherRequest);
                }
                debouncedEvents++;
            }
        }
    }
    
    /** 检查防抖窗口是否满足，满足则结束合并，否则延迟重试。 */
    private void pushWorker() {
        long eventDelay = System.currentTimeMillis() - startDebounce.getTime();
        long quietTime = System.currentTimeMillis() - lastConfigUpdateTime.getTime();
        
        if (eventDelay > istioConfig.getDebounceMax()
            || quietTime > istioConfig.getDebounceAfter()) {
            if (pushRequest != null) {
                free = false;
                flag = true;
                debouncedEvents = 0;
            }
        } else {
            new Timer().schedule(new TimerTask() {
                
                @Override
                public void run() {
                    if (free) {
                        try {
                            pushWorker();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }, istioConfig.getDebounceAfter() - quietTime);
        }
    }
    
    /** 将后续请求的原因集合与 full 标志合并到当前 pushRequest。 */
    private void merge(PushRequest otherRequest) {
        pushRequest.getReason().addAll(otherRequest.getReason());
        pushRequest.setFull(pushRequest.isFull() || otherRequest.isFull());
    }
}
