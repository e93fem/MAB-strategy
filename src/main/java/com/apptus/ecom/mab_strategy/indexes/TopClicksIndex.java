package com.apptus.ecom.mab_strategy.indexes;

import java.util.List;

public class TopClicksIndex extends CoClicksIndex {

    public TopClicksIndex(String name, ChunkHandler chunkHandler) {
        super(name, chunkHandler);
    }

    @Override
    public void addClicks(String productFrom, String productTo) {
        super.addClicks("0", productTo);
    }

    @Override
    public void addSuccess(History history, double value) {
        super.addSuccess(history, value);
    }

    @Override
    public void addFailure(History history, double value) {
        super.addFailure(history, value);
    }

    @Override
    public List<History> bestMatches(String thisProduct, int maxMatches) {
        return super.bestMatches("0", maxMatches);
    }

    @Override
    public boolean hasMatches(String thisProduct) {
        return super.hasMatches("0");
    }
}
