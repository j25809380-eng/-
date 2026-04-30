module.exports = {
  // 开发环境：端口 8080 和 8081 自动切换
  // 真机调试：将 baseUrls 改为后端服务器的实际地址，如 'https://your-server.com/api'
  // 同时需在微信公众平台→开发管理→开发设置→服务器域名中配置 request 合法域名
  baseUrls: [
    'http://127.0.0.1:8080/api',
    'http://127.0.0.1:8081/api'
  ],
  useMock: false,
  // 开启后，后端不可用时自动降级到本地 mock 数据（答辩演示推荐开启）
  fallbackToMock: true
};
