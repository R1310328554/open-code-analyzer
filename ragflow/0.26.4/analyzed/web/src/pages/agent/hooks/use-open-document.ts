// use-open-document.ts — 在新标签页打开 Agent 组件官方文档。

import { useCallback } from 'react';

/** 返回 openDocument 回调，跳转 ragflow.io Agent Components 文档页。 */
export function useOpenDocument() {
  const openDocument = useCallback(() => {
    window.open(
      'https://ragflow.io/docs/dev/category/agent-components',
      '_blank',
    );
  }, []);

  return openDocument;
}
