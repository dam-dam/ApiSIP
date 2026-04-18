
const urlParams = new URLSearchParams(window.location.search);
const enrollment = urlParams.get('enrollment');

async function InicializarSeccionRevision(config) {
    const {
        statusSeccion,
        mapaNombres,
        endpointPost,
        proximaEtapa,
        contenedorListaId = 'docs-list'
    } = config;

    if (!enrollment) {
        alert("No se especificó la boleta del alumno.");
        window.location.href = 'home.html';
        return;
    }

    try {
        const [dataInicial, dataCartas, dataTermino] = await Promise.all([
            SeccionInfoEstudiante(enrollment, 'DOC_INICIAL'),
            SeccionInfoEstudiante(enrollment, 'CARTAS'),
            SeccionInfoEstudiante(enrollment, 'DOC_FINAL')
        ]);

        // Decidimos qué mostrar en la lista principal según el statusSeccion que recibimos
        let dataActual;
        if (statusSeccion === 'DOC_INICIAL') dataActual = dataInicial;
        else if (statusSeccion === 'CARTAS') dataActual = dataCartas;
        else if (statusSeccion === 'DOC_FINAL') dataActual = dataTermino;

        if (dataActual && dataActual.documents) {
            currentDocuments = dataActual.documents;
            
            // 1. Renderizamos los documentos de la sección actual
            renderDocumentsGenerico(currentDocuments, mapaNombres, contenedorListaId);
            
            // 2. Pasamos el objeto con TODAS las etapas a la navegación
            // para que pueda validar la cascada (A -> B -> C)
            gestionarNavegacionRevision(statusSeccion, {
                inicial: dataInicial.documents || [],
                cartas: dataCartas.documents || [],
                termino: dataTermino.documents || []
            });
      
            if (proximaEtapa) {
                proximaEtapa.functionVerificar(currentDocuments, proximaEtapa.idBoton);
            }
        }

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

            if (radio) {
                reviews.push({
                    typeName: doc.typeCode,
                    approved: radio.value === 'REVISADO_CORRECTO',
                    comment: commentArea ? commentArea.value.trim() : ""
                });
            }
        });

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