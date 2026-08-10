import type ClientScopeRepresentation from "../defs/clientScopeRepresentation.js";
import Resource from "./resource.js";
import type { KeycloakAdminClient } from "../client.js";
import type ProtocolMapperRepresentation from "../defs/protocolMapperRepresentation.js";
import type MappingsRepresentation from "../defs/mappingsRepresentation.js";
import type RoleRepresentation from "../defs/roleRepresentation.js";

/** 客户端 Scope Admin 资源：Realm 级 Scope CRUD、默认 Scope、协议映射器与 Scope 角色映射。 */
export class ClientScopes extends Resource<{ realm?: string }> {
  /** 查询列表 */
  public find = this.makeRequest<{}, ClientScopeRepresentation[]>({
    method: "GET",
    path: "/client-scopes",
  });

  /** 创建 */
  public create = this.makeRequest<ClientScopeRepresentation, { id: string }>({
    method: "POST",
    path: "/client-scopes",
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /**
   * 按 ID 操作客户端 Scope
   * Client-Scopes by id
   */

  public findOne = this.makeRequest<
    { id: string },
    ClientScopeRepresentation | undefined
  >({
    method: "GET",
    path: "/client-scopes/{id}",
    urlParamKeys: ["id"],
    catchNotFound: true,
  });

  /** 更新 */
  public update = this.makeUpdateRequest<
    { id: string },
    ClientScopeRepresentation,
    void
  >({
    method: "PUT",
    path: "/client-scopes/{id}",
    urlParamKeys: ["id"],
  });

  /** 删除 */
  public del = this.makeRequest<{ id: string }, void>({
    method: "DELETE",
    path: "/client-scopes/{id}",
    urlParamKeys: ["id"],
  });

  /**
   * Realm 默认客户端 Scope
   * Default Client-Scopes
   */

  public listDefaultClientScopes = this.makeRequest<
    void,
    ClientScopeRepresentation[]
  >({
    method: "GET",
    path: "/default-default-client-scopes",
  });

  /** 添加默认客户端 Scope */
  public addDefaultClientScope = this.makeRequest<{ id: string }, void>({
    method: "PUT",
    path: "/default-default-client-scopes/{id}",
    urlParamKeys: ["id"],
  });

  /** 移除默认客户端 Scope */
  public delDefaultClientScope = this.makeRequest<{ id: string }, void>({
    method: "DELETE",
    path: "/default-default-client-scopes/{id}",
    urlParamKeys: ["id"],
  });

  /**
   * 默认可选客户端 Scope
   * Default Optional Client-Scopes
   */

  public listDefaultOptionalClientScopes = this.makeRequest<
    void,
    ClientScopeRepresentation[]
  >({
    method: "GET",
    path: "/default-optional-client-scopes",
  });

  /** 添加默认可选客户端 Scope */
  public addDefaultOptionalClientScope = this.makeRequest<{ id: string }, void>(
    {
      method: "PUT",
      path: "/default-optional-client-scopes/{id}",
      urlParamKeys: ["id"],
    },
  );

  /** 移除默认可选客户端 Scope */
  public delDefaultOptionalClientScope = this.makeRequest<{ id: string }, void>(
    {
      method: "DELETE",
      path: "/default-optional-client-scopes/{id}",
      urlParamKeys: ["id"],
    },
  );

  /**
   * 协议映射器
   * Protocol Mappers
   */

  public addMultipleProtocolMappers = this.makeUpdateRequest<
    { id: string },
    ProtocolMapperRepresentation[],
    void
  >({
    method: "POST",
    path: "/client-scopes/{id}/protocol-mappers/add-models",
    urlParamKeys: ["id"],
  });

  /** 添加协议映射器 */
  public addProtocolMapper = this.makeUpdateRequest<
    { id: string },
    ProtocolMapperRepresentation,
    void
  >({
    method: "POST",
    path: "/client-scopes/{id}/protocol-mappers/models",
    urlParamKeys: ["id"],
  });

  /** 列出协议映射器 */
  public listProtocolMappers = this.makeRequest<
    { id: string },
    ProtocolMapperRepresentation[]
  >({
    method: "GET",
    path: "/client-scopes/{id}/protocol-mappers/models",
    urlParamKeys: ["id"],
  });

  /** 按 ID 获取协议映射器 */
  public findProtocolMapper = this.makeRequest<
    { id: string; mapperId: string },
    ProtocolMapperRepresentation | undefined
  >({
    method: "GET",
    path: "/client-scopes/{id}/protocol-mappers/models/{mapperId}",
    urlParamKeys: ["id", "mapperId"],
    catchNotFound: true,
  });

  /** 按协议类型列出协议映射器 */
  public findProtocolMappersByProtocol = this.makeRequest<
    { id: string; protocol: string },
    ProtocolMapperRepresentation[]
  >({
    method: "GET",
    path: "/client-scopes/{id}/protocol-mappers/protocol/{protocol}",
    urlParamKeys: ["id", "protocol"],
    catchNotFound: true,
  });

  /** 更新协议映射器 */
  public updateProtocolMapper = this.makeUpdateRequest<
    { id: string; mapperId: string },
    ProtocolMapperRepresentation,
    void
  >({
    method: "PUT",
    path: "/client-scopes/{id}/protocol-mappers/models/{mapperId}",
    urlParamKeys: ["id", "mapperId"],
  });

  /** 删除协议映射器 */
  public delProtocolMapper = this.makeRequest<
    { id: string; mapperId: string },
    void
  >({
    method: "DELETE",
    path: "/client-scopes/{id}/protocol-mappers/models/{mapperId}",
    urlParamKeys: ["id", "mapperId"],
  });

  /**
   * Scope 角色映射
   * Scope Mappings
   */
  public listScopeMappings = this.makeRequest<
    { id: string },
    MappingsRepresentation
  >({
    method: "GET",
    path: "/client-scopes/{id}/scope-mappings",
    urlParamKeys: ["id"],
  });

  /** 添加客户端 Scope 角色映射 */
  public addClientScopeMappings = this.makeUpdateRequest<
    { id: string; client: string },
    RoleRepresentation[],
    void
  >({
    method: "POST",
    path: "/client-scopes/{id}/scope-mappings/clients/{client}",
    urlParamKeys: ["id", "client"],
  });

  /** 列出客户端 Scope 角色映射 */
  public listClientScopeMappings = this.makeRequest<
    { id: string; client: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/client-scopes/{id}/scope-mappings/clients/{client}",
    urlParamKeys: ["id", "client"],
  });

  /** 列出可分配的客户端 Scope 角色映射 */
  public listAvailableClientScopeMappings = this.makeRequest<
    { id: string; client: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/client-scopes/{id}/scope-mappings/clients/{client}/available",
    urlParamKeys: ["id", "client"],
  });

