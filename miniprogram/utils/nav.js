function navigateBack(fallback = '/pages/home/index') {
  const pages = getCurrentPages();
  if (pages.length > 1) {
    wx.navigateBack();
    return;
  }

  wx.reLaunch({ url: fallback });
}

module.exports = {
  navigateBack
};
