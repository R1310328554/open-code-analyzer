// jest-puppeteer 配置：启动 Chrome 与本地 Python HTTP 静态服务
// jest-puppeteer.config.js
module.exports = {
    // Puppeteer 启动选项：非 headless 便于调试
    launch: {
        headless: false,
        product: 'chrome'
    },
    browserContext: 'default',
    // 内置静态服务器：9811 端口提供测试页与模型文件
    server: {
        command: 'python3 -m http.server 9811',
        port: 9811,
        launchTimeout: 10000,
        debug: true
    }
};
