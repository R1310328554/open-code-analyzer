// monaco-config.ts — Code 节点 Monaco 编辑器选项与 RAGFlow 主题名映射。

/** Code 编辑器 Monaco 选项：关闭 minimap、自动布局与滚动条尺寸。 */
export const CodeEditorOptions = {
  minimap: { enabled: false },
  automaticLayout: true,
  scrollbar: {
    verticalScrollbarSize: 10,
    horizontalScrollbarSize: 10,
  },
};

/** 明暗主题与 Monaco 内置主题 id 的对应关系。 */
export const RAGFlowMonacoTheme = {
  Light: 'vs',
  Dark: 'vs-dark',
} as const;
