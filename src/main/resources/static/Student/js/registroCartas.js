document.addEventListener('DOMContentLoaded', () => {
    const contenedor = document.getElementById('contenedor-tarjeta-carta');

    // 1. Configuración de la tarjeta única
    const configCarta = { id: 'carta_aceptacion', label: 'Carta de Aceptación' };

    // Supongamos que pedimos los datos al servidor (o enviamos objeto vacío si es nuevo)
    const datosServidor = { status: 'SIN CARGAR', observations: 'Pendiente de entrega.' };

    // 2. Renderizar la tarjeta usando el componente reusable
    // No pasamos "accionEspecial" porque aquí no hay botón de generar
    contenedor.innerHTML = crearTarjetaDocumento(configCarta, datosServidor);

    // 3. Referencias a los elementos generados dinámicamente
    const entradaArchivo = document.getElementById(`file-${configCarta.id}`);
    const textoNombreArchivo = document.getElementById(`name-${configCarta.id}`);
    const botonEnviarCarta = document.getElementById('boton-enviar-carta');
    const contenedorVisorPdf = document.getElementById('contenedor-visor-pdf');

    // 4. Lógica de Vista Previa (Tu código original adaptado)
    entradaArchivo.addEventListener('change', (evento) => {
        const archivoSeleccionado = evento.target.files[0];

        if (archivoSeleccionado) {
            // 1. Validar formato PDF
            if (archivoSeleccionado.type !== 'application/pdf') {
                showModal('Formato no válido', 'Solo puedes subir archivos en formato PDF', 'error');
                evento.target.value = ''; // Limpiamos el input
                return;
            }

            // 2. Validar tamaño (Máximo 1MB como indica tu manual)
            const limiteTamano = 1024 * 1024; // 1MB en bytes
            if (archivoSeleccionado.size > limiteTamano) {
                showModal('Archivo muy pesado', 'El PDF no debe pesar más de 1MB', 'error');
                evento.target.value = '';
                return;
            }

            // 3. Actualizar la interfaz de la tarjeta
            // Usamos el ID dinámico que genera tu componente reusable
            const displayNombre = document.getElementById(`name-carta_aceptacion`);
            if (displayNombre) {
                displayNombre.textContent = archivoSeleccionado.name;
            }

            botonEnviarCarta.disabled = false;

            // 4. Generar Vista Previa "Gigante"
            // Primero liberamos la URL anterior si existía para optimizar memoria
            if (contenedorVisorPdf.querySelector('iframe')) {
                URL.revokeObjectURL(contenedorVisorPdf.querySelector('iframe').src);
            }

            const urlTemporal = URL.createObjectURL(archivoSeleccionado);

            // Inyectamos el iframe. El CSS que pusimos antes (80vh) hará que se vea grande.
            contenedorVisorPdf.innerHTML = `
            <iframe src="${urlTemporal}#toolbar=0&navpanes=0"
                    title="Vista previa del documento"
                    style="width: 100%; height: 80vh; border: none; border-radius: 12px;">
            </iframe>
        `;
        }
    });

    // 5. Función de Envío
    botonEnviarCarta.addEventListener('click', async () => {
        // ... Tu lógica de fetch/FormData se mantiene igual ...
        showModal('Enviando', 'Subiendo tu carta...', 'info');
    });
});