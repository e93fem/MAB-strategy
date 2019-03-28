package com.apptus.ecom.mab_strategy.indexes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CoClicksIndexOrg {

    private HashMap<Integer, History> historyMap = new HashMap<>();
    private HashMap<String, Set<History>> productHistoryMap = new HashMap<>();

//    public void addClicks(String productFrom, String productTo) {
//        History history = historyMap.computeIfAbsent(History.externalHashCode(productFrom, productTo),
//                h -> new History(productFrom, productTo));
//        productHistoryMap.computeIfAbsent(productFrom, h -> new HashSet<>()).add(history);
//        history.counter++;
//    }
//
//    public void addSuccess(String productFrom, String productTo) {
//        History history = historyMap.computeIfAbsent(History.externalHashCode(productFrom, productTo),
//                h -> new History(productFrom, productTo));
//        history.success++;
//    }
//
//    public void addFailure(String productFrom, String productTo) {
//        History history = historyMap.computeIfAbsent(History.externalHashCode(productFrom, productTo),
//                h -> new History(productFrom, productTo));
//        history.failure++;
//    }

    private boolean interval(History value, int minConf, int maxConf, int minSuccess, int maxSuccess) {
        return value.confidence() >= minConf && value.confidence() < maxConf && value.success >= minSuccess
               && value.success < maxSuccess;
    }

//    public List<String> bestMatches(String thisProduct, int maxMatches, int minConf,
//                                    int maxConf,
//                                    int minSuccess,
//                                    int maxSuccess
//    ) {
//        List<History> histories = productHistoryMap.getOrDefault(thisProduct, new HashSet<>()).stream().
//                filter(history -> interval(history, minConf, maxConf, minSuccess,
//                        maxSuccess)).collect(
//                Collectors.toList());
//
//        Collections.sort(histories, (h1, h2) -> h2.rank() - h1.rank());
//
//        List<History> bestHistories = histories.subList(0, Math.min(histories.size(), maxMatches));
//        bestHistories.forEach(h -> h.failure++);
//        if (!bestHistories.isEmpty() && bestHistories.get(0).success > 0) {
//            int i = 0;
//        }
//        return bestHistories.stream().map(history -> history.productTo).collect(
//                Collectors.toList());
//    }

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
//
//        return "Histories: " + histories.size() + ", sumCounter: " + sumCounter + ", sumSuccess: " + sumSuccess + ", sumFailure: "
//               + sumFailure;
        return "";
    }

}
