// use-agent-history-manager.ts — 画布 undo/redo：HistoryManager 栈与 Ctrl/Cmd+Z 快捷键。

import { useEffect, useRef } from 'react';
import useGraphStore from './store';

// 画布图状态历史栈，支持撤销/重做
// History management class
/** 维护 nodes/edges 快照栈，上限 maxSize 条，避免重复 push 相同状态。 */
export class HistoryManager {
  private history: { nodes: any[]; edges: any[] }[] = [];
  private currentIndex: number = -1;
  private readonly maxSize: number = 50; // 历史记录上限
  // Limit maximum number of history records
  private setNodes: (nodes: any[]) => void;
  private setEdges: (edges: any[]) => void;
  private lastSavedState: string = ''; // 上次序列化快照，用于去重
  // Used to compare if state has changed

  constructor(
    setNodes: (nodes: any[]) => void,
    setEdges: (edges: any[]) => void,
  ) {
    this.setNodes = setNodes;
    this.setEdges = setEdges;
  }

  /** 通过 JSON 序列化比较两帧图状态是否相同。 */
  // Compare if two states are equal
  private statesEqual(
    state1: { nodes: any[]; edges: any[] },
    state2: { nodes: any[]; edges: any[] },
  ): boolean {
    return JSON.stringify(state1) === JSON.stringify(state2);
  }

  /** 深拷贝当前图并入栈；若与当前帧相同则跳过。 */
  push(nodes: any[], edges: any[]) {
    const currentState = {
      nodes: JSON.parse(JSON.stringify(nodes)),
      edges: JSON.parse(JSON.stringify(edges)),
    };

    // 状态未变化时不重复入栈
    // If state hasn't changed, don't save
    if (
      this.history.length > 0 &&
      this.statesEqual(currentState, this.history[this.currentIndex])
    ) {
      return;
    }

    // 撤销后再编辑时截断 redo 分支
    // If current index is not at the end of history, remove subsequent states
    if (this.currentIndex < this.history.length - 1) {
      this.history.splice(this.currentIndex + 1);
    }

    // Add current state
    this.history.push(currentState);

    // Limit history record size
    if (this.history.length > this.maxSize) {
      this.history.shift();
      this.currentIndex = this.history.length - 1;
    } else {
      this.currentIndex = this.history.length - 1;
    }

    // Update last saved state
    this.lastSavedState = JSON.stringify(currentState);
  }

  /** 回退到上一帧并写回 store。 */
  undo() {
    if (this.canUndo()) {
      this.currentIndex--;
      const prevState = this.history[this.currentIndex];
      this.setNodes(JSON.parse(JSON.stringify(prevState.nodes)));
      this.setEdges(JSON.parse(JSON.stringify(prevState.edges)));
      return true;
    }
    return false;
  }

  /** 前进到下一帧并写回 store。 */
  redo() {
    console.log('redo');
    if (this.canRedo()) {
      this.currentIndex++;
      const nextState = this.history[this.currentIndex];
      this.setNodes(JSON.parse(JSON.stringify(nextState.nodes)));
      this.setEdges(JSON.parse(JSON.stringify(nextState.edges)));
      return true;
    }
    return false;
  }

  canUndo() {
    return this.currentIndex > 0;
  }

  canRedo() {
    return this.currentIndex < this.history.length - 1;
  }

  /** 清空历史栈（如加载新 DSL 时）。 */
  // Reset history records
  reset() {
    this.history = [];
    this.currentIndex = -1;
    this.lastSavedState = '';
  }
}

/** 订阅 nodes/edges 自动 push，并注册全局 undo/redo 快捷键。 */
export const useAgentHistoryManager = () => {
  // 从 store 读取图状态与 setNodes/setEdges
  // Get current state and history state
  const nodes = useGraphStore((state) => state.nodes);
  const edges = useGraphStore((state) => state.edges);
  const setNodes = useGraphStore((state) => state.setNodes);
  const setEdges = useGraphStore((state) => state.setEdges);

  // useRef 保证 HistoryManager 单例跨渲染复用
  // Use useRef to keep HistoryManager instance unchanged
  const historyManagerRef = useRef<HistoryManager | null>(null);

  // 首次渲染时构造 HistoryManager
  // Initialize HistoryManager
  if (!historyManagerRef.current) {
    historyManagerRef.current = new HistoryManager(setNodes, setEdges);
  }

  const historyManager = historyManagerRef.current;

  // nodes/edges 变化时 push 历史（useEffect 避免多余渲染）
  // Save state history - use useEffect instead of useMemo to avoid re-rendering
  useEffect(() => {
    historyManager.push(nodes, edges);
  }, [nodes, edges, historyManager]);

  // 监听 Ctrl/Cmd+Z 与 Ctrl/Cmd+Shift+Z
  // Keyboard event handling
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // 输入框聚焦时不拦截快捷键
      // Check if focused on an input element
      const activeElement = document.activeElement;
      const isInputFocused =
        activeElement instanceof HTMLInputElement ||
        activeElement instanceof HTMLTextAreaElement ||
        activeElement?.hasAttribute('contenteditable');

      // 用户在表单内输入时跳过 undo/redo
      // Skip keyboard shortcuts if typing in an input field
      if (isInputFocused) {
        return;
      }
      // Ctrl+Z / Cmd+Z：撤销
      // Ctrl+Z or Cmd+Z undo
      if (
        (e.ctrlKey || e.metaKey) &&
        (e.key === 'z' || e.key === 'Z') &&
        !e.shiftKey
      ) {
        e.preventDefault();
        historyManager.undo();
      }
      // Ctrl+Shift+Z / Cmd+Shift+Z：重做
      // Ctrl+Shift+Z or Cmd+Shift+Z redo
      else if (
        (e.ctrlKey || e.metaKey) &&
        (e.key === 'z' || e.key === 'Z') &&
        e.shiftKey
      ) {
        e.preventDefault();
        historyManager.redo();
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [historyManager]);
};
