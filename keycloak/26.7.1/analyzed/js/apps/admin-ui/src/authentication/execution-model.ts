import type AuthenticationExecutionInfoRepresentation from "@keycloak/keycloak-admin-client/lib/defs/authenticationExecutionInfoRepresentation";

/** 可展开树形结构的认证执行步骤，子步骤列表与折叠状态由 UI 维护。 */
export type ExpandableExecution = AuthenticationExecutionInfoRepresentation & {
  executionList?: ExpandableExecution[];
  isCollapsed: boolean;
};

/** 同一父级下仅发生排序变化（index 变更）。 */
export class IndexChange {
  oldIndex: number;
  newIndex: number;

  constructor(oldIndex: number, newIndex: number) {
    this.oldIndex = oldIndex;
    this.newIndex = newIndex;
  }
}

/** 跨层级移动：除 index 外还记录新的父节点（顶层时 parent 为 undefined）。 */
export class LevelChange extends IndexChange {
  parent?: ExpandableExecution;

  constructor(
    oldIndex: number,
    newIndex: number,
    parent?: ExpandableExecution,
  ) {
    super(oldIndex, newIndex);
    this.parent = parent;
  }
}

/**
 * 认证流执行步骤的扁平列表与树形视图之间的转换与拖拽变更计算。
 * level/index 来自 Keycloak Admin API，expandableList 供可折叠 UI 使用。
 */
export class ExecutionList {
  #list: ExpandableExecution[];
  expandableList: ExpandableExecution[];

  constructor(list: AuthenticationExecutionInfoRepresentation[]) {
    this.#list = list as ExpandableExecution[];

    const exList = {
      executionList: [],
      isCollapsed: false,
    };
    this.#transformToExpandableList(0, -1, exList);
    this.expandableList = exList.executionList;
  }

  /** 将扁平列表按 level 递归构造成嵌套 executionList。 */
  #transformToExpandableList(
    currentIndex: number,
    currentLevel: number,
    execution: ExpandableExecution,
  ) {
    for (let index = currentIndex; index < this.#list.length; index++) {
      const ex = this.#list[index];
      const level = ex.level || 0;
      if (level <= currentLevel) {
        return index - 1;
      }

      const nextRowLevel = this.#list[index + 1]?.level || 0;
      const hasChild = level < nextRowLevel;

      if (hasChild) {
        const subLevel = { ...ex, executionList: [], isCollapsed: false };
        index = this.#transformToExpandableList(index + 1, level, subLevel);
        execution.executionList?.push(subLevel);
      } else {
        execution.executionList?.push(ex);
      }
    }
    return this.#list.length;
  }

  /** 深度优先展开树，得到与 UI 展示顺序一致的扁平列表（跳过已折叠分支）。 */
  order(list?: ExpandableExecution[]) {
    let result: ExpandableExecution[] = [];
    for (const row of list || this.expandableList) {
      result.push(row);
      if (row.executionList && !row.isCollapsed) {
        result = result.concat(this.order(row.executionList));
      }
    }
    return result;
  }

  /** 按展开后的视觉顺序查找第 index 个执行步骤。 */
  findExecution(
    index: number,
    current: { index: number } = { index: 0 },
    list?: ExpandableExecution[],
  ): ExpandableExecution | undefined {
    const l = list || this.expandableList;
    for (let i = 0; i < l.length; i++) {
      const ex = l[i];

      if (current.index === index) {
        return ex;
      }
      current.index++;
      if (ex.executionList && !ex.isCollapsed) {
        const found = this.findExecution(index, current, ex.executionList);
        if (found) {
          return found;
        }
      }
    }
    return undefined;
  }

  /** 在扁平 #list 中查找指定 level 与 index 对应行的直接父节点。 */
  #getParentNodes(level: number, index: number) {
    let parent = undefined;
    for (let i = 0; i < index; i++) {
      const ex = this.#list[i];
      if (level - 1 === ex.level) {
        parent = ex;
      }
    }
    return parent;
  }

  /**
   * 比较拖拽前后 id 顺序，返回 IndexChange 或 LevelChange。
   * @param changed 被移动的执行步骤
   * @param order 拖拽后的 id 顺序
   */
  getChange(
    changed: AuthenticationExecutionInfoRepresentation,
    order: string[],
  ) {
    const currentOrder = this.order();
    const newLocIndex = order.findIndex((id) => id === changed.id);
    const oldLocIndex = currentOrder.findIndex((ex) => ex.id === changed.id);
    const oldLocation = currentOrder[oldLocIndex];
    const newLocation = currentOrder[newLocIndex];

    const currentParent = this.#getParentNodes(oldLocation.level!, oldLocIndex);
    const parent = this.#getParentNodes(newLocation.level!, newLocIndex);
    if (currentParent?.id !== parent?.id) {
      if (newLocation.level! > 0) {
        return new LevelChange(
          parent?.executionList?.length || 0,
          newLocation.index!,
          parent,
        );
      }
      return new LevelChange(this.expandableList.length, newLocation.index!);
    }

    return new IndexChange(oldLocation.index!, newLocation.index!);
  }

  /** 浅克隆：共享底层 #list 与 expandableList 引用。 */
  clone() {
    const newList = new ExecutionList([]);
    newList.#list = this.#list;
    newList.expandableList = this.expandableList;
    return newList;
  }
}
