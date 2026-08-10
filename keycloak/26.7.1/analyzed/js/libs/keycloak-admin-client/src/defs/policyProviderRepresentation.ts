/** 授权服务（Authorization Services）中已注册的 Policy Provider SPI 描述信息。 */
export default interface PolicyProviderRepresentation {
  /** Provider 实现类型 ID（如 role、group、js） */
  type?: string;
  /** Provider 显示名称 */
  name?: string;
  /** 管理控制台分组（用于策略类型选择器分类） */
  group?: string;
  /** Provider 功能说明 */
  description?: string;
  /** 前端/脚本策略的内联代码或模板标识（部分 Provider 使用） */
  code?: string;
}
