const API_BASE_URL = window.location.origin;
let emailRegistro = '';
let countdownTimer = null;

document.addEventListener('DOMContentLoaded', () => {
    cambiarVista('login');
    setupCatalogListeners();
});

function cambiarVista(vista) {
    document.querySelectorAll('.auth-view').forEach(v => v.classList.remove('active-view'));
    const viewId = vista === 'registro' ? 'registro-view' : (vista === 'verificacion' ? 'verificacion-view' : 'login-view');
    document.getElementById(viewId).classList.add('active-view');
    if (vista === 'registro') {
        cargarCatalogosIniciales();
    }
}

function showModal(title, msg, type = 'info', callback = null) {
    const overlay = document.getElementById('global-modal');
    const icon = document.getElementById('modal-icon');
    const btn = document.getElementById('modal-btn');

    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-msg').textContent = msg;

    const icons = {
        success: '<svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9 12l2 2 4-4"/></svg>',
        error: '<svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="#ef4444" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>',
        info: '<svg width="60" height="60" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>'
    };
    icon.innerHTML = icons[type] || icons.info;

    overlay.style.display = 'flex';
    btn.onclick = () => {
        overlay.style.display = 'none';
        if (callback) callback();
    };
}

async function iniciarSesion() {
    const email = document.getElementById('login-email').value;
    const pass = document.getElementById('login-password').value;
    const btn = document.getElementById('login-button');

    if (!email || !pass) return showModal('Datos incompletos', 'Ingresa correo y contraseña', 'error');

    btn.disabled = true;
    btn.textContent = "Verificando...";

    try {
        const resp = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password: pass })
        });
        const data = await resp.json();

        if (data.flag) {
            const routes = { 'ALUMNO': 'Student/home.html', 'ADMINISTRADOR': 'Administrator/home.html', 'OPERADOR': 'Operative/home.html' };
            window.location.href = routes[data.userType] || '#';
        } else {
            showModal('Error', data.message, 'error');
        }
    } catch (e) {
        showModal('Error', 'No se pudo conectar al servidor', 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = "Iniciar Sesión";
    }
}

