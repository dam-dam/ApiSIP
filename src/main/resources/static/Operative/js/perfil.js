document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('users');
    loadProfileData();
    setupPasswordUpdate();
    renderUniversalFooter();
});

async function loadProfileData() {
    try {
        // Consumo de la API real
        const resp = await fetch('/users/data');

        if (resp.ok) {
            const data = await resp.json();

            // Mapeo de datos (StudentProfileDto sin InfoInstitutional)
            const fullName = `${data.name} ${data.fLastName} ${data.mLastName}`;

            // Actualizar UI
            document.getElementById('info-full-name').textContent = fullName;
            document.getElementById('display-initial').textContent = data.name.charAt(0).toUpperCase();

            document.getElementById('info-email').textContent = data.email || 'No registrado';
            // TELEFONO IGNORADO EXPLICITAMENTE

        } else {
            console.error("Error al obtener perfil:", resp.status);
            // Sin modal de error de carga, solo consola
        }
    } catch (error) {
        console.error("Error de conexión:", error);
        // Sin modal de error de carga, solo consola
    }
}


function setupPasswordUpdate() {
    const btnUpdate = document.getElementById('btn-update-pwd');
    const inputNew = document.getElementById('new-password');
    const inputConfirm = document.getElementById('confirm-password');

    btnUpdate.addEventListener('click', async () => {
        const pwd = inputNew.value;
        const confirm = inputConfirm.value;

        if (!pwd || !confirm) return showModal('Campos Vacíos', 'Llena ambos campos.', 'error');
        if (pwd.length < 6) return showModal('Contraseña Corta', 'Mínimo 6 caracteres.', 'error');
        if (pwd !== confirm) return showModal('Error', 'Las contraseñas no coinciden.', 'error');

        btnUpdate.disabled = true;
        btnUpdate.textContent = "Actualizando...";

        try {
            const resp = await fetch('/operatives/change-password', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ newPassword: pwd })
            });

            if (resp.ok) {
                showModal('¡Éxito!', 'Contraseña actualizada.', 'success', () => {
                    inputNew.value = ''; inputConfirm.value = '';
                });
            } else {
                const txt = await resp.text();
                showModal('Error', txt || 'No se pudo actualizar.', 'error');
            }
        } catch (error) {
            showModal('Error', 'Fallo de conexión.', 'error');
        } finally {
            btnUpdate.disabled = false;
            btnUpdate.textContent = "Actualizar Contraseña";
        }
    });
}

function showModal(title, message, type, callback) {
    const modal = document.getElementById('custom-modal');
    const iconBox = document.getElementById('modal-icon-box');

    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-message').textContent = message;

    if (type === 'success') {
        iconBox.className = 'modal-icon-box icon-success';
        iconBox.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" /></svg>`;
    } else {
        iconBox.className = 'modal-icon-box icon-error';
        iconBox.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" /></svg>`;
    }

    modal.classList.add('active');
    document.getElementById('btn-modal-close').onclick = () => {
        modal.classList.remove('active');
        if (callback) callback();
    };
}