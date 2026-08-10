// use-download-output.ts — 从 trace 中提取 END 节点 JSON 输出并触发文件下载。

import { useFetchAgent } from '@/hooks/use-agent-request';
import { ITraceData } from '@/interfaces/database/agent';
import { downloadJsonFile } from '@/utils/file-util';
import { get, isEmpty } from 'lodash';
import { useCallback } from 'react';

/** 在 trace 列表中查找 component_id 为 END 的首条 message 并解析为 JSON。 */
export function findEndOutput(list?: ITraceData[]) {
  if (Array.isArray(list)) {
    const trace = list.find((x) => x.component_id === 'END')?.trace;

    const str = get(trace, '0.message');

    try {
      if (!isEmpty(str)) {
        const json = JSON.parse(str);
        return json;
      }
    } catch (error) {
      console.warn(error);
    }
  }
}

/** 判断 END 输出是否为空（无 trace 或 message 为空）。 */
export function isEndOutputEmpty(list?: ITraceData[]) {
  return isEmpty(findEndOutput(list));
}
/** 将 END 输出以 Agent 标题命名下载为 .json 文件。 */
export function useDownloadOutput(data?: ITraceData[]) {
  const { data: agent } = useFetchAgent();

  const handleDownloadJson = useCallback(() => {
    const output = findEndOutput(data);
    if (!isEndOutputEmpty(data)) {
      downloadJsonFile(output, `${agent.title}.json`);
    }
  }, [agent.title, data]);

  return {
    handleDownloadJson,
  };
}
