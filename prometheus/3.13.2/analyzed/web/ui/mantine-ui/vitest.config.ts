// Mantine UI 子项目的 Vitest 配置：启用 React 插件、jsdom 环境与全局测试 API。

import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./src/setupTests.ts",
  },
});
