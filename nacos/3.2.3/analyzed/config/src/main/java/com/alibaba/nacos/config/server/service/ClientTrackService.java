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

import com.alibaba.nacos.config.server.model.SubscriberStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 客户端 MD5 跟踪服务：记录各 IP 订阅的 groupKey 与 MD5，供控制台查询订阅状态与是否最新。
 * 与 {@link ConfigCacheService#isUptodate} 配合判断客户端配置是否落后。
 * ClientTrackService which tracks client's md5 service and delete expired ip's records.
 *
 * @author Nacos
 */
public class ClientTrackService {
    
    /**
     * 记录客户端对某 groupKey 上报的 MD5，并刷新活跃时间与轮询时间戳。
     *
     * Put the specified value(ip/groupKey/clientMd5) into clientRecords Map.
     *
     * @param ip        ip string value.
     * @param groupKey  groupKey string value.
     * @param clientMd5 clientMd5 string value.
     */
    public static void trackClientMd5(String ip, String groupKey, String clientMd5) {
        ClientRecord record = getClientRecord(ip);
        record.setLastTime(System.currentTimeMillis());
        record.getGroupKey2md5Map().put(groupKey, clientMd5);
        record.getGroupKey2pollingTsMap().put(groupKey, record.getLastTime());
    }
    
    /**
     * Get subscribe client count.
     *
     * @return subscribe client count.
      * <p>客户端 MD5 跟踪；详见类级说明。</p>
     */
    public static int subscribeClientCount() {
        return clientRecords.size();
    }
    
    /**
     * Get all of subscriber count.
     *
     * @return all of subscriber count.
      * <p>客户端 MD5 跟踪；详见类级说明。</p>
     */
    public static long subscriberCount() {
        long count = 0;
        for (ClientRecord record : clientRecords.values()) {
            count += record.getGroupKey2md5Map().size();
        }
        return count;
    }
    
    /**
     * 查询指定 IP 下各 groupKey 的订阅状态（是否最新、MD5、最后轮询时间）。
     *
     * Groupkey ->  SubscriberStatus.
     */
    public static Map<String, SubscriberStatus> listSubStatus(String ip) {
        Map<String, SubscriberStatus> status = new HashMap<>(100);
        
        // getClientRecord 保证返回非空记录
        ClientRecord record = getClientRecord(ip);
        for (Map.Entry<String, String> entry : record.getGroupKey2md5Map().entrySet()) {
            String groupKey = entry.getKey();
            String clientMd5 = entry.getValue();
            long lastPollingTs = record.getGroupKey2pollingTsMap().get(groupKey);
            boolean isUpdate = ConfigCacheService.isUptodate(groupKey, clientMd5);
            
            status.put(groupKey,
                new SubscriberStatus(groupKey, isUpdate, clientMd5, lastPollingTs));
        }
        
        return status;
    }
    
    /**
     * 查询指定订阅者 IP 下各 groupKey 配置是否与服务器缓存一致。
     * groupKey -> isUptodate.
     */
    public static Map<String, Boolean> isClientUptodate(String ip) {
        Map<String, Boolean> result = new HashMap<>(100);
        for (Map.Entry<String, String> entry : getClientRecord(ip).getGroupKey2md5Map()
            .entrySet()) {
            String groupKey = entry.getKey();
            String clientMd5 = entry.getValue();
            Boolean isuptodate = ConfigCacheService.isUptodate(groupKey, clientMd5);
            result.put(groupKey, isuptodate);
        }
        return result;
    }
    
    /**
     * Get and return the record of specified client ip.
     *
     * @param clientIp clientIp string value.
     * @return the record of specified client ip.
      * <p>客户端 MD5 跟踪；详见类级说明。</p>
     */
    private static ClientRecord getClientRecord(String clientIp) {
        ClientRecord record = clientRecords.get(clientIp);
        if (null != record) {
            return record;
        }
        ClientRecord clientRecord = new ClientRecord(clientIp);
        record = clientRecords.putIfAbsent(clientIp, clientRecord);
        return null == record ? clientRecord : record;
    }
    
    public static void refreshClientRecord() {
        clientRecords = new ConcurrentHashMap<>(50);
    }
    
    /**
     * 全局客户端记录表（IP → {@link ClientRecord}），支持并发读写与整表刷新。
     */
    static volatile ConcurrentMap<String, ClientRecord> clientRecords = new ConcurrentHashMap<>();
}
