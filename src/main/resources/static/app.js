/**
 * SafeExchange Complete Client with Real Category Image Headers
 */

const state = {
  token: localStorage.getItem('safeexchange_token') || null,
  username: localStorage.getItem('safeexchange_username') || null,
  wallet: null,
  marketplaceItems: [],
  myEscrows: [],
  activeCategory: 'ALL',
  searchQuery: '',
  sortBy: 'NEWEST',
  authMode: 'login',
};

// 4 Exact Popular Categories with Real Images
const POPULAR_CATEGORIES = [
  {
    name: 'Gaming Accounts',
    type: 'Gaming Account',
    badge: 'GAMING ACCOUNTS',
    badgeColor: 'background:#dc2626',
    count: 120,
    image: '/images/realistic_gaming_accounts_1788430933238.jpg',
    desc: 'Trade verified gaming accounts, rare skins, ranks, weapons and game inventories with escrow lock.',
    popular: 'Steam, Valorant, CoD, Epic',
  },
  {
    name: 'Domain Names',
    type: 'Domain Name',
    badge: 'DOMAINS & WEB',
    badgeColor: 'background:#2563eb',
    count: 45,
    image: '/images/realistic_domain_names_1788430959706.jpg',
    desc: 'Transfer web domain ownership, premium DNS, and brand assets safely with verified auth codes.',
    popular: '.com, .io, .ai, Brandables',
  },
  {
    name: 'Software & Licenses',
    type: 'Software License',
    badge: 'SOURCE CODE & KEYS',
    badgeColor: 'background:#16a34a',
    count: 80,
    image: '/images/realistic_software_code_1788430983789.jpg',
    desc: 'Exchange proprietary source code, turnkey digital scripts, and commercial software licenses.',
    popular: 'Turnkey Apps, Scripts, Licenses',
  },
  {
    name: 'SaaS Platforms',
    type: 'SaaS Business',
    badge: 'SAAS BUSINESSES',
    badgeColor: 'background:#7c3aed',
    count: 30,
    image: '/images/realistic_saas_platforms_1788431003850.jpg',
    desc: 'Buy and sell recurring-revenue cloud startups, apps, and digital business assets with full handover.',
    popular: 'Micro-Startups, MRR Apps, Tools',
  },
];

async function api(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };
  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }

  try {
    const res = await fetch(endpoint, { ...options, headers });
    if (res.status === 401) {
      logout();
      showToast('Session expired. Please sign in.', 'error');
      throw new Error('Unauthorized');
    }
    const data = await res.json().catch(() => null);
    if (!res.ok) {
      throw new Error(data?.message || data?.error || `Server error (${res.status})`);
    }
    return data;
  } catch (err) {
    console.error(`API [${endpoint}] failed:`, err);
    throw err;
  }
}

window.addEventListener('DOMContentLoaded', () => {
  renderPopularCategories('ALL');
  updateAuthUI();
  loadMarketplace();
  if (state.token) {
    refreshWallet();
  }
});

