/**
 * Ollama 桌面 React UI 的 Vite 构建配置：路由插件、Tailwind、路径别名与 Safari 14 兼容 PostCSS。
 */
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { TanStackRouterVite } from "@tanstack/router-plugin/vite";
import tailwindcss from "@tailwindcss/vite";
import tsconfigPaths from "vite-tsconfig-paths";
import postcssPresetEnv from "postcss-preset-env";
import { resolve } from "path";

export default defineConfig(() => ({
  base: "/",

  plugins: [
    TanStackRouterVite({ target: "react" }),
    react(),
    tailwindcss(),
    tsconfigPaths(),
  ],

  resolve: {
    alias: {
      "@/gotypes": resolve(__dirname, "codegen/gotypes.gen.ts"),
      "@": resolve(__dirname, "src"),
      "micromark-extension-math": "micromark-extension-llm-math",
    },
  },

  css: {
    postcss: {
      plugins: [
        postcssPresetEnv({
          stage: 1, // 启用 Safari 14 所需的更多实验性 CSS 特性
          browsers: ["Safari >= 14"],
          // autoprefixer: false,
          features: {
            "custom-properties": true, // 由 TailwindCSS 处理 CSS 变量
            "nesting-rules": true,
            "logical-properties-and-values": true, // 逻辑属性 polyfill
            "media-query-ranges": true, // 现代媒体查询范围语法
            "color-function": true, // CSS 颜色函数
            "double-position-gradients": true,
            "gap-properties": true, // flexbox gap 支持（Safari 14 关键）
            "place-properties": true,
            "overflow-property": true,
            "focus-visible-pseudo-class": true, // :focus-visible
            "focus-within-pseudo-class": true, // :focus-within
            "any-link-pseudo-class": true, // :any-link
            "not-pseudo-class": true, // 增强 :not() 支持
            "dir-pseudo-class": true, // :dir()
            "all-property": true, // CSS all 属性
            "image-set-function": true, // image-set()
            "hwb-function": true, // hwb() 颜色
            "lab-function": true, // lab() 颜色
            "oklab-function": true, // oklab() 颜色
          },
        }),
      ],
    },
  },

  build: {
    target: "es2017",
  },

  esbuild: {
    target: "es2017",
  },
}));
