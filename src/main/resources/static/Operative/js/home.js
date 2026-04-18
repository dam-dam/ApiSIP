const API_CATALOGS = '/catalogs';
const API_STUDENTS = '/students';
const API_LOGOUT = '/auth/logout';

let selectedCareer = 'all';
let selectedPlan = 'all';
let selectedFilter = 'total';
let currentPage = 0; // Control de página actual

document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('users');
    init();
    renderUniversalFooter();
});

async function init() {
    setupListeners();
    await fetchCareers();
    await fetchSyllabus();
    await updateDashboard();
}

function setupListeners() {
    document.getElementById('searchInput').addEventListener('input', debounce(() => {
        currentPage = 0; 
        renderTable();
    }, 300));
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
            currentPage = 0;

            await fetchSyllabus();
            await renderTable();
            await fetchStats();
        };
    });
}

async function fetchSyllabus() {
    const container = document.getElementById('planContainer');
    let url;

    if (selectedCareer === 'all') {
        url = `${API_CATALOGS}/allSyllabus?schoolAcronym=UPIICSA`;
    } else {
        url = `${API_CATALOGS}/syllabus?schoolAcronym=UPIICSA&careerAcronym=${selectedCareer}`;
    }

    const syllabus = await apiRequest(url);

    container.innerHTML = '<div class="selectable-item active" data-code="all">Todos los planes</div>' +
        (syllabus ? syllabus.map(s => `<div class="selectable-item" data-code="${s.code}">${s.code}</div>`).join('') : '');

    container.querySelectorAll('.selectable-item').forEach(item => {
        item.onclick = async () => {
            container.querySelector('.active').classList.remove('active');
            item.classList.add('active');
            selectedPlan = item.dataset.code;
            currentPage = 0;

            await renderTable();
            await fetchStats();
        };
    });
}

async function updateDashboard() {
    await fetchStats();
    await renderTable();
}

async function fetchStats() {
    const url = `${API_STUDENTS}/stats?careerAcronym=${selectedCareer}&planCode=${selectedPlan}`;
    const stats = await apiRequest(url);
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
    currentPage = 0;
    updateDashboard();
};

async function renderTable() {
    const container = document.getElementById('studentTableBody');
    const searchTerm = document.getElementById('searchInput').value.trim();
    let url = `${API_STUDENTS}/filtered?page=${currentPage}&career=${selectedCareer}&plan=${selectedPlan}`;

    if (searchTerm) {
        url += `&search=${encodeURIComponent(searchTerm)}`;
    }

    const response = await fetch(url);
    const data = await response.json();
    const students = data.content || [];

    try {
        if (students.length === 0) {
            container.innerHTML = '<tr><td colspan="4" style="text-align:center; padding:3rem; color:var(--text-muted)">No hay alumnos con estos filtros.</td></tr>';
            updatePaginationUI(0, 0, 0, true, true);
            return;
        }

        container.innerHTML = students.map(s => `
            <tr onclick="window.location.href='documentosInicio.html?enrollment=${s.enrollment}'" style="cursor:pointer;">
                <td><strong>${s.syllabusCode || 'N/A'}</strong></td>
                <td>${s.name} ${s.fLastName} ${s.mLastName}</td>
                <td>${s.enrollment}</td>
                <td><span class="visual-status">${s.processStatus || 'N/A'}</span></td>
            </tr>
        `).join('');

       
        const p = data.page;
        const start = (p.number * p.size) + 1;
        const end = (p.number * p.size) + students.length;

        
        const isFirst = p.number === 0;
        const isLast = p.number >= p.totalPages - 1;

        updatePaginationUI(start, end, p.totalElements, isFirst, isLast);

    } catch (e) { console.error("Error:", e); }
}

function updatePaginationUI(start, end, total, isFirst, isLast) {
    const legend = document.getElementById('paginationLegend');
    if (legend) {
        legend.innerText = `Mostrando ${start} a ${end} de ${total} alumnos`;
    }

    const btnPrev = document.getElementById('btnPrev');
    const btnNext = document.getElementById('btnNext');

    if (btnPrev) btnPrev.disabled = isFirst;
    if (btnNext) btnNext.disabled = isLast;
}

window.changePage = function(step) {
    currentPage += step;
    renderTable();
};

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