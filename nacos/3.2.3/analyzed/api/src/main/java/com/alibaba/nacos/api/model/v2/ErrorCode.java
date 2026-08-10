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
 */

package com.alibaba.nacos.api.model.v2;

/**
 * Nacos Open API v2 统一错误码枚举。
 *
 * <p>每个常量对应 HTTP 响应中的业务错误码与默认英文消息，供 {@link com.alibaba.nacos.api.exception.api.NacosApiException} 使用。</p>
 *
 * @author dongyafei
 * @date 2022/7/22
 */

public enum ErrorCode {
    
    /** 操作成功。 */
    SUCCESS(0, "success"),
    
    /** 缺少必填参数。 */
    PARAMETER_MISSING(10000, "parameter missing"),
    
    /** 访问被拒绝（鉴权失败）。 */
    ACCESS_DENIED(10001, "access denied"),
    
    /** 数据访问层错误。 */
    DATA_ACCESS_ERROR(10002, "data access error"),
    
    /** tenant 参数错误。 */
    TENANT_PARAM_ERROR(20001, "'tenant' parameter error"),
    
    /** 参数校验失败。 */
    PARAMETER_VALIDATE_ERROR(20002, "parameter validate error"),
    
    /** 请求 MediaType 不支持。 */
    MEDIA_TYPE_ERROR(20003, "MediaType Error"),
    
    /** 资源不存在。 */
    RESOURCE_NOT_FOUND(20004, "resource not found"),
    
    /** 资源冲突（如重复创建）。 */
    RESOURCE_CONFLICT(20005, "resource conflict"),
    
    /** 配置监听器为空。 */
    CONFIG_LISTENER_IS_NULL(20006, "config listener is null"),
    
    /** 配置监听器执行异常。 */
    CONFIG_LISTENER_ERROR(20007, "config listener error"),
    
    /** dataId 格式非法。 */
    INVALID_DATA_ID(20008, "invalid dataId"),
    
    /** 参数不匹配。 */
    PARAMETER_MISMATCH(20009, "parameter mismatch"),
    
    /** 灰度版本数超过上限。 */
    CONFIG_GRAY_OVER_MAX_VERSION_COUNT(20010, "config gray version version over max count"),
    
    /** 灰度规则格式非法。 */
    CONFIG_GRAY_RULE_FORMAT_INVALID(20011, "config gray rule format invalid"),
    
    /** 灰度规则版本非法。 */
    CONFIG_GRAY_VERSION_INVALID(20012, "config gray rule version invalid"),
    
    /** 灰度名称无法识别。 */
    CONFIG_GRAY_NAME_UNRECOGNIZED_ERROR(20013, "config gray name not recognized"),
    
    /** 集群配置容量达到配额上限。 */
    OVER_CLUSTER_QUOTA(5031, "cluster capacity reach quota"),
    
    /** 分组配置容量达到配额上限。 */
    OVER_GROUP_QUOTA(5032, "group capacity reach quota"),
    
    /** 租户配置容量达到配额上限。 */
    OVER_TENANT_QUOTA(5033, "tenant capacity reach quota"),
    
    /** 配置内容大小超过限制。 */
    OVER_MAX_SIZE(5034, "config content size is over limit"),
    
    /** 服务名格式错误。 */
    SERVICE_NAME_ERROR(21000, "service name error"),
    
    /** 实例权重参数错误。 */
    WEIGHT_ERROR(21001, "weight error"),
    
    /** 实例元数据错误。 */
    INSTANCE_METADATA_ERROR(21002, "instance metadata error"),
    
    /** 实例不存在。 */
    INSTANCE_NOT_FOUND(21003, "instance not found"),
    
    /** 实例操作通用错误。 */
    INSTANCE_ERROR(21004, "instance error"),
    
    /** 服务元数据错误。 */
    SERVICE_METADATA_ERROR(21005, "service metadata error"),
    
    /** 服务选择器配置错误。 */
    SELECTOR_ERROR(21006, "selector error"),
    
    /** 服务已存在。 */
    SERVICE_ALREADY_EXIST(21007, "service already exist"),
    
