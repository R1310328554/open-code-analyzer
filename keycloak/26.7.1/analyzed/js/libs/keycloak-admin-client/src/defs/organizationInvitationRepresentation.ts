/** 组织邀请的生命周期状态。 */
export enum OrganizationInvitationStatus {
  /** 邀请已发出，等待被邀请人接受 */
  PENDING = "PENDING",
  /** 邀请已超过有效期 */
  EXPIRED = "EXPIRED",
}

/** 组织成员邀请记录：含被邀请人信息与邀请链接等 Admin API 字段。 */
export default interface OrganizationInvitationRepresentation {
  /** 邀请记录唯一 ID */
  id?: string;
  /** 被邀请人电子邮箱 */
  email?: string;
  /** 目标组织 ID */
  organizationId?: string;
  /** 被邀请人名字（可选，预填注册表单） */
  firstName?: string;
  /** 被邀请人姓氏（可选） */
  lastName?: string;
  /** 邀请发送时间（毫秒时间戳） */
  sentDate?: number;
  /** 邀请过期时间（毫秒时间戳） */
  expiresAt?: number;
  /** 当前邀请状态 */
  status?: OrganizationInvitationStatus;
  /** 供被邀请人点击加入组织的完整邀请 URL */
  inviteLink?: string;
}
