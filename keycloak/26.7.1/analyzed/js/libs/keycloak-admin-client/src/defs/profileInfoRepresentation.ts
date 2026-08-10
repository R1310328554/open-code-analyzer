/**
 * Keycloak 运行时 Profile 信息：当前激活的配置文件名称与各特性开关分类。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_profileinforepresentation
 */
export default interface ProfileInfoRepresentation {
  /** 当前 Profile 名称（如 default、preview、product） */
  name?: string;
  /** 在本 Profile 下被显式禁用的特性名称列表 */
  disabledFeatures?: string[];
  /** 标记为 Preview（预览）阶段的特性列表 */
  previewFeatures?: string[];
  /** 标记为 Experimental（实验性）的特性列表 */
  experimentalFeatures?: string[];
}
