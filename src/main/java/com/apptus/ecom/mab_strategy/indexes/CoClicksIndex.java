package com.apptus.ecom.mab_strategy.indexes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CoClicksIndex {

    private Map<String, Map<String, Integer>> map = new HashMap<>();
    private Map<String, TreeMap<Integer, List<String>>> matchTree = new HashMap<>();

    public void addClicks(String productFrom, String productTo) {
        Map<String, Integer> stringIntegerMap = map.computeIfAbsent(productFrom, m -> new HashMap<>());
        TreeMap<Integer, List<String>> stringIntegerMap2 = matchTree.computeIfAbsent(productFrom,
                m -> new TreeMap<>(Collections.reverseOrder()));

        Integer value = stringIntegerMap.computeIfAbsent(productTo, i -> 0);
        stringIntegerMap.put(productTo, value + 1);

        if (value > 0) {
            List<String> strings = stringIntegerMap2.get(value);
            strings.remove(productTo);
            if (strings.isEmpty()) {
                stringIntegerMap2.remove(value);
            }
        }

        List<String> strings1 = stringIntegerMap2.computeIfAbsent(value + 1, t -> new ArrayList<>());
        strings1.add(productTo);
    }

    public List<String> bestMatches(String thisProduct, int maxMatches) {
        TreeMap<Integer, List<String>> integerListTreeMap = matchTree.get(thisProduct);
        if (integerListTreeMap == null) {
            return new ArrayList<>();
        } else {
            List<String> results = new ArrayList<>();
            for (List<String> products : integerListTreeMap.values()) {
                for (String product : products) {
                    results.add(product);
                    if (results.size()>=maxMatches) {
                        return results;
                    }
                }
            }
            return results;
        }
    }

}
