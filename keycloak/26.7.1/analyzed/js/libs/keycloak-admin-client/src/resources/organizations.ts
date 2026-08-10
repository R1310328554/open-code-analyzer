import type { KeycloakAdminClient } from "../client.js";
import IdentityProviderRepresentation from "../defs/identityProviderRepresentation.js";
import type OrganizationRepresentation from "../defs/organizationRepresentation.js";
import type OrganizationInvitationRepresentation from "../defs/organizationInvitationRepresentation.js";
import UserRepresentation from "../defs/userRepresentation.js";
import Resource from "./resource.js";
import { Groups } from "./groups.js";
import OrganizationMemberRepresentation from "../defs/organizationMemberRepresentation.js";

interface PaginatedQuery {
  /** 分页起始偏移 */ first?: number; // The position of the first result to be processed (pagination offset)
  /** 返回条数上限 */ max?: number; // The maximum number of results to be returned - defaults to 10
  search?: string;
}
/** 组织列表查询参数 */
export interface OrganizationQuery extends PaginatedQuery {
  /** 自定义属性查询 */ q?: string; // A query to search for custom attributes, in the format 'key1:value2 key2:value2'
  /** search 是否精确匹配 */ exact?: boolean; // Boolean which defines whether the param 'search' must match exactly or not
}

interface MemberQuery extends PaginatedQuery {
  /** 组织 ID */ orgId: string; //Id of the organization to get the members of
  membershipType?: string;
}

interface InvitationQuery extends PaginatedQuery {
  /** 组织 ID */ orgId: string; //Id of the organization to get the invitations of
  /** 按邀请状态过滤 */ status?: string; //Filter by invitation status
  /** 按邮箱过滤 */ email?: string; //Filter by email
  /** 在邮箱/姓名中搜索 */ search?: string; //Search across email, firstName, and lastName
  /** 按名过滤 */ firstName?: string; //Filter by first name
  /** 按姓过滤 */ lastName?: string; //Filter by last name
}

/** 组织 Admin 资源：多租户组织 CRUD、成员、IdP 关联、邀请与组织内组。 */
export class Organizations extends Resource<{ realm?: string }> {
  /**
   * 组织
   * Organizations
   */
  #client: KeycloakAdminClient;

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}/organizations",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
    this.#client = client;
  }

  /** 查询列表 */
  public find = this.makeRequest<
    OrganizationQuery,
    OrganizationRepresentation[]
  >({
    method: "GET",
    path: "/",
  });

  /** 按 ID 获取单个 */
  public findOne = this.makeRequest<{ id: string }, OrganizationRepresentation>(
    {
      method: "GET",
      path: "/{id}",
      urlParamKeys: ["id"],
    },
  );

  /** 创建 */
  public create = this.makeRequest<OrganizationRepresentation, { id: string }>({
    method: "POST",
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /** 按 ID 删除 */
  public delById = this.makeRequest<{ id: string }, void>({
    method: "DELETE",
    path: "/{id}",
    urlParamKeys: ["id"],
  });

  /** 按 ID 更新 */
  public updateById = this.makeUpdateRequest<
    { id: string },
    OrganizationRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}",
    urlParamKeys: ["id"],
  });

  /** 列出成员 */
  public listMembers = this.makeRequest<MemberQuery, UserRepresentation[]>({
    method: "GET",
    path: "/{orgId}/members",
    urlParamKeys: ["orgId"],
  });

  /** 添加组织成员 */
  public addMember = this.makeRequest<
    { orgId: string; userId: string },
    string
  >({
    method: "POST",
    path: "/{orgId}/members",
    urlParamKeys: ["orgId"],
    payloadKey: "userId",
  });

  /** 移除组织成员 */
  public delMember = this.makeRequest<
    { orgId: string; userId: string },
    string
  >({
    method: "DELETE",
    path: "/{orgId}/members/{userId}",
    urlParamKeys: ["orgId", "userId"],
  });

  /** 获取组织成员详情 */
  public getMember = this.makeRequest<
    { orgId: string; userId: string },
    OrganizationMemberRepresentation
  >({
    method: "GET",
    path: "/{orgId}/members/{userId}",
    urlParamKeys: ["orgId", "userId"],
  });

  /** 列出用户所属组织 */
  public memberOrganizations = this.makeRequest<
    { userId: string },
    OrganizationRepresentation[]
  >({
    method: "GET",
    path: "/members/{userId}/organizations",
    urlParamKeys: ["userId"],
  });

  /** 邀请新用户加入组织 */
  public invite = this.makeUpdateRequest<{ orgId: string }, FormData>({
    method: "POST",
    path: "/{orgId}/members/invite-user",
    urlParamKeys: ["orgId"],
  });

  /** 邀请已有用户加入组织 */
  public inviteExistingUser = this.makeUpdateRequest<
    { orgId: string },
    FormData
  >({
    method: "POST",
    path: "/{orgId}/members/invite-existing-user",
    urlParamKeys: ["orgId"],
  });

  /** 列出组织关联 IdP */
  public listIdentityProviders = this.makeRequest<
    { orgId: string },
    IdentityProviderRepresentation[]
  >({
    method: "GET",
    path: "/{orgId}/identity-providers",
    urlParamKeys: ["orgId"],
  });

  /** 关联 IdP 到组织 */
  public linkIdp = this.makeRequest<{ orgId: string; alias: string }, string>({
    method: "POST",
    path: "/{orgId}/identity-providers",
    urlParamKeys: ["orgId"],
    payloadKey: "alias",
  });

  /** 解除组织 IdP 关联 */
  public unLinkIdp = this.makeRequest<{ orgId: string; alias: string }, string>(
    {
      method: "DELETE",
      path: "/{orgId}/identity-providers/{alias}",
      urlParamKeys: ["orgId", "alias"],
    },
  );

  // 组织邀请管理
  /** 列出组织邀请 */
  public listInvitations = this.makeRequest<
    InvitationQuery,
    OrganizationInvitationRepresentation[]
  >({
    method: "GET",
    path: "/{orgId}/invitations",
    urlParamKeys: ["orgId"],
  });

  /** 获取组织邀请详情 */
  public findInvitation = this.makeRequest<
    { orgId: string; invitationId: string },
    OrganizationInvitationRepresentation
  >({
    method: "GET",
    path: "/{orgId}/invitations/{invitationId}",
    urlParamKeys: ["orgId", "invitationId"],
  });

  /** 重新发送邀请 */
  public resendInvitation = this.makeRequest<
    { orgId: string; invitationId: string },
    void
  >({
    method: "POST",
    path: "/{orgId}/invitations/{invitationId}/resend",
    urlParamKeys: ["orgId", "invitationId"],
  });

  /** 删除组织邀请 */
  public deleteInvitation = this.makeRequest<
    { orgId: string; invitationId: string },
    void
  >({
    method: "DELETE",
    path: "/{orgId}/invitations/{invitationId}",
    urlParamKeys: ["orgId", "invitationId"],
  });

  /** 获取组织内组管理资源 */
  public groups = (orgId: string) => new Groups(this.#client, orgId);
}
