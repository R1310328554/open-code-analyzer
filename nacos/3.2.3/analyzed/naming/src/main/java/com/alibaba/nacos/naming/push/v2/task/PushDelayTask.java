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

package com.alibaba.nacos.naming.push.v2.task;

import com.alibaba.nacos.common.task.AbstractDelayTask;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.Loggers;

import java.util.HashSet;
import java.util.Set;

/**
 * 命名服务推送延迟任务。
 *
 * <p>关联 {@link Service}，可推送给全部订阅者或指定 clientId 集合；同服务多次入队时 merge 合并目标（任一为全量则变为 pushToAll）。</p>
 *
 * @author xiweng.yy
 */
public class PushDelayTask extends AbstractDelayTask {
    
    private final Service service;
    
    private boolean pushToAll;
    
    private Set<String> targetClients;
    
    /** 构造推送给该服务全部订阅者的延迟任务。 */
    public PushDelayTask(Service service, long delay) {
        this.service = service;
        pushToAll = true;
        targetClients = null;
        setTaskInterval(delay);
        setLastProcessTime(System.currentTimeMillis());
    }
    
    /** 构造仅推送给指定客户端的延迟任务。 */
    public PushDelayTask(Service service, long delay, String targetClient) {
        this.service = service;
        this.pushToAll = false;
        this.targetClients = new HashSet<>(1);
        this.targetClients.add(targetClient);
        setTaskInterval(delay);
        setLastProcessTime(System.currentTimeMillis());
    }
    
    /** 合并同服务推送任务：全量优先，否则合并目标客户端集合。 */
    @Override
    public void merge(AbstractDelayTask task) {
        if (!(task instanceof PushDelayTask)) {
            return;
        }
        PushDelayTask oldTask = (PushDelayTask) task;
        if (isPushToAll() || oldTask.isPushToAll()) {
            pushToAll = true;
            targetClients = null;
        } else {
            targetClients.addAll(oldTask.getTargetClients());
        }
        setLastProcessTime(Math.min(getLastProcessTime(), task.getLastProcessTime()));
        Loggers.PUSH.info("[PUSH] Task merge for {}", service);
    }
    
    public Service getService() {
        return service;
    }
    
    public boolean isPushToAll() {
        return pushToAll;
    }
    
    public Set<String> getTargetClients() {
        return targetClients;
    }
}
