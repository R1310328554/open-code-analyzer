// use-agent-tool-initial-values.ts — Agent 挂载工具时的 params 初值：按算子裁剪/覆盖敏感或冗余字段。

import { omit, pick } from 'lodash';
import { useCallback } from 'react';
import { Operator } from '../constant';
import { useInitializeOperatorParams } from './use-add-node';

/** 复用 initialFormValuesMap，按 Operator  omit/pick 生成 Agent 子工具专用默认 params。 */
export function useAgentToolInitialValues() {
  const { initialFormValuesMap } = useInitializeOperatorParams();

  const initializeAgentToolValues = useCallback(
    (operatorName: Operator) => {
      const initialValues = initialFormValuesMap[operatorName];

      switch (operatorName) {
        // Retrieval 工具不含 query（由 Agent 运行时注入）
        case Operator.Retrieval:
          return {
            ...omit(initialValues, 'query'),
            description: '',
          };
        // Tavily 工具仅保留 api_key 空串
        case (Operator.TavilySearch, Operator.TavilyExtract):
          return {
            api_key: '',
          };
        case Operator.ExeSQL:
          return omit(initialValues, 'sql');
        case Operator.Bing:
          return omit(initialValues, 'query');
        case Operator.YahooFinance:
          return omit(initialValues, 'stock_code');

        case Operator.Email:
          return pick(
            initialValues,
            'smtp_server',
            'smtp_port',
            'email',
            'smtp_username',
            'password',
            'sender_name',
          );

        case Operator.DuckDuckGo:
          return pick(initialValues, 'top_n', 'channel');

        case Operator.Wikipedia:
          return pick(initialValues, 'top_n', 'language');
        case Operator.Google:
          return pick(initialValues, 'api_key', 'country', 'language');
        case Operator.GoogleScholar:
          return omit(initialValues, 'query', 'outputs');
        case Operator.ArXiv:
          return pick(initialValues, 'top_n', 'sort_by');
        case Operator.PubMed:
          return pick(initialValues, 'top_n', 'email');
        case Operator.BGPT:
          return pick(initialValues, 'top_n', 'api_key', 'days_back');
        case Operator.GitHub:
          return pick(initialValues, 'top_n');
        case Operator.WenCai:
          return pick(initialValues, 'top_n', 'query_type');
        case Operator.Code:
          return {};
        case Operator.SearXNG:
          return pick(initialValues, 'searxng_url', 'top_n');
        case Operator.KeenableSearch:
          return pick(initialValues, 'api_key', 'mode', 'site', 'top_n');

        // 其余算子使用完整 initialFormValuesMap 条目
        default:
          return initialValues;
      }
    },
    [initialFormValuesMap],
  );

  return { initializeAgentToolValues };
}
