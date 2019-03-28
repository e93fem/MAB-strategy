package com.apptus.ecom.mab_strategy.indexes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;

public class CoClicksIndex {

    private HashMap<Integer, History> historyMap = new HashMap<>();
    private HashMap<Chunk, Map<String, TreeMap<Long, History>>> productChunkHistoryMap = new HashMap<>();
    private ObjectIntHashMap<Chunk> chunkSize = new ObjectIntHashMap<>();

//    ObjectIntHashMap<String> productIdMap = new ObjectIntHashMap<>();

    private int getId(String product) {
        return Integer.parseInt(product);
//        if (!productIdMap.containsKey(product)) {
//            int size = productIdMap.size();
//            productIdMap.put(product, size);
//        }
//        return productIdMap.get(product);
    }

    public History getHistory(String productFrom, String productTo, ChunkHandler chunkHandler) {
        int productFromId = getId(productFrom);
        int productToId = getId(productTo);
        History history = historyMap.computeIfAbsent(History.externalHashCode(productFrom, productTo),
                h -> new History(productFromId, productToId, productTo));
        return history;
    }

    public void addClicks(String productFrom, String productTo, ChunkHandler chunkHandler) {
        History history = getHistory(productFrom, productTo, chunkHandler);
        Chunk currentChunk = chunkHandler.getChunk(history.confidence(), history.success);
        chunkSize.addToValue(currentChunk, 1);
        history.counter++;
        if (history.counter == 1) {
            TreeMap<Long, History> longHistoryTreeMap = productChunkHistoryMap.computeIfAbsent(currentChunk, h -> new HashMap<>())
                                                                              .computeIfAbsent(productFrom,
                                                                                      h -> new TreeMap<>());
            longHistoryTreeMap.put(history.rank(), history);
        }
    }

    private void addAction(String productFrom, String productTo, ChunkHandler chunkHandler, boolean success, double value) {
        History history = getHistory(productFrom, productTo, chunkHandler);
        long orgRank = history.rank();
        Chunk currentChunk = chunkHandler.getChunk(history.confidence(), history.success);
        if (success) {
            history.success += value;
            history.failure -= value;
        } else {
            history.failure += value;
        }
        Chunk chunk = chunkHandler.getChunk(history.confidence(), history.success);
        if (currentChunk != chunk) {
            chunkSize.addToValue(currentChunk, -1);
            chunkSize.addToValue(chunk, 1);
            productChunkHistoryMap.get(currentChunk).get(productFrom).remove(orgRank);
            productChunkHistoryMap.computeIfAbsent(chunk, k -> new HashMap<>()).computeIfAbsent(productFrom, k -> new TreeMap<>())
                                  .put(history.rank(), history);
        }
    }

    public void addSuccess(String productFrom, String productTo, ChunkHandler chunkHandler, double value) {
        addAction(productFrom, productTo, chunkHandler, true, value);
    }

    public void addFailure(String productFrom, String productTo, ChunkHandler chunkHandler, int maxMatches) {
        addAction(productFrom, productTo, chunkHandler, false, 1 / (double) maxMatches);
    }

    public List<String> bestMatches(String thisProduct, int maxMatches, ChunkHandler chunkHandler, Chunk chunk) {
        List<History> collect = productChunkHistoryMap.getOrDefault(chunk, new HashMap<>()).getOrDefault(thisProduct,
                new TreeMap<>()).values().stream().limit(maxMatches).collect(Collectors.toList());
        collect.forEach(history -> {
            addFailure(thisProduct, history.productToId, chunkHandler, maxMatches);
        });
        return collect.stream().map(history -> history.productToId).collect(
                Collectors.toList());
    }

    protected String internalString(Chunk chunk) {
        List<History> histories = productChunkHistoryMap.getOrDefault(chunk, new HashMap<>()).values().stream().flatMap(
                entry -> entry.values().stream()).collect(
                Collectors.toList());

        int sumCounter = histories.stream().mapToInt(h -> h.counter).sum();
        double sumSuccess = histories.stream().mapToDouble(h -> h.success).sum();
        double sumFailure = histories.stream().mapToDouble(h -> h.failure).sum();

        return "chunkSize: " + chunkSize.get(chunk)+ ", chunk: " + chunk + ", histories: " + histories.size() + ", sumCounter: " + sumCounter + ", sumSuccess: "
               + sumSuccess
               + ", sumFailure: "
               + sumFailure;
    }

    public String toString(Chunk chunk) {
        return "CoClicksIndex: " + internalString(chunk);
    }

    public boolean containsData(Chunk chunk) {
        return chunkSize.get(chunk) > 10000;
    }

}
