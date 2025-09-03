package com.example.demo.service;

import com.example.demo.entities.Individual;
import com.example.demo.repository.IndividualRepository;
import org.springframework.stereotype.Service;

@Service
public class IndividualQueryService {

    private final IndividualRepository individualRepository;

    public IndividualQueryService(IndividualRepository individualRepository) {
        this.individualRepository = individualRepository;
    }



}