import type ResourceRepresentation from "./resourceRepresentation.js";

/**
 * 授权资源评估请求：模拟用户对指定资源的访问权限判定。
 */
export default interface ResourceEvaluation {
  /** 参与评估的角色 ID 列表 */
  roleIds?: string[];
  /** 客户端 ID（OAuth 客户端上下文） */
  clientId?: string;
  /** 被评估用户 ID（必填） */
  userId: string;
  /** 待评估的资源列表 */
  resources?: ResourceRepresentation[];
  /** 资源类型过滤（按类型批量评估时使用） */
  resourceType?: string;
  /** 是否返回完整授权声明（entitlements）而非仅 PERMIT/DENY */
  entitlements: boolean;
  /** 评估上下文属性（自定义键值对，供策略脚本使用） */
  context: {
    attributes: {
      [key: string]: string;
    };
  };
}
