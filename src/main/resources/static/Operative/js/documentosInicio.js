const urlParams = new URLSearchParams(window.location.search);
const enrollment = urlParams.get('enrollment');
const API_SAVE_DOC = `/documents/review`; //post
const DOC_PATH = '/view-documents/';

let currentDocuments = [];
const MAPA_DOCS_INICIALES = {
    'CEDULA_REGISTRO': 'Cedula de Registro',
    'CONSTANCIA_IMSS': 'IMSS',
    'CAPTURA_EMPRESA': 'Captura de Pantalla (Empresa)',
    'CAPTURA_ALUMNO': 'Captura de Pantalla (Alumno)',
    'HORARIO': 'Horario',
};

document.addEventListener('DOMContentLoaded', async() => {
    if (!enrollment) {
        alert("No se especificó la boleta del alumno.");
        window.location.href = 'home.html';
        return;
    }
    renderUniversalHeader('operative');

   // 1. LLAMADA ÚNICA: El componente hace el fetch e inyecta el HTML solo
    const data = await SeccionInfoEstudiante(enrollment, 'DOC_INICIAL');

    // 2. Si el componente cargó bien los datos, seguimos con los documentos
    if (data && data.documents) {
        currentDocuments = data.documents;
        renderDocuments(currentDocuments); // El que ya hicimos de las tarjetas
        verificarAccesoACartas(currentDocuments);
    }
    setupActionButtons();
    renderUniversalFooter();
});

 //Función auxiliar para controlar la visibilidad del botón "Cartas"
 
function verificarAccesoACartas(documentos) {
    const btnContenedor = document.getElementById('irCartas');
    if (!btnContenedor) return;

    // Verificamos que TODOS los documentos tengan status "CORRECTO"
    const todosListos = documentos.length > 0 && documentos.every(doc => doc.status === 'CORRECTO');

    if (todosListos) {
        btnContenedor.classList.add("visible")
        console.log("Acceso a cartas habilitado: Todos los documentos están CORRECTOS.");
    } else {
        btnContenedor.classList.remove("visible");
    }
}

function renderDocuments(docs) {
    const container = document.getElementById('docs-list');
    container.innerHTML = '';

    if (!docs || docs.length === 0) {
        container.innerHTML = '<div style="padding:20px; text-align:center; color:#666;">No hay documentos iniciales para revisar.</div>';
        return;
    }

    const fragment = document.createDocumentFragment();

    docs.forEach((doc, index) => {
        // Configuramos la tarjeta para "Documentos Iniciales"
        const opciones = {
            index: index,
            docPath: DOC_PATH,
            mapaNombres: MAPA_DOCS_INICIALES, // Pasamos nuestro diccionario
            onView: (url, title) => viewPdf(url, title) // Conectamos con tu función viewPdf
        };

        // Creamos la tarjeta usando el componente reutilizable
        const tarjeta = TarjetaOperador(doc, opciones);
        fragment.appendChild(tarjeta);
    });

    container.appendChild(fragment);
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

                const commentArea = document.getElementById(`comm-${uniqueId}`);
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