async function iniciarRegistro() {
    const pass = document.getElementById('password').value;
    const confirm = document.getElementById('confirm-password').value;

    if (pass.length < 6) return showModal('Contraseña', 'Mínimo 6 caracteres', 'error');
    if (pass !== confirm) return showModal('Error', 'Las contraseñas no coinciden', 'error');

    const btn = document.getElementById('register-button');
    btn.disabled = true;
    btn.textContent = "Procesando...";

    emailRegistro = document.getElementById('email').value;
    const isEgresado = document.getElementById('egresado-check').checked;

    const payload = {
        email: emailRegistro,
        fLastName: document.getElementById('apellido-paterno').value,
        mLastName: document.getElementById('apellido-materno').value,
        name: document.getElementById('nombre').value,
        password: pass,
        confirmPassword: confirm,
        enrollment: document.getElementById('boleta').value,
        phone: document.getElementById('phone').value,
        semester: isEgresado ? "1" : document.getElementById('semestre').value,
        graduated: isEgresado,
        schoolName: document.getElementById('escuela').value,
        acronymCareer: document.getElementById('carrera').value,
        syllabusCode: document.getElementById('plan-estudios').value
    };

    try {
        const resp = await fetch(`${API_BASE_URL}/student/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!resp.ok) {
            const err = await resp.text();
            let msg = "Error en registro";
            try { msg = JSON.parse(err).message; } catch(e) {}
            throw new Error(msg);
        }

        cambiarVista('verificacion');
        document.getElementById('email-destino').textContent = emailRegistro;
        iniciarCountdown();
    } catch (e) {
        showModal('Error', e.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = "Crear Cuenta";
    }
}

async function cargarCatalogosIniciales() {
    const escuelaSel = document.getElementById('escuela');
    if (escuelaSel.options.length > 1) return;

    try {
        const [schools, semesters] = await Promise.all([
            fetch(`${API_BASE_URL}/catalogs/schools`).then(r => r.json()),
            fetch(`${API_BASE_URL}/catalogs/semesters`).then(r => r.json())
        ]);

        schools.forEach(s => {
            const op = new Option(s.acronym, s.acronym);
            op.dataset.name = s.acronym;
            escuelaSel.add(op);
        });

        const semSel = document.getElementById('semestre');
        semesters.sort((a,b) => parseInt(a.description) - parseInt(b.description));
        semesters.forEach(s => semSel.add(new Option(`Semestre ${s.description}`, s.description)));
    } catch (e) { console.error(e); }
}

function setupCatalogListeners() {
    document.getElementById('escuela').addEventListener('change', async function() {
        const name = this.options[this.selectedIndex].dataset.name;
        const carreraSel = document.getElementById('carrera');
        carreraSel.innerHTML = '<option value="">Cargando...</option>';
        carreraSel.disabled = true;

        if (name) {
            const data = await fetch(`${API_BASE_URL}/catalogs/careers?SchoolName=${encodeURIComponent(name)}`).then(r => r.json());
            carreraSel.innerHTML = '<option value="">Selecciona...</option>';
            data.forEach(c => carreraSel.add(new Option(c.acronym, c.acronym)));
            carreraSel.disabled = false;
        }
    });

    document.getElementById('carrera').addEventListener('change', async function() {
        const schoolAc = document.getElementById('escuela').value;
        const careerAc = this.value;
        const planSel = document.getElementById('plan-estudios');
        planSel.innerHTML = '<option value="">Cargando...</option>';
        planSel.disabled = true;

        if (careerAc) {
            const data = await fetch(`${API_BASE_URL}/catalogs/syllabus?schoolAcronym=${schoolAc}&careerAcronym=${careerAc}`).then(r => r.json());
            planSel.innerHTML = '<option value="">Selecciona...</option>';
            data.forEach(p => planSel.add(new Option(p.code, p.code)));
            planSel.disabled = false;
        }
    });

    document.getElementById('egresado-check').addEventListener('change', function() {
        const div = document.getElementById('semester-group');
        div.style.opacity = this.checked ? '0.3' : '1';
        div.style.pointerEvents = this.checked ? 'none' : 'auto';
        if(this.checked) document.getElementById('semestre').value = '';
    });
}

function iniciarCountdown() {
    let t = 60;
    const el = document.getElementById('countdown');
    const btn = document.getElementById('resend-button');
    btn.disabled = true;
    if (countdownTimer) clearInterval(countdownTimer);
    countdownTimer = setInterval(() => {
        t--;
        el.textContent = t;
        if (t <= 0) {
            clearInterval(countdownTimer);
            btn.disabled = false;
            el.textContent = "0";
        }
    }, 1000);
}

async function verificarCodigo() {
    const code = document.getElementById('codigo-verificacion').value;
    if (code.length !== 6) return showModal('Código', 'Debe tener 6 dígitos', 'error');

    try {
        const resp = await fetch(`${API_BASE_URL}/auth/confirm-email`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: emailRegistro, code })
        });

        if (resp.ok) {
            showModal('¡Éxito!', 'Cuenta verificada. Inicia sesión.', 'success', () => {
                cambiarVista('login');
            });
        } else {
            throw new Error('Código incorrecto');
        }
    } catch (e) {
        showModal('Error', e.message, 'error');
    }
}

function mostrarRecuperacionModal() { document.getElementById('recuperar-contrasena-modal').style.display = 'flex'; }
function cerrarRecuperacionModal() { document.getElementById('recuperar-contrasena-modal').style.display = 'none'; }

async function enviarCodigoRecuperacion() {
    const email = document.getElementById('recovery-email').value;
    if (!email) return;

    try {
        await fetch(`${API_BASE_URL}/api/forgot-password`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });
        cerrarRecuperacionModal();
        showModal('Enviado', 'Revisa tu correo.', 'success');
    } catch (e) {
        showModal('Error', 'No se pudo enviar', 'error');
    }
}