    /** 服务不存在。 */
    SERVICE_NOT_EXIST(21008, "service not exist"),
    
    /** 服务删除失败。 */
    SERVICE_DELETE_FAILURE(21009, "service delete failure"),
    
    /** 健康检查参数缺失。 */
    HEALTHY_PARAM_MISS(21010, "healthy param miss"),
    
    /** 健康检查仍在运行中。 */
    HEALTH_CHECK_STILL_RUNNING(21011, "health check still running"),
    
    /** 命名空间非法。 */
    ILLEGAL_NAMESPACE(22000, "illegal namespace"),
    
    /** 命名空间不存在。 */
    NAMESPACE_NOT_EXIST(22001, "namespace not exist"),
    
    /** 命名空间已存在。 */
    NAMESPACE_ALREADY_EXIST(22002, "namespace already exist"),
    
    /** 非法状态。 */
    ILLEGAL_STATE(23000, "illegal state"),
    
    /** 节点信息错误。 */
    NODE_INFO_ERROR(23001, "node info error"),
    
    /** 节点下线失败。 */
    NODE_DOWN_FAILURE(23002, "node down failure"),
    
    /** 服务端内部错误。 */
    SERVER_ERROR(30000, "server error"),
    
    /** API 已废弃。 */
    API_DEPRECATED(40000, "API deprecated."),
    
    /** API 功能模式已禁用。 */
    API_FUNCTION_DISABLED(40001, "API function disabled."),
    
    /** MCP 服务器不存在。 */
    MCP_SERVER_NOT_FOUND(50000, "MCP server not found"),
    
    /** MCP 服务器指定版本不存在。 */
    MCP_SEVER_VERSION_NOT_FOUND(50001, "MCP server version not found"),
    
    /** MCP 服务器版本已存在。 */
    MCP_SERVER_VERSION_EXIST(50002, "MCP server version has existed"),
    
    /** MCP 服务器引用的端点服务不存在。 */
    MCP_SERVER_REF_ENDPOINT_SERVICE_NOT_FOUND(50003, "MCP server ref endpoint service not found"),
    
    /** Agent 不存在。 */
    AGENT_NOT_FOUND(50100, "Agent not found"),
    
    /** Agent 指定版本不存在。 */
    AGENT_VERSION_NOT_FOUND(50101, "Agent version not found"),
    
    /** Agent 版本已存在。 */
    AGENT_VERSION_EXIST(50102, "Agent version already existed"),
    
    /** 导入元数据非法（配置模块使用 100001～100999）。 */
    METADATA_ILLEGAL(100002, "Imported metadata is invalid"),
    
    /** 数据校验失败，未读取到有效数据。 */
    DATA_VALIDATION_FAILED(100003, "No valid data was read"),
    
    /** 数据解析失败。 */
    PARSING_DATA_FAILED(100004, "Failed to parse data"),
    
    /** 导入文件数据为空。 */
    DATA_EMPTY(100005, "Imported file data is empty"),
    
    /** 未选择任何配置项。 */
    NO_SELECTED_CONFIG(100006, "No configuration selected"),
    
    /** 模糊监听模式数量超过上限。 */
    FUZZY_WATCH_PATTERN_OVER_LIMIT(50310, "fuzzy watch pattern over limit"),
    
    /** 模糊监听模式匹配项数超过上限。 */
    FUZZY_WATCH_PATTERN_MATCH_COUNT_OVER_LIMIT(50311,
        "fuzzy watch pattern matched count over limit");
    
    /** 数值错误码。 */
    private final Integer code;
    
    /** 默认英文错误消息。 */
    private final String msg;
    
    /** 获取数值错误码。 */
    public Integer getCode() {
        return code;
    }
    
    /** 获取默认错误消息。 */
    public String getMsg() {
        return msg;
    }
    
    /**
     * 按枚举名称查找错误码。
     *
     * @param name 枚举常量名
     * @return 匹配的 {@link ErrorCode}，未找到返回 {@code null}
     */
    public static ErrorCode getErrorCode(String name) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.name().equals(name)) {
                return errorCode;
            }
        }
        return null;
    }
    
    /** 构造错误码枚举常量。 */
    ErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
