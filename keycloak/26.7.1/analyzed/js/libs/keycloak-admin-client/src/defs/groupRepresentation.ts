/**
 * Realm 用户组表示：支持层级结构与角色/属性挂载。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_grouprepresentation
 */

export default interface GroupRepresentation {
  /** 组 UUID */
  id?: string;
  /** 组显示名称 */
  name?: string;
  /** 组描述文本 */
  description?: string;
  /** 层级路径（如 /parent/child） */
  path?: string;
  /** 父组 ID（根组无父节点） */
  parentId?: string;
  /** 直接子组数量（分页查询时可能仅返回计数） */
  subGroupCount?: number;
  /** 嵌套子组列表（完整树展开时使用） */
  subGroups?: GroupRepresentation[];

  // 以下字段通常仅在 GET 详情响应中出现
  /** 当前用户对组的管理权限映射 */
  access?: Record<string, boolean>;
  /** 组级自定义属性 */
  attributes?: Record<string, any>;
  /** 按客户端划分的组角色映射 */
  clientRoles?: Record<string, any>;
  /** 组拥有的 Realm 角色名称列表 */
  realmRoles?: string[];
}
