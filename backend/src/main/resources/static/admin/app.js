const viewTitles = {
  dashboard: 'Dashboard',
  users: 'User Management',
  workouts: 'Workout Data',
  posts: 'Community Review',
  logs: 'Audit Logs',
  plans: 'Plan Management'
};

const state = {
  currentView: 'dashboard',
  token: localStorage.getItem('fitnote_admin_token') || '',
  admin: readJson('fitnote_admin_profile')
};

document.addEventListener('DOMContentLoaded', () => {
  bindAuth();
  bindNav();
  bindLogFilters();
  document.getElementById('refreshBtn').addEventListener('click', () => {
    if (state.token) {
      loadCurrentView();
    }
  });
  document.getElementById('logoutBtn').addEventListener('click', logout);
  initialize();
});

async function initialize() {
  renderSession();

  if (!state.token) {
    showAuth();
    return;
  }

  try {
    state.admin = await apiGet('/api/admin/auth/me');
    persistSession();
    hideAuth();
    loadCurrentView();
  } catch (error) {
    clearSession();
    showAuth(error.message || 'Session expired. Please login again.');
  }
}

function bindNav() {
  document.querySelectorAll('.nav-item').forEach((button) => {
    button.addEventListener('click', () => {
      const view = button.dataset.view;
      state.currentView = view;
      document.getElementById('pageTitle').textContent = viewTitles[view];
      document.querySelectorAll('.nav-item').forEach((item) => item.classList.remove('active'));
      button.classList.add('active');
      document.querySelectorAll('.view').forEach((panel) => panel.classList.remove('active'));
      document.getElementById(`${view}View`).classList.add('active');
      if (state.token) {
        loadCurrentView();
      }
    });
  });
}

function bindLogFilters() {
  const applyButton = document.getElementById('logsApplyBtn');
  if (!applyButton) {
    return;
  }
  applyButton.addEventListener('click', () => {
    if (state.currentView === 'logs' && state.token) {
      loadCurrentView();
    }
  });
}

function bindAuth() {
  document.getElementById('loginForm').addEventListener('submit', async (event) => {
    event.preventDefault();

    const username = document.getElementById('usernameInput').value.trim();
    const password = document.getElementById('passwordInput').value.trim();
    const loginBtn = document.getElementById('loginBtn');

    if (!username || !password) {
      setAuthMessage('Please enter admin username and password.');
      return;
    }

    loginBtn.disabled = true;
    loginBtn.textContent = 'Signing in...';
    setAuthMessage('');

    try {
      const data = await apiPost('/api/admin/auth/login', { username, password });
      state.token = data.token;
      state.admin = data.admin;
      persistSession();
      hideAuth();
      loadCurrentView();
    } catch (error) {
      setAuthMessage(error.message || 'Login failed. Check credentials.');
    } finally {
      loginBtn.disabled = false;
      loginBtn.textContent = 'Login';
    }
  });
}

async function loadCurrentView() {
  if (state.currentView === 'dashboard') {
    const data = await apiGet('/api/admin/dashboard');
    renderDashboard(data);
    return;
  }

  if (state.currentView === 'users') {
    const data = await apiGet('/api/admin/users');
    renderUsers(data);
    return;
  }

  if (state.currentView === 'workouts') {
    const data = await apiGet('/api/admin/workouts');
    renderWorkouts(data);
    return;
  }

  if (state.currentView === 'posts') {
    const data = await apiGet('/api/admin/posts');
    renderPosts(data);
    return;
  }

  if (state.currentView === 'logs') {
    const params = new URLSearchParams();
    const status = document.getElementById('logsStatusFilter').value;
    const targetType = document.getElementById('logsTypeFilter').value;
    const days = Number(document.getElementById('logsDaysFilter').value || 30);
    if (status) params.set('status', status);
    if (targetType) params.set('targetType', targetType);
    if (Number.isFinite(days) && days > 0) params.set('days', String(days));
    params.set('limit', '200');
    const data = await apiGet(`/api/admin/audit-logs?${params.toString()}`);
    renderLogs(data);
    return;
  }

  if (state.currentView === 'plans') {
    const data = await apiGet('/api/admin/plans');
    renderPlans(data);
  }
}

