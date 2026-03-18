const API_GET_STATUS = '/documents/my-status';
const API_POST_UPLOAD = '/documents/upload';
const DOC_PATH = '/view-documents/';

// Mapeo exacto según tu catálogo de backend
const DOC_CONFIG = [
    { id: 'cedula', label: 'Cédula de Registro', typeCode: 'CEDULA_REGISTRO' },
    { id: 'imss', label: 'Constancia de Vigencia (IMSS)', typeCode: 'CONSTANCIA_IMSS' },
    { id: 'sisae-empresa', label: 'Captura Empresa (SISAE)', typeCode: 'CAPTURA_EMPRESA' },
    { id: 'sisae-alumno', label: 'Captura Alumno (SISAE)', typeCode: 'CAPTURA_ALUMNO' },
    { id: 'horario', label: 'Copia de Horario (SAES)', typeCode: 'HORARIO' }
];

document.addEventListener('DOMContentLoaded', () => {
    loadUserProfile();
    setupLogout();
    initUI();
    loadStatus();
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

function setupLogout() {
    const btnLogout = document.getElementById('logoutBtn');
    if (!btnLogout) return;

    btnLogout.addEventListener('click', async () => {
        try {
            const response = await fetch('/auth/logout', { method: 'POST' });
            if (response.ok) {
                window.location.href = '/index.html';
            }
        } catch (error) {
            console.error("Error al intentar cerrar sesión:", error);
        }
    });
}

function initUI() {
    const container = document.getElementById('docs-container');
    container.innerHTML = DOC_CONFIG.map(doc => `
                    <div class="doc-card status-none" id="card-${doc.id}">
                        <div class="doc-header">
                            <div style="display: flex; align-items: baseline; gap: 10px;">
                                <span class="doc-title">${doc.label}</span>
                                <span id="date-${doc.id}" style="font-size: 0.85rem; color: #6b7280; font-weight: 500;">(--/--/---- --:--)</span>
                            </div>
                            <span class="status-badge badge-none" id="badge-${doc.id}">Sin Cargar</span>
                        </div>
                        <div class="doc-body">
                            <div class="upload-area">
                                <div class="file-controls">
                                    <input type="file" id="file-${doc.id}" style="display:none" accept=".pdf">
                                    <label for="file-${doc.id}" class="btn-browse" id="btn-${doc.id}">Seleccionar PDF</label>
                                    <span class="file-display" id="name-${doc.id}">No se ha seleccionado archivo</span>
                                </div>
                            </div>
                            <div class="comment-area">
                                <span class="comment-label">Observaciones</span>
                                <p class="comment-text" id="comment-${doc.id}">Pendiente de carga inicial.</p>
                            </div>
                        </div>
                    </div>
                `).join('');

    // Listeners para cambios de archivo local
    DOC_CONFIG.forEach(doc => {
        document.getElementById(`file-${doc.id}`).addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                document.getElementById(`name-${doc.id}`).textContent = e.target.files[0].name;
            }
        });
    });

    document.getElementById('btn-global-save').addEventListener('click', handleGlobalUpload);
}

async function loadStatus() {
    try {
        const resp = await fetch(API_GET_STATUS);
        if (!resp.ok) return;
        const data = await resp.json();

        data.forEach(item => {
            const config = DOC_CONFIG.find(c => c.typeCode === item.typeCode);
            if (config) updateCard(config.id, item);
        });
    } catch (e) { console.error("Error cargando estatus:", e); }
}

function updateCard(id, data) {
    const card = document.getElementById(`card-${id}`);
    const badge = document.getElementById(`badge-${id}`);
    const comment = document.getElementById(`comment-${id}`);
    const display = document.getElementById(`name-${id}`);
    const input = document.getElementById(`file-${id}`);
    const labelBtn = document.getElementById(`btn-${id}`);
    const dateEl = document.getElementById(`date-${id}`);

    card.className = "doc-card"; // Reset
    let statusCls = "status-none", badgeCls = "badge-none", label = "Sin Cargar";

    // Mapeo de estados del backend
    if (data.status === "CORRECTO") {
        statusCls = "status-correct"; badgeCls = "badge-correct"; label = "Aceptado";
        input.disabled = true;
        labelBtn.style.opacity = "0.5";
        labelBtn.style.pointerEvents = "none";
    } else if (data.status === "INCORRECTO") {
        statusCls = "status-incorrect"; badgeCls = "badge-incorrect"; label = "Rechazado";
    } else if (data.status === "PENDIENTE") {
        statusCls = "status-pending"; badgeCls = "badge-pending"; label = "En Revisión";
    } else if (data.fileName) {
        statusCls = "status-pending"; badgeCls = "badge-pending"; label = "Pendiente";
    }

    card.classList.add(statusCls);
    badge.textContent = label;
    badge.className = `status-badge ${badgeCls}`;
    comment.textContent = data.comment || "Sin observaciones.";

    if (data.fileName) {
        // Lógica para la fecha
        let dateStr = '(--/--/---- --:--)';
        if (data.uploadDate) {
            const dateObj = new Date(data.uploadDate);
            dateStr = dateObj.toLocaleDateString('es-MX', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
                hour: '2-digit',
                minute:'2-digit'
            }).replace(',', '');
        }
        // Actualizar la fecha en el header
        if(dateEl) dateEl.textContent = dateStr;

        // Actualizar solo el nombre del archivo con su enlace
        display.innerHTML = `<a href="${DOC_PATH}${data.fileName}" target="_blank" class="file-link">${data.fileName}</a>`;
    }
}

/**
 * Procesa la subida de múltiples archivos
 */
async function handleGlobalUpload() {
    const btn = document.getElementById('btn-global-save');
    let filesSent = 0;

    btn.disabled = true;
    btn.textContent = "Subiendo archivos...";

    for (const config of DOC_CONFIG) {
        const input = document.getElementById(`file-${config.id}`);
        if (input.files.length > 0) {
            const formData = new FormData();
            formData.append('file', input.files[0]);
            formData.append('type', config.typeCode);
            try {
                await fetch(API_POST_UPLOAD, { method: 'POST', body: formData });
                filesSent++;
            } catch (e) { console.error("Error subiendo " + config.label); }
        }
    }

    if (filesSent > 0) {
        // Modal de Éxito
        showModal(
            '¡Envío Exitoso!',
            'Tus documentos han sido enviados correctamente.',
            'success',
            () => location.reload()
        );
    } else {
        // Modal de Error/Advertencia
        showModal(
            'Sin cambios',
            'No has seleccionado nuevos archivos para subir.',
            'error'
        );
        btn.disabled = false;
        btn.textContent = "Guardar Cambios";
    }
}
/*

function showModal(title, message, type, callback) {
    const modal = document.getElementById('custom-modal');
    const iconBox = document.getElementById('modal-icon-box');
    const titleEl = document.getElementById('modal-title');
    const msgEl = document.getElementById('modal-message');
    const btn = document.getElementById('btn-modal-close');

    titleEl.textContent = title;
    msgEl.textContent = message;

    // Configurar icono y color según tipo
    if (type === 'success') {
        iconBox.className = 'modal-icon-box icon-success';
        iconBox.innerHTML = `
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                    </svg>`;
    } else {
        iconBox.className = 'modal-icon-box icon-error';
        iconBox.innerHTML = `
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
                    </svg>`;
    }

    // Mostrar modal
    modal.classList.add('active');

    // Manejar cierre
    btn.onclick = () => {
        modal.classList.remove('active');
        if (callback) callback();
    };
}
*/