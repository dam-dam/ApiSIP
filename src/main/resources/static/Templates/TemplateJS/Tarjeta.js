function crearTarjetaDocumento(doc, data = {}, extraActionHtml = "", mostrarObservaciones = true) {
    const estaAprobado = data.status === 'CORRECTO' || data.status === 'ACEPTADO';
    const fechaFmt = (data.date && data.date !== "-") ? data.date : "--/--/---- --:--";
    const statusBadge = data.status || 'SIN CARGAR';
    const badgeClass = statusBadge.toLowerCase().replace(/\s+/g, "-");
    
    // 1. Identificamos si es la carta
    const esCartaPresentacion = doc.typeCode === 'CARTA_PRESENTACION';

    // 2. Si es carta de presentación, forzamos que NO se vean las observaciones
    if (esCartaPresentacion) {
        mostrarObservaciones = false;
    }

    const verDocHtml = data.fileName ? `
        <div class="view-doc-container">
            <a href="/view-documents/${data.fileName}" target="_blank" class="view-document-btn">
                <i class="fas fa-eye"></i> Ver documento
            </a>
        </div>
    ` : `<span class="file-display" id="name-${doc.id}">No se ha seleccionado archivo</span>`;

    // 3. El grid se ajustará automáticamente a 'block' gracias a tu lógica existente
    const gridStyle = mostrarObservaciones ? 'display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));' : 'display: block;';

    let zonaAccionHtml = "";

    if (esCartaPresentacion) {
        zonaAccionHtml = data.fileName ? `
            <div class="download-section" style="width: 100%;">
                <a href="/view-documents/${data.fileName}" download class="btn-browse" style="width: 100%; display: block; text-align: center; text-decoration: none; background-color: #136b10;">
                    <i class="fas fa-file-download"></i> Descargar Mi Carta
                </a>
                <p style="font-size: 0.8rem; color: #666; margin-top: 8px; text-align: center;">
                    Este documento ha sido emitido por tu operador.
                </p>
            </div>
        ` : `
            <div class="waiting-section" style="text-align: center; color: #6b7280; padding: 1rem; border: 2px dashed #ccc; border-radius: 8px;">
                <i class="fas fa-clock"></i> Pendiente de emisión por el operador.
            </div>
        `;
    } else {
        zonaAccionHtml = `
            <input type="file" id="file-${doc.id}" style="display:none" accept=".pdf" ${estaAprobado ? 'disabled' : ''}>
            <label for="file-${doc.id}" class="btn-browse ${estaAprobado ? 'disabled' : ''}" id="btn-${doc.id}" style="width: 100%; display: block; text-align: center;">
                <i class="fas fa-file-upload"></i> Seleccionar PDF
            </label>
            ${verDocHtml}
        `;
    }

    return `
        <div class="doc-card status-${badgeClass}" id="card-${doc.id}">
            <div class="doc-header">
                <div class="title-group">
                    <span class="doc-title">${doc.label}</span>
                    <span id="date-${doc.id}" class="doc-date">(${fechaFmt})</span>
                </div>
                <span class="status-badge badge-${badgeClass}" id="badge-${doc.id}">${statusBadge}</span>
            </div>

            <div class="doc-body-flex" style="${gridStyle} gap: 1.5rem; padding: 1.5rem;">
                <div class="column-item upload-section">
                    ${zonaAccionHtml}
                </div>

                ${extraActionHtml ? `<div class="column-item action-section">${extraActionHtml}</div>` : ''}

                ${mostrarObservaciones ? `
                <div class="column-item comment-section">
                    <span class="comment-label">Observaciones</span>
                    <p class="comment-text" id="comment-${doc.id}">${data.observations || 'Sin observaciones.'}</p>
                </div>
                ` : ''}
            </div>
        </div>
    `;
}