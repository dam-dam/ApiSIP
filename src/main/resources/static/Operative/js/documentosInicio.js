const urlParams = new URLSearchParams(window.location.search);
const enrollment = urlParams.get('enrollment'); // Obtenemos la boleta

// Endpoints ajustados al Controller
const API_REVIEW_DATA = `/operatives/student-review?enrollment=${enrollment}`;
// Endpoints de acción(POST)
const API_SAVE_DOC = `/operatives/review-document`;
const API_FINALIZE = `/operatives/finalize-review`;
const API_APPROVE_ACTA = `/operatives/approve-acceptance-act`;
//console.log("desde documentosInicio");
// RUTA BASE PARA VER DOCUMENTOS (IMPORTANTE: Esto corrige el error del backend)
// El backend tiene configurado /view-documents/**, así que debemos usar esa base
const DOC_PATH = '/view-documents/';

// Variable global para mantener el estado actual de los documentos cargados
let currentDocuments = [];

document.addEventListener('DOMContentLoaded', () => {
    if (!enrollment) {
        alert("No se especificó la boleta del alumno.");
        window.location.href = 'home.html';
        return;
    }
    loadStudentReview();
    setupActionButtons();
});

async function loadStudentReview() {
    try {
        console.log("Consultando:", API_REVIEW_DATA);
        const resp = await fetch(API_REVIEW_DATA);

        const contentType = resp.headers.get("content-type");
        if (!contentType || !contentType.includes("application/json")) {
            throw new Error(`Respuesta no válida del servidor. Status: ${resp.status}`);
        }

        if (!resp.ok) {
            console.error("Error HTTP:", resp.status);
            return;
        }

        const data = await resp.json();

        // 1. Llenar datos del encabezado
        document.getElementById('st-name').textContent = data.name || '--';
        document.getElementById('st-enrollment').textContent = data.enrollment || '--';
        document.getElementById('st-career').textContent = data.career || '--';
        document.getElementById('st-semester').textContent = data.semester || '--';
        document.getElementById('st-syllabus').textContent = data.syllabus || '--';

        // 2. Renderizar documentos
        currentDocuments = data.documents || [];
        renderDocuments(currentDocuments);

    } catch (e) {
        console.error("Error cargando datos:", e);
        const list = document.getElementById('docs-list');
        if(list) list.innerHTML = `<div style="color:red; text-align:center;">Error al cargar datos: ${e.message}</div>`;
    }
}

function renderDocuments(docs) {
    const container = document.getElementById('docs-list');

    if (!docs || docs.length === 0) {
        container.innerHTML = '<div style="padding:20px; text-align:center; color:#666;">No hay documentos requeridos para este estado del proceso.</div>';
        return;
    }

    container.innerHTML = docs.map((doc, index) => {
        const isRevisado = doc.status === 'CORRECTO';
        const isIncorrecto = doc.status === 'INCORRECTO';

        // Verificar si hay nombre de archivo real
        const hasFile = doc.fileName && doc.fileName.trim() !== '';
        const isSinDoc = !hasFile || doc.status === 'SIN_CARGAR';
        const fileUrl = hasFile ? `${DOC_PATH}${doc.fileName}` : '';

        //antes del cambio de la bd: const isCargado = (doc.status === 'CARGADO' || doc.status === 'EN_REVISION' || isIncorrecto) && hasFile;
        const isCargado = (doc.status === 'PENDIENTE' || isIncorrecto) && hasFile;
        let cardClass = '';
        if (isSinDoc) cardClass = 'card-sin-doc';
        else if (isRevisado) cardClass = 'card-revisado';
        else if (isIncorrecto) cardClass = 'card-cargado';
        else cardClass = 'card-cargado';

        let uploadDateStr = doc.uploadDate ? new Date(doc.uploadDate).toLocaleString('es-MX') : "Sin archivo cargado";
        const uniqueId = doc.typeCode.replace(/\s+/g, '_') + '_' + index;

        return `
                <div class="doc-review-card ${cardClass}" data-typecode="${doc.typeCode}">
                    <div class="doc-header">
                        <div class="doc-title-box">
                            <span class="doc-name">${doc.typeCode}</span>
                            <span class="doc-date">${uploadDateStr}</span>
                        </div>
                        <div style="display:flex; gap:0.8rem; align-items:center;">
                            ${isRevisado ? '<span class="locked-badge">Revisado Correcto</span>' : ''}
                            ${isIncorrecto ? '<span class="locked-badge" style="background:var(--error);">Corrección Solicitada</span>' : ''}

                            ${!isSinDoc && fileUrl ?
            `<button class="btn-view" onclick="viewPdf('${fileUrl}', '${doc.typeCode}')">Ver Archivo</button>` :
            '<button class="btn-view" disabled style="opacity:0.5; cursor:not-allowed;">Sin Archivo</button>'
        }
                        </div>
                    </div>

                    ${isCargado ? `
                    <div class="status-actions">
                        <label class="action-label opt-ok">
                            <input type="radio" name="st-${uniqueId}" value="REVISADO_CORRECTO" ${doc.status === 'REVISADO_CORRECTO' ? 'checked' : ''}>
                            Correcto
                        </label>
                        <label class="action-label opt-err">
                            <input type="radio" name="st-${uniqueId}" value="REVISADO_INCORRECTO" ${doc.status === 'REVISADO_INCORRECTO' ? 'checked' : ''}>
                            Incorrecto
                        </label>
                    </div>
                    <textarea class="comment-area" id="comm-${uniqueId}" placeholder="Observaciones de revisión...">${doc.comment || ''}</textarea>
                    ` : ''}

                    ${isSinDoc ? `<div style="font-size:0.85rem; color:var(--text-muted); font-style:italic;">El alumno aún no ha cargado este documento.</div>` : ''}
                </div>
            `;
    }).join('');
}

