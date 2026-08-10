// reference-utils.ts — Markdown 引用标记解析：连续引用分组与图片轮播判定。

import { IReference } from '@/interfaces/database/chat';
import { currentReg, normalizeCitationDigits, showImage } from '@/utils/chat';

/** 文本中单个引用标记的 id、原文片段及起止偏移。 */
export interface ReferenceMatch {
  id: string;
  fullMatch: string;
  start: number;
  end: number;
}

/** 相邻引用标记分组，用于合并展示或轮播。 */
export type ReferenceGroup = ReferenceMatch[];

/** 全局扫描 text，按 currentReg 提取全部引用匹配项。 */
export const findAllReferenceMatches = (text: string): ReferenceMatch[] => {
  const matches: ReferenceMatch[] = [];
  let match;
  while ((match = currentReg.exec(text)) !== null) {
    matches.push({
      id: normalizeCitationDigits(match[1]),
      fullMatch: match[0],
      start: match.index,
      end: match.index + match[0].length,
    });
  }
  return matches;
};

/**
 * 将首尾相接的引用标记合并为 ReferenceGroup 二维数组。
 */
export const groupConsecutiveReferences = (text: string): ReferenceGroup[] => {
  const matches = findAllReferenceMatches(text);
  // Construct a two-dimensional array to distinguish whether images are continuous.
  const groups: ReferenceGroup[] = [];

  if (matches.length === 0) return groups;

  let currentGroup: ReferenceGroup = [matches[0]];
  // A group with only one element contains non-contiguous images,
  // while a group with multiple elements contains contiguous images.
  for (let i = 1; i < matches.length; i++) {
    // If the end of the previous element equals the start of the current element,
    // it means that they are consecutive images.
    if (matches[i].start === currentGroup[currentGroup.length - 1].end) {
      currentGroup.push(matches[i]);
    } else {
      // Save current group and start a new one
      groups.push(currentGroup);
      currentGroup = [matches[i]];
    }
  }
  groups.push(currentGroup);

  return groups;
};

/** 组内至少两个引用且对应 chunk 均为可展示图片类型时启用轮播。 */
export const shouldShowCarousel = (
  group: ReferenceGroup,
  reference: IReference,
): boolean => {
  if (group.length < 2) return false; // Need at least 2 images for carousel

  return group.every((ref) => {
    const chunkIndex = Number(ref.id);
    const chunk = reference.chunks[chunkIndex];
    return chunk && showImage(chunk.doc_type);
  });
};
