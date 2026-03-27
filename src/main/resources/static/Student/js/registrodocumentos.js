const API_GET_STATUS = '/documents/my-status'; //get
const API_POST_UPLOAD = '/documents/upload';//post
const DOC_PATH = '/view-documents/'; //post

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
        const resp = await fetch('/students/data');
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

function initUI(docsData = []) {
    const container = document.getElementById('docs-container');

    container.innerHTML = DOC_CONFIG.map(doc => {
        const dataDoc = docsData.find(d => d.typeCode === doc.id) || {};
        const estaAprobado = dataDoc.status === 'CORRECTO';

        // Definimos la acción especial solo para la cédula
        let accionEspecial = "";
        if (doc.id === 'cedula' && !estaAprobado) {
            accionEspecial = `
                <a href="generarCedula.html" class="btn-generate-inline">
                    <i class="fas fa-file-signature"></i> Generar Cédula
                </a>`;
        }

        return crearTarjetaDocumento(doc, dataDoc, accionEspecial);
    }).join('');

    // Re-activar los listeners de archivos
    DOC_CONFIG.forEach(doc => {
        const input = document.getElementById(`file-${doc.id}`);
        if(input) {
            input.addEventListener('change', (e) => {
                if (e.target.files.length > 0) {
                    document.getElementById(`name-${doc.id}`).textContent = e.target.files[0].name;
                }
            });
        }
    });
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
    } catch (e) {
        console.error("Error cargando estatus:", e);
    }
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
       // display.innerHTML = `<a href="${DOC_PATH}${data.fileName}" target="_blank" class="file-link">${data.fileName}</a>`;
        // Actualizar solo el nombre del archivo con su enlace e icono
        display.innerHTML = `
            <a href="${DOC_PATH}${data.fileName}" target="_blank" class="file-link view-document-btn">
                <i class="fa-solid fa-eye"></i> Ver documento
            </a>
        `;
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
