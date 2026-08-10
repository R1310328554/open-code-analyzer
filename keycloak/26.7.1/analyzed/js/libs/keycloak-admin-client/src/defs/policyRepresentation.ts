/**
 * 细粒度授权策略（Authorization Policy）的完整表示，含决策策略与关联资源/角色。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_policyrepresentation
 */

/** 多条子策略组合时的聚合决策策略。 */
export enum DecisionStrategy {
  /** 任一关联策略 PERMIT 即整体 PERMIT */
  AFFIRMATIVE = "AFFIRMATIVE",
  /** 全部关联策略 PERMIT 才整体 PERMIT */
  UNANIMOUS = "UNANIMOUS",
  /** 多数关联策略 PERMIT 则整体 PERMIT */
  CONSENSUS = "CONSENSUS",
}

/** 单条策略或评估节点的最终决策效果。 */
export enum DecisionEffect {
  /** 允许访问 */
  Permit = "PERMIT",
  /** 拒绝访问 */
  Deny = "DENY",
}

/** 策略条件逻辑：正向匹配或取反（负向）匹配。 */
export enum Logic {
  /** 条件满足时策略生效 */
  POSITIVE = "POSITIVE",
  /** 条件不满足时策略生效（逻辑取反） */
  NEGATIVE = "NEGATIVE",
}

/** 策略中引用的 Realm/Client 角色及其是否必须持有。 */
export interface PolicyRoleRepresentation {
  /** 角色 ID */
  id: string;
  /** 是否要求用户必须拥有该角色（否则仅作为可选条件） */
  required?: boolean;
}

export default interface PolicyRepresentation {
  /** Provider 特定的键值配置（如 claim、group 路径、JS 脚本等） */
  config?: Record<string, any>;
  /** 嵌套/聚合策略的决策组合方式 */
  decisionStrategy?: DecisionStrategy;
  /** 策略描述 */
  description?: string;
  /** 策略 UUID */
  id?: string;
  /** 条件匹配逻辑（正向/负向） */
  logic?: Logic;
  /** 策略名称 */
  name?: string;
  /** 策略所属 Resource Server 或创建者标识 */
  owner?: string;
  /** 引用的子策略 ID 列表（聚合策略） */
  policies?: string[];
  /** 策略保护的资源 ID 列表 */
  resources?: string[];
  /** 策略适用的 Scope ID 列表 */
  scopes?: string[];
  /** Policy Provider 类型（如 role、time、js） */
  type?: string;
  /** 策略绑定的用户 ID 列表（用户策略） */
  users?: string[];
  /** 策略绑定的角色及必选标记 */
  roles?: PolicyRoleRepresentation[];
  /** 按资源类型批量匹配时的类型名称 */
  resourceType?: string;
}
