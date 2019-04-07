package com.apptus.ecom.mab_strategy.indexes;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;

public class SearchCoClicksIndex extends CoClicksIndex {

    class Key {
        String searchPhrase;
        String productFrom;

        public Key(String searchPhrase, String productFrom) {
            this.searchPhrase = searchPhrase;
            this.productFrom = productFrom;
        }
    }

    private HashMap<String, History> historyMap = new HashMap<>();
    private HashMap<Chunk, Map<String, TreeMap<Long, History>>> productChunkHistoryMap = new HashMap<>();
    private ObjectIntHashMap<Chunk> chunkSize = new ObjectIntHashMap<>();

    public SearchCoClicksIndex(String name, ChunkHandler chunkHandler) {
        super(name, chunkHandler);
    }

//    private History getHistory(String productFrom, String productTo) {
//        int productToId = getId(productTo);
//        History history = historyMap.computeIfAbsent(History.getKey(productFrom, productTo),
//                h -> new History(productFrom, productTo, productToId));
//        return history;
//    }
//
    public void addClicks(String searchPhrase, String productFrom, String productTo) {


    }
}
