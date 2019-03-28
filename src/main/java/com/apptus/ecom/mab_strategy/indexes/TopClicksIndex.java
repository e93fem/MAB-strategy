package com.apptus.ecom.mab_strategy.indexes;

import java.util.List;

public class TopClicksIndex extends CoClicksIndex{

    @Override
    public History getHistory(String productFrom, String productTo, ChunkHandler chunkHandler) {
        return super.getHistory("0", productTo, chunkHandler);
    }

    @Override
    public void addClicks(String productFrom, String productTo, ChunkHandler chunkHandler) {
        super.addClicks("0", productTo, chunkHandler);
    }

    @Override
    public void addSuccess(String productFrom, String productTo, ChunkHandler chunkHandler, double value) {
        super.addSuccess("0", productTo, chunkHandler, value);
    }

    @Override
    public void addFailure(String productFrom, String productTo, ChunkHandler chunkHandler, int maxMatches) {
        super.addFailure("0", productTo, chunkHandler, maxMatches);
    }

    @Override
    public List<String> bestMatches(String thisProduct, int maxMatches, ChunkHandler chunkHandler, Chunk chunk) {
        return super.bestMatches("0", maxMatches, chunkHandler, chunk);
    }

    @Override
    public String toString(Chunk chunk) {
        return "TopClicksIndex: " + internalString(chunk);
    }
}
