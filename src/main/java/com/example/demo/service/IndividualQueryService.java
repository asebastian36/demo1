package com.example.demo.service;

import com.example.demo.entities.Individual;
import com.example.demo.repository.IndividualRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IndividualQueryService {

    private final IndividualRepository individualRepository;

    public IndividualQueryService(IndividualRepository individualRepository) {
        this.individualRepository = individualRepository;
    }

    public List<List<Individual>> getGenerationsByRun(int numGenerations) {
        return individualRepository.findAll().stream()
                .sorted(Comparator.comparing(Individual::getGeneration)
                        .thenComparingDouble(ind -> -ind.getAdaptative())) // descendente
                .collect(Collectors.groupingBy(Individual::getGeneration, LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(numGenerations)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}