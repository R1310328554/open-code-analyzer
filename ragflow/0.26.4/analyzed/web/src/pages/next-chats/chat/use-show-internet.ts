// use-show-internet.ts — 是否展示联网搜索：当前对话 prompt_config 含 tavily_api_key。

import { useFetchChat } from '@/hooks/use-chat-request';
import { isEmpty } from 'lodash';

/** 拉取当前 dialog 配置，非空 tavily_api_key 时启用 Internet 相关 UI。 */
export function useShowInternet() {
  const { data: currentDialog } = useFetchChat();

  return !isEmpty(currentDialog?.prompt_config?.tavily_api_key);
}
