package com.example.demo.service;

import com.example.demo.domain.Table;
import com.example.demo.utils.IntPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LoopConverterService {

    @Autowired
    private CrossConverterService crossConverterService;

    @Autowired
    private BinaryConverterService binaryService;

    @Autowired
    private RealConverterService realService;

    @Autowired
    private AdaptitiveConverterService adaptitiveService;


    public List<Table> generateGenerations(List<String> binaryNumbers, double xmin, double xmax, int L) {
        List<Table> tables = new ArrayList<>();


        for (int i = 0; i < 3; i++) {
            //  generando la primera generacion
            if ( i == 0 ) {
                updateBinaries(binaryNumbers, xmin, xmax, L, tables);
            }

            //  cruces para 2da
            else if ( i == 1 ) {
                /*
            2da generacion:
            4 y 14,
            3 y 7
            8 y 9
            15 y 1
            13 y 9
            7 y 8
            9 y 10
            5 y 9
            15 y 7
            9 y 1
            3 y 4
         */

                List<IntPair> listaPares = getPairs();

                //  se actualiza la lista
                binaryNumbers = crossConverterService.cruceGeneracion(binaryNumbers, listaPares);
                List<Integer> decimalNumbers = binaryService.convertBinaryListToIntegers(binaryNumbers);
                Table segunda = operations(decimalNumbers,  xmin, xmax, L);

                List<Double> listaOrdenada = segunda.getAdaptatives()
                        .stream()
                        .sorted(Comparator.reverseOrder())
                        .toList();

                segunda.setOrders(listaOrdenada);
                segunda.setBinaries(binaryNumbers);

                tables.add(segunda);

            } else  {   //  cruces para 3ra generacion

                /*
                3ra generacion:
                15 y 3
                8 y 7
                2 y 9
                6 y 5
                14 y 7
                3 y 9
                13 y 4
                9 y 2
                15 y 9
                7 y 3
                8 y 12
                 */
                List<IntPair> listaPares = getIntPairs();

                //  se actualiza la lista
                binaryNumbers = crossConverterService.cruceGeneracion(binaryNumbers, listaPares);
                updateBinaries(binaryNumbers, xmin, xmax, L, tables);
            }

        }

        return tables;
    }

    private void updateBinaries(List<String> binaryNumbers, double xmin, double xmax, int L, List<Table> tables) {
        List<Integer> decimalNumbers = binaryService.convertBinaryListToIntegers(binaryNumbers);
        Table primera = operations(decimalNumbers,  xmin, xmax, L);
        primera.setBinaries(binaryNumbers);

        List<Double> listaOrdenada = primera.getAdaptatives()
                .stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        primera.setOrders(listaOrdenada);
        primera.setBinaries(binaryNumbers);

        tables.add(primera);
    }

    private static List<IntPair> getPairs() {
        List<IntPair> listaPares = new ArrayList<>();
        listaPares.add(new IntPair(4, 14));
        listaPares.add(new IntPair(3, 7));
        listaPares.add(new IntPair(8, 9));
        listaPares.add(new IntPair(15, 1));
        listaPares.add(new IntPair(13, 9));
        listaPares.add(new IntPair(7, 8));
        listaPares.add(new IntPair(9, 10));
        listaPares.add(new IntPair(5, 9));
        listaPares.add(new IntPair(15, 7));
        listaPares.add(new IntPair(9, 1));
        listaPares.add(new IntPair(3, 4));
        return listaPares;
    }

    private static List<IntPair> getIntPairs() {
        List<IntPair> listaPares = new ArrayList<>();
        listaPares.add(new IntPair(15, 3));
        listaPares.add(new IntPair(8, 7));
        listaPares.add(new IntPair(2, 9));
        listaPares.add(new IntPair(6, 5));
        listaPares.add(new IntPair(14, 7));
        listaPares.add(new IntPair(3, 9));
        listaPares.add(new IntPair(13, 4));
        listaPares.add(new IntPair(9, 2));
        listaPares.add(new IntPair(15, 9));
        listaPares.add(new IntPair(7, 3));
        listaPares.add(new IntPair(8, 12));
        return listaPares;
    }

    private Table operations(List<Integer> decimalNumbers, double xmin, double xmax,  int L) {
        Table generationProcess = new Table();

        generationProcess.setDecimals(decimalNumbers);

        List<Double> reals = realService.toReal(generationProcess.getDecimals(),  xmin, xmax, L);
        generationProcess.setReals(reals);

        List<Double> adaptatives = adaptitiveService.toAdaptive(generationProcess.getReals());
        generationProcess.setAdaptatives(adaptatives);

        return generationProcess;
    }
}