function viewPdf(url, title) {
    if (!url) return;

    const titleEl = document.getElementById('pdf-title');
    if (titleEl) titleEl.textContent = title;

    const container = document.getElementById('pdfContainer');
    if (container) {
        container.innerHTML = `<iframe src="${url}" style="width:100%; height:100%; border:none;"></iframe>`;
    }
}

function setupActionButtons() {
    const btnFinalize = document.getElementById('btn-finalize-review');
    if (btnFinalize) {
        btnFinalize.onclick = async () => {
            if (!enrollment) return;

            const reviews = [];

            currentDocuments.forEach((doc, index) => {
                if (!doc.fileName) return;

                const uniqueId = doc.typeCode.replace(/\s+/g, '_') + '_' + index;
                const radio = document.querySelector(`input[name="st-${uniqueId}"]:checked`);
                //console.log("Buscando input con name: st-" + uniqueId);
                const commentArea = document.getElementById(`comm-${uniqueId}`);

                //ver que esta madando el script
                //console.log("Revisando ID:", uniqueId, "Radio encontrado:", !!radio, "Comentario:", commentArea ? commentArea.value : "No existe");

                /* cambiarlo a como me dijo Yael
                if (radio) {
                    reviews.push({
                        typeCode: doc.typeCode,
                        status: radio.value,
                        comment: commentArea ? commentArea.value : ""
                    });
                }*/
                if (radio) {
                    reviews.push({
                        // Cambiamos typeCode por typeName
                        typeName: doc.typeCode,
                        // Convertimos el string 'REVISADO_CORRECTO' a true, y cualquier otro a false
                        approved: radio.value === 'REVISADO_CORRECTO',
                        // Mantenemos el comentario
                        comment: commentArea ? commentArea.value : ""
                    });
                }
            });

            //ver que est amandando el json
            console.log("JSON FINAL QUE VOY A ENVIAR AL SERVIDOR:");
            console.log(JSON.stringify(reviews, null, 2));

            if (reviews.length === 0) {
                showModal(
                    "Advertencia",
                    "Debes seleccionar al menos un estatus de un documento",
                    "info"
                )
                //alert("No has seleccionado 'Correcto' o 'Incorrecto' para ningún documento.");
                return;
            }

            btnFinalize.disabled = true;
            btnFinalize.textContent = "Guardando...";

            try {
                // SE QUITA EL FOR: Ahora se envía la lista 'reviews' completa en una sola petición
                const res = await fetch(`${API_SAVE_DOC}?enrollment=${enrollment}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(reviews) // Enviamos el arreglo completo []
                });

                if (res.ok) {
                    showModal(
                        "Guardado",
                        "Revisión general guardada correctamente.",
                        "success"
                    );
                    //alert("Revisión guardada correctamente.");
                    loadStudentReview();
                } else {
                    showModal(
                        "Error",
                        "Hubo un error al guardar la revisión, favor de actualizar la pagina",
                        "error"
                    );
                }
            } catch (e) {
                showModal(
                    "Uppss ...",
                    "Error de coneccion, favor de pedir ayuda al ingeniero de software",
                    "error"
                );
                console.log("Error de conexión." + e);
            } finally {
                btnFinalize.disabled = false;
                btnFinalize.textContent = "Finalizar Revisión General";
            }
        };
    }

    const btnApprove = document.getElementById('btn-approve-acta');
    if (btnApprove) {
        btnApprove.onclick = async () => {
            if(!confirm("¿Confirmar la aprobación del Acta de Aceptación?")) return;

            try {
                const res = await fetch(`${API_APPROVE_ACTA}?enrollment=${enrollment}`, { method: 'POST' });
                if (res.ok) alert("Acta aprobada con éxito.");
                else alert("Error al aprobar el acta.");
            } catch (e) {
                alert("Error de conexión.");
            }
        };
    }
}