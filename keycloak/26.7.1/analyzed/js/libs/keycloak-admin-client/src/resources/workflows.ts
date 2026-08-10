import Resource from "./resource.js";
import type { KeycloakAdminClient } from "../client.js";
import WorkflowRepresentation from "../defs/workflowRepresentation.js";

/**
 * 工作流（Workflow）Admin 资源：Realm 级自动化流程的 CRUD 与查询。
 * 对应 REST 路径 `/admin/realms/{realm}/workflows`，支持 JSON 与 YAML 创建。
 */
export class Workflows extends Resource<{ realm?: string }> {
  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}/workflows",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }

  /** 列出当前 Realm 下的全部工作流 */
  find = this.makeRequest({
    method: "GET",
    path: "/",
  });

  /** 按 ID 获取单个工作流；`includeId` 控制响应是否包含 ID 字段 */
  public findOne = this.makeRequest<
    { id: string; includeId: boolean },
    WorkflowRepresentation | undefined
  >({
    method: "GET",
    path: "/{id}",
    urlParamKeys: ["id"],
    queryParamKeys: ["includeId"],
    catchNotFound: true,
  });

  /** 查询指定用户已调度（scheduled）的工作流列表 */
  public scheduled = this.makeRequest<
    { userId: string },
    WorkflowRepresentation[]
  >({
    method: "GET",
    path: "/scheduled/{userId}",
    urlParamKeys: ["userId"],
    catchNotFound: true,
  });

  /** 更新已有工作流定义（PUT） */
  public update = this.makeUpdateRequest<
    { id: string },
    WorkflowRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}",
    urlParamKeys: ["id"],
  });

  /** 以 JSON 创建新工作流；新资源 ID 从 Location 响应头解析 */
  public create = this.makeRequest<WorkflowRepresentation, { id: string }>({
    method: "POST",
    headers: { "Content-Type": "application/json" },
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /** 以 YAML 正文创建新工作流，适用于声明式工作流配置 */
  public createAsYaml = this.makeRequest<
    { realm: string; yaml: string },
    { id: string }
  >({
    method: "POST",
    headers: { "Content-Type": "application/yaml", Accept: "application/yaml" },
    returnResourceIdInLocationHeader: { field: "id" },
    payloadKey: "yaml",
  });

  /** 按 ID 删除工作流 */
  public delById = this.makeRequest<{ id: string }, void>({
    method: "DELETE",
    path: "/{id}",
    urlParamKeys: ["id"],
  });
}
