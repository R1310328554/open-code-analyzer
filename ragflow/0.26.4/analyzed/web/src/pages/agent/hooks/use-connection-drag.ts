// use-connection-drag.ts — 画布连线拖拽：占位节点创建、下拉选节点与点击/拖拽区分。

import {
  Connection,
  OnConnectEnd,
  OnConnectStart,
  Position,
  ReactFlowInstance,
} from '@xyflow/react';
import { useCallback, useRef } from 'react';
import { useDropdownManager } from '../canvas/context';
import { Operator, PREVENT_CLOSE_DELAY } from '../constant';
import { useAddNode } from './use-add-node';

/** 连线起点缓存：源节点 ID 与 handle ID。 */
interface ConnectionStartParams {
  nodeId: string;
  handleId: string;
}

/**
 * 连线拖拽管理 Hook：处理拖拽起止、占位节点与节点类型下拉。
 * Connection drag management Hook
 * Responsible for handling connection drag start and end logic
 */
export const useConnectionDrag = (
  onConnect: (connection: Connection) => void,
  showModal: () => void,
  hideModal: () => void,
  setDropdownPosition: (position: { x: number; y: number }) => void,
  setCreatedPlaceholderRef: (nodeId: string | null) => void,
  calculateDropdownPosition: (
    clientX: number,
    clientY: number,
  ) => { x: number; y: number },
  removePlaceholderNode: () => void,
  clearActiveDropdown: () => void,
  checkAndRemoveExistingPlaceholder: () => void,
  reactFlowInstance?: ReactFlowInstance<any, any>,
) => {
  // 是否已成功建立连线
  // Reference for whether connection is established
  const isConnectedRef = useRef(false);
  // 连线起点参数
  // Reference for connection start parameters
  const connectionStartRef = useRef<ConnectionStartParams | null>(null);
  // 防止下拉菜单刚打开即被关闭
  // Reference to prevent immediate close
  const preventCloseRef = useRef(false);
  // 记录鼠标起点以区分点击 handle 与拖拽连线
  // Reference to track mouse position for click detection
  const mouseStartPosRef = useRef<{ x: number; y: number } | null>(null);

  const { addCanvasNode } = useAddNode(reactFlowInstance);
  const { setActiveDropdown } = useDropdownManager();

  /** 连线开始：重置连接标志并记录起点坐标与 handle 信息。 */
  /**
   * Connection start handler function
   */
  const onConnectStart: OnConnectStart = useCallback((event, params) => {
    isConnectedRef.current = false;

    // 记录鼠标起点，用于后续判断点击还是拖拽
    // Record mouse start position to detect click vs drag
    if ('clientX' in event && 'clientY' in event) {
      mouseStartPosRef.current = { x: event.clientX, y: event.clientY };
    }

    if (params && params.nodeId && params.handleId) {
      connectionStartRef.current = {
        nodeId: params.nodeId,
        handleId: params.handleId,
      };
    } else {
      connectionStartRef.current = null;
    }
  }, []);

  /** 连线结束：未连上目标时在松手处创建占位节点并弹出节点类型下拉。 */
  /**
   * Connection end handler function
   */
  const onConnectEnd: OnConnectEnd = useCallback(
    (event) => {
      if ('clientX' in event && 'clientY' in event) {
        const { clientX, clientY } = event;
        setDropdownPosition({ x: clientX, y: clientY });

        if (!isConnectedRef.current && connectionStartRef.current) {
          // 移动距离小于 5px 视为点击 handle，不创建占位节点
          // Check mouse movement distance to distinguish click from drag
          let isHandleClick = false;
          if (mouseStartPosRef.current) {
            const movementDistance = Math.sqrt(
              Math.pow(clientX - mouseStartPosRef.current.x, 2) +
                Math.pow(clientY - mouseStartPosRef.current.y, 2),
            );
            isHandleClick = movementDistance < 5; // 5px 内视为 handle 点击
            // Consider clicks within 5px as handle clicks
          }

          if (isHandleClick) {
            removePlaceholderNode();
            hideModal();
            clearActiveDropdown();
            connectionStartRef.current = null;
            mouseStartPosRef.current = null;
            return;
          }

          // 创建新占位前先清理已有 placeholder
          // Check and remove existing placeholder-node before creating new one
          checkAndRemoveExistingPlaceholder();

          // 在松手位置创建 Placeholder 节点并准备连线
          // Create placeholder node and establish connection
          const mockEvent = { clientX, clientY };
          const contextData = {
            nodeId: connectionStartRef.current.nodeId,
            id: connectionStartRef.current.handleId,
            type: 'source' as const,
            position: Position.Right,
            isFromConnectionDrag: true,
          };

          // 以 Placeholder 算子创建临时节点
          // Use Placeholder operator to create node
          const newNodeId = addCanvasNode(
            Operator.Placeholder,
            contextData,
          )(mockEvent);

          if (newNodeId) {
            setCreatedPlaceholderRef(newNodeId);
          }

          // 换算下拉屏幕坐标并打开节点选择菜单
          // Calculate placeholder node position and display dropdown menu
          if (newNodeId && reactFlowInstance) {
            const dropdownScreenPosition = calculateDropdownPosition(
              clientX,
              clientY,
            );

            setDropdownPosition({
              x: dropdownScreenPosition.x,
              y: dropdownScreenPosition.y,
            });

            setActiveDropdown('drag');
            showModal();
            preventCloseRef.current = true;
            setTimeout(() => {
              preventCloseRef.current = false;
            }, PREVENT_CLOSE_DELAY);
          }

          // 重置连线上下文
          // Reset connection state
          connectionStartRef.current = null;
          mouseStartPosRef.current = null;
        }
      }
    },
    [
      setDropdownPosition,
      checkAndRemoveExistingPlaceholder,
      addCanvasNode,
      reactFlowInstance,
      removePlaceholderNode,
      hideModal,
      clearActiveDropdown,
      setCreatedPlaceholderRef,
      calculateDropdownPosition,
      setActiveDropdown,
      showModal,
    ],
  );

  /** 成功连线上目标节点时标记 isConnectedRef。 */
  /**
   * Connection establishment handler function
   */
  const handleConnect = useCallback(
    (connection: Connection) => {
      onConnect(connection);
      isConnectedRef.current = true;
    },
    [onConnect],
  );

  /** 供 addNode 使用的连线起点上下文（含 isFromConnectionDrag）。 */
  /**
   * Get connection start context data
   */
  const getConnectionStartContext = useCallback(() => {
    if (!connectionStartRef.current) {
      return null;
    }

    return {
      nodeId: connectionStartRef.current.nodeId,
      id: connectionStartRef.current.handleId,
      type: 'source' as const,
      position: Position.Right,
      isFromConnectionDrag: true,
    };
  }, []);

  /** 下拉打开后的短暂窗口内禁止立即关闭。 */
  /**
   * Check if close should be prevented
   */
  const shouldPreventClose = useCallback(() => {
    return preventCloseRef.current;
  }, []);

  /** 画布平移/缩放时移除占位节点并关闭下拉。 */
  /**
   * Handle canvas move/zoom events
   * Hide dropdown and remove placeholder when user scrolls or moves canvas
   */
  const onMove = useCallback(() => {
    // 平移缩放时清理占位与下拉
    // Clean up placeholder and dropdown when canvas moves/zooms
    removePlaceholderNode();
    hideModal();
    clearActiveDropdown();
  }, [removePlaceholderNode, hideModal, clearActiveDropdown]);

  return {
    nodeId: connectionStartRef.current?.nodeId,
    onConnectStart,
    onConnectEnd,
    handleConnect,
    getConnectionStartContext,
    shouldPreventClose,
    onMove,
  };
};
