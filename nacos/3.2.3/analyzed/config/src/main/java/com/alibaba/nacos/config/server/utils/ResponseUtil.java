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

package com.alibaba.nacos.config.server.utils;

import com.alibaba.nacos.api.config.model.ConfigBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigDetailInfo;
import com.alibaba.nacos.api.config.model.ConfigGrayInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryBasicInfo;
import com.alibaba.nacos.api.config.model.ConfigHistoryDetailInfo;
import com.alibaba.nacos.config.server.model.ConfigAllInfo;
import com.alibaba.nacos.config.server.model.ConfigHistoryInfo;
import com.alibaba.nacos.config.server.model.ConfigInfo;
import com.alibaba.nacos.config.server.model.ConfigInfoGrayWrapper;
import com.alibaba.nacos.config.server.model.ConfigInfoWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;

import java.io.IOException;

import static com.alibaba.nacos.config.server.utils.LogUtil.DEFAULT_LOG;

/**
 * HTTP 响应与 API 模型转换工具：写错误响应、将内部 Config 实体映射为 Open API DTO。
 * Response Utils.
 *
 * @author Nacos
 */
public class ResponseUtil {
    
    /**
     * 设置 HTTP 状态码并将错误消息写入响应体。
     * Write error msg.
     */
    public static void writeErrMsg(HttpServletResponse response, int httpCode, String msg) {
        response.setStatus(httpCode);
        try {
            response.getWriter().println(msg);
        } catch (IOException e) {
            DEFAULT_LOG.error("ResponseUtil:writeErrMsg wrong", e);
        }
    }
    
    /**
     * {@link ConfigAllInfo} → {@link ConfigDetailInfo}，tenant/group 映射为 namespaceId/groupName。
     * Transfer from {@link ConfigAllInfo} to {@link ConfigDetailInfo} for APIs.
     *
     * @param configAllInfo {@link ConfigAllInfo} config all information from storage.
     * @return {@link ConfigDetailInfo} for APIs response.
     */
    public static ConfigDetailInfo transferToConfigDetailInfo(ConfigAllInfo configAllInfo) {
        ConfigDetailInfo result = new ConfigDetailInfo();
        BeanUtils.copyProperties(configAllInfo, result);
        result.setNamespaceId(configAllInfo.getTenant());
        result.setGroupName(configAllInfo.getGroup());
        return result;
    }
    
    /**
     * {@link ConfigInfo} → {@link ConfigBasicInfo}。
     * Transfer from {@link ConfigInfo} to {@link ConfigBasicInfo} for APIs.
     *
     * @param configInfo {@link ConfigInfo} config basic information from storage.
     * @return {@link ConfigBasicInfo} for APIs response.
     */
    public static ConfigBasicInfo transferToConfigBasicInfo(ConfigInfo configInfo) {
        ConfigBasicInfo result = new ConfigBasicInfo();
        BeanUtils.copyProperties(configInfo, result);
        result.setNamespaceId(configInfo.getTenant());
        result.setGroupName(configInfo.getGroup());
        return result;
    }
    
    /**
     * {@link ConfigInfoWrapper} → {@link ConfigBasicInfo}，modifyTime 取 lastModified。
     * Transfer from {@link ConfigInfoWrapper} to {@link ConfigBasicInfo} for APIs.
     *
     * @param configInfo {@link ConfigInfoWrapper} config basic information from storage.
     * @return {@link ConfigBasicInfo} for APIs response.
     */
    public static ConfigBasicInfo transferToConfigBasicInfo(ConfigInfoWrapper configInfo) {
        ConfigBasicInfo result = transferToConfigBasicInfo((ConfigInfo) configInfo);
        result.setModifyTime(configInfo.getLastModified());
        return result;
    }
    
    /**
     * {@link ConfigInfoGrayWrapper} → {@link ConfigGrayInfo}，含灰度创建用户与修改时间。
     * Transfer from {@link ConfigInfoGrayWrapper} to {@link ConfigGrayInfo} for APIs.
     *
     * @param configInfoGray {@link ConfigInfoGrayWrapper} config gray information from storage.
     * @return {@link ConfigGrayInfo} for APIs response.
     */
    public static ConfigGrayInfo transferToConfigGrayInfo(ConfigInfoGrayWrapper configInfoGray) {
        ConfigGrayInfo result = new ConfigGrayInfo();
        BeanUtils.copyProperties(configInfoGray, result);
        result.setNamespaceId(configInfoGray.getTenant());
        result.setGroupName(configInfoGray.getGroup());
        result.setCreateUser(configInfoGray.getSrcUser());
        result.setModifyTime(configInfoGray.getLastModified());
        return result;
    }
    
    /**
     * {@link ConfigHistoryInfo} → {@link ConfigHistoryBasicInfo}。
     * Transfer from {@link ConfigHistoryInfo} to {@link ConfigHistoryBasicInfo} for APIs.
     *
     * @param historyInfo {@link ConfigHistoryInfo} config history information from storage.
     * @return {@link ConfigHistoryBasicInfo} for APIs response.
     */
    public static ConfigHistoryBasicInfo transferToConfigHistoryBasicInfo(
        ConfigHistoryInfo historyInfo) {
        ConfigHistoryBasicInfo result = new ConfigHistoryBasicInfo();
        BeanUtils.copyProperties(historyInfo, result);
        injectHistoryBasicInfo(result, historyInfo);
        return result;
    }
    
    /**
     * {@link ConfigHistoryInfo} → {@link ConfigHistoryDetailInfo}（含完整内容）。
     * Transfer from {@link ConfigHistoryInfo} to {@link ConfigHistoryDetailInfo} for APIs.
     *
     * @param historyInfo {@link ConfigHistoryInfo} config history information from storage.
     * @return {@link ConfigHistoryDetailInfo} for APIs response.
     */
    public static ConfigHistoryDetailInfo transferToConfigHistoryDetailInfo(
        ConfigHistoryInfo historyInfo) {
        ConfigHistoryDetailInfo result = new ConfigHistoryDetailInfo();
        BeanUtils.copyProperties(historyInfo, result);
        injectHistoryBasicInfo(result, historyInfo);
        return result;
    }
    
    /** 注入历史记录公共字段：namespaceId、groupName、createTime、modifyTime */
    private static void injectHistoryBasicInfo(ConfigHistoryBasicInfo historyBasicInfo,
        ConfigHistoryInfo historyInfo) {
        historyBasicInfo.setNamespaceId(historyInfo.getTenant());
        historyBasicInfo.setGroupName(historyInfo.getGroup());
        historyBasicInfo.setCreateTime(historyInfo.getCreatedTime().getTime());
        historyBasicInfo.setModifyTime(historyInfo.getLastModifiedTime().getTime());
    }
}
