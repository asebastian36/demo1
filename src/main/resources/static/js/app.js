/**
 * Convierte el URI de datos Base64 a datos binarios limpios para JSZip.
 * @param {string} base64Image - Imagen en formato data URI.
 * @returns {string} Datos binarios limpios.
 */
function base64ImageToBinary(base64Image) {
    // Retorna solo la parte del Base64 después de 'base64,'
    return base64Image.split(',')[1];
}

/**
 * Recorre todas las gráficas visibles y fuerza su descarga en un archivo .zip con nombres descriptivos.
 */
function exportAllCharts() {
    // Es CRUCIAL que JSZip esté cargado antes de llamar a esta función.
    if (typeof JSZip === 'undefined') {
        alert("Error: La librería JSZip no está cargada. Asegúrate de incluir la etiqueta script de JSZip.");
        return;
    }

    const zip = new JSZip();
    const isElitistMode = document.querySelector('h1')?.innerText.includes('Modo Elitista');
    const zipFolderName = isElitistMode ? 'Resultados_Elitistas' : 'Resultados_Normales';
    let chartCount = 0;

    const downloadZip = (folderName) => {
        if (chartCount === 0) {
             alert('No se encontraron gráficos válidos para exportar.');
             return;
        }
        // Generar el archivo ZIP
        zip.generateAsync({ type: "blob" })
            .then(function(content) {
                // Usar FileSaver.js o un truco simple para forzar la descarga
                const link = document.createElement('a');
                link.href = URL.createObjectURL(content);
                link.download = folderName + '.zip';
                document.body.appendChild(link);
                link.click();
                URL.revokeObjectURL(link.href); // Limpiar URL
                document.body.removeChild(link);
                alert(`Exportación de ${chartCount} gráficos comprimidos en .zip iniciada.`);
            });
    };
    
    // --- LÓGICA PARA MODO ELITISTA ---
    if (isElitistMode) {
        const resultDivs = document.querySelectorAll('.container > div[style*="margin-bottom"]');
        
        resultDivs.forEach(div => {
            const combinationRaw = div.querySelector('h3')?.innerText;
            if (!combinationRaw) return;

            // Limpiamos el nombre: roulette_single_inversive
            const combinationId = combinationRaw.replace(/[^a-zA-Z0-9]+/g, '_');
            const images = div.querySelectorAll('img');
            
            if (images.length > 0) {
                const folder = zip.folder(combinationId);
                
                // Imagen 1: Convergencia
                if (images[0] && images[0].src && !images[0].src.startsWith('data:image/png;base64,error')) {
                    folder.file(`${combinationId}_Convergencia_LINEAS.png`, base64ImageToBinary(images[0].src), { base64: true });
                    chartCount++;
                }
                
                // Imagen 2: Distribución (si existe)
                if (images.length > 1 && images[1].src && !images[1].src.startsWith('data:image/png;base64,error')) {
                    folder.file(`${combinationId}_Distribucion_BOXPLOT.png`, base64ImageToBinary(images[1].src), { base64: true });
                    chartCount++;
                }
            }
        });
        downloadZip(zipFolderName);

    } else {
        // --- LÓGICA PARA MODO NORMAL ---
        
        // Usamos IDs para localizar las gráficas en modo normal
        const convergenceImg = document.getElementById('chartImage');
        const distributionImg = document.getElementById('distributionChartImage');
        const functionType = document.querySelector('.results-header span:nth-child(2)')?.innerText.trim() || 'AG';

        if (convergenceImg && convergenceImg.src && !convergenceImg.src.startsWith('data:image/png;base64,error')) {
            zip.file(`Convergencia_${functionType}_LINEAS.png`, base64ImageToBinary(convergenceImg.src), { base64: true });
            chartCount++;
        }
        
        if (distributionImg && distributionImg.src && !distributionImg.src.startsWith('data:image/png;base64,error')) {
            zip.file(`Distribucion_${functionType}_BOXPLOT.png`, base64ImageToBinary(distributionImg.src), { base64: true });
            chartCount++;
        }
        downloadZip(zipFolderName);
    }
}