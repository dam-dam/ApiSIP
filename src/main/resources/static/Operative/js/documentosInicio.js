const urlParams = new URLSearchParams(window.location.search);
const enrollment = urlParams.get('enrollment');


const MAPA_DOCS_INICIALES = {
    'CEDULA_REGISTRO': 'Cedula de Registro',
    'CONSTANCIA_IMSS': 'IMSS',
    'CAPTURA_EMPRESA': 'Captura de Pantalla (Empresa)',
    'CAPTURA_ALUMNO': 'Captura de Pantalla (Alumno)',
    'HORARIO': 'Horario',
};

document.addEventListener('DOMContentLoaded', () => {
    InicializarSeccionRevision({
        statusSeccion: 'DOC_INICIAL',
        mapaNombres: MAPA_DOCS_INICIALES,
        endpointPost: '/documents/review',
        proximaEtapa: { 
            idBoton: 'irCartas', 
            functionVerificar: verificarAccesoACartas 
        }
    });
});
function verificarAccesoACartas(documentos, idBotonContenedor) {
    const btnContenedor = document.getElementById(idBotonContenedor);
    if (!btnContenedor) return;

    // Verificamos que TODOS los documentos tengan status "CORRECTO"
    const todosListos = documentos.length > 0 && documentos.every(doc => doc.status === 'CORRECTO');

    if (todosListos) {
        btnContenedor.classList.add("visible");
        console.log("Acceso habilitado: Todos los documentos están CORRECTOS.");
        
        // Configurar el clic del botón para navegar
        const btnReal = btnContenedor.querySelector('button');
        if (btnReal) {
            btnReal.onclick = () => {
                window.location.href = `Operadorcartas.html?enrollment=${enrollment}`;
            };
        }
    } else {
        btnContenedor.classList.remove("visible");
    }
}