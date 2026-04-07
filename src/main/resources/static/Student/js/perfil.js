document.addEventListener('DOMContentLoaded', () => {
    renderUniversalHeader('students');
    
    loadUserProfile();
    setupPasswordUpdate();
    renderUniversalFooter();
});

// Cargar datos desde la API
async function loadUserProfile() {
    try {
        const resp = await fetch('/students/data');
        if (resp.ok) {
            const data = await resp.json();

            // Actualizar Tarjeta de Identidad
            document.getElementById('display-initial').textContent = data.name.charAt(0).toUpperCase();
            document.getElementById('info-full-name').textContent = `${data.name} ${data.fLastName} ${data.mLastName}`;
            document.getElementById('info-boleta').textContent = data.enrollment;

            // Actualizar Información de Contacto
            document.getElementById('info-email').textContent = data.email;
            document.getElementById('info-phone').textContent = data.infoInstitutional.phone;

            // Actualizar Información Institucional
            document.getElementById('info-career').textContent = data.infoInstitutional.career;
            document.getElementById('info-plan').textContent = data.infoInstitutional.syllabus;
            document.getElementById('info-semester').textContent = data.infoInstitutional.semester;
            document.getElementById('info-practice-status').textContent = data.infoInstitutional.processStatus;
        } else {
            console.error("No se pudo cargar el perfil. Estado HTTP:", resp.status);
        }
    } catch (error) {
        console.error("Error al cargar perfil:", error);
    }
}

// Funcionalidad de Actualizar Contraseña
function setupPasswordUpdate() {
    const btnUpdate = document.getElementById('btn-update-pwd');
    const inputNew = document.getElementById('new-password');
    const inputConfirm = document.getElementById('confirm-password');

    btnUpdate.addEventListener('click', async () => {
        const pwd = inputNew.value;
        const confirm = inputConfirm.value;

        // Validaciones locales
        if (!pwd || !confirm) {
            showModal('Campos Vacíos', 'Por favor, llena ambos campos de contraseña.', 'error');
            return;
        }
        if (pwd.length < 6) {
            showModal('Contraseña Corta', 'La contraseña debe tener al menos 6 caracteres.', 'error');
            return;
        }
        if (pwd !== confirm) {
            showModal('Contraseñas no coinciden', 'La nueva contraseña y la confirmación deben ser iguales.', 'error');
            return;
        }

        // Iniciar Petición a la API
        btnUpdate.disabled = true;
        btnUpdate.textContent = "Actualizando...";

        try {
            // NOTA: Asegúrate de tener implementado el endpoint /student/change-password en tu StudentController
            const resp = await fetch('/student/change-password', {
                method: 'POST', // o PUT
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ newPassword: pwd })
            });

            if (resp.ok) {
                showModal('¡Actualización Exitosa!', 'Tu contraseña ha sido cambiada correctamente.', 'success', () => {
                    inputNew.value = '';
                    inputConfirm.value = '';
                });
            } else {
                const errorTxt = await resp.text();
                showModal('Error al actualizar', errorTxt || 'No se pudo cambiar la contraseña. Inténtalo más tarde.', 'error');
            }
        } catch (error) {
            showModal('Error de Conexión', 'No se pudo contactar con el servidor. Revisa tu conexión a internet.', 'error');
        } finally {
            btnUpdate.disabled = false;
            btnUpdate.textContent = "Actualizar Contraseña";
        }
    });
}
