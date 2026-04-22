function crearTarjetaDocumento(doc, dataRaw = {}, extraActionHtml = "", mostrarObservaciones = true) {
    const data = Array.isArray(dataRaw) ? dataRaw[0] : dataRaw;

    const statusBadge = data?.status || 'SIN CARGAR';
    let statusClass = 'none';
    
    const s = statusBadge.toUpperCase();
    if (s === 'CORRECTO' || s === 'ACEPTADO') {
        statusClass = 'correct';
    } else if (s === 'PENDIENTE') {
        statusClass = 'pending';
    } else if (s === 'INCORRECTO' || s === 'RECHAZADO') {
        statusClass = 'incorrect';
    }

    const fechaOriginal = data?.uploadDate || data?.date;
    const fechaFmt = (fechaOriginal && fechaOriginal !== "-") ? fechaOriginal : "--/--/---- --:--";
    
    const esCartaPresentacion = doc.typeCode === 'CARTA_PRESENTACION';
    if (esCartaPresentacion) mostrarObservaciones = false;

    const verDocHtml = data?.fileName ? `
        <a href="/view-documents/${data.fileName}" target="_blank" class="view-document-btn">
            <i class="fas fa-eye"></i> Ver documento
        </a>
    ` : `<span class="file-display" id="name-${doc.id}">No se ha seleccionado archivo</span>`;

    let zonaAccionHtml = "";

    if (esCartaPresentacion) {
        zonaAccionHtml = data?.fileName ? `
            <div class="column-item">
                <a href="/view-documents/${data.fileName}" download class="btn-browse" style="text-align: center; text-decoration: none;">
                    <i class="fas fa-file-download"></i> Descargar Mi Carta
                </a>
                ${verDocHtml}
                <p style="font-size: 0.75rem; color: var(--text-muted); margin-top: 8px; text-align: center;">
                    Este documento ha sido emitido por tu operador.
                </p>
            </div>
        ` : `
            <div class="column-item pendienteEmision">
                <i class="fas fa-clock"></i> Pendiente de emisión por el operador.
            </div>
        `;
    } else {
        zonaAccionHtml = `
            <div class="column-item">
                <input type="file" id="file-${doc.id}" style="display:none" accept=".pdf" ${statusClass === 'correct' ? 'disabled' : ''}>
                <label for="file-${doc.id}" class="btn-browse ${statusClass === 'correct' ? 'disabled' : ''}" id="btn-${doc.id}" >
                    <i class="fas fa-file-upload"></i> Seleccionar PDF
                </label>
                ${verDocHtml}
            </div>
        `;
    }

    const gridStyle = mostrarObservaciones ? 'siObservaciones' : 'noObservaciones';

    // IMPORTANTE: Aquí se inyectan las clases status-correct y badge-correct
    return `
        <div class="doc-card status-${statusClass}" id="card-${doc.id}">
            <div class="doc-header">
                <div class="title-group">
                    <span class="doc-title">${doc.label}</span>
                    <div id="date-${doc.id}" class="doc-date" >${fechaFmt}</div>
                </div>
                <span class="status-badge badge-${statusClass}" id="badge-${doc.id}">${statusBadge}</span>
            </div>

            <div class="doc-body-flex ${gridStyle}" padding: 1.5rem;">
                <div class="upload-area" >
                    <div style="flex: 1;">${zonaAccionHtml}</div>
                    ${extraActionHtml ? `<div style="flex: 1;">${extraActionHtml}</div>` : ''}
                </div>

                ${mostrarObservaciones ? `
                <div class="column-item comment-section">
                    <span class="comment-label">Observaciones</span>
                    <p class="comment-text" id="comment-${doc.id}">${data?.comment || data?.observations || 'Sin observaciones.'}</p>
                </div>
                ` : ''}
            </div>
        </div>
    `;
}