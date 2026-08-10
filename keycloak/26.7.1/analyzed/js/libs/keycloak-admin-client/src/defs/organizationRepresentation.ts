import type OrganizationDomainRepresentation from "./organizationDomainRepresentation.js";
import type IdentityProviderRepresentation from "./identityProviderRepresentation.js";
import type MemberRepresentation from "./memberRepresentation.js";

/** Organization 功能中的组织实体：聚合域名、成员、关联 IdP 与扩展属性。 */
export default interface OrganizationRepresentation {
  /** 组织内部 UUID */
  id?: string;
  /** 组织显示名称 */
  name?: string;
  /** 组织别名（URL 友好标识，用于路由与 API 路径） */
  alias?: string;
  /** 组织描述 */
  description?: string;
  /** 组织登录/注册完成后的默认重定向 URL */
  redirectUrl?: string;
  /** 组织是否启用 */
  enabled?: boolean;
  /** 自定义扩展属性（键 → 字符串数组） */
  attributes?: Record<string, string[]>;
  /** 绑定的已验证/待验证域名列表 */
  domains?: OrganizationDomainRepresentation[];
  /** 组织成员摘要列表（列表 API 可能仅返回部分字段） */
  members?: MemberRepresentation[];
  /** 与该组织关联的身份提供者配置 */
  identityProviders?: IdentityProviderRepresentation[];
}
