const API_STATUS = '/students/process-status';//get
const API_LOGOUT = '/auth/logout';//post
const API_DOCS_STATUS = '/documents/my-status';//get
const PHASES = ["Registrado", "Doc Inicial", "Cartas", "Doc Término", "Liberación" ];// solo para vista

document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('students');
    loadData();
    renderUniversalFooter();
});
async function loadData() {
    let stagesData = [];
    let docsData = [];
    let docsCarts = [];
    let docsTermino = [];

    try {
        const urlDocs = `${API_DOCS_STATUS}?processStatus=DOC_INICIAL`;
        const urlCarts = `${API_DOCS_STATUS}?processStatus=CARTAS`;
        const urlTermino = `${API_DOCS_STATUS}?processStatus=DOC_FINAL`;
        const [respStatus, respDocs, respCarts, respTermino] = await Promise.all([
            fetch(API_STATUS),
            fetch(urlDocs),
            fetch(urlCarts),
            fetch(urlTermino)
        ]);

        if (respStatus.ok) stagesData = await respStatus.json();
        if (respDocs.ok) docsData = await respDocs.json();
        if (respCarts.ok) docsCarts = await respCarts.json();
        if (respTermino.ok) docsTermino = await respTermino.json();

        console.log("Docs inicio:", docsData); 
        console.log("Docs Cartas:", docsCarts); 
        console.log("Docs Término:", docsTermino); 
    } catch (e) {
        console.warn("Error cargando datos", e);
    }

    renderProgress(stagesData, docsData, docsCarts, docsTermino);
}

