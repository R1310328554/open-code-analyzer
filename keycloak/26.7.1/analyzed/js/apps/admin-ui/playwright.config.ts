import { type ViewportSize, defineConfig, devices } from "@playwright/test";

/** 端到端测试统一视口尺寸（全高清）。 */
const viewport: ViewportSize = { width: 1920, height: 1080 };

/**
 * Admin Console Playwright 测试配置。
 * 详见 https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: "./test",
  fullyParallel: true,
  // 管理控制台测试尚未针对并行执行优化，长期应恢复 workers 默认值
  workers: 1,
  forbidOnly: !!process.env.CI,
  reporter: process.env.CI ? [["github"], ["html"]] : "list",

  use: {
    // 失败时保留 trace，便于在 Playwright 报告中回放
    trace: "retain-on-failure",
  },

  projects: [
    {
      name: "chromium",
      use: {
        ...devices["Desktop Chrome"],
        viewport,
      },
    },
    {
      name: "firefox",
      use: {
        ...devices["Desktop Firefox"],
        viewport,
      },
    },
  ],
});
