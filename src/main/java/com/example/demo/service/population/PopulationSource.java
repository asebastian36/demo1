package com.example.demo.service.population;

import java.util.List;

public interface PopulationSource {
    List<String> generatePopulation(int L);
    String getName();
}
