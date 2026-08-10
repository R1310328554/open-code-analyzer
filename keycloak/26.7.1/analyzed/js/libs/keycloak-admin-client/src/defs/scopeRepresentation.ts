/**
 * 授权作用域（Scope）表示：对资源可执行的操作粒度（如 view、edit、delete）。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_scoperepresentation
 */
import type PolicyRepresentation from "./policyRepresentation.js";
import type ResourceRepresentation from "./resourceRepresentation.js";

export default interface ScopeRepresentation {
  /** 面向用户的显示名称 */
  displayName?: string;
  /** 作用域图标 URI */
  iconUri?: string;
  /** 作用域 UUID */
  id?: string;
  /** 作用域名称（唯一标识） */
  name?: string;
  /** 引用该作用域的授权策略列表 */
  policies?: PolicyRepresentation[];
  /** 关联的资源列表 */
  resources?: ResourceRepresentation[];
}
