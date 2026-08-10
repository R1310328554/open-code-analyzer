// utils.test.ts — single-debug-sheet 工具函数单元测试。

import { Operator } from '../../constant';
import { shouldUseCodeExecDebugLayout } from './utils';

/** 验证 shouldUseCodeExecDebugLayout 仅对 CodeExec 节点返回 true。 */
describe('shouldUseCodeExecDebugLayout', () => {
  it('仅 Code 算子启用 CodeExec 调试布局', () => {
    expect(shouldUseCodeExecDebugLayout(Operator.Code)).toBe(true);
    expect(shouldUseCodeExecDebugLayout(Operator.Http)).toBe(false);
    expect(shouldUseCodeExecDebugLayout(undefined)).toBe(false);
  });
});
