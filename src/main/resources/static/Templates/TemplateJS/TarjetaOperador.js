function TarjetaOperador(doc, options = {}) {
    
    const {
        index = Math.floor(Math.random() * 1000),
        docPath = '',
        mapaNombres = {}, 
        extraClass = '',  
        onView = (url, name) => console.log("Visualizando:", url),
    } = options;

    // Lógica de Negocio
    const isRevisado = doc.status === 'CORRECTO';
    const isIncorrecto = doc.status === 'INCORRECTO';
    const hasFile = doc.fileName && doc.fileName.trim() !== '';
    const isSinDoc = !hasFile || doc.status === 'SIN_CARGAR';
    const isCargado = (doc.status === 'PENDIENTE' || isIncorrecto) && hasFile;
    
    const esCartaPresentacion = doc.typeCode === 'CARTA_PRESENTACION';
    
    const fileUrl = hasFile ? `${docPath}${doc.fileName}` : '';
    const uploadDateStr = doc.uploadDate 
        ? new Date(doc.uploadDate).toLocaleString('es-MX') 
        : "Sin archivo cargado";
    
    const nombreAMostrar = mapaNombres[doc.typeCode] || doc.typeCode;
    const typeCodeSeguro = doc.typeCode || 'DOC_SIN_NOMBRE';
    const uniqueId = typeCodeSeguro.replace(/\s+/g, '_') + '_' + index;

    let cardClass = `doc-review-card ${extraClass}`;
    if (isSinDoc && !esCartaPresentacion) cardClass += ' card-sin-doc';
    else if (isRevisado) cardClass += ' card-revisado';
    else if (isIncorrecto) cardClass += ' card-error';
    else cardClass += ' card-cargado';

    const card = document.createElement('div');
    card.className = cardClass;
    card.setAttribute('data-typecode', doc.typeCode);

    
    if (esCartaPresentacion) {
        card.innerHTML = `
            <div class="doc-header">
                <div class="doc-title-box">
                    <span class="doc-name">${nombreAMostrar}</span>
                    <span class="doc-date" id="name-CP">${hasFile ? doc.fileName : 'No se ha subido archivo'}</span>
                </div>
                <div style="display:flex; gap:0.8rem; align-items:center;">
                    ${hasFile ? `<button class="btn-view">Ver Archivo</button>` : ''}
                </div>
            </div>
            <div class="upload-zone-operador">
                <label for="file-CP" class="btn-select-file">
                    <i class="fas fa-file-upload"></i> Seleccionar PDF
                </label>
                <input type="file" id="file-CP" accept="application/pdf" style="display: none;">
                <p style="font-size: 0.8rem; color: #666; margin-top: 5px;">Peso no mayor a 1MB.</p>
            </div>
        `;
    } else {
        card.innerHTML = `
            <div class="doc-header">
                <div class="doc-title-box">
                    <span class="doc-name">${nombreAMostrar}</span>
                    <span class="doc-date">${uploadDateStr}</span>
                </div>
                <div style="display:flex; gap:0.8rem; align-items:center;">
                    ${isRevisado ? '<span class="locked-badge">Revisado Correcto</span>' : ''}
                    ${isIncorrecto ? '<span class="locked-badge" style="background:var(--error);">Corrección Solicitada</span>' : ''}
                    
                    <button class="btn-view" ${!hasFile ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''}>
                        ${hasFile ? 'Ver Archivo' : 'Sin Archivo'}
                    </button>
                </div>
            </div>

            ${isCargado ? `
                <div class="status-actions">
                    <label class="action-label opt-ok">
                        <input type="radio" name="st-${uniqueId}" value="REVISADO_CORRECTO" ${doc.status === 'REVISADO_CORRECTO' ? 'checked' : ''}>
                        Correcto
                    </label>
                    <label class="action-label opt-err">
                        <input type="radio" name="st-${uniqueId}" value="REVISADO_INCORRECTO" ${doc.status === 'REVISADO_INCORRECTO' ? 'checked' : ''}>
                        Incorrecto
                    </label>
                </div>
                <textarea class="comment-area" id="comm-${uniqueId}" placeholder="Observaciones de revisión...">${doc.comment || ''}</textarea>
            ` : ''}

            ${isSinDoc ? `<div class="sin-doc-msg">El alumno aún no ha cargado este documento.</div>` : ''}
        `;
    }

    // Evento común para el botón de ver
    if (hasFile) {
        const btnView = card.querySelector('.btn-view');
        if (btnView) {
            btnView.addEventListener('click', (e) => {
                e.preventDefault();
                onView(fileUrl, nombreAMostrar);
            });
        }
    }

    return card;
}