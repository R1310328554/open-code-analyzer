import type { ConfigPropertyRepresentation } from "./configPropertyRepresentation.js";

/**
 * 身份提供者映射器类型元数据：描述某 mapper 工厂可配置的属性 schema。
 */
export interface IdentityProviderMapperTypeRepresentation {
  /** Mapper Provider 工厂 ID */
  id?: string;
  /** 映射器显示名称 */
  name?: string;
  /** 映射器分类（如 attribute-importer、role-importer） */
  category?: string;
  /** 映射器用途说明文本 */
  helpText?: string;
  /** 可配置属性列表（表单字段定义） */
  properties?: ConfigPropertyRepresentation[];
}
