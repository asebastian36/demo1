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
        // Asumiendo que 'file' existe en index.html
        if (fileInput) fileInput.setAttribute('required', 'required');
    } else {
        if (fileGroup) fileGroup.style.display = 'none';
        if (randomParams) randomParams.style.display = 'block';
        if (fileInput) fileInput.removeAttribute('required');
    }
}

// Lógica de Paginación para el modo f(x) (results.html)
function initializeGenerationViewer(generationsData) {
    if (!generationsData || generationsData.length === 0) return;

    const totalGenerations = generationsData.length;
    let currentGenIndex = 0; // 0-based index

    // Función de utilidad para convertir binario a decimal (Long)
    function convertBinaryToDecimal(binaryString) {
        // Usar BigInt para manejar binarios grandes
        try {
            return BigInt("0b" + binaryString).toString();
        } catch (e) {
            return "Error: " + e.message;
        }
    }

    // Función para actualizar la tabla de la generación
    function updateGenerationView(index) {
        const tableBody = document.getElementById('generation-table')?.getElementsByTagName('tbody')[0];
        const currentGenNumSpan = document.getElementById('currentGenerationNum');

        if (!tableBody || !currentGenNumSpan || index < 0 || index >= totalGenerations) return;

        tableBody.innerHTML = '';
        const generation = generationsData[index];

        // Ordenar la generación para garantizar que el primero es el mejor
        generation.sort((a, b) => b.adaptative - a.adaptative);

        generation.forEach((individual, i) => {
            const row = tableBody.insertRow();
            const decimalValue = convertBinaryToDecimal(individual.binary);

            row.innerHTML = '<td>' + (i + 1) + '</td>' +
                '<td>' + individual.binary + '</td>' +
                '<td>' + decimalValue + '</td>' +
                '<td>' + individual.real.toFixed(6) + '</td>' +
                '<td>' + individual.adaptative.toFixed(6) + '</td>';

            // Resaltar el mejor individuo de esta generación
            if (i === 0) {
                row.classList.add('best-individual-highlight');
            }
        });

        currentGenIndex = index;
        currentGenNumSpan.textContent = (index + 1);
    }

    // Función global para cambiar de generación (conectada a los botones)
    window.changeGeneration = function(delta) {
        const newIndex = currentGenIndex + delta;
        if (newIndex >= 0 && newIndex < totalGenerations) {
            updateGenerationView(newIndex);
        }
    };

    // Inicializar la vista con la Generación 1
    updateGenerationView(0);
}


// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar toggle de modo (Para index.html)
    const modeSelect = document.getElementById('mode');
    if (modeSelect) {
        toggleMode();
        modeSelect.addEventListener('change', toggleMode);
    }

    // Manejar Enter en input de salto de generación (Para index.html/results.html si aplica)
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

    // 🚨 Inicialización de la paginación para results.html (Solo si existe la data)
    // Asumimos que generationsData y isCreditFunction son definidas globalmente por Thymeleaf.
    if (typeof isCreditFunction !== 'undefined' && !isCreditFunction && typeof generationsData !== 'undefined') {
        initializeGenerationViewer(generationsData);
    }
});