// system.ts — Langfuse 观测配置类型。

/** Langfuse 项目连接配置：密钥、host 与 project 标识。 */
export interface ILangfuseConfig {
  secret_key: string;
  public_key: string;
  host: string;
  project_id: string;
  project_name: string;
}
