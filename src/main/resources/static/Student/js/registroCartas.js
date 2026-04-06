document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('students');
    tituloFijo(
        "Registro de Carta de Aceptación",
        "Por favor, carga tus archivos en formato PDF. Peso no mayor a 1MB."
    );
    initRegistroCarta();
    renderUniversalFooter();
});
function initRegistroCarta() {
    const contenedor = document.getElementById('contenedor-tarjeta-carta');
    const contenedorVisorPdf = document.getElementById('contenedor-visor-pdf');
    const botonEnviarCarta = document.getElementById('boton-enviar-carta');




    // Configuración de la tarjeta
    const configCarta = { id: 'carta_aceptacion', label: 'Carta de Aceptación' };

    // Datos iniciales (esto podría venir de un fetch en el futuro)
    const datosServidor = { status: 'SIN CARGAR', observations: 'Pendiente de entrega.' };

    // 1. Renderizamos la tarjeta usando el componente global
    if (contenedor) {
        contenedor.innerHTML = crearTarjetaDocumento(configCarta, datosServidor);
    }

    // 2. Obtenemos las referencias a los elementos creados por el componente
    const entradaArchivo = document.getElementById(`file-${configCarta.id}`);
    const displayNombre = document.getElementById(`name-${configCarta.id}`);

    // 3. Listener para el cambio de archivo (Validaciones y Vista Previa)
    if (entradaArchivo) {
        entradaArchivo.addEventListener('change', (evento) => {
            const archivo = evento.target.files[0];

            if (archivo) {
                // Validación de Formato
                if (archivo.type !== 'application/pdf') {
                    showModal('Formato no válido', 'Solo puedes subir archivos PDF', 'error');
                    evento.target.value = '';
                    return;
                }

                // Validación de Tamaño (1MB)
                if (archivo.size > 1024 * 1024) {
                    showModal('Archivo muy pesado', 'El PDF no debe pesar más de 1MB', 'error');
                    evento.target.value = '';
                    return;
                }

                // UI: Nombre del archivo y habilitar botón
                if (displayNombre) displayNombre.textContent = archivo.name;
                if (botonEnviarCarta) botonEnviarCarta.disabled = false;

                // UI: Vista Previa Gigante
                mostrarVistaPrevia(archivo, contenedorVisorPdf);
            }
        });
    }

    // 4. Listener para el envío
    if (botonEnviarCarta) {
        botonEnviarCarta.addEventListener('click', () => {
            // Aquí iría tu fetch de envío
            showModal('Enviando', 'Subiendo tu carta de aceptación...', 'info');
        });
    }

    const urlDesdeServidor = datosServidor.url_carta_presentacion || null;

    // LLAMAMOS A LA FUNCIÓN DE DESCARGA
    manejarDescargaCarta(urlDesdeServidor);
}

// Función auxiliar para la vista previa (para no amontonar código arriba)
function mostrarVistaPrevia(archivo, contenedor) {
    if (!contenedor) return;

    // Liberar memoria de una URL previa si existe
    const iframeAnterior = contenedor.querySelector('iframe');
    if (iframeAnterior) URL.revokeObjectURL(iframeAnterior.src);

    const url = URL.createObjectURL(archivo);
    contenedor.innerHTML = `
        <iframe src="${url}#toolbar=0&navpanes=0"
                title="Vista previa del documento"
                style="width: 100%; height: 80vh; border: none; border-radius: 12px; background: white; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
        </iframe>
    `;
}

function manejarDescargaCarta(url) {
    const botonDescargar = document.getElementById('boton-descargar-carta');
    if (!botonDescargar) return;

    if (url && url !== "") {
        // Si hay URL, habilitamos el botón y cambiamos el estilo
        botonDescargar.disabled = false;
        botonDescargar.style.opacity = "1";
        botonDescargar.style.cursor = "pointer";

        // Al dar clic, abre el archivo
        botonDescargar.onclick = () => {
            window.open(url, '_blank');
            // Opcional: un mensajito de éxito
            console.log("Descarga de carta iniciada");
        };
    } else {
        // Si no hay URL, nos aseguramos que esté deshabilitado
        botonDescargar.disabled = true;
        botonDescargar.style.opacity = "0.5";
        botonDescargar.onclick = null;
    }
}