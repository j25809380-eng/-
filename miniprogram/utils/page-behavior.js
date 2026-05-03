// 通用页面 Behavior：抽取 handleBack、onReachBottom 分页加载等公共逻辑
const { navigateBack } = require('./nav');

module.exports = Behavior({
  methods: {
    handleBack() {
      navigateBack('/pages/home/index');
    }
  }
});
