
function renderUniversalFooter() {
    const footerElement = document.querySelector('footer');
    if (!footerElement) {
        console.warn("No se encontró la etiqueta <footer> para renderizar el componente.");
        return;
    }

    // Obtenemos el año actual para que siempre esté al día
    const anioActual = new Date().getFullYear();

    footerElement.innerHTML = `
        <div class="footer-container">
            <div class="footer-info">
                <p>
                    <strong>Instituto Politécnico Nacional</strong> |
                    UPIICSA |
                    © ${anioActual} Sistema ApiSIP
                </p>
            </div>
            <div class="footer-tagline">
                <span>Excelencia en la Formación Profesional</span>
            </div>
        </div>
    `;
}