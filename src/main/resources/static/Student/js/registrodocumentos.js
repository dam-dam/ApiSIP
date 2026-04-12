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
    renderUniversalHeader('students');
    tituloFijo(
        "Documentación Inicial",
        "Por favor, carga tus archivos en formato PDF. Peso no mayor a 1MB."
    );
    initUI();
    loadStatus();
    renderUniversalFooter();
    const btnGuardar = document.getElementById('btn-global-save');
    if (btnGuardar) {
        btnGuardar.addEventListener('click', handleGlobalUpload);
    }
});

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
        const urlCompleta = API_GET_STATUS + "?processStatus=DOC_INICIAL";
        const resp = await fetch(urlCompleta);

        if (!resp.ok) {
            console.error("Error HTTP:", resp.status);
            return;
        }

        const isJson = resp.headers.get("content-type")?.includes("application/json");
        if (!isJson) {
            console.error("El servidor no devolvió JSON. Probablemente te redirigió al login.");
            return;
        }

        const data = await resp.json();
        console.log("¡Por fin llegaron los datos!:", data);

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
    const textoOriginal = btn.textContent;

    // 1. Obtener solo los inputs que SÍ tienen archivos
    const inputsConArchivos = DOC_CONFIG.map(config => ({
        config,
        input: document.getElementById(`file-${config.id}`)
    })).filter(item => item.input && item.input.files.length > 0);

    if (inputsConArchivos.length === 0) {
        showModal('Sin cambios', 'No has seleccionado archivos nuevos.', 'warning');
        return;
    }

    btn.disabled = true;
    btn.textContent = "Subiendo documentos...";

    // 2. Enviamos uno por uno (como le gusta al Back actual)
    for (const item of inputsConArchivos) {
        const formData = new FormData();
        formData.append('file', item.input.files[0]);
        formData.append('type', item.config.typeCode);

        try {
            console.log(`Subiendo ${item.config.label} a la ruta de servidor...`);
            
            const response = await fetch(API_POST_UPLOAD, {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                filesSent++;
            } else {
                // Si uno falla, capturamos el error pero seguimos con los demás
                const errorMsg = await response.text();
                console.error(`Error en ${item.config.label}: ${errorMsg}`);
            }
        } catch (e) {
            console.error(`Error de conexión en ${item.config.label}:`, e);
        }
    }

    // 3. Resultado final
    if (filesSent > 0) {
        showModal('¡Éxito!', `Se guardaron ${filesSent} archivo(s) en el servidor.`, 'success', () => location.reload());
    } else {
        showModal('Error', 'No se pudo subir ningún archivo. Revisa la consola.', 'error');
        btn.disabled = false;
        btn.textContent = textoOriginal;
    }
} 