const config = require('./config');
const ACTIVE_BASE_URL_KEY = 'fitnote_active_base_url';

function request(options) {
  const { url, method = 'GET', data = {}, mockData } = options;
  const app = getApp();
  const token = app.globalData.token || wx.getStorageSync('fitnote_token') || '';

  const baseUrls = Array.isArray(config.baseUrls) ? config.baseUrls : [config.baseUrl];
  return requestWithFallback(baseUrls, {
    url,
    method,
    data,
    token,
    mockData
  });
}

function requestWithFallback(baseUrls, payload, failedUrls, index) {
  if (!index) index = 0;
  if (!failedUrls) failedUrls = [];

  return new Promise((resolve, reject) => {
    const baseUrl = baseUrls[index];
    wx.request({
      url: baseUrl + payload.url,
      method: payload.method,
      data: payload.data,
      timeout: 15000,
      header: {
        'Content-Type': 'application/json',
        Authorization: payload.token ? 'Bearer ' + payload.token : ''
      },
      success: function (res) {
        if (res.statusCode >= 200 && res.statusCode < 300 && res.data && (res.data.code === 0 || res.data.code === 200)) {
          rememberBaseUrl(baseUrl);
          resolve(res.data.data);
          return;
        }

        if (res.statusCode === 401) {
          clearAuthAndRedirect();
          reject(new Error('登录已过期，请重新登录'));
          return;
        }

        tryNextOrMock(baseUrls, payload, index, failedUrls.concat([baseUrl]), resolve, reject);
      },
      fail: function () {
        tryNextOrMock(baseUrls, payload, index, failedUrls.concat([baseUrl]), resolve, reject);
      }
    });
  });
}

function tryNextOrMock(baseUrls, payload, index, failedUrls, resolve, reject) {
  // 还有下一个 URL 没试过
  if (index < baseUrls.length - 1) {
    requestWithFallback(baseUrls, payload, failedUrls, index + 1)
      .then(resolve)
      .catch(function (err) {
        // 所有 URL 都失败了，尝试 mock 降级
        if (config.fallbackToMock) {
          resolve(autoMock(payload.url, payload.mockData));
          return;
        }
        reject(err);
      });
    return;
  }

  // 所有 URL 都失败了
  if (config.fallbackToMock) {
    resolve(autoMock(payload.url, payload.mockData));
    return;
  }

  reject(new Error('网络请求失败'));
}

function autoMock(url, mockData) {
  // 有显式 mockData 就直接用
  if (mockData) {
    return mockData;
  }

  // 根据 URL 推断合理的默认值
  if (url.indexOf('/history') !== -1 || url.indexOf('/logs') !== -1 ||
      url.indexOf('/posts') !== -1 || url.indexOf('/rankings') !== -1 ||
      url.indexOf('/exercises') !== -1 || url.indexOf('/plans') !== -1 ||
      url.indexOf('/comments') !== -1 || url.indexOf('/heatmap') !== -1 ||
      url.indexOf('/personal-records') !== -1) {
    return [];
  }

  if (url.indexOf('/overview') !== -1 || url.indexOf('/analytics') !== -1 ||
      url.indexOf('/today') !== -1 || url.indexOf('/goal') !== -1 ||
      url.indexOf('/generate') !== -1 || url.indexOf('/options') !== -1) {
    return {};
  }

  if (url.indexOf('/dashboard') !== -1) {
    return { hero: { title: '选择你的动能', subtitle: '保持训练节奏', targetType: '增肌', trainingLevel: '进阶' }, readiness: { mealReady: '离线模式', hydration: '离线模式', recovery: '离线模式' }, overview: { weeklySessions: 0, activePlans: 0, goalWeight: 80 }, quickPlans: [], quickActions: [{ name: '开始训练', path: '/pages/workout-editor/index' }, { name: '饮食分析', path: '/pages/diet-analysis/index' }] };
  }

  // 其他情况（如 login、chat 等）
  return { token: 'mock-token-offline', user: { id: 1, nickname: '离线用户', avatarUrl: '' } };
}

function clearAuthAndRedirect() {
  const app = getApp();
  app.globalData.token = '';
  app.globalData.user = null;
  wx.removeStorageSync('fitnote_token');
  wx.removeStorageSync('fitnote_user');
  wx.reLaunch({ url: '/pages/login/index' });
}

function uploadFile(options) {
  const { url, filePath, name, formData, mockData } = options;
  const app = getApp();
  const token = app.globalData.token || wx.getStorageSync('fitnote_token') || '';

  const baseUrls = getBaseUrls();
  return uploadWithFallback(baseUrls, {
    url,
    filePath,
    name: name || 'file',
    formData: formData || {},
    token,
    mockData
  });
}

function uploadWithFallback(baseUrls, payload, index) {
  if (!index) index = 0;
  return new Promise((resolve, reject) => {
    const baseUrl = baseUrls[index];
    wx.uploadFile({
      url: baseUrl + payload.url,
      filePath: payload.filePath,
      name: payload.name,
      formData: payload.formData,
      timeout: 30000,
      header: {
        Authorization: payload.token ? 'Bearer ' + payload.token : ''
      },
      success: function (res) {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          try {
            const json = JSON.parse(res.data);
            if (json && (json.code === 0 || json.code === 200)) {
              rememberBaseUrl(baseUrl);
              resolve(json.data);
              return;
            }
          } catch (e) {
            // JSON parse failed
          }
        }

        if (index < baseUrls.length - 1) {
          uploadWithFallback(baseUrls, payload, index + 1).then(resolve).catch(reject);
        } else if (config.fallbackToMock && payload.mockData) {
          resolve(payload.mockData);
        } else {
          reject(new Error('上传失败'));
        }
      },
      fail: function () {
        if (config.fallbackToMock && payload.mockData) {
          resolve(payload.mockData);
        } else if (index < baseUrls.length - 1) {
          uploadWithFallback(baseUrls, payload, index + 1).then(resolve).catch(reject);
        } else {
          reject(new Error('上传失败'));
        }
      }
    });
  });
}

function rememberBaseUrl(baseUrl) {
  wx.setStorageSync(ACTIVE_BASE_URL_KEY, baseUrl);
}

function getBaseUrls() {
  const stored = wx.getStorageSync(ACTIVE_BASE_URL_KEY);
  const configured = Array.isArray(config.baseUrls) ? config.baseUrls : [config.baseUrl];

  if (stored && configured.indexOf(stored) !== -1) {
    return [stored].concat(configured.filter(function (item) { return item !== stored; }));
  }

  return configured;
}

function getActiveBaseUrl() {
  return getBaseUrls()[0] || '';
}

function resolveFileUrl(url) {
  if (!url) return '';

  const value = String(url);
  if (/^(https?:)?\/\//.test(value) || value.indexOf('wxfile://') === 0 || value.indexOf('data:') === 0) {
    return value;
  }

  const baseUrl = getActiveBaseUrl();
  const origin = baseUrl.indexOf('/api') !== -1 ? baseUrl.slice(0, -4) : baseUrl;
  const normalized = value.indexOf('/') === 0 ? value : '/' + value;
  return origin + normalized;
}

module.exports = {
  request: request,
  uploadFile: uploadFile,
  resolveFileUrl: resolveFileUrl
};
