import type { KeycloakAdminClient } from "../client.js";
import type IdentityProviderMapperRepresentation from "../defs/identityProviderMapperRepresentation.js";
import type { IdentityProviderMapperTypeRepresentation } from "../defs/identityProviderMapperTypeRepresentation.js";
import type IdentityProviderRepresentation from "../defs/identityProviderRepresentation.js";
import type { ManagementPermissionReference } from "../defs/managementPermissionReference.js";
import type CertificateRepresentation from "../defs/certificateRepresentation.js";
import Resource from "./resource.js";

/** 分页查询参数 */
export interface PaginatedQuery {
  first?: number;
  max?: number;
}

/** 身份提供者列表查询参数 */
export interface IdentityProvidersQuery extends PaginatedQuery {
  search?: string;
  realmOnly?: boolean;
  type?: string;
  capability?: string;
}

/** 身份提供者 Admin 资源：外部 IdP 实例、映射器与证书管理。 */
export class IdentityProviders extends Resource<{ realm?: string }> {
  /**
   * 身份提供者
   * Identity provider
   * https://www.keycloak.org/docs-api/11.0/rest-api/#_identity_providers_resource
   */

  public find = this.makeRequest<
    IdentityProvidersQuery,
    IdentityProviderRepresentation[]
  >({
    method: "GET",
    path: "/instances",
  });

  /** 创建 */
  public create = this.makeRequest<
    IdentityProviderRepresentation,
    { id: string }
  >({
    method: "POST",
    path: "/instances",
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /** 按 ID 获取单个 */
  public findOne = this.makeRequest<
    { alias: string },
    IdentityProviderRepresentation | undefined
  >({
    method: "GET",
    path: "/instances/{alias}",
    urlParamKeys: ["alias"],
    catchNotFound: true,
  });

  /** 上传证书 */
  public uploadCertificate = this.makeUpdateRequest<
    {},
    FormData,
    CertificateRepresentation
  >({
    method: "POST",
    path: "/upload-certificate",
  });

  /** 更新 */
  public update = this.makeUpdateRequest<
    { alias: string },
    IdentityProviderRepresentation,
    void
  >({
    method: "PUT",
    path: "/instances/{alias}",
    urlParamKeys: ["alias"],
  });

  /** 删除 */
  public del = this.makeRequest<{ alias: string }, void>({
    method: "DELETE",
    path: "/instances/{alias}",
    urlParamKeys: ["alias"],
  });

  /** 获取 IdP 提供者工厂配置 */
  public findFactory = this.makeRequest<{ providerId: string }, any>({
    method: "GET",
    path: "/providers/{providerId}",
    urlParamKeys: ["providerId"],
  });

  /** 列出 IdP 映射器 */
  public findMappers = this.makeRequest<
    { alias: string },
    IdentityProviderMapperRepresentation[]
  >({
    method: "GET",
    path: "/instances/{alias}/mappers",
    urlParamKeys: ["alias"],
  });

  /** 按 ID 获取 IdP 映射器 */
  public findOneMapper = this.makeRequest<
    { alias: string; id: string },
    IdentityProviderMapperRepresentation | undefined
  >({
    method: "GET",
    path: "/instances/{alias}/mappers/{id}",
    urlParamKeys: ["alias", "id"],
    catchNotFound: true,
  });

  /** 创建 IdP 映射器 */
  public createMapper = this.makeRequest<
    {
      alias: string;
      identityProviderMapper: IdentityProviderMapperRepresentation;
    },
    { id: string }
  >({
    method: "POST",
    path: "/instances/{alias}/mappers",
    urlParamKeys: ["alias"],
    payloadKey: "identityProviderMapper",
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /** 更新 IdP 映射器 */
  public updateMapper = this.makeUpdateRequest<
    { alias: string; id: string },
    IdentityProviderMapperRepresentation,
    void
  >({
    method: "PUT",
    path: "/instances/{alias}/mappers/{id}",
    urlParamKeys: ["alias", "id"],
  });

  /** 删除 IdP 映射器 */
  public delMapper = this.makeRequest<{ alias: string; id: string }, void>({
    method: "DELETE",
    path: "/instances/{alias}/mappers/{id}",
    urlParamKeys: ["alias", "id"],
  });

  /** 列出 IdP 映射器类型 */
  public findMapperTypes = this.makeRequest<
    { alias: string },
    Record<string, IdentityProviderMapperTypeRepresentation>
  >({
    method: "GET",
    path: "/instances/{alias}/mapper-types",
    urlParamKeys: ["alias"],
  });

  /** 从 URL 导入 IdP 配置 */
  public importFromUrl = this.makeRequest<
    | {
        fromUrl: string;
        providerId: string;
      }
    | FormData,
    Record<string, string>
  >({
    method: "POST",
    path: "/import-config",
  });

  /** 更新权限 */
  public updatePermission = this.makeUpdateRequest<
    { alias: string },
    ManagementPermissionReference,
    ManagementPermissionReference
  >({
    method: "PUT",
    path: "/instances/{alias}/management/permissions",
    urlParamKeys: ["alias"],
  });

  /** 获取细粒度管理权限 */
  public listPermissions = this.makeRequest<
    { alias: string },
    ManagementPermissionReference
  >({
    method: "GET",
    path: "/instances/{alias}/management/permissions",
    urlParamKeys: ["alias"],
  });

  /** 重新加载 IdP 签名密钥 */
  public reloadKeys = this.makeRequest<{ alias: string }, boolean>({
    method: "GET",
    path: "/instances/{alias}/reload-keys",
    urlParamKeys: ["alias"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}/identity-provider",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
