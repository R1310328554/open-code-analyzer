// util.ts — 知识图谱节点/组合（combo）构建：按边连通分量或 communities 字段分组。

import { isEmpty } from 'lodash';
import { v4 as uuid } from 'uuid';

/** 无社区信息时为全部节点分配的默认 combo 标签。 */
export const defaultComboLabel = 'defaultCombo';
/** 按 a-z 顺序生成 combo 分组键，避免与节点 id 冲突。 */
class KeyGenerator {
  idx = 0;
  chars: string[] = [];
  constructor() {
    const chars = Array(26)
      .fill(1)
      .map((x, idx) => String.fromCharCode(97 + idx)); // 26 char
    this.chars = chars;
  }
  generateKey() {
    const key = this.chars[this.idx];
    this.idx++;
    return key;
  }
}

// 基于边连通关系将节点划入同一 combo（并查类合并逻辑）
// Classify nodes based on edge relationships
/** 根据边列表构建 nodeId -> comboKey 映射并输出带 combo 的节点与 combos。 */
export class Converter {
  keyGenerator;
  /** nodeId -> comboKey 映射表。 */
  dict: Record<string, string> = {}; // key is node id, value is combo
  constructor() {
    this.keyGenerator = new KeyGenerator();
  }
  /** 遍历边，将 source/target 合并到同一 combo 键。 */
  buildDict(edges: { source: string; target: string }[]) {
    edges.forEach((x) => {
      if (this.dict[x.source] && !this.dict[x.target]) {
        this.dict[x.target] = this.dict[x.source];
      } else if (!this.dict[x.source] && this.dict[x.target]) {
        this.dict[x.source] = this.dict[x.target];
      } else if (!this.dict[x.source] && !this.dict[x.target]) {
        this.dict[x.source] = this.dict[x.target] =
          this.keyGenerator.generateKey();
      }
    });
    return this.dict;
  }
  /** 为每个节点附加 combo 字段并去重生成 combos 配置。 */
  buildNodesAndCombos(nodes: any[], edges: any[]) {
    this.buildDict(edges);
    const nextNodes = nodes.map((x) => ({ ...x, combo: this.dict[x.id] }));

    const combos = Object.values(this.dict).reduce<any[]>((pre, cur) => {
      if (pre.every((x) => x.id !== cur)) {
        pre.push({
          id: cur,
          data: {
            label: `Combo ${cur}`,
          },
        });
      }
      return pre;
    }, []);

    return { nodes: nextNodes, combos };
  }
}

/** 判断接口返回是否包含非空的 graph 子结构。 */
export const isDataExist = (data: any) => {
  return (
    data?.data && typeof data?.data !== 'boolean' && !isEmpty(data?.data?.graph)
  );
};

/** 取 communities 数组首项作为 combo 标签。 */
const findCombo = (communities: string[]) => {
  const combo = Array.isArray(communities) ? communities[0] : undefined;
  return combo;
};

/** 按节点 communities 字段分组；无社区时创建 defaultCombo 包裹全部节点。 */
export const buildNodesAndCombos = (nodes: any[]) => {
  const combos: any[] = [];
  nodes.forEach((x) => {
    const combo = findCombo(x?.communities);
    if (combo && combos.every((y) => y.data.label !== combo)) {
      combos.push({
        isCombo: true,
        id: uuid(),
        data: {
          label: combo,
        },
      });
    }
  });

  const nextNodes = nodes.map((x) => {
    return {
      ...x,
      combo: combos.find((y) => y.data.label === findCombo(x?.communities))?.id,
    };
  });
  if (!combos.length) {
    const defaultComboId = uuid();
    const defaultCombo = {
      id: defaultComboId,
      label: 'defaultCombo',
      data: {
        label: 'defaultCombo',
      },
    };

    combos.push(defaultCombo);
  }

  return { nodes: nextNodes, combos };
};
