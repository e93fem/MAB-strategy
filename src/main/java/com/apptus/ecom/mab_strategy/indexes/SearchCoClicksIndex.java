package com.apptus.ecom.mab_strategy.indexes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SearchCoClicksIndex extends CoClicksIndex {

    public SearchCoClicksIndex(String name, ChunkHandler chunkHandler) {
        super(name, chunkHandler);
    }

    private History getHistory(String searchPhrase, String productFrom, String productTo) {
        int productToId = getId(productTo);
        History history = historyMap.computeIfAbsent(SearchHistory.getKey(searchPhrase, productFrom, productTo),
                h -> new SearchHistory(searchPhrase, productFrom, productTo, productToId));
        return history;
    }

    public void addClicks(String searchPhrase, String productFrom, String productTo) {
        History history = getHistory(searchPhrase, productFrom, productTo);
        Chunk currentChunk = chunkHandler.getChunk(history.confidence(), history.success);
        chunkSize.addToValue(currentChunk, 1);
        history.counter++;
        if (history.counter == 1) {
            TreeMap<Long, History> longHistoryTreeMap = productChunkHistoryMap.computeIfAbsent(currentChunk,
                    h -> new HashMap<>())
                                                                                    .computeIfAbsent(((SearchHistory)history).getChunkKey(),
                                                                                            h -> new TreeMap<>());
            longHistoryTreeMap.put(history.rank(), history);
        }
    }

    public boolean hasMatches(String searchPhrase, String thisProduct, Set<String> forbidden) {
        for (Chunk chunk : chunkHandler.getChunkList()) {
            if (productChunkHistoryMap.get(chunk).getOrDefault(SearchHistory.getChunkKey(searchPhrase, thisProduct),
                    new TreeMap<>()).
                                              values().stream().filter(
                    searchHistory -> !forbidden.contains(searchHistory.productTo)).count() > 0) {
                return true;
            }
        }
        return false;
    }

    public List<History> bestMatches(String searchPhrase, String thisProduct, int maxMatches, Set<String> forbidden,
                                     int numberOfCandidates) {
        List<History> collect = new ArrayList<>();
        for (Chunk chunk : chunkHandler.getChunkList()) {
            collect.addAll(productChunkHistoryMap.get(chunk).getOrDefault(SearchHistory.getChunkKey(searchPhrase, thisProduct),
                    new TreeMap<>()).
                                                         values().stream().filter(
                    searchHistory -> !forbidden.contains(searchHistory.productTo)).limit(maxMatches - collect.size())
                                                 .collect(Collectors.toList()));
            if (collect.size() >= maxMatches) {
                break;
            }
        }
        collect.forEach(history -> addFailure(history, 1 / (double) maxMatches));
        return collect;
    }

}
