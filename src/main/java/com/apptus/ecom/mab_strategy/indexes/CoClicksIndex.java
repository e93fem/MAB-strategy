package com.apptus.ecom.mab_strategy.indexes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;

public class CoClicksIndex {

    protected HashMap<String, History> historyMap = new HashMap<>();
    protected HashMap<Chunk, Map<String, TreeMap<Long, History>>> productChunkHistoryMap = new HashMap<>();
    protected ObjectIntHashMap<Chunk> chunkSize = new ObjectIntHashMap<>();

    ObjectIntHashMap<String> productIdMap = new ObjectIntHashMap<>();
    protected String name;
    protected ChunkHandler chunkHandler;

    public CoClicksIndex(String name, ChunkHandler chunkHandler) {
        this.name = name;
        this.chunkHandler = chunkHandler;
        for (Chunk chunk : chunkHandler.getChunkList()) {
            productChunkHistoryMap.put(chunk, new HashMap<>());
        }
    }

    protected int getId(String product) {
        if (!productIdMap.containsKey(product)) {
            int size = productIdMap.size();
            productIdMap.put(product, size);
        }
        return productIdMap.get(product);
    }

    private History getHistory(String productFrom, String productTo) {
        int productToId = getId(productTo);
        History history = historyMap.computeIfAbsent(History.getKey(productFrom, productTo),
                h -> new History(productFrom, productTo, productToId));
        return history;
    }

    public void addClicks(String productFrom, String productTo) {
        History history = getHistory(productFrom, productTo);
        Chunk currentChunk = chunkHandler.getChunk(history.confidence(), history.success);
        chunkSize.addToValue(currentChunk, 1);
        history.counter++;
        if (history.counter == 1) {
            TreeMap<Long, History> longHistoryTreeMap = productChunkHistoryMap.computeIfAbsent(currentChunk, h -> new HashMap<>())
                                                                              .computeIfAbsent(history.productFrom,
                                                                                      h -> new TreeMap<>());
            longHistoryTreeMap.put(history.rank(), history);
        }
    }

    protected void addAction(History history, boolean success,
                             double value) {
        long orgRank = history.rank();
        Chunk currentChunk = chunkHandler.getChunk(history.confidence(), history.success);
        if (success) {
            history.success += value;
            history.failure -= value;
        } else {
            history.failure += value;
        }
        if (history.failure < 0) {
            history.failure = 0;
        }

        Chunk chunk = chunkHandler.getChunk(history.confidence(), history.success);
        if (chunk == null) {
            chunkHandler.getChunk(history.confidence(), history.success);
            int i = 0;
        }
        if (currentChunk != chunk) {
            chunkSize.addToValue(currentChunk, -1);
            chunkSize.addToValue(chunk, 1);
            productChunkHistoryMap.get(currentChunk).get(history.getChunkKey()).remove(orgRank);
            productChunkHistoryMap.get(chunk).computeIfAbsent(history.getChunkKey(), k -> new TreeMap<>())
                                  .put(history.rank(), history);
        }
    }

    public void addSuccess(History history, double value) {
        addAction(history, true, value);
    }

    public void addFailure(History history, double value) {
        addAction(history, false, value);
    }

    public boolean hasMatches(String searchPhrase, String thisProduct, Set<String> forbidden) {
        for (Chunk chunk : chunkHandler.getChunkList()) {
            if (productChunkHistoryMap.get(chunk).getOrDefault(thisProduct,
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
            collect.addAll(productChunkHistoryMap.get(chunk).getOrDefault(thisProduct,
                    new TreeMap<>()).
                                                         values().stream().filter(
                    searchHistory -> !forbidden.contains(searchHistory.productTo)).limit(maxMatches - collect.size())
                                                 .collect(Collectors.toList()));
            if (collect.size() >= maxMatches) {
                break;
            }
        }
        collect.forEach(history -> addFailure(history, 1 / (double) numberOfCandidates));
        return collect;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: " + name + "\n");
        for (Map.Entry<Chunk, Map<String, TreeMap<Long, History>>> entry : productChunkHistoryMap.entrySet()) {
            sb.append(entry.getKey().toString() + ": " + entry.getValue().size());
            List<History> histories = entry.getValue().values().stream().flatMap(
                    e -> e.values().stream()).collect(
                    Collectors.toList());
            int sumCounter = histories.stream().mapToInt(h -> h.counter).sum();
            double sumSuccess = histories.stream().mapToDouble(h -> h.success).sum();
            double sumFailure = histories.stream().mapToDouble(h -> h.failure).sum();
            sb.append(" histories: " + histories.size() + ", sumCounter: "
                      + sumCounter + ", sumSuccess: "
                      + sumSuccess
                      + ", sumFailure: "
                      + sumFailure + "\n");
        }
        return sb.toString();
    }
}
