const urlParams = new URLSearchParams(window.location.search);
const enrollment = urlParams.get('enrollment');

// Mapa para que los nombres se vean bonitos (puedes agregar más si quieres)
const MAPA_DOCS_CARTAS = {
    'CARTA_PRESENTACION': 'Carta de Presentación',
    'CARTA_ACEPTACION': 'Carta de Aceptación'
};

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Validaciones de boleta
    if (!enrollment) {
        alert("No se especificó la boleta del alumno.");
        window.location.href = 'home.html';
        return;
    }

    // 2. Renderizamos el encabezado azul
    renderUniversalHeader('operative');

    // 3. Inicializamos con la lógica genérica
    InicializarSeccionRevision({
        statusSeccion: 'CARTAS',
        enrollment: enrollment,
        mapaNombres: MAPA_DOCS_CARTAS,
        // Usamos la misma función que ya te funcionaba para traer la data
        endpointGet: async () => {
            const data = await SeccionInfoEstudiante(enrollment, 'CARTAS');
            // IMPORTANTE: El componente espera un ARRAY, así que le pasamos solo los documentos
            return data && data.documents ? data.documents : [];
        },
        endpointPost: '/documents/review',
        containerId: 'docs-list' // Asegúrate de que este ID sea el de tu HTML
    });

    renderUniversalFooter();
});