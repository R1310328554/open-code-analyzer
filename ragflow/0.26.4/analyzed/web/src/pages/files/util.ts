// util.ts — 文件列表类型判断：区分普通文件夹与知识库挂载节点。

/** 判断条目 type 是否为文件夹（可展开导航）。 */
export function isFolderType(type: string) {
  return type === 'folder';
}

/** 判断 sourceType 是否来自知识库虚拟目录。 */
export function isKnowledgeBaseType(sourceType: string) {
  return sourceType === 'knowledgebase';
}
