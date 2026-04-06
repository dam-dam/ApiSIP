document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('students');
    initOperador();
    renderUniversalFooter();

});
function initOperador() {
    const contenedor = document.getElementById('contenedor-tarjeta-operador');
    const visor = document.getElementById('visor-pdf-operador');
    const botonPublicar = document.getElementById('btn-subir-carta');
    
    const config = {
        id: 'carta_presentacion',
        label: 'Carta de Presentación (Generada)'
    };
    const datos = {
        status: 'PENDIENTE DE SUBIR',
        observations: 'Sube el archivo final con sellos y firmas.'
    };

    if (contenedor) {
        contenedor.innerHTML = crearTarjetaDocumento(config, datos, "", false);
    }

    const inputArchivo = document.getElementById(`file-${config.id}`);
    const labelNombre = document.getElementById(`name-${config.id}`);


    // Lógica de carga y vista previa
    if (inputArchivo) {
        inputArchivo.addEventListener('change', (e) => {
            const archivo = e.target.files[0];
            if (archivo && archivo.type === 'application/pdf') {

                // Validar tamaño 1MB
                if (archivo.size > 1024 * 1024) {
                    showModal('Error', 'El archivo supera 1MB', 'error');
                    return;
                }

                if (labelNombre) labelNombre.textContent = archivo.name;
                if (botonPublicar) botonPublicar.disabled = false;

                // Mostrar vista previa para que el operador revise
                mostrarVistaPrevia(archivo, visor);
            }
        });
    }

    // Botón de Publicar
    if (botonPublicar) {
        botonPublicar.addEventListener('click', () => {
            showModal('Confirmación', '¿Estás segura de publicar esta carta? El alumno podrá verla de inmediato.', 'info');
            // Aquí iría el fetch real hacia el servidor
        });
    }
}