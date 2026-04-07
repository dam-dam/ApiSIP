function crearTarjetaDocumento(doc, data = {}, extraActionHtml = "", mostrarObservaciones = true) {
    const estaAprobado = data.status === 'CORRECTO' || data.status === 'ACEPTADO';
    const fechaFmt = (data.date && data.date !== "-") ? data.date : "--/--/---- --:--";
    const statusBadge = data.status || 'SIN CARGAR';
    const badgeClass = statusBadge.toLowerCase().replace(/\s+/g, "-");

    const verDocHtml = data.fileName ? `
        <div class="view-doc-container">
            <a href="uploads/${data.fileName}" target="_blank" class="view-document-btn">
                <i class="fas fa-eye"></i> Ver documento
            </a>
        </div>
    ` : `<span class="file-display" id="name-${doc.id}">No se ha seleccionado archivo</span>`;

    // Ajuste de columnas: Si no hay observaciones, usamos 1 sola columna para que se estire
    const gridStyle = mostrarObservaciones ? 'display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));' : 'display: block;';

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
                <div class="column-item upload-section" >
                    <input type="file" id="file-${doc.id}" style="display:none" accept=".pdf" ${estaAprobado ? 'disabled' : ''}>
                    <label for="file-${doc.id}" class="btn-browse ${estaAprobado ? 'disabled' : ''}" id="btn-${doc.id}" style="width: 100%; display: block; text-align: center;">
                        <i class="fas fa-file-upload"></i> Seleccionar PDF
                    </label>
                    ${verDocHtml}
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