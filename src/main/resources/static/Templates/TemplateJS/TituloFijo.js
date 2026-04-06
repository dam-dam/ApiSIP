function tituloFijo(titulo = "Título de Página", descripcion = "Descripción de la sección.") {
    const introElement = document.querySelector('.page-intro');
    if (!introElement) {
        console.warn("No se encontró el contenedor .page-intro en el HTML.");
        return;
    }

    introElement.innerHTML = `
        <h2>${titulo}</h2>
        <p>${descripcion}</p>
    `;
}