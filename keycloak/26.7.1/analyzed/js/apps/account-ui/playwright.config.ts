import { type ViewportSize, defineConfig, devices } from "@playwright/test";

/** Account UI 端到端测试使用的固定视口尺寸（1920×1080）。 */
const viewport: ViewportSize = { width: 1920, height: 1080 };

/**
 * Account UI 的 Playwright 测试配置。
 * 详见 https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: "./test",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  reporter: process.env.CI ? [["github"], ["html"]] : "list",

  use: {
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