function navigateTo(viewName) {
  document.querySelectorAll('.view-page').forEach(el => el.classList.add('hidden'));
  const target = document.getElementById(`view-${viewName}`);
  if (target) target.classList.remove('hidden');

  closeAllDropdowns();

  if (viewName === 'marketplace') {
    loadMarketplace();
  } else if (viewName === 'dashboard') {
    if (!state.token) return openAuthModal('login');
    loadMyEscrows();
  } else if (viewName === 'wallet') {
    if (!state.token) return openAuthModal('login');
    refreshWallet();
    loadTransactions();
  }

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function filterCategoryAndNavigate(catType) {
  state.activeCategory = catType;
  navigateTo('marketplace');
  document.querySelectorAll('.cat-pill').forEach(pill => {
    pill.classList.toggle('active', pill.textContent.trim() === (catType === 'ALL' ? 'All Assets' : catType));
  });
  applyMarketFilters();
}

function switchCategoryTab(category, btn) {
  document.querySelectorAll('.cat-tab').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  renderPopularCategories(category);
}

function renderPopularCategories(filter) {
  const container = document.getElementById('categories-cards-grid');
  const filtered = filter === 'ALL'
    ? POPULAR_CATEGORIES
    : POPULAR_CATEGORIES.filter(c => c.type === filter);

  container.innerHTML = filtered.map(cat => `
    <div class="cat-card" onclick="filterCategoryAndNavigate('${cat.type}')">
      <div class="cat-img-wrap">
        <img src="${cat.image}" alt="${cat.name}" class="cat-img" />
        <span class="cat-badge" style="${cat.badgeColor}">${cat.badge}</span>
        <span class="cat-count">${cat.count}+ active</span>
      </div>
      <div class="cat-body">
        <div>
          <div class="cat-name-row">
            <h4 class="cat-name">${cat.name}</h4>
          </div>
          <p class="cat-desc">${cat.desc}</p>
        </div>
        <div class="cat-footer">
          <span class="cat-tag">BUY & SELL SECURELY</span>
          <span class="cat-explore">Explore →</span>
        </div>
      </div>
    </div>
  `).join('');
}

function handleHeroSearch(e) {
  e.preventDefault();
  const q = document.getElementById('hero-search-input').value.trim();
  state.searchQuery = q;
  navigateTo('marketplace');
  const marketInput = document.getElementById('market-search-input');
  if (marketInput) marketInput.value = q;
  applyMarketFilters();
}

function handleNavSearch(e) {
  e.preventDefault();
  const q = document.getElementById('nav-search-input').value.trim();
  state.searchQuery = q;
  navigateTo('marketplace');
  const marketInput = document.getElementById('market-search-input');
  if (marketInput) marketInput.value = q;
  applyMarketFilters();
}

async function loadMarketplace() {
  const grid = document.getElementById('marketplace-grid');
  const featuredGrid = document.getElementById('landing-featured-grid');

  try {
    const data = await api('/api/escrow');
    state.marketplaceItems = Array.isArray(data) ? data : [];

    if (featuredGrid) {
      const top3 = state.marketplaceItems.slice(0, 3);
      if (top3.length === 0) {
        featuredGrid.innerHTML = '<div class="text-secondary text-sm">No listings available. List the first asset!</div>';
      } else {
        featuredGrid.innerHTML = top3.map(item => renderListingCard(item)).join('');
      }
    }

    applyMarketFilters();
  } catch (err) {
    if (grid) grid.innerHTML = `<div class="text-danger text-sm">Failed to load: ${err.message}</div>`;
  }
}

function filterCategory(cat, btn) {
  document.querySelectorAll('.cat-pill').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  state.activeCategory = cat;
  applyMarketFilters();
}

function applyMarketFilters() {
  const searchVal = (document.getElementById('market-search-input')?.value || state.searchQuery || '').toLowerCase();
  const sortVal = document.getElementById('market-sort-select')?.value || state.sortBy;

  let filtered = state.marketplaceItems.filter(item => {
    const matchCat = state.activeCategory === 'ALL' || (item.assetType || '').toLowerCase() === state.activeCategory.toLowerCase();
    const matchSearch =
      (item.assetName || '').toLowerCase().includes(searchVal) ||
      (item.assetDescription || '').toLowerCase().includes(searchVal) ||
      (item.sellerUsername || '').toLowerCase().includes(searchVal);
    return matchCat && matchSearch;
  });

  if (sortVal === 'PRICE_HIGH') filtered.sort((a,b) => Number(b.amount) - Number(a.amount));
  if (sortVal === 'PRICE_LOW') filtered.sort((a,b) => Number(a.amount) - Number(b.amount));
  if (sortVal === 'NEWEST') filtered.sort((a,b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));

  const countBadge = document.getElementById('marketplace-count-badge');
  if (countBadge) countBadge.textContent = `${filtered.length} Listings`;

  const grid = document.getElementById('marketplace-grid');
  if (!grid) return;

  if (filtered.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding: 48px 0; color: #94a3b8">
        <h3>No Listings Found</h3>
        <p style="font-size:0.8rem; margin-top:4px">No digital assets match your filter criteria.</p>
      </div>
    `;
    return;
  }

  grid.innerHTML = filtered.map(item => renderListingCard(item)).join('');
}

function renderListingCard(item) {
  const isMine = state.username && item.sellerUsername.toLowerCase() === state.username.toLowerCase();
  const sellerInitial = (item.sellerUsername || 'U').charAt(0).toUpperCase();

  return `
    <div class="listing-card">
      <div>
        <div class="listing-top">
          <span class="listing-type-pill">${escapeHtml(item.assetType)}</span>
          <span class="listing-status-badge">${item.status}</span>
        </div>
        <h3 class="listing-name">${escapeHtml(item.assetName)}</h3>
        <p class="listing-desc">${escapeHtml(item.assetDescription || 'No terms provided.')}</p>

        <div class="listing-seller-row">
          <div class="flex-row gap-2">
            <div class="seller-avatar-chip">${sellerInitial}</div>
            <span>${escapeHtml(item.sellerUsername)}</span>
          </div>
          <span>${item.createdAt ? new Date(item.createdAt).toLocaleDateString() : 'Active'}</span>
        </div>
      </div>

      <div class="listing-bottom">
        <div>
          <span class="text-xs text-muted block">Price</span>
          <div class="listing-amount">₹${Number(item.amount).toLocaleString('en-IN')}</div>
        </div>
        ${
          isMine
            ? `<span class="badge-vault" style="background:#1e293b; color:#94a3b8; font-size:0.75rem; padding:6px 12px; border-radius:999px">Your Listing</span>`
           : `<button class="btn btn-emerald-pill btn-sm" onclick="this.disabled=true; claimEscrow(${item.id})">Claim This Listing →</button>`
        }
      </div>
    </div>
  `;
}

async function claimEscrow(id) {
  if (!state.token) return openAuthModal('login');

  try {
    await api(`/api/escrow/${id}/claim`, { method: 'POST' });
    showToast(`Listing #${id} claimed! Lock funds in your vault.`, 'success');
    navigateTo('dashboard');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function loadMyEscrows() {
  const container = document.getElementById('dashboard-escrows-list');
  container.innerHTML = '<div class="loading-state">Loading your escrows...</div>';

  try {
    const data = await api('/api/escrow/user/all').catch(() => api('/api/escrow'));
    state.myEscrows = Array.isArray(data) ? data : [];
    renderDashboardEscrows('ALL');
  } catch (err) {
    container.innerHTML = `<div class="text-danger text-sm">Failed to load: ${err.message}</div>`;
  }
}

function filterMyEscrows(filter, btn) {
  document.querySelectorAll('.tab-pill').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  renderDashboardEscrows(filter);
}

function renderDashboardEscrows(filter) {
  const container = document.getElementById('dashboard-escrows-list');
  const user = (state.username || '').toLowerCase();

  let list = state.myEscrows;
  if (filter === 'BUYER') list = list.filter(e => e.buyerUsername && e.buyerUsername.toLowerCase() === user);
  if (filter === 'SELLER') list = list.filter(e => e.sellerUsername.toLowerCase() === user);

  if (list.length === 0) {
    container.innerHTML = `<div class="card p-6 text-center text-muted text-sm">No escrows found in this view.</div>`;
    return;
  }

  container.innerHTML = list.map(escrow => {
    const isBuyer = escrow.buyerUsername && escrow.buyerUsername.toLowerCase() === user;
    const isSeller = escrow.sellerUsername.toLowerCase() === user;

    return `
      <div class="card p-6 space-y-4">
        <div class="flex-between">
          <div>
            <span class="count-badge">${escrow.assetType}</span>
            <h3 style="font-size:1.15rem; font-weight:800; margin-top:6px">${escapeHtml(escrow.assetName)}</h3>
            <span class="text-muted" style="font-size:0.75rem">
              Seller: <strong>${escapeHtml(escrow.sellerUsername)}</strong> | Buyer: <strong>${escapeHtml(escrow.buyerUsername || 'Awaiting Claim')}</strong>
            </span>
          </div>
          <div style="text-align:right">
            <span class="text-muted text-xs">Escrow Value</span>
            <div class="listing-amount">₹${Number(escrow.amount).toLocaleString('en-IN')}</div>
          </div>
        </div>

        <div class="flex-between bg-surface-2 p-3 rounded text-xs font-semibold">
          <span>Status: <strong class="text-emerald">${escrow.status}</strong></span>
          ${renderEscrowAction(escrow, isBuyer, isSeller)}
        </div>
      </div>
    `;
  }).join('');
}

function renderEscrowAction(escrow, isBuyer, isSeller) {
  if (escrow.status === 'CREATED' && isBuyer) {
    return `<button class="btn btn-emerald-pill btn-sm" onclick="lockFunds(${escrow.id})">Lock Funds (₹${Number(escrow.amount).toLocaleString('en-IN')})</button>`;
  }
  if (escrow.status === 'FUNDED' && isSeller) {
    return `<button class="btn btn-emerald-pill btn-sm" onclick="openSubmitAssetModal(${escrow.id})">Deliver Asset Link →</button>`;
  }
  if (escrow.status === 'ASSET_SUBMITTED' && isBuyer) {
    return `
      <div class="flex-row gap-2">
        <a href="${escapeHtml(escrow.submissionLink)}" target="_blank" class="text-emerald text-xs">View Asset ↗</a>
        <button class="btn btn-emerald-pill btn-sm" onclick="confirmDelivery(${escrow.id})">Confirm & Release</button>
      </div>
    `;
  }
  if (escrow.status === 'COMPLETED') {
    return `<span class="text-emerald">✓ Completed</span>`;
  }
  return '';
}

async function lockFunds(id) {
  try {
    await api(`/api/escrow/${id}/lock-funds`, { method: 'POST' });
    showToast('Funds locked securely!', 'success');
    refreshWallet();
    loadMyEscrows();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function confirmDelivery(id) {
  try {
    await api(`/api/escrow/${id}/confirm-delivery`, { method: 'POST' });
    showToast('Delivery verified! Funds released.', 'success');
    refreshWallet();
    loadMyEscrows();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function refreshWallet() {
  if (!state.token) return;
  try {
    const data = await api('/api/wallet/balance');
    state.wallet = data;
    const formatted = `₹${Number(data.balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
    const navBal = document.getElementById('nav-balance');
    const cardBal = document.getElementById('wallet-card-balance');
    if (navBal) navBal.textContent = formatted;
    if (cardBal) cardBal.textContent = formatted;
  } catch (err) {
    console.error(err);
  }
}

async function handleDeposit(e) {
  e.preventDefault();
  const amount = parseFloat(document.getElementById('deposit-amount-input').value);
  if (!amount || amount <= 0) return;

  try {
    await api('/api/wallet/deposit', {
      method: 'POST',
      body: JSON.stringify({ amount }),
    });
    showToast(`Deposited ₹${amount.toLocaleString('en-IN')}!`, 'success');
    document.getElementById('deposit-amount-input').value = '';
    refreshWallet();
    loadTransactions();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function setDepositValue(val) {
  const el = document.getElementById('deposit-amount-input');
  if (el) el.value = val;
}

async function loadTransactions() {
  const container = document.getElementById('transactions-ledger-list');
  container.innerHTML = '<div class="loading-state">Loading ledger...</div>';

  try {
    const txs = await api('/api/transactions');
    if (!txs || txs.length === 0) {
      container.innerHTML = '<div class="text-muted text-xs p-4 text-center">No transactions recorded yet.</div>';
      return;
    }

    container.innerHTML = txs.map(tx => {
      const isCredit = tx.type === 'DEPOSIT' || tx.type === 'ESCROW_RELEASE';
      return `
        <div class="flex-between p-3 bg-surface-2 rounded text-xs">
          <div>
            <div style="font-weight:700">${escapeHtml(tx.description || tx.type)}</div>
            <div style="font-size:0.68rem; color:#64748b">${new Date(tx.createdAt).toLocaleString()}</div>
          </div>
          <div style="font-family:monospace; font-weight:800; color:${isCredit ? '#10b981' : '#f59e0b'}">
            ${isCredit ? '+' : '-'}₹${Number(tx.amount).toLocaleString('en-IN')}
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    container.innerHTML = `<div class="text-danger text-xs">Failed to load transactions: ${err.message}</div>`;
  }
}

function openModal(id) { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

function toggleDropdown(id) {
  const el = document.getElementById(id);
  const isHidden = el.classList.contains('hidden');
  closeAllDropdowns();
  if (isHidden) el.classList.remove('hidden');
}

function closeAllDropdowns() {
  document.querySelectorAll('.dropdown-menu').forEach(el => el.classList.add('hidden'));
}

window.addEventListener('click', (e) => {
  if (!e.target.closest('.dropdown-wrapper')) {
    closeAllDropdowns();
  }
});

function openCreateListingModal() {
  if (!state.token) return openAuthModal('login');
  openModal('modal-create-listing');
}

async function handleCreateListingSubmit(e) {
  e.preventDefault();
  const assetName = document.getElementById('create-asset-name').value.trim();
  const assetType = document.getElementById('create-asset-type').value;
  const amount = parseFloat(document.getElementById('create-amount').value);
  const assetDescription = document.getElementById('create-description').value.trim();

  try {
    await api('/api/escrow', {
      method: 'POST',
      body: JSON.stringify({ assetName, assetType, amount, assetDescription }),
    });
    showToast('Listing created successfully!', 'success');
    closeModal('modal-create-listing');
    document.getElementById('create-asset-name').value = '';
    document.getElementById('create-description').value = '';
    document.getElementById('create-amount').value = '';
    loadMarketplace();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function openSubmitAssetModal(escrowId) {
  document.getElementById('submit-asset-id').value = escrowId;
  document.getElementById('submit-asset-link').value = '';
  openModal('modal-submit-asset');
}

async function handleSubmitAssetForm(e) {
  e.preventDefault();
  const id = document.getElementById('submit-asset-id').value;
  const submissionLink = document.getElementById('submit-asset-link').value.trim();

  try {
    await api(`/api/escrow/${id}/submit-asset`, {
      method: 'POST',
      body: JSON.stringify({ submissionLink }),
    });
    showToast('Asset link submitted to buyer!', 'success');
    closeModal('modal-submit-asset');
    loadMyEscrows();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function openAuthModal(mode = 'login') {
  state.authMode = mode;
  switchAuthMode(mode);
  openModal('modal-auth');
}

function switchAuthMode(mode) {
  state.authMode = mode;
  const isLogin = mode === 'login';
  document.getElementById('tab-auth-login').classList.toggle('active', isLogin);
  document.getElementById('tab-auth-register').classList.toggle('active', !isLogin);
  document.getElementById('auth-title').textContent = isLogin ? 'Sign In' : 'Create Account';
  document.getElementById('auth-submit-btn').textContent = isLogin ? 'Sign In →' : 'Register Account →';
  document.getElementById('auth-email-group').classList.toggle('hidden', isLogin);
}

async function handleAuthSubmit(e) {
  e.preventDefault();
  const username = document.getElementById('auth-username').value.trim();
  const password = document.getElementById('auth-password').value;
  const email = document.getElementById('auth-email').value.trim();

  try {
    if (state.authMode === 'register') {
      await api('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, email, password }),
      });
      showToast('Account registered! Logging in...', 'success');
    }

    const data = await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });

    state.token = data.token;
    state.username = data.username;
    localStorage.setItem('safeexchange_token', data.token);
    localStorage.setItem('safeexchange_username', data.username);

    updateAuthUI();
    closeModal('modal-auth');
    showToast(`Welcome, ${data.username}!`, 'success');
    refreshWallet();
    loadMarketplace();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function updateAuthUI() {
  const loggedOut = document.getElementById('auth-logged-out');
  const loggedIn = document.getElementById('auth-logged-in');
  const usernameSpan = document.getElementById('nav-username');
  const dropdownUser = document.getElementById('dropdown-username');
  const avatar = document.getElementById('nav-avatar');

  if (state.token && state.username) {
    loggedOut.classList.add('hidden');
    loggedIn.classList.remove('hidden');
    if (usernameSpan) usernameSpan.textContent = state.username;
    if (dropdownUser) dropdownUser.textContent = state.username;
    if (avatar) avatar.textContent = state.username.charAt(0).toUpperCase();
  } else {
    loggedOut.classList.remove('hidden');
    loggedIn.classList.add('hidden');
  }
}

function logout() {
  state.token = null;
  state.username = null;
  localStorage.removeItem('safeexchange_token');
  localStorage.removeItem('safeexchange_username');
  updateAuthUI();
  showToast('Logged out.', 'success');
  navigateTo('landing');
}

function showToast(msg, type = 'success') {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = `toast ${type === 'error' ? 'text-danger' : ''}`;
  setTimeout(() => t.className = 'toast hidden', 3500);
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}