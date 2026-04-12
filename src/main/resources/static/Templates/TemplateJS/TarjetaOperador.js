function TarjetaOperador(doc, options = {}) {
    // 1. Configuración por defecto (Mapping y Rutas)
    const {
        index = Math.floor(Math.random() * 1000),
        docPath = '',
        mapaNombres = {}, // Para convertir "CEDULA_REGISTRO" en "Cédula de Registro"
        extraClass = '',  // Por si quieres pasarle 'card-carta' o 'card-final'
        onView = (url, name) => console.log("Visualizando:", url),
    } = options;

    // 2. Lógica de Negocio (Mantenemos tu lógica base)
    const isRevisado = doc.status === 'CORRECTO';
    const isIncorrecto = doc.status === 'INCORRECTO';
    const hasFile = doc.fileName && doc.fileName.trim() !== '';
    const isSinDoc = !hasFile || doc.status === 'SIN_CARGAR';
    const isCargado = (doc.status === 'PENDIENTE' || isIncorrecto) && hasFile;
    
    const fileUrl = hasFile ? `${docPath}${doc.fileName}` : '';
    const uploadDateStr = doc.uploadDate 
        ? new Date(doc.uploadDate).toLocaleString('es-MX') 
        : "Sin archivo cargado";
    
    // Aquí ocurre la magia del nombre personalizado
    const nombreAMostrar = mapaNombres[doc.typeCode] || doc.typeCode;
    const uniqueId = doc.typeCode.replace(/\s+/g, '_') + '_' + index;

    // 3. Clases Dinámicas
    let cardClass = `doc-review-card ${extraClass}`;
    if (isSinDoc) cardClass += ' card-sin-doc';
    else if (isRevisado) cardClass += ' card-revisado';
    else if (isIncorrecto) cardClass += ' card-error';
    else cardClass += ' card-cargado';

    // 4. Construcción del DOM
    const card = document.createElement('div');
    card.className = cardClass;
    card.setAttribute('data-typecode', doc.typeCode);

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

    // 5. Eventos
    if (hasFile) {
        card.querySelector('.btn-view').addEventListener('click', () => onView(fileUrl, nombreAMostrar));
    }

    return card;
}