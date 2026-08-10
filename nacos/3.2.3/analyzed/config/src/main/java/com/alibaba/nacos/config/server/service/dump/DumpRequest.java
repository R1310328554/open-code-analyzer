
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

package com.alibaba.nacos.config.server.service.dump;

/**
 * Dump 请求 DTO：携带 dataId/group/tenant、灰度名、变更时间与来源 IP。
 * dump request.
 *
 * @author shiyiyue
 */
public class DumpRequest {
    
    /** 配置 dataId。 */
    String dataId;
    
    /** 配置 group。 */
    String group;
    
    /** 命名空间 tenant。 */
    String tenant;
    
    /** 灰度版本名，正式配置为空。 */
    String grayName;
    
    private long lastModifiedTs;
    
    private String sourceIp;
    
    public String getDataId() {
        return dataId;
    }
    
    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getTenant() {
        return tenant;
    }
    
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    public long getLastModifiedTs() {
        return lastModifiedTs;
    }
    
    public void setLastModifiedTs(long lastModifiedTs) {
        this.lastModifiedTs = lastModifiedTs;
    }
    
    public String getSourceIp() {
        return sourceIp;
    }
    
    public String getGrayName() {
        return grayName;
    }
    
    public void setGrayName(String grayName) {
        this.grayName = grayName;
    }
    
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
    
    /**
     * create dump request.
     *
     * @param dataId         dataId.
     * @param group          group.
     * @param tenant         tenant.
     * @param lastModifiedTs lastModifiedTs.
     * @param sourceIp       sourceIp.
     * @return
      * <p>Dump 请求模型；详见类级说明。</p>
     */
    public static DumpRequest create(String dataId, String group, String tenant,
        long lastModifiedTs, String sourceIp) {
        DumpRequest dumpRequest = new DumpRequest();
        dumpRequest.dataId = dataId;
        dumpRequest.group = group;
        dumpRequest.tenant = tenant;
        dumpRequest.lastModifiedTs = lastModifiedTs;
        dumpRequest.sourceIp = sourceIp;
        return dumpRequest;
    }
}