async function apiGet(url) {
  return apiRequest(url);
}

async function apiPut(url) {
  return apiRequest(url, { method: 'PUT' });
}

async function apiPost(url, body) {
  return apiRequest(url, {
    method: 'POST',
    body: JSON.stringify(body)
  });
}

async function apiRequest(url, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }

  const response = await fetch(url, {
    method: options.method || 'GET',
    headers,
    body: options.body
  });

  const json = await response.json().catch(() => null);
  if (response.status === 401 || response.status === 403) {
    clearSession();
    showAuth('Admin session expired. Please login again.');
    throw new Error('Please login first');
  }

  if (!response.ok || !json || json.code !== 0) {
    throw new Error((json && json.message) || 'Request failed');
  }

  return json.data;
}

function renderDashboard(data) {
  const summaryCards = document.getElementById('summaryCards');
  const summary = data.summary || {};
  const metrics = [
    ['Users', summary.userCount || 0],
    ['Workouts', summary.workoutCount || 0],
    ['Posts', summary.postCount || 0],
    ['PR Count', summary.prCount || 0],
    ['Plans', summary.activePlans || 0],
    ['Pending Posts', summary.pendingPosts || 0],
    ['Total Volume', summary.totalVolume || 0]
  ];

  summaryCards.innerHTML = metrics.map(([label, value]) => `
    <div class="metric-card">
      <div class="metric-label">${label}</div>
      <div class="metric-value">${value}</div>
    </div>
  `).join('');

  document.getElementById('weeklyTrend').innerHTML = (data.weeklyTrend || []).map((item, index) => `
    <div class="trend-bar-col">
      <div class="trend-bar" style="height:${Math.max(16, Number(item.value || 0) * 18)}px"></div>
      <div class="trend-tag">${item.label || `D${index + 1}`}</div>
    </div>
  `).join('');

  document.getElementById('auditQueue').innerHTML = (data.auditQueue || []).map((item) => `
    <div class="queue-card">
      <div class="queue-head">
        <div class="queue-title">${item.authorName || '-'}</div>
        <span class="status ${statusClass(item.auditStatus)}">${item.auditStatus || '-'}</span>
      </div>
      <div class="queue-content">${item.content || ''}</div>
    </div>
  `).join('');

  document.getElementById('recentUsers').innerHTML = renderMiniList(
    (data.recentUsers || []).map((item) => `${item.nickname} · ${item.trainingLevel} · ${item.sessionCount} sessions`)
  );

  document.getElementById('recentWorkouts').innerHTML = renderMiniList(
    (data.recentWorkouts || []).map((item) => `${item.userName} · ${item.title} · ${item.totalVolume} kg`)
  );
}

function renderUsers(users) {
  document.getElementById('usersTable').innerHTML = users.map((item) => `
    <tr>
      <td>${item.id}</td>
      <td>${item.nickname}</td>
      <td>${item.trainingLevel}</td>
      <td>${item.targetType}</td>
      <td>${item.weightKg}</td>
      <td>${item.sessionCount}</td>
      <td>${item.prCount}</td>
      <td>${item.status === 1 ? 'ACTIVE' : 'DISABLED'}</td>
    </tr>
  `).join('');
}

function renderWorkouts(workouts) {
  document.getElementById('workoutsTable').innerHTML = workouts.map((item) => `
    <tr>
      <td>${item.id}</td>
      <td>${item.userName}</td>
      <td>${item.title}</td>
      <td>${item.sessionDate || '-'}</td>
      <td>${item.durationMinutes} min</td>
      <td>${item.totalVolume}</td>
      <td>${item.calories}</td>
      <td>${item.setCount}</td>
      <td>${item.completionStatus}</td>
    </tr>
  `).join('');
}

