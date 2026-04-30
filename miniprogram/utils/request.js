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

function requestWithFallback(baseUrls, payload, index = 0) {
  return new Promise((resolve, reject) => {
    const baseUrl = baseUrls[index];
    wx.request({
      url: `${baseUrl}${payload.url}`,
      method: payload.method,
      data: payload.data,
      header: {
        'Content-Type': 'application/json',
        Authorization: payload.token ? `Bearer ${payload.token}` : ''
      },
      success(res) {
        if (res.statusCode >= 200 && res.statusCode < 300 && res.data && res.data.code === 0) {
          rememberBaseUrl(baseUrl);
          resolve(res.data.data);
          return;
        }

        if (res.statusCode === 401) {
          clearAuthAndRedirect();
          reject(new Error('登录已过期，请重新登录'));
          return;
        }

        tryNext(baseUrls, payload, index, resolve, reject, res);
      },
      fail(error) {
        if (config.fallbackToMock && payload.mockData) {
          resolve(payload.mockData);
          return;
        }
        tryNext(baseUrls, payload, index, resolve, reject, error);
      }
    });
  });
}

function tryNext(baseUrls, payload, index, resolve, reject, error) {
  if (index < baseUrls.length - 1) {
    requestWithFallback(baseUrls, payload, index + 1).then(resolve).catch(reject);
    return;
  }

  if (config.fallbackToMock && payload.mockData) {
    resolve(payload.mockData);
    return;
  }

  reject(error);
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
  const { url, filePath, name = 'file', formData = {}, mockData } = options;
  const app = getApp();
  const token = app.globalData.token || wx.getStorageSync('fitnote_token') || '';

  const baseUrls = getBaseUrls();
  return uploadWithFallback(baseUrls, {
    url,
    filePath,
    name,
    formData,
    token,
    mockData
  });
}

function uploadWithFallback(baseUrls, payload, index = 0) {
  return new Promise((resolve, reject) => {
    const baseUrl = baseUrls[index];
    wx.uploadFile({
      url: `${baseUrl}${payload.url}`,
      filePath: payload.filePath,
      name: payload.name,
      formData: payload.formData,
      header: {
        Authorization: payload.token ? `Bearer ${payload.token}` : ''
      },
      success(res) {
        const json = parseUploadResponse(res.data);
        if (res.statusCode >= 200 && res.statusCode < 300 && json && json.code === 0) {
          rememberBaseUrl(baseUrl);
          resolve(json.data);
          return;
        }

        if (res.statusCode === 401) {
          clearAuthAndRedirect();
          reject(new Error('登录已过期，请重新登录'));
          return;
        }

        tryNextUpload(baseUrls, payload, index, resolve, reject, json || res);
      },
      fail(error) {
        if (config.fallbackToMock && payload.mockData) {
          resolve(payload.mockData);
          return;
        }
        tryNextUpload(baseUrls, payload, index, resolve, reject, error);
      }
    });
  });
}

function tryNextUpload(baseUrls, payload, index, resolve, reject, error) {
  if (index < baseUrls.length - 1) {
    uploadWithFallback(baseUrls, payload, index + 1).then(resolve).catch(reject);
    return;
  }

  if (config.fallbackToMock && payload.mockData) {
    resolve(payload.mockData);
    return;
  }

  reject(error);
}

function parseUploadResponse(data) {
  if (!data) {
    return null;
  }

  if (typeof data === 'object') {
    return data;
  }

  try {
    return JSON.parse(data);
  } catch (error) {
    return null;
  }
}

function rememberBaseUrl(baseUrl) {
  wx.setStorageSync(ACTIVE_BASE_URL_KEY, baseUrl);
}

function getBaseUrls() {
  const stored = wx.getStorageSync(ACTIVE_BASE_URL_KEY);
  const configured = Array.isArray(config.baseUrls) ? config.baseUrls : [config.baseUrl];

  if (stored && configured.includes(stored)) {
    return [stored].concat(configured.filter((item) => item !== stored));
  }

  return configured;
}

function getActiveBaseUrl() {
  return getBaseUrls()[0] || '';
}

function resolveFileUrl(url) {
  if (!url) {
    return '';
  }

  const value = String(url);
  if (/^(https?:)?\/\//.test(value) || value.startsWith('wxfile://') || value.startsWith('data:')) {
    return value;
  }

  const baseUrl = getActiveBaseUrl();
  const origin = baseUrl.endsWith('/api') ? baseUrl.slice(0, -4) : baseUrl;
  const normalized = value.startsWith('/') ? value : `/${value}`;
  return `${origin}${normalized}`;
}

module.exports = {
  request,
  uploadFile,
  resolveFileUrl
};
