// use-placeholder-manager.ts — 占位节点生命周期：创建、去重、连边替换与用户取消清理。

import { pick } from 'lodash';
import { useCallback, useRef } from 'react';
import { Operator } from '../constant';
import useGraphStore from '../store';

/**
 * 占位节点管理 Hook
 * 负责占位节点的创建、删除与状态追踪（画布拖拽连边时的临时节点）
 * Placeholder node management Hook
 * Responsible for managing placeholder node creation, deletion, and state tracking
 */
export const usePlaceholderManager = (reactFlowInstance: any) => {
  // 当前画布上占位节点的 ID 引用
  // Reference to the created placeholder node ID
  const createdPlaceholderRef = useRef<string | null>(null);
  // 标记用户是否已从下拉菜单选定真实节点类型
  // Flag indicating whether user has selected a node
  const userSelectedNodeRef = useRef(false);

  /**
   * 检查并移除已存在的占位节点，确保画布上最多只有一个占位节点
   * Check if placeholder node exists and remove it if found
   * Ensures only one placeholder can exist on the panel
   */
  const checkAndRemoveExistingPlaceholder = useCallback(() => {
    const { nodes, edges } = useGraphStore.getState();

    // 按 label === Operator.Placeholder 查找已有占位节点
    // Find existing placeholder node
    const existingPlaceholder = nodes.find(
      (node) => node.data?.label === Operator.Placeholder,
    );

    if (existingPlaceholder && reactFlowInstance) {
      // Remove edges related to placeholder
      const edgesToRemove = edges.filter(
        (edge) =>
          edge.target === existingPlaceholder.id ||
          edge.source === existingPlaceholder.id,
      );

      // Remove placeholder node
      const nodesToRemove = [existingPlaceholder];

      if (nodesToRemove.length > 0 || edgesToRemove.length > 0) {
        reactFlowInstance.deleteElements({
          nodes: nodesToRemove,
          edges: edgesToRemove,
        });
      }

      // Update ref reference
      if (createdPlaceholderRef.current === existingPlaceholder.id) {
        createdPlaceholderRef.current = null;
      }
    }
  }, [reactFlowInstance]);

  /**
   * 移除占位节点，在用户点击空白区域或取消操作时调用
   * Function to remove placeholder node
   * Called when user clicks blank area or cancels operation
   */
  const removePlaceholderNode = useCallback(() => {
    if (
      createdPlaceholderRef.current &&
      reactFlowInstance &&
      !userSelectedNodeRef.current
    ) {
      const { nodes, edges } = useGraphStore.getState();

      // Remove edges related to placeholder
      const edgesToRemove = edges.filter(
        (edge) =>
          edge.target === createdPlaceholderRef.current ||
          edge.source === createdPlaceholderRef.current,
      );

      // Remove placeholder node
      const nodesToRemove = nodes.filter(
        (node) => node.id === createdPlaceholderRef.current,
      );

      if (nodesToRemove.length > 0 || edgesToRemove.length > 0) {
        reactFlowInstance.deleteElements({
          nodes: nodesToRemove,
          edges: edgesToRemove,
        });
      }

      createdPlaceholderRef.current = null;
    }

    // Reset user selection flag
    userSelectedNodeRef.current = false;
  }, [reactFlowInstance]);

  /**
   * 用户选定节点类型后的回调：继承占位位置/连边后删除占位
   * User node selection callback
   * Called when user selects a node type from dropdown menu
   */
  const onNodeCreated = useCallback(
    (newNodeId: string) => {
      // 先将新节点与源节点连边并继承占位位置，再删除占位
      // First establish connection between new node and source, then delete placeholder
      if (createdPlaceholderRef.current && reactFlowInstance) {
        const { nodes, edges, addEdge, updateNode } = useGraphStore.getState();

        // Find placeholder node to get its position
        const placeholderNode = nodes.find(
          (node) => node.id === createdPlaceholderRef.current,
        );

        // Find placeholder-related connection and get source node info
        const placeholderEdge = edges.find(
          (edge) => edge.target === createdPlaceholderRef.current,
        );

        // Update new node position to match placeholder position
        if (placeholderNode) {
          const newNode = nodes.find((node) => node.id === newNodeId);
          if (newNode) {
            updateNode({
              ...newNode,
              ...pick(placeholderNode, ['position', 'parentId', 'extent']),
            });
          }
        }

        if (placeholderEdge) {
          // Establish connection between new node and source node
          addEdge({
            source: placeholderEdge.source,
            target: newNodeId,
            sourceHandle: placeholderEdge.sourceHandle || null,
            targetHandle: placeholderEdge.targetHandle || null,
          });
        }

        // Remove placeholder node and related connections
        const edgesToRemove = edges.filter(
          (edge) =>
            edge.target === createdPlaceholderRef.current ||
            edge.source === createdPlaceholderRef.current,
        );

        const nodesToRemove = nodes.filter(
          (node) => node.id === createdPlaceholderRef.current,
        );

        if (nodesToRemove.length > 0 || edgesToRemove.length > 0) {
          reactFlowInstance.deleteElements({
            nodes: nodesToRemove,
            edges: edgesToRemove,
          });
        }
      }

      // Mark that user has selected a node
      userSelectedNodeRef.current = true;
      createdPlaceholderRef.current = null;
    },
    [reactFlowInstance],
  );

  /**
   * 记录新创建的占位节点 ID
   * Set the created placeholder node ID
   */
  const setCreatedPlaceholderRef = useCallback((nodeId: string | null) => {
    createdPlaceholderRef.current = nodeId;
  }, []);

  /**
   * 重置用户已选节点标记
   * Reset user selection flag
   */
  const resetUserSelectedFlag = useCallback(() => {
    userSelectedNodeRef.current = false;
  }, []);

  return {
    removePlaceholderNode,
    onNodeCreated,
    setCreatedPlaceholderRef,
    resetUserSelectedFlag,
    checkAndRemoveExistingPlaceholder,
    createdPlaceholderRef: createdPlaceholderRef.current,
    userSelectedNodeRef: userSelectedNodeRef.current,
  };
};
