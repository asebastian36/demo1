package com.example.demo.function;

import com.example.demo.io.conversion.BinaryToDecimalConverter;
import java.util.Map;

public interface ChromosomeBasedFitnessFunction extends FitnessFunction {
    Map<String, Object> decodeAndInterpret(String binary, BinaryToDecimalConverter converter);
    int[] getSegmentLengths();
    String[] getVariableNames();
}