// Funcionalidad para el formulario de configuración (index.html)
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

// Manejar Enter en input de salto de generación
document.addEventListener('DOMContentLoaded', function() {
    const modeSelect = document.getElementById('mode');
    if (modeSelect) {
        toggleMode();
        modeSelect.addEventListener('change', toggleMode);
    }

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