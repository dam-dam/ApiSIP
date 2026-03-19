const API_CATALOGS = '/catalogs';
const API_OPERATIVES = '/operatives';
const API_LOGOUT = '/auth/logout';

let selectedCareer = 'all';
let selectedPlan = 'all';
let selectedFilter = 'total';

document.addEventListener('DOMContentLoaded', () => {
    init();
    setupLogout();
});

async function init() {
    setupListeners();
    await fetchCareers();
    await updateDashboard();
}

function setupLogout() {
    document.getElementById('logoutBtn').addEventListener('click', async () => {
        try {
            const response = await fetch(API_LOGOUT, { method: 'POST' });
            if (response.ok) {
                window.location.href = '/index.html';
            }
        } catch (error) {
            console.error("Error al cerrar sesión:", error);
        }
    });
}

function setupListeners() {
    document.getElementById('searchInput').addEventListener('input', debounce(() => renderTable(), 300));
}

async function fetchCareers() {
    const careers = await apiRequest(`${API_CATALOGS}/careers?SchoolName=UPIICSA`);
    if (!careers) return;

    const container = document.getElementById('careerContainer');
    container.innerHTML = '<div class="selectable-item active" data-acronym="all">Todas las carreras</div>' +
        careers.map(c => `<div class="selectable-item" data-acronym="${c.acronym}">${c.name}</div>`).join('');

    container.querySelectorAll('.selectable-item').forEach(item => {
        item.onclick = async () => {
            container.querySelector('.active').classList.remove('active');
            item.classList.add('active');
            selectedCareer = item.dataset.acronym;
            selectedPlan = 'all';
            await fetchSyllabus();
            await updateDashboard();
        };
    });
}

async function fetchSyllabus() {
    const container = document.getElementById('planContainer');
    if (selectedCareer === 'all') {
        container.innerHTML = '<div class="selectable-item active" data-code="all">Todos los planes</div>';
        return;
    }

    const syllabus = await apiRequest(`${API_CATALOGS}/syllabus?schoolAcronym=UPIICSA&careerAcronym=${selectedCareer}`);
    container.innerHTML = '<div class="selectable-item active" data-code="all">Todos los planes</div>' +
        (syllabus ? syllabus.map(s => `<div class="selectable-item" data-code="${s.code}">${s.code}</div>`).join('') : '');

    container.querySelectorAll('.selectable-item').forEach(item => {
        item.onclick = async () => {
            container.querySelector('.active').classList.remove('active');
            item.classList.add('active');
            selectedPlan = item.dataset.code;
            await updateDashboard();
        };
    });
}

async function updateDashboard() {
    await fetchStats();
    await renderTable();
}

async function fetchStats() {
    const stats = await apiRequest(`${API_OPERATIVES}/stats?careerAcronym=${selectedCareer}`);
    const grid = document.getElementById('statsGrid');

    const labels = [
        { k: 'total', l: 'Total Alumnos' },
        { k: 'registered', l: 'Registrados' },
        { k: 'docInitial', l: 'Doc. Inicial' },
        { k: 'letterAccep', l: 'Aceptación' },
        { k: 'docFinal', l: 'Doc. Final' }
    ];

    grid.innerHTML = labels.map(item => `
            <div class="stat-card ${selectedFilter === item.k ? 'active' : ''}" onclick="filterByStat('${item.k}')">
                <div class="stat-val">${stats ? stats[item.k] : 0}</div>
                <div class="stat-lab">${item.l}</div>
            </div>
        `).join('');
}

window.filterByStat = function(key) {
    selectedFilter = key;
    updateDashboard();
};

async function renderTable() {
    const container = document.getElementById('studentTableBody');
    const searchInput = document.getElementById('searchInput').value.toLowerCase();

    const response = await fetch(`${API_OPERATIVES}/get-allStudents?page=0&size=50`);
    const data = await response.json();
    const students = data.content || [];

    if (students.length === 0) {
        container.innerHTML = '<tr><td colspan="4" style="text-align:center; padding:3rem; color:var(--text-muted)">No se encontraron alumnos registrados.</td></tr>';
        return;
    }

    /* CAMBIO CLAVE: Redirección usando s.enrollment (boleta) en lugar de ID */
    container.innerHTML = students.map(s => `
            <tr onclick="window.location.href='documentosInicio.html?enrollment=${s.enrollment}'">
                <td><strong>${s.offer?.syllabus?.code || 'N/A'}</strong></td>
                <td>${s.name} ${s.fLastName} ${s.mLastName}</td>
                <td>${s.enrollment}</td>
                <td><span class="visual-status" style="background:#e0f2fe; color:#0369a1; padding:4px 10px; border-radius:20px; font-size:0.7rem; font-weight:800;">En Proceso</span></td>
            </tr>
        `).join('');
}

async function apiRequest(url) {
    try {
        const resp = await fetch(url);
        return resp.ok ? await resp.json() : null;
    } catch (e) { return null; }
}

function debounce(func, wait) {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}