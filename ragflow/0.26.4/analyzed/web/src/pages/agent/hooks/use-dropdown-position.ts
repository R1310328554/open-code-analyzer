// use-dropdown-position.ts — 占位节点旁下拉菜单的屏幕/画布坐标换算与偏移计算。

import { ReactFlowInstance } from '@xyflow/react';
import { useCallback } from 'react';
import {
  DROPDOWN_HORIZONTAL_OFFSET,
  DROPDOWN_VERTICAL_OFFSET,
  HALF_PLACEHOLDER_NODE_WIDTH,
} from '../constant';

/**
 * 下拉位置计算 Hook：基于占位节点半宽与常量偏移换算屏幕坐标。
 * Dropdown position calculation Hook
 * Responsible for calculating dropdown menu position relative to placeholder node
 */
export const useDropdownPosition = (
  reactFlowInstance?: ReactFlowInstance<any, any>,
) => {
  /**
   * 计算下拉菜单屏幕坐标：屏幕→画布→加偏移→再转回屏幕。
   * Calculate dropdown menu position
   * @param clientX Mouse click screen X coordinate
   * @param clientY Mouse click screen Y coordinate
   * @returns Dropdown menu screen coordinates
   */
  const calculateDropdownPosition = useCallback(
    (clientX: number, clientY: number) => {
      if (!reactFlowInstance) {
        return { x: clientX, y: clientY };
      }

      // 屏幕坐标转为 React Flow 画布坐标
      // Convert screen coordinates to flow coordinates
      const placeholderNodePosition = reactFlowInstance.screenToFlowPosition({
        x: clientX,
        y: clientY,
      });

      // 在画布坐标系中加上半宽与水平/垂直偏移
      // Calculate dropdown position in flow coordinate system
      const dropdownFlowPosition = {
        x:
          placeholderNodePosition.x +
          HALF_PLACEHOLDER_NODE_WIDTH +
          DROPDOWN_HORIZONTAL_OFFSET,
        y: placeholderNodePosition.y - DROPDOWN_VERTICAL_OFFSET,
      };

      // 画布坐标转回屏幕坐标供 Portal 定位
      // Convert flow coordinates back to screen coordinates
      const dropdownScreenPosition =
        reactFlowInstance.flowToScreenPosition(dropdownFlowPosition);

      return {
        x: dropdownScreenPosition.x,
        y: dropdownScreenPosition.y,
      };
    },
    [reactFlowInstance],
  );

  /** 将鼠标松手处的屏幕坐标转为占位节点的画布坐标。 */
  /**
   * Calculate placeholder node flow coordinate position
   * @param clientX Mouse click screen X coordinate
   * @param clientY Mouse click screen Y coordinate
   * @returns Placeholder node flow coordinates
   */
  const getPlaceholderNodePosition = useCallback(
    (clientX: number, clientY: number) => {
      if (!reactFlowInstance) {
        return { x: clientX, y: clientY };
      }

      return reactFlowInstance.screenToFlowPosition({
        x: clientX,
        y: clientY,
      });
    },
    [reactFlowInstance],
  );

  /** 画布坐标 → 屏幕坐标（无实例时原样返回）。 */
  /**
   * Convert flow coordinates to screen coordinates
   * @param flowPosition Flow coordinates
   * @returns Screen coordinates
   */
  const flowToScreenPosition = useCallback(
    (flowPosition: { x: number; y: number }) => {
      if (!reactFlowInstance) {
        return flowPosition;
      }

      return reactFlowInstance.flowToScreenPosition(flowPosition);
    },
    [reactFlowInstance],
  );

  /** 屏幕坐标 → 画布坐标（无实例时原样返回）。 */
  /**
   * Convert screen coordinates to flow coordinates
   * @param screenPosition Screen coordinates
   * @returns Flow coordinates
   */
  const screenToFlowPosition = useCallback(
    (screenPosition: { x: number; y: number }) => {
      if (!reactFlowInstance) {
        return screenPosition;
      }

      return reactFlowInstance.screenToFlowPosition(screenPosition);
    },
    [reactFlowInstance],
  );

  return {
    calculateDropdownPosition,
    getPlaceholderNodePosition,
    flowToScreenPosition,
    screenToFlowPosition,
  };
};