  /** 列出客户端 Scope 复合角色映射 */
  public listCompositeClientScopeMappings = this.makeRequest<
    { id: string; client: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/client-scopes/{id}/scope-mappings/clients/{client}/composite",
    urlParamKeys: ["id", "client"],
  });

  /** 删除客户端 Scope 角色映射 */
  public delClientScopeMappings = this.makeUpdateRequest<
    { id: string; client: string },
    RoleRepresentation[],
    void
  >({
    method: "DELETE",
    path: "/client-scopes/{id}/scope-mappings/clients/{client}",
    urlParamKeys: ["id", "client"],
  });

  /** 添加 Realm Scope 角色映射 */
  public addRealmScopeMappings = this.makeUpdateRequest<
    { id: string },
    RoleRepresentation[],
    void
  >({
    method: "POST",
    path: "/client-scopes/{id}/scope-mappings/realm",
    urlParamKeys: ["id"],
  });

  /** 列出 Realm Scope 角色映射 */
  public listRealmScopeMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/client-scopes/{id}/scope-mappings/realm",
    urlParamKeys: ["id"],
  });

  /** 列出可分配的 Realm Scope 角色映射 */
  public listAvailableRealmScopeMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/client-scopes/{id}/scope-mappings/realm/available",
    urlParamKeys: ["id"],
  });

  /** 列出 Realm Scope 复合角色映射 */
  public listCompositeRealmScopeMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/client-scopes/{id}/scope-mappings/realm/composite",
    urlParamKeys: ["id"],
  });

  /** 删除 Realm Scope 角色映射 */
  public delRealmScopeMappings = this.makeUpdateRequest<
    { id: string },
    RoleRepresentation[],
    void
  >({
    method: "DELETE",
    path: "/client-scopes/{id}/scope-mappings/realm",
    urlParamKeys: ["id"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }

  /**
   * 按名称查找
   * Find client scope by name.
   */
  public async findOneByName(payload: {
    realm?: string;
    name: string;
  }): Promise<ClientScopeRepresentation | undefined> {
    const allScopes = await this.find({
      ...(payload.realm ? { realm: payload.realm } : {}),
    });
    return allScopes.find((item) => item.name === payload.name);
  }

  /**
   * 按名称删除
   * Delete client scope by name.
   */
  public async delByName(payload: {
    realm?: string;
    name: string;
  }): Promise<void> {
    const scope = await this.findOneByName(payload);

    if (!scope) {
      throw new Error("Scope not found.");
    }

    await this.del({
      ...(payload.realm ? { realm: payload.realm } : {}),
      id: scope.id!,
    });
  }

  /**
   * 按名称查找协议映射器
   * Find single protocol mapper by name.
   */
  public async findProtocolMapperByName(payload: {
    realm?: string;
    id: string;
    name: string;
  }): Promise<ProtocolMapperRepresentation | undefined> {
    const allProtocolMappers = await this.listProtocolMappers({
      id: payload.id,
      ...(payload.realm ? { realm: payload.realm } : {}),
    });
    return allProtocolMappers.find((mapper) => mapper.name === payload.name);
  }
}
