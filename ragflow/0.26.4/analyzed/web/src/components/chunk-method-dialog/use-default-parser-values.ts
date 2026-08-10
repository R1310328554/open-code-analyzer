// use-default-parser-values.ts — 分块解析器表单的默认值与缺失字段回填。

import { IParserConfig } from '@/interfaces/database/document';
import { useCallback, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import { ParseDocumentType } from '../layout-recognize-form-field';

/** 构建 IParserConfig 默认项（分块大小、RAPTOR、MinerU 等）。 */
export function useDefaultParserValues() {
  const { t } = useTranslation();

  const defaultParserValues = useMemo(() => {
    const defaultParserValues = {
      task_page_size: 12,
      layout_recognize: ParseDocumentType.DeepDOC,
      chunk_token_num: 512,
      delimiter: '\n',
      enable_children: false,
      children_delimiter: '\n',
      auto_keywords: 0,
      auto_questions: 0,
      html4excel: false,
      toc_extraction: false,
      image_table_context_window: 0,
      mineru_parse_method: 'auto',
      mineru_formula_enable: true,
      mineru_table_enable: true,
      mineru_lang: 'English',
      raptor: {
        use_raptor: false,
        prompt: t('knowledgeConfiguration.promptText'),
        max_token: 256,
        threshold: 0.1,
        max_cluster: 64,
        random_seed: 0,
        scope: 'file',
        clustering_method: 'gmm',
        tree_builder: 'raptor',
      },
      // graphrag: {
      //   use_graphrag: false,
      // },
      entity_types: [],
      pages: [],
      metadata: [],
      built_in_metadata: [],
      enable_metadata: false,
    };

    return defaultParserValues as IParserConfig;
  }, [t]);

  return defaultParserValues;
}

/** 将已有 parserConfig 与默认值合并，缺失键用默认填充。 */
export function useFillDefaultValueOnMount() {
  const defaultParserValues = useDefaultParserValues();

  const fillDefaultValue = useCallback(
    (parserConfig: IParserConfig) => {
      return Object.entries(defaultParserValues).reduce<Record<string, any>>(
        (pre, [key, value]) => {
          if (key in parserConfig) {
            pre[key] = parserConfig[key as keyof IParserConfig];
          } else {
            pre[key] = value;
          }
          return pre;
        },
        {},
      );
    },
    [defaultParserValues],
  );

  return fillDefaultValue;
}
