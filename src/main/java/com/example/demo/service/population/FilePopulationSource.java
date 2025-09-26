package com.example.demo.service.population;

import org.springframework.stereotype.Component;
import java.util.List;

@Component("file")
public class FilePopulationSource implements PopulationSource {

    private List<String> binaries;

    // Constructor para inyección de datos
    public FilePopulationSource() {}

    // Método para establecer los binarios desde el controlador
    public void setBinaries(List<String> binaries) {
        this.binaries = binaries;
    }

    @Override
    public List<String> generatePopulation(int L) {
        if (binaries == null || binaries.isEmpty()) {
            throw new IllegalStateException("No se han proporcionado binarios para el modo archivo");
        }
        return binaries; // Ya están normalizados en el controlador
    }

    @Override
    public String getName() {
        return "Desde archivo";
    }
}
