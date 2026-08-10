// markdown-remark-plugins.ts — Markdown 渲染用 remark 插件组合：GFM、数学公式与换行。

import remarkBreaks from 'remark-breaks';
import remarkGfm from 'remark-gfm';
import remarkMath from 'remark-math';

/**
 * 轻量 Markdown 管道：GFM + 硬换行，不含 TeX（用于未接入 rehype-katex 的文档预览等场景）。
 */
/** 不含 remark-math 的 remark 插件列表。 */
export const MarkdownRemarkPluginsLite = [remarkGfm, remarkBreaks];

/**
 * 助手消息等场景的完整 Markdown 管道：
 * - remark-gfm：表格、任务列表、删除线、自动链接等 GFM 特性
 * - remark-math：TeX（$...$ / $$...$$），渲染端需配合 rehype-katex
 * - remark-breaks：单行换行视为硬换行（LLM 聊天常见）
 */
/** 含 GFM、数学公式与硬换行的完整 remark 插件列表。 */
export const MarkdownRemarkPlugins = [remarkGfm, remarkMath, remarkBreaks];
