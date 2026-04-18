

const MAPA_DOCS_INICIALES = {
    'HOJAS_ASISTENCIA': 'Hojas de Asistencia',
    'INFORMES_MENSUALES': 'Informes Mensuales',
    'CARTA_TERMINO': 'Carta de Término',
};

document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('users');
    volverAtras();
    InicializarSeccionRevision({
        statusSeccion: 'DOC_FINAL',
        mapaNombres: MAPA_DOCS_INICIALES,
        endpointPost: '/documents/review',
    });
    renderUniversalFooter();
});
