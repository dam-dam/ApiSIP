function crearTarjetaDocumento(doc, data = {}, extraActionHtml = "") {
    const estaAprobado = data.status === 'CORRECTO';
    const fechaFmt = (data.date && data.date !== "-") ? data.date : "--/--/---- --:--";
    const statusBadge = data.status || 'SIN CARGAR';
    const badgeClass = statusBadge.toLowerCase().replace(" ", "-");
    
    const verDocHtml = data.fileName ? `
        <div class="view-doc-container">
            <a href="uploads/${data.fileName}" target="_blank" class="view-document-btn">
                <i class="fas fa-eye"></i> Ver documento
            </a>
        </div>
    ` : `<span class="file-display" id="name-${doc.id}">No se ha seleccionado archivo</span>`;

    return `
        <div class="doc-card" id="card-${doc.id}">
            <div class="doc-header">
                <div class="title-group">
                    <span class="doc-title">${doc.label}</span>
                    <span id="date-${doc.id}" class="doc-date">(${fechaFmt})</span>
                </div>
                <span class="status-badge badge-${badgeClass}" id="badge-${doc.id}">${statusBadge}</span>
            </div>

            <div class="doc-body-flex">
                <div class="column-item upload-section">
                    <input type="file" id="file-${doc.id}" style="display:none" accept=".pdf" ${estaAprobado ? 'disabled' : ''}>
                    <label for="file-${doc.id}" class="btn-browse ${estaAprobado ? 'disabled' : ''}" id="btn-${doc.id}">
                        Seleccionar PDF
                    </label>
                    ${verDocHtml}
                </div>

                ${extraActionHtml ? `<div class="column-item action-section">${extraActionHtml}</div>` : ''}

                <div class="column-item comment-section">
                    <span class="comment-label">Observaciones</span>
                    <p class="comment-text" id="comment-${doc.id}">${data.observations || 'Sin observaciones.'}</p>
                </div>
            </div>
        </div>
    `;
}