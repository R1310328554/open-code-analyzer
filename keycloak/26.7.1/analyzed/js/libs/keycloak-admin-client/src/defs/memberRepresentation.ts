import type UserRepresentation from "./userRepresentation.js";

/** 组织（Organization）成员视图：在用户信息基础上附加成员关系类型。 */
export default interface MemberRepresentation extends UserRepresentation {
  /** 成员关系类型（如 MANAGED、UNMANAGED，标识成员来源与管理方式） */
  membershipType?: string;
}
