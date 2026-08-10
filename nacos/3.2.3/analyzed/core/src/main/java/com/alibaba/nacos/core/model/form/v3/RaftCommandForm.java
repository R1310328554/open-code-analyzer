/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.model.form.v3;

import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.distributed.raft.utils.JRaftConstants;
import com.alibaba.nacos.api.model.NacosForm;

import java.util.HashMap;
import java.util.Map;

/**
 * Raft 运维命令 HTTP 表单（v3 API），支持 transferLeader、doSnapshot 等管理操作。
 * <p>校验 command 与 value 必填，可转换为 JRaft 执行参数 Map。</p>
 * Raft command form.
 *
 * @author yunye
 * @since 3.0.0-beta
 */
public class RaftCommandForm implements NacosForm {
    
    /**
     * 目标 Raft Group ID；为空则对所有 Group 执行命令。
     */
    private String groupId;
    
    /**
     * Raft 命令名。有效值：transferLeader、doSnapshot、resetRaftCluster、removePeer。
     */
    private String command;
    
    /**
     * 命令参数值，格式：{ip}:{port} 或逗号分隔的多节点列表。
     */
    private String value;
    
    /** 校验 command 与 value 非空。 */
    @Override
    public void validate() throws NacosApiException {
        if (StringUtils.isBlank(command)) {
            throw new NacosApiException(NacosApiException.INVALID_PARAM,
                ErrorCode.PARAMETER_MISSING,
                "Raft command is required.");
        }
        if (StringUtils.isBlank(value)) {
            throw new NacosApiException(NacosApiException.INVALID_PARAM,
                ErrorCode.PARAMETER_MISSING,
                "Raft command value is required.");
        }
    }
    
    /** 获取目标 Raft Group ID。 */
    public String getGroupId() {
        return groupId;
    }
    
    /** 设置目标 Raft Group ID。 */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    /** 获取 Raft 命令名。 */
    public String getCommand() {
        return command;
    }
    
    /** 设置 Raft 命令名。 */
    public void setCommand(String command) {
        this.command = command;
    }
    
    /** 获取命令参数值。 */
    public String getValue() {
        return value;
    }
    
    /** 设置命令参数值。 */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * 转换为 JRaft 命令执行所需的参数 Map（含 GROUP_ID、COMMAND_NAME、COMMAND_VALUE）。
     *
     * @return args map.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>(4);
        if (StringUtils.isNotBlank(groupId)) {
            map.put(JRaftConstants.GROUP_ID, groupId);
        }
        map.put(JRaftConstants.COMMAND_NAME, command);
        map.put(JRaftConstants.COMMAND_VALUE, value);
        return map;
    }
}
