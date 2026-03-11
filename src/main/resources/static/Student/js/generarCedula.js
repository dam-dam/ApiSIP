const API_STATES = '/catalogs/states';
const API_CEDULA_DATA = '/cedula/get-data';
const API_GENERATE_CEDULA = '/cedula/generate';
const API_VIEW_PDF = '/cedula/view-pdf';

document.addEventListener('DOMContentLoaded', async () => {
    loadUserProfile();
    setupLogout();
    // 1. Cargamos estados
    await loadStates();
    // 2. Cargamos datos previos si existen
    await loadExistingData();
    // 3. Activamos el listener del formulario
    setupForm();
});

async function loadUserProfile() {
    try {
        const resp = await fetch('/users/my-name');
        if (resp.ok) {
            const data = await resp.json();
            const firstName = data.name.split(' ')[0];
            const lastName = data.fLastName.split(' ')[0];

            const nameEl = document.getElementById('user-pill-name');
            const initialEl = document.getElementById('user-pill-initial');

            if(nameEl) nameEl.textContent = `${firstName} ${lastName}`;
            if(initialEl) initialEl.textContent = firstName.charAt(0).toUpperCase();
        }
    } catch (error) {
        console.error("Error al cargar perfil:", error);
    }
}

/**
 * Carga estados. Espera que StateDto tenga {id, name}
 */
async function loadStates() {
    try {
        const resp = await fetch(API_STATES);
        if (resp.ok) {
            const states = await resp.json();
            const options = states.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
            document.getElementById('studentStateId').innerHTML += options;
            document.getElementById('companyStateId').innerHTML += options;
        }
    } catch (e) { console.error("Error al cargar estados"); }
}

async function loadExistingData() {
    try {
        const resp = await fetch(API_CEDULA_DATA);
        if (resp.ok) {
            const data = await resp.json();
            if (data && (data.studentAddress || data.companyInfo)) {
                fillFormData(data);
                loadPdfPreview();
            }
        }
    } catch (e) { console.warn("No hay datos previos"); }
}

function fillFormData(data) {
    const { studentAddress, companyInfo, companyAddress } = data;

    if (studentAddress) {
        document.getElementById('studentStreet').value = studentAddress.street || '';
        document.getElementById('studentNumber').value = studentAddress.number || '';
        document.getElementById('studentZipCode').value = studentAddress.zipCode || '';
        document.getElementById('studentNeighborhood').value = studentAddress.neighborhood || '';
        document.getElementById('studentStateId').value = studentAddress.stateId || '';
    }

    if (companyInfo) {
        document.getElementById('companyName').value = companyInfo.name || '';
        document.getElementById('companyEmail').value = companyInfo.email || '';
        document.getElementById('companyPhone').value = companyInfo.phone || '';
        document.getElementById('companyExtension').value = companyInfo.extension || '';
        document.getElementById('companyFax').value = companyInfo.fax || '';
        document.getElementById('companySector').value = companyInfo.sector || 'PUBLICO';
        document.getElementById('companySupervisorGrade').value = companyInfo.supervisorGrade || 'Lic.';
        document.getElementById('companySupervisor').value = companyInfo.supervisor || '';
        document.getElementById('companyPositionSupervisor').value = companyInfo.positionSupervisor || '';
        document.getElementById('companyPositionStudent').value = companyInfo.positionStudent || '';
    }

    if (companyAddress) {
        document.getElementById('companyStreet').value = companyAddress.street || '';
        document.getElementById('companyNumber').value = companyAddress.number || '';
        document.getElementById('companyZipCode').value = companyAddress.zipCode || '';
        document.getElementById('companyNeighborhood').value = companyAddress.neighborhood || '';
        document.getElementById('companyStateId').value = companyAddress.stateId || '';
    }
}

