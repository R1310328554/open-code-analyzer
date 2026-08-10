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

package com.alibaba.nacos.plugin.datasource.constants;

/**
 * Mapper 上下文参数字段名常量。
 *
 * <p>统一 {@link com.alibaba.nacos.plugin.datasource.model.MapperContext} 中 WHERE/UPDATE 参数的键名，避免 SQL 拼接层硬编码字符串。</p>
 *
 * @author vividfish
 **/

public class FieldConstant {
    
    /** 租户/命名空间 ID 参数键。 */
    public static final String TENANT_ID = "tenantId";
    
    /** 租户标识（模糊匹配场景）。 */
    public static final String TENANT = "tenant";
    
    /** 配置内容正文。 */
    public static final String CONTENT = "content";
    
    /** 配置分组 ID。 */
    public static final String GROUP_ID = "groupId";
    
    /** 配置 dataId。 */
    public static final String DATA_ID = "dataId";
    
    /** 应用名。 */
    public static final String APP_NAME = "app_name";
    
    /** 加密数据密钥字段。 */
    public static final String ENCRYPTED_DATA_KEY = "encrypted_data_key";
    
    /** 分页起始行（偏移量）。 */
    public static final String START_ROW = "startRow";
    
    /** 分页大小。 */
    public static final String PAGE_SIZE = "pageSize";
    
    /** 主键 ID。 */
    public static final String ID = "id";
    
    /** 历史记录 nid 字段。 */
    public static final String NID = "nid";
    
    /** 查询起始时间。 */
    public static final String START_TIME = "startTime";
    
    /** 查询结束时间。 */
    public static final String END_TIME = "endTime";
    
    /** 标签数组参数。 */
    public static final String TAG_ARR = "tagARR";
    
    /** 增量同步游标（上一批最大 ID）。 */
    public static final String LAST_MAX_ID = "lastMaxId";
    
    /** 数据项 datumId。 */
    public static final String DATUM_ID = "datumId";
    
    /** IN 条件标志。 */
    public static final String IS_IN = "isIn";
    
    /** 内容 MD5 校验值。 */
    public static final String MD5 = "md5";
    
    /** Beta 发布 IP 白名单。 */
    public static final String BETA_IPS = "betaIps";
    
    /** 最后修改时间。 */
    public static final String GMT_MODIFIED = "gmtModified";
    
    /** 变更来源用户。 */
    public static final String SRC_USER = "srcUser";
    
    /** 变更来源 IP。 */
    public static final String SRC_IP = "srcIp";
    
    /** 批量 ID 列表。 */
    public static final String IDS = "ids";
    
    /** 配置描述。 */
    public static final String C_DESC = "cDesc";
    
    /** 配置用途说明。 */
    public static final String C_USE = "cUse";
    
    /** 配置生效策略。 */
    public static final String EFFECT = "effect";
    
    /** 配置 Schema 元数据。 */
    public static final String C_SCHEMA = "cSchema";
    
    /** 配置/资源类型。 */
    public static final String TYPE = "type";
    
    /** 标签 ID。 */
    public static final String TAG_ID = "tagId";
    
    /** 发布类型。 */
    public static final String PUBLISH_TYPE = "publishType";
    
    /** 灰度规则名称。 */
    public static final String GRAY_NAME = "grayName";
    
    /** 灰度规则表达式。 */
    public static final String GRAY_RULE = "grayRule";
    
    /** 容量配额。 */
    public static final String QUOTA = "quota";
    
    /** 最大容量限制。 */
    public static final String MAX_SIZE = "maxSize";
    
    /** 最大聚合条数。 */
    public static final String MAX_AGGR_COUNT = "maxAggrCount";
    
    /** 最大聚合大小。 */
    public static final String MAX_AGGR_SIZE = "maxAggrSize";
    
    /** 创建时间。 */
    public static final String GMT_CREATE = "gmtCreate";
    
    /** 使用量统计。 */
    public static final String USAGE = "usage";
    
    /** 限制大小阈值。 */
    public static final String LIMIT_SIZE = "limitSize";
    
    /** 迁移目标 ID。 */
    public static final String TARGET_ID = "targetId";
    
    /** 迁移目标租户。 */
    public static final String TARGET_TENANT = "targetTenant";
    
    /** 迁移源租户。 */
    public static final String SRC_TENANT = "srcTenant";
    
    /** AI 资源命名空间 ID。 */
    public static final String NAMESPACE_ID = "namespaceId";
    
    /** 资源/配置名称。 */
    public static final String NAME = "name";
    
    /** 资源版本号。 */
    public static final String VERSION = "version";
    
    /** 资源状态。 */
    public static final String STATUS = "status";
    
    /** 业务标签（模糊匹配）。 */
    public static final String BIZ_TAGS = "bizTags";
    
    /** 元数据版本号。 */
    public static final String META_VERSION = "metaVersion";
    
    /** 排序字段键。 */
    public static final String ORDER_BY = "orderBy";
    
    /** 按下载次数降序排序。 */
    public static final String ORDER_BY_DOWNLOAD_COUNT = "download_count";
    
    /** AI 资源可见范围。 */
    public static final String SCOPE = "scope";
    
    /** 资源所有者。 */
    public static final String OWNER = "owner";
}
