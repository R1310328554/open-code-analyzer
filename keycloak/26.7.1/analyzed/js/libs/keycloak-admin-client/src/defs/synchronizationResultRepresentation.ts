/**
 * 用户/组同步操作结果：LDAP 或联邦存储同步后的统计摘要。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_synchronizationresult
 */

export default interface SynchronizationResultRepresentation {
  /** 本次同步是否被跳过（如未启用同步） */
  ignored?: boolean;
  /** 新增记录数 */
  added?: number;
  /** 更新记录数 */
  updated?: number;
  /** 删除记录数 */
  removed?: number;
  /** 失败记录数 */
  failed?: number;
  /** 同步状态描述（如 SUCCESS、ERROR） */
  status?: string;
}
