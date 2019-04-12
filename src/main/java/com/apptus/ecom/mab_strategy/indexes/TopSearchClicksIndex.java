package com.apptus.ecom.mab_strategy.indexes;

import java.util.List;
import java.util.Set;

public class TopSearchClicksIndex extends SearchCoClicksIndex {

    public TopSearchClicksIndex(String name, ChunkHandler chunkHandler) {
        super(name, chunkHandler);
    }

    @Override
    public void addClicks(String searchPhrase, String productFrom, String productTo) {
        super.addClicks(searchPhrase, "0", productTo);
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
    public List<History> bestMatches(String searchPhrase, String thisProduct, int maxMatches, Set<String> forbidden,
                                     int numberOfCandidates) {
        return super.bestMatches(searchPhrase, "0", maxMatches, forbidden, numberOfCandidates);
    }

    @Override
    public boolean hasMatches(String searchPhrase, String thisProduct, Set<String> forbidden) {
        return super.hasMatches(searchPhrase, "0", forbidden);
    }
}
