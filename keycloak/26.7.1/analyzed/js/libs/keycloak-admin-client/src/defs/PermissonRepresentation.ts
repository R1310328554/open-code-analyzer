/** UMA/授权决策中的权限表示（PermissionRepresentation，文件名保留历史拼写 Permisson）。 */
export default interface PermissionRepresentation {
  /** 附加声明键值对 */
  claims?: { [index: string]: string };
  /** 资源服务器内资源 ID */
  rsid?: string;
  /** 资源名称 */
  rsname?: string;
  /** 授权 scope 列表 */
  scopes?: string[];
}
