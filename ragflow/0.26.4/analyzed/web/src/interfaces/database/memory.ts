// memory.ts — Agent 长期记忆实体：存储类型、权限与嵌入/LLM 绑定。

/** 记忆库实体：memory_type、storage_type、权限与 embd_id/llm_id。 */
export interface IMemory {
  avatar: null;
  description: null;
  id: string;
  memory_type: string[];
  name: string;
  owner_name: string;
  permissions: string;
  storage_type: string;
  tenant_id: string;
  embd_id: string;
  llm_id: string;
}
