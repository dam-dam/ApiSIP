const API_STATUS = '/student/process-status';
const API_LOGOUT = '/auth/logout';
const PHASES = ["Registrado", "Doc Inicial", "Cartas", "Doc Término", "Liberación" ];// , "Finalización de informes"

document.addEventListener('DOMContentLoaded', () => {
    loadUserProfile();
    loadData();
    setupLogout();
});

async function loadUserProfile() {
    try {
        const resp = await fetch('/users/my-name');
        if (resp.ok) {
            const data = await resp.json();
            const firstName = data.name.split(' ')[0];
            const lastName = data.fLastName.split(' ')[0];

            const nameEl = document.getElementById('user-pill-name');
            const initialEl = document.getElementById('user-pill-initial');

            if(nameEl) nameEl.textContent = `${firstName} ${lastName}`;
            if(initialEl) initialEl.textContent = firstName.charAt(0).toUpperCase();
        }
    } catch (error) {
        console.error("Error al cargar perfil:", error);
    }
}

async function loadData() {
    let stagesData = [];
    try {
        const resp = await fetch(API_STATUS);
        if (resp.ok) stagesData = await resp.json();
    } catch (e) { console.warn("Modo Offline"); }

    renderProgress(stagesData);
}

function renderProgress(apiData) {
    const stepper = document.getElementById('main-stepper');
    const faseDocInicial = apiData[1] || {};
    const docsAprobados = faseDocInicial.status === 'ACEPTADO' && faseDocInicial.date !== "-";

    stepper.innerHTML = PHASES.map((name, idx) => {
        const data = apiData[idx] || {};
        let done = data.date && data.date !== "" && data.date !== "-";
        let current = data.isCurrent || false;
        let displayDate = data.date;

        // --- LÓGICA DE ESPEJO PARA PASO 3 (CARTAS) ---
        // Si estamos en el índice 2 (Cartas) y los docs iniciales ya están aceptados:
        if (idx === 2 && docsAprobados) {
            done = false;   // Para que se vea verde 'active' y no con palomita todavía
            current = true; // Forzamos a que brille como fase actual
            displayDate = faseDocInicial.date; // Le "robamos" la fecha a Doc Inicial
        }

        let statusClass = done && !current ? 'completed' : (current ? 'active' : '');

        return `
            <div class="step ${statusClass}">
                <div class="dot">${(done && !current) ? '✓' : idx + 1}</div>
                <div class="step-info">
                    <span class="label">${name}</span>
                    <div class="date-container">
                        <span class="date-badge">
                            ${done ? 'Terminó: ' + fmt(displayDate) : (current ? 'En progreso' : '—')}
                        </span>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    const cardsParaActivar = ['card-cartas', 'card-seguimiento'];
    const tagSeguimiento = document.getElementById('lock-tag-seguimiento');
    const tagCartas = document.getElementById('lock-tag-cartas');

    cardsParaActivar.forEach(id => {
        const card = document.getElementById(id);
        if (!card) return;

        if (docsAprobados) {
            card.classList.remove('locked');
            card.onclick = null;
            // Ocultamos candados
            if (id === 'card-cartas' && tagCartas) tagCartas.style.display = 'none';
            if (id === 'card-seguimiento' && tagSeguimiento) tagSeguimiento.style.display = 'none';
        } else {
            card.classList.add('locked');
            card.onclick = (e) => {
                e.preventDefault();
                showModal(
                    "Upsss...",
                    "Primero deben aceptar todos tus Documentos Iniciales.",
                    "info"
                );
            };
        }
    });
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

function fmt(d) {
    try {
        const date = new Date(d);
        if (isNaN(date.getTime())) return d;
        return date.toLocaleDateString('es-MX', { day: '2-digit', month: '2-digit', year: 'numeric' });
    } catch (e) { return d; }
}