function renderProgress(apiData, docsData, docsCarts, docsTermino) {
    const stepper = document.getElementById('main-stepper');
    if (!stepper) return;

    const docsObligatorios = ["CEDULA_REGISTRO", "CONSTANCIA_IMSS", "CAPTURA_EMPRESA", "CAPTURA_ALUMNO", "HORARIO"];
    const docsCartsFase2 = ["CARTA_ACEPTACION"/*",CARTA_PRESENTACION"*/ ];
    const docsTerminoFase3 = ["HOJAS_ASISTENCIA", "INFORMES_MENSUALES", "CARTA_TERMINO"];

    const haSubidoAlgo = docsData && docsData.some(doc => doc.fileName !== null && doc.status !== 'SIN_CARGA');
    const haSubidoAlgoCarts = docsCarts && docsCartsFase2.some(doc => doc.fileName !== null && doc.status !== 'SIN_CARGA');
    const haSubidoAlgoTermino = docsTermino && docsTerminoFase3.some(doc => doc.fileName !== null && doc.status !== 'SIN_CARGA');

    let nuevoStatusCalculado = "DOC_INICIAL";
    // ===================== Aprobado
    const todoAprobadoReal = docsObligatorios.every(type => {
        const doc = docsData.find(d => d.typeCode === type);
        return doc && doc.status === 'CORRECTO';
    });

    const todoAprobadoRealCarts = docsCartsFase2.every(type => {
        const doc = docsCarts.find(d => d.typeCode === type);
        return doc && doc.status === 'CORRECTO';
    });

    const todoAprobadoRealTermino = docsTerminoFase3.every(type => {
        const doc = docsTermino.find(d => d.typeCode === type);
        return doc && doc.status === 'CORRECTO';
    });

    // ================== falta subir
    const faltaSubirArchivo = docsObligatorios.some(type => {
        const doc = docsData.find(d => d.typeCode === type);
        return !doc || doc.status === 'SIN_CARGA' || !doc.fileName;
    });
    const faltaSubirArchivoCarts = docsCartsFase2.some(type => {
        const doc = docsCarts.find(d => d.typeCode === type);
        return !doc || doc.status === 'SIN_CARGA' || !doc.fileName;
    });
    const faltaSubirArchivoTermino = docsTerminoFase3.some(type => {
        const doc = docsTermino.find(d => d.typeCode === type);
        return !doc || doc.status === 'SIN_CARGA' || !doc.fileName;
    });

    // ================== En revisión
    const estaEnRevision = !faltaSubirArchivo && !todoAprobadoReal;
    const estaEnRevisionCarts = !faltaSubirArchivoCarts && !todoAprobadoRealCarts;
    const estaEnRevisionTermino = !faltaSubirArchivoTermino && !todoAprobadoRealTermino;


    stepper.innerHTML = PHASES.map((name, idx) => {
        const data = apiData[idx] || {};
        let current = data.isCurrent || false;
        let done = data.date && data.date !== "" && data.date !== "-"; 
        let displayDate = data.date;
        let customStatus = "";
        const fechaValida = (displayDate && displayDate !== "-") ? displayDate : new Date().toISOString();

        const etapaActivaBackend = apiData.find(d => d.isCurrent)?.stageName || "";

        // --- LÓGICA PARA FASE 0 (Registrado) ---
        if (idx === 0) {
        if (etapaActivaBackend !== "REGISTRADO") { // Si ya no es la actual, es porque ya pasó
            done = true;
            current = false;
        } else {
            done = false;
            current = true;
            customStatus = `Inició: ${fmt(fechaValida)}`;
        }
    }

    // --- LÓGICA PARA FASE 1 (Doc Inicial) ---
    
        if (idx === 0) {
            if (haSubidoAlgo) {
                done = true;
                current = false;
            } else {
                done = false;
                current = true;
                customStatus = `Inició: ${fmt(fechaValida)}`;
            }
        }

        // --- LÓGICA PARA FASE 1 (Doc Inicial) ---
        if (idx === 1) {
            if (todoAprobadoReal) {
                done = true;
                current = false;
            } else if (haSubidoAlgo) {
                done = false;
                current = true;
                customStatus = faltaSubirArchivo ? "Documentación incompleta" : "Revisando...";
            } else {
                done = false;
                current = false;
            }
        }

        // --- FASE 2 desbloquear Cartas ---
        if (idx === 2) {
            if (todoAprobadoRealCarts) {
                done = true; 
                current = false;
                const ultimaCarta = docsCarts.find(d => d.typeCode === "CARTA_ACEPTACION");
                displayDate = ultimaCarta ? ultimaCarta.uploadDate : displayDate;
            } else if (todoAprobadoReal) {
                done = false;
                current = true;
                if (haSubidoAlgoCarts) {
                    customStatus = faltaSubirArchivoCarts ? "Documentación incompleta" : "Revisando...";
                }
            } else {
                done = false;
                current = false;
            }
        }
        //faase 3 desbloquear doc termino
         
        if (idx === 3) {
            if (todoAprobadoRealTermino) {
                done = true; 
                current = false;
                const ultimoTermino = docsTermino.find(d => d.typeCode === "CARTA_TERMINO");
                displayDate = ultimoTermino ? ultimoTermino.uploadDate : displayDate;
            } else if (todoAprobadoRealCarts) {
                done = false;
                current = true;
                if (haSubidoAlgoTermino) {
                    customStatus = faltaSubirArchivoTermino ? "Documentación incompleta" : "Revisando...";
                }
            } else {
                done = false;
                current = false;
            }
        }

        if(idx === 4 && todoAprobadoRealTermino){
            done = true; 
            current = false; 
        }



        let statusClass = done ? 'completed' : (current ? 'active' : '');

        return `
            <div class="step ${statusClass}">
                <div class="dot">${done ? '✓' : idx + 1}</div>
                <div class="step-info">
                    <span class="label">${name}</span>
                    <div class="date-container">
                        <span class="date-badge">
                            ${done ? 'Terminó: ' + fmt(displayDate) :
                             (current ? (customStatus || 'En progreso') : '—')}
                        </span>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    // if (todoAprobadoRealTermino) {
    //     nuevoStatusCalculado = 'LIBERACION';
    // } else if (todoAprobadoRealCarts) {
    //     nuevoStatusCalculado = 'DOC_FINAL';
    // } else if (todoAprobadoReal) {
    //     nuevoStatusCalculado = 'CARTAS';
    // }

    // localStorage.setItem('currentProcessStatus', nuevoStatusCalculado);
    actualizarTarjetas(todoAprobadoReal, todoAprobadoRealCarts, todoAprobadoRealTermino);
    console.log(todoAprobadoReal);
    console.log(todoAprobadoRealCarts);
    console.log(todoAprobadoRealTermino);
}

function actualizarTarjetas(docsInicialesOK, cartasOK, terminoOK) {
    const configuracion = [
        {
            id: 'card-cartas',
            link: 'registroCartas.html',
            tag: 'lock-tag-cartas',
            puedeAbrir: docsInicialesOK,
            mensaje: "Primero deben aceptar todos tus Documentos Iniciales."
        },
        {
            id: 'card-seguimiento',
            link: 'registroseguimiento.html',
            tag: 'lock-tag-seguimiento',
            puedeAbrir: cartasOK, 
            mensaje: "Primero deben Aceptar tus Cartas."
        }
    ];

    configuracion.forEach(item => {
        const card = document.getElementById(item.id);
        const lock = document.getElementById(item.tag);
        if (!card) return;

        if (item.puedeAbrir) {
            // DESBLOQUEADO
            card.classList.remove('locked');
            if (lock) lock.style.display = 'none';
            card.onclick = () => window.location.href = item.link;
            card.style.cursor = "pointer";
            card.style.opacity = "1";
        } else {
            // BLOQUEADO
            card.classList.add('locked');
            if (lock) lock.style.display = 'flex';
            card.style.cursor = "not-allowed";
            card.onclick = (e) => {
                e.preventDefault();
                showModal("Aviso", item.mensaje, "info");
            };
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