function renderPosts(posts) {
  document.getElementById('postsList').innerHTML = posts.map((item) => `
    <div class="post-card">
      <div class="post-head">
        <div>
          <div class="post-author">${item.authorName}</div>
          <div class="panel-head"><span>${item.postType || 'TRAINING'} · ${item.createdAt || '-'}</span></div>
        </div>
        <span class="status ${statusClass(item.auditStatus)}">${item.auditStatus}</span>
      </div>
      <div class="post-content">${item.content || ''}</div>
      <div class="audit-actions">
        <button class="audit-btn approve" onclick="auditPost(${item.id}, 'APPROVED')">Approve</button>
        <button class="audit-btn reject" onclick="auditPost(${item.id}, 'REJECTED')">Reject</button>
      </div>
    </div>
  `).join('');
}

function renderPlans(plans) {
  document.getElementById('plansTable').innerHTML = plans.map((item) => `
    <tr>
      <td>${item.id}</td>
      <td>${item.title}</td>
      <td>${item.targetType}</td>
      <td>${item.difficulty}</td>
      <td>${item.durationWeeks} weeks</td>
      <td>${item.daysPerWeek} days/week</td>
      <td><span class="tag">${item.isCustom ? 'CUSTOM' : 'SYSTEM'}</span></td>
      <td>${item.summary || ''}</td>
    </tr>
  `).join('');
}

function renderLogs(logs) {
  document.getElementById('logsTable').innerHTML = logs.map((item) => `
    <tr>
      <td>${item.id}</td>
      <td>${item.targetType}</td>
      <td>${item.targetId}</td>
      <td>${item.previousStatus || '-'}</td>
      <td><span class="status ${statusClass(item.auditStatus)}">${item.auditStatus}</span></td>
      <td>${item.reason || '-'}</td>
      <td>${item.operatorName || '-'} (#${item.operatorId || '-'})</td>
      <td>${item.targetSnapshot || '-'}</td>
      <td>${item.createdAt || '-'}</td>
    </tr>
  `).join('');
}

async function auditPost(id, status) {
  const reason = window.prompt(`Optional reason for ${status}:`, '') || '';
  await apiPut(`/api/admin/posts/${id}/audit?status=${encodeURIComponent(status)}&reason=${encodeURIComponent(reason)}`);
  loadCurrentView();
}

function persistSession() {
  if (state.token) {
    localStorage.setItem('fitnote_admin_token', state.token);
  }
  if (state.admin) {
    localStorage.setItem('fitnote_admin_profile', JSON.stringify(state.admin));
  }
  renderSession();
}

function clearSession() {
  state.token = '';
  state.admin = null;
  localStorage.removeItem('fitnote_admin_token');
  localStorage.removeItem('fitnote_admin_profile');
  renderSession();
}

function logout() {
  clearSession();
  showAuth('You have logged out.', false);
}

function renderSession() {
  const chip = document.getElementById('adminChip');
  if (!chip) {
    return;
  }

  if (state.admin) {
    chip.textContent = `${state.admin.nickname || state.admin.username} · ${state.admin.roleCode || 'ADMIN'}`;
    return;
  }

  chip.textContent = 'Not Logged In';
}

function showAuth(message = '', isError = true) {
  document.getElementById('authMask').style.display = 'grid';
  setAuthMessage(message, isError);
}

function hideAuth() {
  document.getElementById('authMask').style.display = 'none';
  setAuthMessage('');
}

function setAuthMessage(message, isError = true) {
  const el = document.getElementById('authMessage');
  el.textContent = message || '';
  el.style.color = isError ? 'var(--danger)' : 'var(--success)';
}

function renderMiniList(items) {
  if (!items.length) {
    return '<div class="mini-list"><div class="mini-list-item">No data</div></div>';
  }
  return `<div class="mini-list">${items.map((item) => `<div class="mini-list-item">${item}</div>`).join('')}</div>`;
}

function statusClass(status) {
  const value = String(status || '').toUpperCase();
  if (value === 'PENDING') return 'pending';
  if (value === 'REJECTED') return 'rejected';
  return 'approved';
}

function readJson(key) {
  try {
    const value = localStorage.getItem(key);
    return value ? JSON.parse(value) : null;
  } catch (error) {
    return null;
  }
}