function setupForm() {
    const form = document.getElementById('cedulaForm');
    const btn = document.getElementById('submitBtn');
    let esValido = true;

    if(!form) return;

    form.onsubmit = async (e) => {
        e.preventDefault();

        // Validamos presencia de elementos
        const requiredFields = [
            'studentStreet', 'studentNumber', 'studentZipCode', 'studentNeighborhood', 'studentStateId',
            'companyName', 'companyEmail', 'companyPhone', 'companyExtension', 'companyFax', 'companySector',
            'companySupervisorGrade', 'companySupervisor', 'companyPositionSupervisor', 'companyPositionStudent',
            'companyStreet', 'companyNumber', 'companyZipCode', 'companyNeighborhood', 'companyStateId'
        ];

        for(let id of requiredFields) {
            if(!document.getElementById(id)) {
                console.error("No se encontró el elemento con ID:", id);
                return;
            }
        }

        // Validamos uno por uno
        for(let id of requiredFields) {
            const el = document.getElementById(id);

            if (!validarCampo(el)) {
                esValido = false;
            }
        }

        if (!esValido) {
            Swal.fire({
                title: "Error",
                text: "Uno o varios campos incorrectos, favor de revisar",
                icon: "error",
                confirmButtoText: "Continuar"
            })
            //alert("Por favor, corrige los errores antes de continuar.");
            return;
        }

        btn.disabled = true;
        btn.textContent = "Generando...";

        // Payload alineado con los records Java
        const payload = {
            studentAddress: {
                street: document.getElementById('studentStreet').value,
                number: document.getElementById('studentNumber').value,
                zipCode: document.getElementById('studentZipCode').value,
                neighborhood: document.getElementById('studentNeighborhood').value,
                stateId: parseInt(document.getElementById('studentStateId').value) || null
            },
            companyInfo: {
                name: document.getElementById('companyName').value,
                email: document.getElementById('companyEmail').value,
                phone: document.getElementById('companyPhone').value,
                extension: document.getElementById('companyExtension').value,
                fax: document.getElementById('companyFax').value,
                sector: document.getElementById('companySector').value,
                supervisorGrade: document.getElementById('companySupervisorGrade').value,
                supervisor: document.getElementById('companySupervisor').value,
                positionSupervisor: document.getElementById('companyPositionSupervisor').value,
                positionStudent: document.getElementById('companyPositionStudent').value
            },
            companyAddress: {
                street: document.getElementById('companyStreet').value,
                number: document.getElementById('companyNumber').value,
                zipCode: document.getElementById('companyZipCode').value,
                neighborhood: document.getElementById('companyNeighborhood').value,
                stateId: parseInt(document.getElementById('companyStateId').value) || null
            }
        };

        try {
            const response = await fetch(API_GENERATE_CEDULA, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                await loadPdfPreview();
                Swal.fire({
                    title: "Error",
                    text: "Uno o varios campos incorrectos, favor de revisar",
                    icon: "error",
                    confirmButtoText: "Continuar"
                })
                let timerInterval;
                Swal.fire(
                    "Exito",
                    "Tus cédula fue generada correctamente",
                    "success"
                )
                //alert("Cédula generada correctamente.");
            } else {
                const errText = await response.text();
                Swal.fire({
                    title: "Error",
                    text: "Error al procesar: " + errText,
                    icon: "error",
                    confirmButtoText: "Continuar"
                })
                //alert("Error al procesar: " + errText);
            }
        } catch (error) {
            console.error("Error:", error);
            Swal.fire({
                title: "Error",
                text: "Sin conexión al servidor ",
                icon: "error",
                confirmButtoText: "Continuar"
            })
            //alert("Sin conexión al servidor.");
        } finally {
            btn.disabled = false;
            btn.textContent = "Generar PDF de Cédula";
        }
    };
}

async function loadPdfPreview() {
    const viewer = document.getElementById('viewerContainer');
    const downloadBtn = document.getElementById('downloadBtn');

    try {
        const response = await fetch(API_VIEW_PDF);
        if (response.ok) {
            const blob = await response.blob();
            if (blob.size < 500) return; // Filtro de seguridad
            const url = URL.createObjectURL(blob);
            viewer.innerHTML = `<iframe src="${url}"></iframe>`;
            downloadBtn.href = url;
            downloadBtn.style.display = "block";
        }
    } catch (e) { console.warn("PDF no disponible."); }
}

function setupLogout() {
    const btn = document.getElementById('logoutBtn');
    if(!btn) return;
    btn.addEventListener('click', async () => {
        try {
            const response = await fetch('/auth/logout', { method: 'POST' });
            if (response.ok) window.location.href = '/index.html';
        } catch (error) { console.error("Error al cerrar sesión."); }
    });
}

//Valida que los campos no esten vacios
function validarCampo(inputElement) {
    const errorId = `error-${inputElement.id}`;
    let errorElement = document.getElementById(errorId);

    if (!inputElement.value || inputElement.value.trim() === '') {
        if (!errorElement) {
            errorElement = document.createElement('small');
            errorElement.id = errorId;
            errorElement.className = 'error-text';
            errorElement.textContent = 'Este campo no puede ir vacío';
            inputElement.parentNode.insertBefore(errorElement, inputElement.nextSibling);
        }
        inputElement.classList.add('input-error');
        return false;
    } else {
        if (errorElement) errorElement.remove();
        inputElement.classList.remove('input-error');
        return true;
    }
}