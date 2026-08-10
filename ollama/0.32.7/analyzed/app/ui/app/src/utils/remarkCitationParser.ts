/**
 * Remark 插件：将 Markdown 文本中的引用标记解析为自定义 AST 节点，供 rehype-react 渲染为 Citation 组件。
 */
import { visit } from "unist-util-visit";
import type { Root, RootContent } from "mdast";

/** 默认导出：返回 remark 转换函数，分两遍扫描处理行号引用与通用引用，并去重相邻相同引用。 */
export default function remarkMyDelimiter() {
  return (tree: Root) => {
    // 第一遍：将文本节点中的引用语法拆分为 custom-citation 节点
    visit(tree, "text", (node, index, parent) => {
      // 示例：【1†L25-L30】 解析为 cursor: 1, start: 25, end: 30
      const regex = /【(\d+)†L(\d+)-L(\d+)】/g;
      let match;
      let last = 0;
      const pieces: RootContent[] = [];

      while ((match = regex.exec(node.value))) {
        // 分隔符前的普通文本
        if (match.index > last) {
          pieces.push({
            type: "text",
            value: node.value.slice(last, match.index),
          });
        }
        // 带行号范围的引用 → 自定义节点
        pieces.push({
          // @ts-expect-error: custom type
          type: "custom-citation" as const,
          data: {
            // 告知 rehype/rehype-react 渲染为 <Citation>（ol-citation）
            hName: "ol-citation",
            hProperties: {
              cursor: match[1],
              start: match[2],
              end: match[3],
            },
          },
        });
        last = match.index + match[0].length;
      }

      // 处理剩余文本中的通用引用，如 【1†...】
      const remaining = node.value.slice(last);
      const generic = /【(\d+)†[^】]*】/g;
      let gLast = 0;
      while ((match = generic.exec(remaining))) {
        if (match.index > gLast) {
          pieces.push({
            type: "text",
            value: remaining.slice(gLast, match.index),
          });
        }
        pieces.push({
          // @ts-expect-error: custom type
          type: "custom-citation" as const,
          data: {
            hName: "ol-citation",
            hProperties: {
              cursor: match[1],
            },
          },
        });
        gLast = match.index + match[0].length;
      }

      // 通用引用匹配后的尾部文本
      if (gLast < remaining.length) {
        pieces.push({ type: "text", value: remaining.slice(gLast) });
      }

      if (pieces.length) {
        parent?.children?.splice(index ?? 0, 1, ...pieces);
        return (index ?? 0) + pieces.length;
      }
    });

    // 第二遍：移除 cursor 相同的相邻重复引用节点
    visit(tree, (node, index, parent) => {
      if (
        parent &&
        parent.children &&
        index !== null &&
        index !== undefined &&
        index > 0
      ) {
        const currentNode = node as any;
        const prevNode = parent.children[index - 1] as any;

        // 连续两个 custom-citation 且 cursor 相同时删除后者
        if (
          currentNode.type === "custom-citation" &&
          prevNode.type === "custom-citation" &&
          currentNode.data?.hProperties?.cursor ===
            prevNode.data?.hProperties?.cursor
        ) {
          parent.children.splice(index, 1);
          return index;
        }
      }
    });
  };
}
