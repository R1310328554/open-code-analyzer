import UserRepresentation from "./userRepresentation.js";

/** 组织成员详情：继承用户表示并强制携带成员关系类型。 */
export default interface OrganizationMemberRepresentation
  extends UserRepresentation {
  /** 成员关系类型（MANAGED 表示由组织 IdP 管理，UNMANAGED 为 Realm 本地用户等） */
  membershipType: string;
}
