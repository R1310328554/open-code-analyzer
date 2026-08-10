// next-message-item/utils.ts — 从消息正文中提取引用标记对应的数字索引列表。

import { currentReg, parseCitationIndex } from '@/utils/chat';

/** 扫描 content 中引用标记，解析为 citation 序号数组；无匹配返回空数组。 */
export const extractNumbersFromMessageContent = (content: string) => {
  const matches = content.match(currentReg);
  if (matches) {
    const list = matches
      .map((match) => {
        const parsed = parseCitationIndex(match);
        return Number.isNaN(parsed) ? null : parsed;
      })
      .filter((num) => num !== null) as number[];

    return list;
  }
  return [];
};
