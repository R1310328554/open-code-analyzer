// use-export-json.ts — 导出 Agent DSL JSON：合并画布状态并清除敏感 api_key 字段。

import { Operator } from '@/constants/agent';
import { useFetchAgent } from '@/hooks/use-agent-request';
import { downloadJsonFile } from '@/utils/file-util';
import { cloneDeepWith, get, isPlainObject } from 'lodash';
import { useCallback } from 'react';
import useGraphStore from '../store';
import { exportDsl } from '../utils/dsl-bridge';

/**
 * 递归清除 Tavily/Google 等算子 params.api_key，避免密钥随 DSL 导出。
 * Recursively clear sensitive fields (api_key) from the DSL object
 */
const clearSensitiveFields = <T,>(obj: T): T =>
  cloneDeepWith(obj, (value) => {
    if (
      isPlainObject(value) &&
      [
        Operator.TavilySearch,
        Operator.TavilyExtract,
        Operator.Google,
        Operator.KeenableSearch,
        Operator.BGPT,
      ].includes(value.component_name) &&
      get(value, 'params.api_key')
    ) {
      return { ...value, params: { ...value.params, api_key: '' } };
    }
  });

/** exportDsl 合成完整 DSL，脱敏后以下载 JSON 文件形式导出。 */
export const useHandleExportJsonFile = () => {
  const { data } = useFetchAgent();
  const { nodes, edges } = useGraphStore((state) => state);

  const handleExportJson = useCallback(() => {
    // exportDsl 从当前图状态与既有 dsl 字段生成标准线型结构
    // bridge.exportDsl returns the canonical wire shape from current
    // graph state plus preserved DSL fields, so export can write it
    // directly after sensitive-field sanitization.
    const full = exportDsl(nodes, edges, data?.dsl ?? {});
    const sanitizedDsl = clearSensitiveFields(full);
    const nextDsl = {
      ...sanitizedDsl,
      globals: { ...(sanitizedDsl.globals ?? {}) },
    };

    downloadJsonFile(nextDsl, `${data.title}.json`);
  }, [nodes, edges, data?.dsl, data.title]);

  return {
    handleExportJson,
  };
};
