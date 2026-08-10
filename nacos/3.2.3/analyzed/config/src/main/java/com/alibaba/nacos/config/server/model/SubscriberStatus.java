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

package com.alibaba.nacos.config.server.model;

import java.io.Serializable;

/**
 * 配置订阅者状态：描述某客户端对指定 groupKey 的 MD5、最后推送时间与连接状态。
 * SubscriberStatus.
 *
 * @author Nacos
 */
public class SubscriberStatus implements Serializable {
    
    private static final long serialVersionUID = 1065466896062351086L;
    
    /** 配置唯一键（dataId+group+tenant 组合） */
    private String groupKey;
    
    /** 客户端当前持有的配置内容 MD5 */
    private String md5;
    
    /** 最后一次推送或确认的时间戳（毫秒） */
    private Long lastTime;
    
    /** 订阅是否仍有效（连接存活且未取消） */
    private Boolean status;
    
    /** 处理该订阅的 Nacos 服务端节点 IP */
    private String serverIp;
    
    /** 无参构造，供 JSON 反序列化使用。 */
    public SubscriberStatus() {
    }
    
    /**
     * 构造订阅者状态快照。
     *
     * @param groupKey 配置 groupKey
     * @param status   订阅是否有效
     * @param md5      当前 MD5
     * @param lastTime 最后活动时间
     */
    public SubscriberStatus(String groupKey, Boolean status, String md5, Long lastTime) {
        this.groupKey = groupKey;
        this.md5 = md5;
        this.lastTime = lastTime;
        this.status = status;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    public Long getLastTime() {
        return lastTime;
    }
    
    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }
    
    public Boolean getStatus() {
        return status;
    }
    
    public void setStatus(Boolean status) {
        this.status = status;
    }
    
    public String getGroupKey() {
        
        return groupKey;
    }
    
    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
    
    public String getServerIp() {
        return serverIp;
    }
    
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }
}
