// constant.ts — 数据集文档列表：RunningStatus 展示标签与颜色，并 re-export 知识库常量。

import { RunningStatus } from '@/constants/knowledge';

/** 文档解析 RunningStatus 到 UI 标签文案与主题色的映射。 */
export const RunningStatusMap = {
  [RunningStatus.UNSTART]: {
    label: 'UNSTART',
    color: 'rgba(var(--accent-primary))',
  },
  [RunningStatus.RUNNING]: {
    label: 'Parsing',
    color: 'var(--team-member)',
  },
  [RunningStatus.CANCEL]: {
    label: 'CANCEL',
    color: 'rgba(var(--state-warning))',
  },
  [RunningStatus.DONE]: {
    label: 'SUCCESS',
    color: 'rgba(var(--state-success))',
  },
  [RunningStatus.FAIL]: { label: 'FAIL', color: 'rgba(var(--state-error))' },
};

// 复用 @/constants/knowledge 中的 RunningStatus 等枚举
export * from '@/constants/knowledge';
