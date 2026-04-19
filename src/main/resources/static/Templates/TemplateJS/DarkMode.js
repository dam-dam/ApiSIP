const themeToggleContainer = document.createElement('div');
themeToggleContainer.id = 'theme-toggle-container';


themeToggleContainer.innerHTML = `
    <button id="btn-dark-mode" class="dakmode-btn">
        <i id="theme-icon" class="fas fa-moon"></i>
    </button>
`;

document.body.appendChild(themeToggleContainer);

const btnDark = document.getElementById('btn-dark-mode');
const themeIcon = document.getElementById('theme-icon');
const currentTheme = localStorage.getItem('theme');

// Función para aplicar el tema
function setTheme(isDark) {
    if (isDark) {
        document.body.classList.add('dark-mode');
        themeIcon.classList.replace('fa-moon', 'fa-sun');
        localStorage.setItem('theme', 'dark');
    } else {
        document.body.classList.remove('dark-mode');
        themeIcon.classList.replace('fa-sun', 'fa-moon');
        localStorage.setItem('theme', 'light');
    }
}

// Detección inicial: Sistema Operativo vs LocalStorage
if (currentTheme === 'dark' || (!currentTheme && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
    setTheme(true);
}

// Evento de click para cambiar manualmente
btnDark.addEventListener('click', () => {
    const isDark = document.body.classList.contains('dark-mode');
    setTheme(!isDark);
});