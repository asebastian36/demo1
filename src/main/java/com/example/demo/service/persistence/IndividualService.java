package com.example.demo.service.persistence;

import com.example.demo.entities.Individual;
import com.example.demo.repository.IndividualRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class IndividualService {

    private final IndividualRepository individualRepository;

    public IndividualService(IndividualRepository individualRepository) {
        this.individualRepository = individualRepository;
    }

    /**
     * Guarda una lista de individuos en la base de datos.
     */
    public List<Individual> saveAll(List<Individual> individuals) {
        return individualRepository.saveAll(individuals);
    }

    /**
     * Obtiene todos los individuos ordenados por generación y adaptativo.
     */
    public List<Individual> findAllOrdered() {
        return individualRepository.findAll().stream()
                .sorted((i1, i2) -> {
                    int genComp = Integer.compare(i1.getGeneration(), i2.getGeneration());
                    return genComp != 0 ? genComp : Double.compare(i2.getAdaptative(), i1.getAdaptative());
                })
                .toList();
    }

    /**
     * Limpia todos los individuos (opcional, útil para reiniciar estado).
     */
    public void deleteAll() {
        individualRepository.deleteAll();
    }

    /**
     * Encuentra todos los individuos de una generación específica.
     */
    public List<Individual> findByGeneration(int generation) {
        return individualRepository.findByGeneration(generation);
    }
}