// utils.ts — 数据集文档解析状态辅助：判断 RunningStatus 是否处于运行中。

import { RunningStatus } from './constant';

/** 根据 RunningStatus 枚举判断文档解析任务是否仍在执行。 */
export const isParserRunning = (text: RunningStatus) => {
  const isRunning = text === RunningStatus.RUNNING;
  return isRunning;
};
