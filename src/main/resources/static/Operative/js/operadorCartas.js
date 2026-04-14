
const MAPA_CARTAS = {
    'CARTA_PRESENTACION': 'Carta de Presentación',
    'CARTA_ACEPTACION': 'Carta de Aceptación Firmada'
};

document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('operative');
    volverAtras();
    
    InicializarSeccionRevision({
        statusSeccion: 'CARTAS',
        mapaNombres: MAPA_CARTAS,
        endpointPost: '/documents/review',
        contenedorListaId: 'docs-list'
    });
    renderUniversalFooter();
});