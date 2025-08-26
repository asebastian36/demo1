package com.example.demo.repository;

import com.example.demo.entities.Individual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IndividualRepository extends JpaRepository<Individual, Long> {
    List<Individual> findByGeneration(int generation);
    List<Individual> findByOrderByAdaptativeDesc();
}