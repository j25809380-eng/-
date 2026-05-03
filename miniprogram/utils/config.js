module.exports = {
  // 开发环境：后端默认运行在 8080 端口
  // 真机调试：改为后端服务器的实际地址，如 'https://your-server.com/api'
  // 同时需在微信公众平台 → 开发管理 → 开发设置 → 服务器域名中配置 request 合法域名
  baseUrls: [
    'http://127.0.0.1:8080/api'
  ],
  useMock: false,
  // 开启后，后端不可用时自动降级到本地 mock 数据
  // 答辩演示时即使不启动后端也能正常浏览所有页面
  fallbackToMock: true
};
