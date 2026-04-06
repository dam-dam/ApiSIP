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

    try {
        const urlDocs = `${API_DOCS_STATUS}?processStatus=DOC_INICIAL`;
        const [respStatus, respDocs] = await Promise.all([
            fetch(API_STATUS),
            fetch(urlDocs)
        ]);

        if (respStatus.ok) stagesData = await respStatus.json();
        if (respDocs.ok) docsData = await respDocs.json();

        console.log("Docs recibidos:", docsData); // Revisa esto en tu consola, amiga
    } catch (e) {
        console.warn("Error cargando datos", e);
    }

    renderProgress(stagesData, docsData);
}

function renderProgress(apiData, docsData) {
    const stepper = document.getElementById('main-stepper');
    if (!stepper) return;

    const docsObligatorios = ["CEDULA_REGISTRO", "CONSTANCIA_IMSS", "CAPTURA_EMPRESA", "CAPTURA_ALUMNO", "HORARIO"];

    const haSubidoAlgo = docsData && docsData.some(doc => doc.fileName !== null && doc.status !== 'SIN_CARGA');

    const todoAprobadoReal = docsObligatorios.every(type => {
        const doc = docsData.find(d => d.typeCode === type);
        return doc && doc.status === 'CORRECTO';
    });

    const faltaSubirArchivo = docsObligatorios.some(type => {
        const doc = docsData.find(d => d.typeCode === type);
        return !doc || doc.status === 'SIN_CARGA' || !doc.fileName;
    });

    const estaEnRevision = !faltaSubirArchivo && !todoAprobadoReal;

    stepper.innerHTML = PHASES.map((name, idx) => {
        const data = apiData[idx] || {};
        let done = data.date && data.date !== "" && data.date !== "-";
        let current = data.isCurrent || false;
        let displayDate = data.date;
        let customStatus = "";

        // --- LÓGICA PARA FASE 0 (Registrado) ---
        if (idx === 0) {
            if (haSubidoAlgo) {
                done = true;
                current = false;
            } else {
                done = false;
                current = true;
                // Si la fecha de apiData es nula, usamos la fecha de hoy
                const fechaValida = (displayDate && displayDate !== "-") ? displayDate : new Date().toISOString();
                customStatus = `Inició: ${fmt(fechaValida)}`;
            }
        }

        // --- LÓGICA PARA FASE 1 (Doc Inicial) ---
        if (idx === 1) {
            if (todoAprobadoReal) {
                done = true;      // Palomita verde
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

        // --- FASE 3 Y 4 ---
        if ((idx === 2 || idx === 3) && todoAprobadoReal) {
            done = false;
            current = true;
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

    actualizarTarjetas(todoAprobadoReal);
}
//funcion paraa abrir cartar y cartas progreso
function actualizarTarjetas(docsInicialesOK, cartasOK) {
    const configuracion = [
        {
            id: 'card-cartas',
            link: 'registroCartas.html',
            tag: 'lock-tag-cartas',
            puedeAbrir: docsInicialesOK, //solo si docs iniciales estan aprobados
            mensaje: "Primero deben aceptar todos tus Documentos Iniciales."
        },
        {
            id: 'card-seguimiento',
            link: 'registroseguimiento.html',
            tag: 'lock-tag-seguimiento',
            puedeAbrir: cartasOK, // Solo si las cartas están aprobadas
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