// GraphHeatmapHelpers 单元测试：验证 isHeatmapData 对各类 API 结果类型的判定逻辑。

import { DataTableProps } from './DataTable';
import { isHeatmapData } from './GraphHeatmapHelpers';

describe('GraphHeatmapHelpers', () => {
  it('isHeatmapData should return false for scalar and string resultType', () => {
    let data = {
      resultType: 'scalar',
      result: [1703091180.125, '1703091180.125'],
    } as DataTableProps['data'];
    expect(isHeatmapData(data)).toBe(false);

    data = {
      resultType: 'string',
      result: [1704305680.332, '2504'],
    } as DataTableProps['data'];
    expect(isHeatmapData(data)).toBe(false);
  });

  it('isHeatmapData should return false for a vector and matrix if length < 2', () => {
    let data = {
      resultType: 'vector',
      result: [
        {
          metric: {
            __name__: 'my_gauge',
            job: 'target',
          },
          value: [1703091180.683, '6'],
        },
      ],
    } as DataTableProps['data'];
    expect(isHeatmapData(data)).toBe(false);

    data = {
      resultType: 'matrix',
      result: [
        {
          metric: {},
          values: [[1703091180.683, '6']],
        },
      ],
    } as DataTableProps['data'];
    expect(isHeatmapData(data)).toBe(false);
  });

// 两条以上 le 标签且非 le 标签一致时判定为 histogram heatmap 输入。
  it('isHeatmapData should return true for valid heatmap data', () => {
    const data = {
      resultType: 'matrix',
      result: [
        {
          metric: {
            le: '100',
          },
          values: [[1703091180.683, '6']],
        },
        {
          metric: {
            le: '1000',
          },
          values: [[1703091190.683, '6.1']],
        },
      ],
    } as DataTableProps['data'];
    expect(isHeatmapData(data)).toBe(true);
  });
});
