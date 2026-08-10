/**
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_rolerepresentation
 */

export default interface RoleRepresentation {
  /** 角色 UUID */
  id?: string;
  /** 角色名称（Realm 内唯一） */
  name?: string;
  /** 角色描述 */
  description?: string;
  /** 客户端角色是否要求 scope 参数 */
  scopeParamRequired?: boolean;
  /** 是否为组合角色（包含其他角色） */
  composite?: boolean;
  /** 组合角色引用的子角色集合 */
  composites?: Composites;
  /** 是否为客户端角色（false 表示 Realm 角色） */
  clientRole?: boolean;
  /** 所属容器 ID（Realm ID 或 Client ID） */
  containerId?: string;
  /** 角色自定义属性 */
  attributes?: { [index: string]: string[] };
}

/** 组合角色包含的子角色，按 Realm/Client/Application 分组 */
export interface Composites {
  /** 引用的 Realm 角色名称列表 */
  realm?: string[];
  /** 按客户端 ID 分组的客户端角色名称 */
  client?: { [index: string]: string[] };
  /** 按应用 ID 分组的应用角色名称（遗留字段） */
  application?: { [index: string]: string[] };
}

// 调用角色映射 API（创建/删除）时 id 与 name 为必填
/** 角色映射 API 请求体：id 与 name 必填 */
export interface RoleMappingPayload extends RoleRepresentation {
  id: string;
  name: string;
}
