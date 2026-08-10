// CRA 开发服务器代理：将 /api 请求转发到本地 Prometheus 9090，避免跨域并复用真实 API。

// createProxyMiddleware 配置 target 与 changeOrigin，模拟生产环境 API 路径。
const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:9090',
      changeOrigin: true,
    })
  );
};
// 仅开发环境生效；生产构建由 Prometheus web 包静态托管 UI。
