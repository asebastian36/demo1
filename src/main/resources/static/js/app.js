// Funcionalidad para el formulario de configuración
function toggleMode() {
    const mode = document.getElementById('mode');
    if (!mode) return;

    const fileGroup = document.getElementById('file-group');
    const randomParams = document.getElementById('random-params');
    const fileInput = document.getElementById('file');

    if (mode.value === 'file') {
        if (fileGroup) fileGroup.style.display = 'block';
        if (randomParams) randomParams.style.display = 'none';
        if (fileInput) fileInput.setAttribute('required', 'required');
    } else {
        if (fileGroup) fileGroup.style.display = 'none';
        if (randomParams) randomParams.style.display = 'block';
        if (fileInput) fileInput.removeAttribute('required');
    }
}

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar toggle de modo
    const modeSelect = document.getElementById('mode');
    if (modeSelect) {
        toggleMode();
        modeSelect.addEventListener('change', toggleMode);
    }

    // Manejar Enter en input de salto de generación
    const genInput = document.getElementById('genInput');
    if (genInput) {
        genInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const form = this.closest('form');
                if (form) form.submit();
            }
        });
    }
});