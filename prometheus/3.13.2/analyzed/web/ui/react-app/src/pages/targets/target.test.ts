// target.ts 单元测试：验证 groupTargets 按 scrape job 分组与 getColor 健康状态配色映射。

import { sampleApiResponse } from './__testdata__/testdata';
import { groupTargets, Target, ScrapePools, getColor } from './target';

describe('groupTargets', () => {
  const targets: Target[] = sampleApiResponse.data.activeTargets as Target[];
  const targetGroups: ScrapePools = groupTargets(targets);

  it('groups a list of targets by scrape job', () => {
    ['blackbox', 'prometheus/test', 'node_exporter'].forEach((scrapePool) => {
      expect(Object.keys(targetGroups)).toContain(scrapePool);
    });
    Object.keys(targetGroups).forEach((scrapePool: string): void => {
      const ts: Target[] = targetGroups[scrapePool].targets;
      ts.forEach((t: Target) => {
        expect(t.scrapePool).toEqual(scrapePool);
      });
    });
  });

  it('adds upCount during aggregation', () => {
    const testCases: { [key: string]: number } = { blackbox: 3, 'prometheus/test': 1, node_exporter: 1 };
    Object.keys(testCases).forEach((scrapePool: string): void => {
      expect(targetGroups[scrapePool].upCount).toEqual(testCases[scrapePool]);
    });
  });
});

// getColor 表驱动测试 up/down/unknown 大小写不敏感时的 Bootstrap 语义色类名。
describe('getColor', () => {
  const testCases: { color: string; status: string }[] = [
    { color: 'danger', status: 'down' },
    { color: 'danger', status: 'DOWN' },
    { color: 'warning', status: 'unknown' },
    { color: 'warning', status: 'foo' },
    { color: 'success', status: 'up' },
    { color: 'success', status: 'Up' },
  ];
  testCases.forEach(({ color, status }) => {
    it(`returns ${color} for ${status} status`, () => {
      expect(getColor(status)).toEqual(color);
    });
  });
});
// Targets 工具函数测试覆盖聚合逻辑与 UI 状态颜色映射。
