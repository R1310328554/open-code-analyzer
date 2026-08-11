// paddleocr-js monorepo Vitest 配置：jsdom 环境与 core 包 v8 覆盖率
import { defineConfig } from "vitest/config";

  // 默认测试环境 jsdom，coverage 覆盖 packages/core/src 下 TS 源码
export default defineConfig({
  test: {
    environment: "jsdom",
    coverage: {
      provider: "v8",
      include: ["packages/core/src/**/*.ts"]
    }
  }
});
