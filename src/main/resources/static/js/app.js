function toggleInputType() {
    const fileGroup = document.getElementById('file-group');
    const fileInput = document.getElementById('file');
    const inputType = document.querySelector('input[name="inputType"]:checked').value;

    if (inputType === 'file') {
        fileGroup.style.display = 'block';
        fileInput.setAttribute('required', 'required');
    } else {
        fileGroup.style.display = 'none';
        fileInput.removeAttribute('required');
    }
}

function toggleExecutionMode() {
    const strategiesGroup = document.getElementById('strategies-group');
    const executionMode = document.querySelector('input[name="executionMode"]:checked').value;

    if (executionMode === 'elitist') {
        strategiesGroup.style.display = 'none';
    } else {
        strategiesGroup.style.display = 'block';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar estados
    toggleInputType();
    toggleExecutionMode();

    // Event listeners
    const inputTypeRadios = document.querySelectorAll('input[name="inputType"]');
    inputTypeRadios.forEach(radio => {
        radio.addEventListener('change', toggleInputType);
    });

    const executionModeRadios = document.querySelectorAll('input[name="executionMode"]');
    executionModeRadios.forEach(radio => {
        radio.addEventListener('change', toggleExecutionMode);
    });

    // Manejar envío del formulario
    const form = document.getElementById('configForm');
    if (form) {
        form.addEventListener('submit', function(e) {
            const executionMode = document.querySelector('input[name="executionMode"]:checked').value;
            if (executionMode === 'elitist') {
                this.action = '/executeElitist';
            } else {
                this.action = '/uploadTxt';
            }
        });
    }
});