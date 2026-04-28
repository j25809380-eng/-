const token = localStorage.getItem('fitnote_admin_token') || '';
const state = {
  token,
  refreshTimer: null
};

document.addEventListener('DOMContentLoaded', async () => {
  bindEvents();
  startClock();

  if (!state.token) {
    window.location.href = '/admin';
    return;
  }

  try {
    await apiGet('/api/admin/auth/me');
    await loadScreen();
    state.refreshTimer = setInterval(loadScreen, 60000);
  } catch (error) {
    localStorage.removeItem('fitnote_admin_token');
    localStorage.removeItem('fitnote_admin_profile');
    window.location.href = '/admin';
  }
});

function bindEvents() {
  document.getElementById('refreshBtn').addEventListener('click', loadScreen);
  document.getElementById('fullscreenBtn').addEventListener('click', toggleFullScreen);
}

async function loadScreen() {
  const data = await apiGet('/api/admin/big-screen');
  renderSummary(data.summary);
  renderBarChart('sessionTrend', data.sessionTrend || []);
  renderLineList('volumeTrend', data.volumeTrend || []);
  renderTagRows('goalDistribution', data.goalDistribution || []);
  renderTagRows('levelDistribution', data.levelDistribution || []);
  renderRankList('hotExercises', data.hotExercises || [], 'exerciseName', 'volume');
  renderTopUsers(data.topUsers || []);
  renderAlerts(data.auditAlerts || []);
  renderTagRows('auditStatusDistribution', data.auditStatusDistribution || []);
  renderRecentAuditLogs(data.recentAuditLogs || []);
  renderHeatmap(data.heatmap || []);
  document.getElementById('generatedAt').textContent = data.generatedAt || '--';
}

async function apiGet(url) {
  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${state.token}`
    }
  });

  const json = await response.json().catch(() => null);
  if (!response.ok || !json || json.code !== 0) {
    throw new Error((json && json.message) || 'Request failed');
  }

  return json.data;
}

function renderSummary(summary = {}) {
  const metrics = [
    ['Users', summary.userCount || 0],
    ['Workouts', summary.workoutCount || 0],
    ['Posts', summary.postCount || 0],
    ['PR Count', summary.prCount || 0],
    ['Pending Posts', summary.pendingPosts || 0],
    ['Custom Plans', summary.customPlans || 0],
    ['Total Volume', summary.totalVolume || 0],
    ['Audit Events (30d)', summary.auditEvents30d || 0]
  ];

  document.getElementById('summaryGrid').innerHTML = metrics.map(([label, value]) => `
    <div class="summary-card">
      <div class="summary-label">${label}</div>
      <div class="summary-value">${value}</div>
    </div>
  `).join('');
}

function renderBarChart(containerId, items) {
  const max = Math.max(...items.map((item) => Number(item.value) || 0), 1);
  document.getElementById(containerId).innerHTML = items.map((item) => {
    const height = Math.max(16, Math.round(((Number(item.value) || 0) / max) * 180));
    return `
      <div class="bar-col">
        <div class="bar-value">${item.value}</div>
        <div class="bar-track">
          <div class="bar-fill" style="height:${height}px"></div>
        </div>
        <div class="bar-label">${item.label}</div>
      </div>
    `;
  }).join('');
}

function renderLineList(containerId, items) {
  const max = Math.max(...items.map((item) => Number(item.value) || 0), 1);
  document.getElementById(containerId).innerHTML = items.map((item) => {
    const width = Math.max(6, Math.round(((Number(item.value) || 0) / max) * 100));
    return `
      <div class="line-row">
        <div class="line-head">
          <span>${item.label}</span>
          <strong>${item.value}</strong>
        </div>
        <div class="line-track">
          <div class="line-fill" style="width:${width}%"></div>
        </div>
      </div>
    `;
  }).join('');
}

function renderTagRows(containerId, items) {
  const container = document.getElementById(containerId);
  if (!items.length) {
    container.innerHTML = '<div class="screen-empty">No data</div>';
    return;
  }

  container.innerHTML = items.map((item) => `
    <div class="tag-row">
      <span>${item.label}</span>
      <span class="tag-chip">${item.value}</span>
    </div>
  `).join('');
}

function renderRankList(containerId, items, labelKey, valueKey) {
  const max = Math.max(...items.map((item) => Number(item[valueKey]) || 0), 1);
  const container = document.getElementById(containerId);
  if (!items.length) {
    container.innerHTML = '<div class="screen-empty">No data</div>';
    return;
  }

  container.innerHTML = items.map((item) => {
    const width = Math.max(10, Math.round(((Number(item[valueKey]) || 0) / max) * 100));
    return `
      <div class="rank-row">
        <div class="rank-head">
          <span>${item[labelKey]}</span>
          <strong>${item[valueKey]}</strong>
        </div>
        <div class="rank-track">
          <div class="rank-fill" style="width:${width}%"></div>
        </div>
      </div>
    `;
  }).join('');
}

function renderTopUsers(items) {
  const container = document.getElementById('topUsers');
  if (!items.length) {
    container.innerHTML = '<div class="screen-empty">No data</div>';
    return;
  }

  container.innerHTML = items.map((item, index) => `
    <div class="leader-row">
      <div class="leader-head">
        <strong>TOP ${index + 1} · ${item.nickname}</strong>
        <span>${item.sessionCount} sessions</span>
      </div>
      <div class="leader-value">Total volume: ${item.totalVolume}</div>
    </div>
  `).join('');
}

function renderAlerts(items) {
  const container = document.getElementById('auditAlerts');
  if (!items.length) {
    container.innerHTML = '<div class="screen-empty">No pending posts</div>';
    return;
  }

  container.innerHTML = items.map((item) => `
    <div class="alert-row">
      <div class="alert-head">
        <strong>${item.authorName}</strong>
        <span class="alert-type">${item.postType}</span>
      </div>
      <div class="alert-content">${item.content}</div>
    </div>
  `).join('');
}

function renderRecentAuditLogs(items) {
  const container = document.getElementById('recentAuditLogs');
  if (!items.length) {
    container.innerHTML = '<div class="screen-empty">No audit logs</div>';
    return;
  }

  container.innerHTML = items.map((item) => `
    <div class="alert-row">
      <div class="alert-head">
        <strong>${item.targetType}#${item.targetId}</strong>
        <span class="alert-type">${item.previousStatus} → ${item.auditStatus}</span>
      </div>
      <div class="alert-content">By ${item.operatorName || '-'} at ${item.createdAt || '-'}</div>
    </div>
  `).join('');
}

function renderHeatmap(items) {
  const max = Math.max(...items.map((item) => Number(item.value) || 0), 1);
  document.getElementById('heatmap').innerHTML = items.map((item) => {
    const alpha = 0.08 + (((Number(item.value) || 0) / max) * 0.92);
    return `
      <div class="heat-item">
        <div class="heat-value">${item.value}</div>
        <div class="heat-block" style="background:rgba(195, 244, 0, ${alpha.toFixed(2)});"></div>
        <div class="heat-label">${item.label}</div>
      </div>
    `;
  }).join('');
}

function startClock() {
  const tick = () => {
    const now = new Date();
    document.getElementById('clockText').textContent = now.toLocaleString('zh-CN', {
      hour12: false
    });
  };
  tick();
  setInterval(tick, 1000);
}

async function toggleFullScreen() {
  if (!document.fullscreenElement) {
    await document.documentElement.requestFullscreen();
    return;
  }

  await document.exitFullscreen();
}
