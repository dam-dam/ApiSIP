

const MAPA_DOCS_INICIALES = {
    'CEDULA_REGISTRO': 'Cedula de Registro',
    'CONSTANCIA_IMSS': 'IMSS',
    'CAPTURA_EMPRESA': 'Captura de Pantalla (Empresa)',
    'CAPTURA_ALUMNO': 'Captura de Pantalla (Alumno)',
    'HORARIO': 'Horario',
};

document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('users');
    volverAtras();
    InicializarSeccionRevision({
        statusSeccion: 'DOC_INICIAL',
        mapaNombres: MAPA_DOCS_INICIALES,
        endpointPost: '/documents/review',
        onDataLoad:(documentos) => {
            gestionarNavegacionRevision(seccionActual, documentos);
        }
    });
    renderUniversalFooter();
});
function verificarAccesoA(documentos, idBotonContenedor) {
    const btnContenedor = document.getElementById(idBotonContenedor);
    if (!btnContenedor) return;
    const todosListos = documentos.length > 0 && documentos.every(doc => doc.status === 'CORRECTO');

    if (todosListos) {
        btnContenedor.classList.add("visible");
        console.log("Acceso habilitado: Todos los documentos están CORRECTOS.");
        const btnReal = btnContenedor.querySelector('button');
        if (btnReal) {
            btnReal.onclick = () => {
                window.location.href = `operadorCartas.html?enrollment=${enrollment}`;
            };
        }
    } else {
        btnContenedor.classList.remove("visible");
    }
}