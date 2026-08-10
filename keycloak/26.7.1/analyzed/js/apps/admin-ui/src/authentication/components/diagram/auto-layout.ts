import { graphlib, layout } from "@dagrejs/dagre";
import { Edge, Node, Position } from "reactflow";

/** Dagre 有向图实例，用于认证流拓扑自动布局。 */
const dagreGraph = new graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

/** 普通节点宽度（像素）。 */
const nodeWidth = 130;
/** 普通节点高度（像素）。 */
const nodeHeight = 40;
/** 条件节点后下一节点需预留的额外垂直空间。 */
const nodeAfterConditionalHeight = 130;

/**
 * 使用 Dagre 为 React Flow 节点计算坐标。
 * @param nodes 待布局节点列表
 * @param direction 布局方向，默认从左到右（LR）
 */
export const getLayoutedNodes = (nodes: Node[], direction = "LR"): Node[] => {
  const isHorizontal = direction === "LR";
  dagreGraph.setGraph({ rankdir: direction });

  nodes.forEach((element, index) => {
    const prevNode = index > 0 ? nodes[index - 1] : undefined;
    dagreGraph.setNode(element.id, {
      width: nodeWidth,
      height:
        prevNode?.type === "conditional"
          ? nodeAfterConditionalHeight
          : nodeHeight,
    });
  });

  layout(dagreGraph);

  return nodes.map((node) => {
    const nodeWithPosition = dagreGraph.node(node.id);
    // 水平布局时连线从左右两侧进出，垂直布局时从上下进出
    node.targetPosition = isHorizontal ? Position.Left : Position.Top;
    node.sourcePosition = isHorizontal ? Position.Right : Position.Bottom;

    // Dagre 返回中心点，转换为 React Flow 所需的左上角坐标
    node.position = {
      x: nodeWithPosition.x - nodeWidth / 2,
      y: nodeWithPosition.y - nodeHeight / 2,
    };

    return node;
  });
};

/**
 * 在 Dagre 中注册边并触发布局（边本身不携带坐标，布局由节点位置决定）。
 */
export const getLayoutedEdges = (edges: Edge[], direction = "LR"): Edge[] => {
  dagreGraph.setGraph({ rankdir: direction });

  edges.forEach((element) => {
    dagreGraph.setEdge(element.source, element.target);
  });

  layout(dagreGraph);

  return edges;
};
