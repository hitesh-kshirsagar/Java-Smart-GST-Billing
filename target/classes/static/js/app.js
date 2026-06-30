/**
 * Smart GST Billing — Frontend Utility Library
 * Handles JWT storage, API calls, auth guards, and common UI helpers.
 */

const API = '/api';

// ============================================================
// Auth helpers
// ============================================================

function getToken()   { return localStorage.getItem('gst_token'); }
function getUser()    { return JSON.parse(localStorage.getItem('gst_user') || '{}'); }
function setAuth(data) {
    localStorage.setItem('gst_token', data.token);
    localStorage.setItem('gst_user', JSON.stringify({ username: data.username, role: data.role }));
}
function clearAuth()  {
    localStorage.removeItem('gst_token');
    localStorage.removeItem('gst_user');
}

/** Redirect to login if no token is stored */
function requireAuth() {
    if (!getToken()) { window.location.href = '/login'; }
}

/** Populate sidebar username */
function populateSidebar() {
    const user = getUser();
    const el = document.getElementById('sidebar-username');
    if (el) el.textContent = user.username || 'User';
    const role = document.getElementById('sidebar-role');
    if (role) role.textContent = user.role || '';
}

function logout() {
    clearAuth();
    window.location.href = '/login';
}

// ============================================================
// HTTP helpers
// ============================================================

async function apiFetch(path, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
    };
    const res = await fetch(API + path, { ...options, headers });

    if (res.status === 401) { logout(); return; }

    // For PDF (binary) responses return raw response
    if (options._raw) return res;

    const text = await res.text();
    let data;
    try { data = text ? JSON.parse(text) : {}; } catch { data = text; }

    if (!res.ok) {
        const msg = data?.message || data?.fieldErrors
            ? JSON.stringify(data.fieldErrors)
            : (typeof data === 'string' ? data : 'Request failed');
        throw new Error(msg);
    }
    return data;
}

const api = {
    get:    (path)         => apiFetch(path, { method: 'GET' }),
    post:   (path, body)   => apiFetch(path, { method: 'POST',   body: JSON.stringify(body) }),
    put:    (path, body)   => apiFetch(path, { method: 'PUT',    body: JSON.stringify(body) }),
    delete: (path)         => apiFetch(path, { method: 'DELETE' }),
    raw:    (path)         => apiFetch(path, { method: 'GET', _raw: true }),
};

// ============================================================
// UI helpers
// ============================================================

function showAlert(containerId, message, type = 'success') {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = `<div class="alert alert-${type}">${message}</div>`;
    setTimeout(() => { el.innerHTML = ''; }, 4000);
}

function openModal(id)  { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }

function setLoading(btn, loading) {
    if (loading) {
        btn.dataset.origText = btn.innerHTML;
        btn.innerHTML = '<span class="spinner"></span> Please wait...';
        btn.disabled = true;
    } else {
        btn.innerHTML = btn.dataset.origText || btn.innerHTML;
        btn.disabled = false;
    }
}

function formatCurrency(val) {
    const n = parseFloat(val || 0);
    return '₹' + n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(d) {
    if (!d) return '';
    const dt = new Date(d + 'T00:00:00');
    return dt.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

// Mark active nav link
function markActiveNav() {
    const path = window.location.pathname;
    document.querySelectorAll('.sidebar-nav a').forEach(a => {
        a.classList.toggle('active', a.getAttribute('href') === path);
    });
}

// Close modal on backdrop click
document.addEventListener('click', e => {
    if (e.target.classList.contains('modal-backdrop')) {
        e.target.classList.remove('open');
    }
});

document.addEventListener('DOMContentLoaded', () => {
    markActiveNav();
    populateSidebar();
});
