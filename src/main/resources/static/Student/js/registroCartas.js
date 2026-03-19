document.addEventListener('DOMContentLoaded', () => {
    const entradaArchivo = document.getElementById('entrada-archivo');
    const textoNombreArchivo = document.getElementById('texto-nombre-archivo');
    const botonEnviarCarta = document.getElementById('boton-enviar-carta');
    const contenedorVisorPdf = document.getElementById('contenedor-visor-pdf');

    //Función para mostrar el PDF en cuanto se selecciona
    entradaArchivo.addEventListener('change', (evento) => {
        const archivoSeleccionado = evento.target.files[0];

        if (archivoSeleccionado) {
            // Validar que realmente sea un PDF
            if (archivoSeleccionado.type !== 'application/pdf') {
                showModal('Error', 'Solo puedes subir archivos en formato PDF', 'error');
                entradaArchivo.value = '';
                return;
            }

            // Validar tamaño (1MB = 1,048,576 bytes)
            const limiteTamano = 1024 * 1024;
            if (archivoSeleccionado.size > limiteTamano) {
                showModal('Archivo muy pesado', 'El PDF no debe pesar más de 1MB', 'error');
                entradaArchivo.value = '';
                return;
            }

            // Actualizar la interfaz
            textoNombreArchivo.textContent = archivoSeleccionado.name;
            botonEnviarCarta.disabled = false;

            // Generar URL temporal para la vista previa
            const urlTemporal = URL.createObjectURL(archivoSeleccionado);
            contenedorVisorPdf.innerHTML = `
                <iframe src="${urlTemporal}"
                        width="100%"
                        height="100%"
                        style="border:none;">
                </iframe>`;
        }
    });

    // 2. Función para manejar el clic en Guardar
    botonEnviarCarta.addEventListener('click', async () => {
        const archivoFinal = entradaArchivo.files[0];
        if (!archivoFinal) return;

        botonEnviarCarta.disabled = true;
        botonEnviarCarta.textContent = "Procesando envío...";

        const datosFormulario = new FormData();
        datosFormulario.append('archivo', archivoFinal);
        datosFormulario.append('tipoDocumento', 'CARTA_ACEPTACION');

        try {
            console.log("Iniciando subida de la carta...");

            // Simulación de envío al servidor
            setTimeout(() => {
                showModal('¡Listo!', 'Tu carta de aceptación se guardó correctamente.', 'success');
                botonEnviarCarta.textContent = "Guardar y Enviar";
                botonEnviarCarta.disabled = false;
            }, 1500);

        } catch (error) {
            showModal('Error de red', 'No pudimos conectar con el servidor', 'error');
            console.error("Error en la subida:", error);
            botonEnviarCarta.disabled = false;
        }
    });
});