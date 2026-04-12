async function renderUniversalHeader(tipoUsuario = 'students') {
    /*Logo en la pestaña del navegador*/
    let favicon = document.querySelector("link[rel~='icon']");
    if (!favicon) {
        favicon = document.createElement('link');
        favicon.rel = 'icon';
        document.getElementsByTagName('head')[0].appendChild(favicon);
    }
    favicon.href = '../Imagenes/Logo/32/SIP_bg.png';

    //COmponente HEADER
    const headerElement = document.querySelector('header');
    if (!headerElement) return;

    // 1. Insertar el HTML base
    // ui-components.js (dentro de tu función de renderUniversalHeader)

    headerElement.innerHTML = `
    <div class="logo-group">
        <div class="school-logo">
                    <img src="../Imagenes/Logo/svg/SIP_alpha.svg" alt="Logo Escuela" class="school-logo-img">
        </div>

        <div class="header-brand">
            <div class="title-box">
                <h1>SISTEMA INSTITUCIONAL DE PRÁCTICAS PROFESIONALES</h1>
                <p>${tipoUsuario === 'students' ? 'Portal Estudiantil' : 'Panel de Control Administrativo'}</p>
            </div>
        </div>
    </div>
    
    <div class="user-actions">
        <div class="user-pill" id="userPill" title="Ver Perfil">
            <span id="user-pill-name">Cargando...</span>
            <div class="user-avatar" id="user-pill-initial">?</div>
        </div>

        <div class="action-group">
            <button class="btn-logout" id="logoutBtn" title="Cerrar Sesión">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <polyline points="16 17 21 12 16 7"></polyline>
                    <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
            </button>
        </div>
    </div>

    
`;

/*
<div class="logo-group">
        <img src="../Imagenes/ipn.png" alt="IPN" class="logo-img">
        <img src="../Imagenes/logo blanco.png" alt="UPIICSA" class="logo-img">
</div>
*/
    // 2. Configurar Eventos
    setupHeaderEvents(tipoUsuario);

    // 3. Cargar datos del perfil
    loadHeaderProfile(tipoUsuario);
}

function setupHeaderEvents(tipoUsuario) {
    // Evento Perfil
    const pill = document.getElementById('userPill');
    if (pill) {
        pill.onclick = () => {
            const path = tipoUsuario === 'students' ? 'perfil.html' : 'perfil_operador.html';
            window.location.href = path;
        };
    }

    // Evento Logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.onclick = async () => {
            try {
                const response = await fetch('/auth/logout', { method: 'POST' });
                if (response.ok) window.location.href = '/index.html';
            } catch (error) {
                console.error("Error al cerrar sesión:", error);
            }
        };
    }
}

async function loadHeaderProfile(tipoUsuario) {
    try {
        const endpoint = `/${tipoUsuario}/data`;
        const resp = await fetch(endpoint);
        if (resp.ok) {
            const data = await resp.json();
            // Tomamos el primer nombre y primer apellido
            const firstName = data.name.split(' ')[0];
            const lastName = data.fLastName ? data.fLastName.split(' ')[0] : "";

            const nameEl = document.getElementById('user-pill-name');
            const initialEl = document.getElementById('user-pill-initial');

            if (nameEl) nameEl.textContent = `${firstName} ${lastName}`;
            if (initialEl) initialEl.textContent = firstName.charAt(0).toUpperCase();
        }
    } catch (error) {
        console.error("Error al cargar perfil en el header:", error);
    }
}