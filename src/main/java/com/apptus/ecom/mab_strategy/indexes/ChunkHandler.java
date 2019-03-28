package com.apptus.ecom.mab_strategy.indexes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkHandler {

    List<Chunk> chunkList = new ArrayList<>();
    Map<Integer, Map<Integer, Chunk>> chunkMap = new HashMap<>();

    int maxConf = 0;
    int maxSuccess = 0;

    public void addChunk(Chunk chunk) {
        chunkList.add(chunk);
        for (int i = chunk.minConf; i < (chunk.maxConf == Integer.MAX_VALUE ? chunk.minConf + 1 : chunk.maxConf); i++) {
            Map<Integer, Chunk> integerChunkMap = chunkMap.computeIfAbsent(i, k -> new HashMap<>());
            if (i > maxConf) {
                maxConf = i;
            }
            for (int j = chunk.minSuccess; j < (chunk.maxSuccess == Integer.MAX_VALUE ? chunk.minSuccess + 1 : chunk.maxSuccess);
                 j++) {
                integerChunkMap.put(j, chunk);
                if (j > maxSuccess) {
                    maxSuccess = j;
                }
            }
        }
    }

    public List<Chunk> getChunkList() {
        return chunkList;
    }

    public Chunk getChunk(double conf, double success) {
        return chunkMap.get((int) (conf > maxConf ? maxConf : conf)).get((int) (success > maxSuccess ? maxSuccess : success));
    }
}