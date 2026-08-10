// system.ts — 系统观测（Langfuse）配置写入请求体。

/** 保存 Langfuse 连接：secret_key、public_key 与 host。 */
export interface ISetLangfuseConfigRequestBody {
  secret_key: string;
  public_key: string;
  host: string;
}
