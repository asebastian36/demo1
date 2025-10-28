// src/main/resources/static/js/app.js
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
            const executionModeElement = document.querySelector('input[name="executionMode"]:checked');
            if (!executionModeElement) {
                console.error("No se encontró executionMode seleccionado");
                return;
            }

            const executionMode = executionModeElement.value;
            console.log("Modo de ejecución detectado:", executionMode); // Para debug

            if (executionMode === 'elitist') {
                this.action = '/executeElitist';
            } else {
                this.action = '/uploadTxt';
            }
        });
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