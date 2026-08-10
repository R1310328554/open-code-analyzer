/**
 * 授权资源（Resource）表示：受保护的业务实体及其 URI、作用域与属性。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_resourcerepresentation
 */
import type { ResourceOwnerRepresentation } from "./resourceServerRepresentation.js";
import type ScopeRepresentation from "./scopeRepresentation.js";

export default interface ResourceRepresentation {
  /** 资源名称（唯一标识） */
  name?: string;
  /** 资源类型（用于策略分组与类型级权限） */
  type?: string;
  /** 资源所有者（用户或客户端） */
  owner?: ResourceOwnerRepresentation;
  /** 是否由资源所有者自行管理访问权限（UMA 场景） */
  ownerManagedAccess?: boolean;
  /** 面向用户的显示名称 */
  displayName?: string;
  /** 自定义属性（键到字符串数组的映射） */
  attributes?: { [index: string]: string[] };
  /** 资源内部 ID */
  _id?: string;
  /** 资源 URI 列表（用于 URI 匹配策略） */
  uris?: string[];
  /** 关联的作用域列表 */
  scopes?: ScopeRepresentation[];
  /** 资源图标 URI */
  icon_uri?: string;
}
