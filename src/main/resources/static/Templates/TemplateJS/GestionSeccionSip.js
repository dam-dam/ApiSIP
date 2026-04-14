
const urlParams = new URLSearchParams(window.location.search);
const enrollment = urlParams.get('enrollment');

async function InicializarSeccionRevision(config) {
    const {
        statusSeccion,      // 'DOC_INICIAL', 'CARTAS', 'DOC_FINAL'
        mapaNombres,        // El diccionario de nombres
        endpointPost,       // URL para guardar
        proximaEtapa,       // { idBoton: 'irCartas', functionVerificar: verificarAccesoACartas }
        contenedorListaId = 'docs-list'
    } = config;

    // Validación de boleta 
    if (!enrollment) {
        alert("No se especificó la boleta del alumno.");
        window.location.href = 'home.html';
        return;
    }
    try {
        // Carga de Datos Única
        const data = await SeccionInfoEstudiante(enrollment, statusSeccion);

        if (data && data.documents) {
            currentDocuments = data.documents;
            
            // Renderizado Dinámico
            renderDocumentsGenerico(currentDocuments, mapaNombres, contenedorListaId);
            
            // Verificación de flujo (Si existe botón para la siguiente etapa)
            if (proximaEtapa) {
                proximaEtapa.functionVerificar(currentDocuments, proximaEtapa.idBoton);
            }
        }

        // Configurar botones de guardado (POST)
        setupActionButtonsGenerico(endpointPost);

    } catch (error) {
        console.error("Error al inicializar la sección:", error);
        
    }
}

// Función de renderizado que usa el mapa de nombres inyectado
function renderDocumentsGenerico(docs, mapa, containerId) {
    const container = document.getElementById(containerId);

    console.log("Contenedor encontrado:", container); 
    console.log("Documentos a renderizar:", docs);    
    
    if (!container) return;
    container.innerHTML = '';

    const fragment = document.createDocumentFragment();
    
    docs.forEach((doc, index) => {
        const tipoKey = doc.typeName || doc.typeCode;
        const nombreVisible = mapa[tipoKey] || tipoKey;
        const urlParaVer = `/view-documents/${doc.fileName}`;

        const tarjeta = TarjetaOperador(doc, {
            index: index,
            docPath: '/view-documents/',
            mapaNombres: mapa,
            tipoDocumento: tipoKey,
            onView: () => {
                if (urlParaVer && urlParaVer !== "") {
                    viewPdf(urlParaVer, nombreVisible);
                } else {
                    showModal("Aviso", "Este documento no tiene un archivo cargado para visualizar.", "info");
                }
            }
        });
        fragment.appendChild(tarjeta);
    });
    
    container.appendChild(fragment);
}


function setupActionButtonsGenerico(endpoint) {
    const btnFinalize = document.getElementById('btn-finalize-review');
    if (!btnFinalize) return;

    btnFinalize.onclick = async () => {
        if (!currentDocuments || currentDocuments.length === 0) return;

        const reviews = [];
        currentDocuments.forEach((doc, index) => {
            const uniqueId = doc.typeCode.replace(/\s+/g, '_') + '_' + index;
            const radio = document.querySelector(`input[name="st-${uniqueId}"]:checked`);
            const commentArea = document.getElementById(`comm-${uniqueId}`);

            // Solo si el operador seleccionó una opción, lo agregamos al arreglo
            if (radio) {
                reviews.push({
                    typeName: doc.typeCode,
                    approved: radio.value === 'REVISADO_CORRECTO',
                    comment: commentArea ? commentArea.value.trim() : ""
                });
            }
        });

        //Al menos un documento debe estar evaluado
        if (reviews.length === 0) {
            showModal(
                "Información",
                "Debes evaluar al menos un documento para poder enviar la revisión.",
                "info"
            );
            return;
        }
        btnFinalize.disabled = true;
        btnFinalize.textContent = "Enviando...";

        try {
            const res = await fetch(`${endpoint}?enrollment=${enrollment}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(reviews)
            });

            if (res.ok) {
                showModal("¡Listo!", "Se han enviado las correcciones al alumno.", "success");
                setTimeout(() => location.reload(), 1500); 
            } else {
                throw new Error("Error en el servidor");
            }

        } catch (e) {
            console.error("Error en el POST:", e);
            showModal("Error", "No se pudo conectar con el servidor de la UPIICSA.", "error");
        } finally {
            btnFinalize.disabled = false;
            btnFinalize.textContent = "Finalizar Revisión General";
        }
    };
}

function viewPdf(url, title) {
    const container = document.getElementById('pdfContainer');
    const titleSpan = document.getElementById('pdf-title');

    if (!container) return;
    if (titleSpan) titleSpan.textContent = title;

    container.innerHTML = `
        <embed 
            src="${url}" 
            type="application/pdf" 
            width="100%" 
            height="100%" 
            style="border: none; border-radius: 8px;"
        />
    `;
}