const API_OPERADOR_UPLOAD = '/documents/uploadLetter'; 
const DOC_PATH = '/view-documents/'; 

const DOC_CONFIG = [
    { id: 'CP', label: 'Carta de Presentación', typeCode: 'CARTA_PRESENTACION' },
    
];
const MAPA_DOCS_INICIALES = {
    'CARTA_PRESENTACION': 'Carta de Presentación',
    'CARTA_ACEPTACION': 'Carta de Aceptación',
};
document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('users');
    volverAtras();
     InicializarSeccionRevision({
        statusSeccion: 'CARTAS',
        mapaNombres: MAPA_DOCS_INICIALES,
        endpointPost: '/documents/review',
        proximaEtapa: { 
            idBoton: 'irTermino', 
            functionVerificar: verificarAccesoTermino 
        }
    });
    renderUniversalFooter();
    const btnGuardar = document.getElementById('btn-global-save');
    if (btnGuardar) {
        btnGuardar.addEventListener('click', handleGlobalUpload);
    }
});

function verificarAccesoTermino(documentos, idBotonContenedor) {
    const btnContenedor = document.getElementById(idBotonContenedor);
    if (!btnContenedor) return;
    const cartaAceptacion = documentos.find(doc => doc.typeCode === 'CARTA_ACEPTACION');
    const accesoHabilitado = cartaAceptacion && cartaAceptacion.status === 'CORRECTO';

    if (accesoHabilitado) {
        btnContenedor.classList.add("visible");
        console.log("Acceso habilitado: La CARTA_ACEPTACION está CORRECTA.");
        
        const btnReal = btnContenedor.querySelector('button');
        if (btnReal) {
            btnReal.onclick = () => {
                window.location.href = `documentosTermino.html?enrollment=${enrollment}`;
            };
        }
    } else {
        btnContenedor.classList.remove("visible");
        console.log("Acceso denegado: CARTA_ACEPTACION aún no está CORRECTA.");
    }
}


function setupUploadListener(enrollment) {
    const input = document.getElementById('file-CP');
    const label = document.getElementById('name-CP'); 

    if (input) {
        input.addEventListener('change', (e) => { 
            if (e.target.files.length > 0) {
                const file = e.target.files[0];
                if(label) label.textContent = `Archivo seleccionado: ${file.name}`;
            }
        });
    }
}
async function subirCartaPresentacion(enrollment, file) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('enrollment', enrollment); 

    try {
        const response = await fetch('/documents/uploadLetter', {
            method: 'POST',
            body: formData 
            
        });

        if (response.ok) {
            showModal('¡Éxito!', 'Archivo vinculado a la boleta ' + enrollment, 'success');
        } else {
            const errorMsg = await response.text();
            console.error("Error en el servidor:", errorMsg);
            showModal('Error', 'El servidor no pudo procesar el archivo.', 'error');
        }
    } catch (error) {
        console.error("Error:", error);
    }
}

async function obtenerEstadoAlumno(enrollment) {
    try {
       
        const response = await fetch(`/students/toReview?enrollment=${enrollment}&processStatus=CARTAS`);
        if (!response.ok) throw new Error("Error al obtener datos del alumno");
        return await response.json();
    } catch (error) {
        console.error("Error en obtenerEstadoAlumno:", error);
        return { documents: [] }; 
    }
}

