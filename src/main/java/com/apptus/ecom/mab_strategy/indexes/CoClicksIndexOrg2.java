package com.apptus.ecom.mab_strategy.indexes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class CoClicksIndexOrg2 {

    private HashMap<Integer, History> historyMap = new HashMap<>();
    private HashMap<String, TreeMap<Long, History>> productHistoryMap = new HashMap<>();
    private HashMap<String, Map<Chunk, TreeMap<Long, History>>> productChunkHistoryMap = new HashMap<>();

//    ObjectIntHashMap<String> productIdMap = new ObjectIntHashMap<>();

    private int getId(String product) {
        return Integer.parseInt(product);
//        if (!productIdMap.containsKey(product)) {
//            int size = productIdMap.size();
//            productIdMap.put(product, size);
//        }
//        return productIdMap.get(product);
    }

    public History getHistory(String productFrom, String productTo) {
        int productFromId = getId(productFrom);
        int productToId = getId(productTo);
        History history = historyMap.computeIfAbsent(History.externalHashCode(productFrom, productTo),
                h -> new History(productFromId, productToId, productTo));
        TreeMap<Long, History> longHistoryTreeMap = productHistoryMap.computeIfAbsent(productFrom,
                h -> new TreeMap<>(Collections.reverseOrder()));
        if (longHistoryTreeMap.containsKey(history.rank())) {
            longHistoryTreeMap.remove(history.rank());
        }
        return history;
    }

    public void addClicks(String productFrom, String productTo) {
        History history = getHistory(productFrom, productTo);
        history.counter++;
        productHistoryMap.get(productFrom).put(history.rank(), history);
    }

    public void addSuccess(String productFrom, String productTo) {
        History history = getHistory(productFrom, productTo);
        history.success++;
        productHistoryMap.get(productFrom).put(history.rank(), history);
    }

    public void addFailure(String productFrom, String productTo) {
        History history = getHistory(productFrom, productTo);
        history.failure++;
        productHistoryMap.get(productFrom).put(history.rank(), history);
    }

    private boolean interval(History value, int minConf, int maxConf, int minSuccess, int maxSuccess) {
        return value.confidence() >= minConf && value.confidence() < maxConf && value.success >= minSuccess
               && value.success < maxSuccess;
    }

    public List<String> bestMatches(String thisProduct, int maxMatches, int minConf,
                                    int maxConf,
                                    int minSuccess,
                                    int maxSuccess) {
        List<History> collect = productHistoryMap.getOrDefault(thisProduct, new TreeMap<>()).values().stream().filter(
                history -> interval(history, minConf, maxConf, minSuccess,
                        maxSuccess)).limit(maxMatches).collect(Collectors.toList());
        collect.forEach(history -> {
            TreeMap<Long, History> longHistoryTreeMap = productHistoryMap.get(thisProduct);
            longHistoryTreeMap.remove(history.rank());
            history.failure++;
            longHistoryTreeMap.put(history.rank(), history);
        });
        return collect.stream().map(history -> history.productToId).collect(
                Collectors.toList());
    }

    public String toString(int minConf,
                           int maxConf,
                           int minSuccess,
                           int maxSuccess
    ) {
        List<History> histories = historyMap.values().stream().
                filter(history -> interval(history, minConf, maxConf, minSuccess,
                        maxSuccess)).collect(
                Collectors.toList());

//        int sumCounter = histories.stream().mapToInt(h -> h.counter).sum();
//        int sumSuccess = histories.stream().mapToInt(h -> h.success).sum();
//        int sumFailure = histories.stream().mapToInt(h -> h.failure).sum();

//        return "Histories: " + histories.size() + ", sumCounter: " + sumCounter + ", sumSuccess: " + sumSuccess + ", sumFailure: "
//               + sumFailure;
        return "";
    }

}
