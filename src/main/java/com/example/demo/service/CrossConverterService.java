package com.example.demo.service;

import com.example.demo.utils.IntPair;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CrossConverterService {

    public List<String> cruceGeneracion(List<String> binaryNumbers, List<IntPair> cruce) {
        List<String> cruce2da = new ArrayList<>(binaryNumbers); // Creamos una copia

        cruce.forEach(integers -> {
            // Obtenemos los índices (ajustados porque restas 1)
            int index1 = integers.first() - 1;
            int index2 = integers.second() - 1;

            // Verificamos que los índices sean válidos
            if (index1 >= 0 && index1 < cruce2da.size() &&
                    index2 >= 0 && index2 < cruce2da.size()) {

                // Obtenemos los valores actuales
                String cruceArriba = cruce2da.get(index1);
                String cruceAbajo = cruce2da.get(index2);

                // Hacemos la extracción (desde el 2do carácter hasta el final)
                String intercambioAr = cruceArriba.substring(1);
                String intercambioAb = cruceAbajo.substring(1);

                // Formamos nuevos valores (corrección en la concatenación)
                String valorNuevo0 = cruceArriba.charAt(0) + intercambioAb;
                String valorNuevo1 = cruceAbajo.charAt(0) + intercambioAr;

                // Actualizamos los valores en la lista
                cruce2da.set(index1, valorNuevo0);
                cruce2da.set(index2, valorNuevo1);
            }
        });

        return cruce2da;
    }

}
