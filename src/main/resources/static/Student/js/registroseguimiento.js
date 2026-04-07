
const DOC_CONFIG = [
    { id: 'reportes', label: 'Reportes Mensuales', typeCode: 'REPORTES_MENSUALES' },
    { id: 'cartaAceptacion', label: 'Carta de Aceptacion', typeCode: 'CARTA_ACEPTACION' },
];


document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('students');
    tituloFijo(
        "Documentos de Finalizacion",
        "Por favor, carga tus archivos en formato PDF. Peso no mayor a 1MB."
    );
    initUI();
    renderUniversalFooter();
});

function initUI(docsData = []) {
    const container = document.getElementById('docs-container');

    container.innerHTML = DOC_CONFIG.map(doc => {
        const dataDoc = docsData.find(d => d.typeCode === doc.id) || {};
        const estaAprobado = dataDoc.status === 'CORRECTO';

        // Definimos la acción especial solo para la cédula
        let accionEspecial = "";
        if (doc.id === 'cedula' && !estaAprobado) {
            accionEspecial = `
                <a href="generarCedula.html" class="btn-generate-inline">
                    <i class="fas fa-file-signature"></i> Generar Cédula
                </a>`;
        }

        return crearTarjetaDocumento(doc, dataDoc, accionEspecial);
    }).join('');

    // Re-activar los listeners de archivos
    DOC_CONFIG.forEach(doc => {
        const input = document.getElementById(`file-${doc.id}`);
        if(input) {
            input.addEventListener('change', (e) => {
                if (e.target.files.length > 0) {
                    document.getElementById(`name-${doc.id}`).textContent = e.target.files[0].name;
                }
            });
        }
    });
}