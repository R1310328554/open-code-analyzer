// chunk-method-dialog/hooks.ts — 分块方式对话框：按文件扩展名过滤解析器与自动关键词显隐。

import { useSelectParserList } from '@/hooks/use-user-setting-request';
import { useCallback, useMemo } from 'react';

/** 扩展名 → 可用 parser 类型列表的静态映射。 */
const ParserListMap = new Map([
  [
    ['pdf'],
    [
      'naive',
      'resume',
      'manual',
      'paper',
      'book',
      'laws',
      'presentation',
      'one',
      'qa',
      'knowledge_graph',
    ],
  ],
  [
    ['doc', 'docx'],
    [
      'naive',
      'resume',
      'book',
      'laws',
      'one',
      'qa',
      'manual',
      'knowledge_graph',
    ],
  ],
  [
    ['xlsx', 'xls'],
    ['naive', 'qa', 'table', 'one', 'knowledge_graph'],
  ],
  [['ppt', 'pptx'], ['presentation']],
  [
    ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'tif', 'tiff', 'webp', 'svg', 'ico'],
    ['picture'],
  ],
  [
    ['txt'],
    [
      'naive',
      'resume',
      'book',
      'laws',
      'one',
      'qa',
      'table',
      'knowledge_graph',
    ],
  ],
  [
    ['csv'],
    [
      'naive',
      'resume',
      'book',
      'laws',
      'one',
      'qa',
      'table',
      'knowledge_graph',
    ],
  ],
  [
    ['md', 'mdx'],
    ['naive', 'qa', 'knowledge_graph'],
  ],
  [['json'], ['naive', 'knowledge_graph']],
  [['eml'], ['email']],
]);

/** 按 parser value 白名单过滤后端返回的 parser 选项。 */
const getParserList = (
  values: string[],
  parserList: Array<{
    value: string;
    label: string;
  }>,
) => {
  return parserList.filter((x) => values?.some((y) => y === x.value));
};

/** 挂载时按文档扩展名计算可选分块/解析方式列表。 */
export const useFetchParserListOnMount = (documentExtension: string) => {
  const parserList = useSelectParserList();

  const nextParserList = useMemo(() => {
    const key = [...ParserListMap.keys()].find((x) =>
      x.some((y) => y === documentExtension),
    );
    if (key) {
      const values = ParserListMap.get(key);
      return getParserList(values ?? [], parserList);
    }

    return getParserList(
      ['naive', 'resume', 'book', 'laws', 'one', 'qa', 'table'],
      parserList,
    );
  }, [parserList, documentExtension]);

  return { parserList: nextParserList };
};

/** 这些 parser 类型不展示「自动关键词」表单项。 */
const hideAutoKeywords = ['qa', 'table', 'resume', 'knowledge_graph', 'tag'];

/** 返回函数：当前 parser 是否应显示自动关键词配置。 */
export const useShowAutoKeywords = () => {
  const showAutoKeywords = useCallback((selectedTag: string) => {
    return hideAutoKeywords.every((x) => selectedTag !== x);
  }, []);

  return showAutoKeywords;
};
