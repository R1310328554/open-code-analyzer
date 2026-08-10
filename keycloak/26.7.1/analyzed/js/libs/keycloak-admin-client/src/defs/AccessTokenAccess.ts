/** 访问令牌中的 realm_access 声明：realm 级角色与调用方校验标志。 */
export default interface AccessTokenAccess {
  /** realm 角色名列表 */
  roles?: string[];
  /** 是否要求校验调用方（UMA 等场景） */
  verify_caller?: boolean;
}
