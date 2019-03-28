package com.apptus.ecom.mab_strategy.indexes;

import java.util.List;

public class BanditCoClick {

    private CoClicksIndex coClicksIndex;
    private Chunk chunk;

    public BanditCoClick(Chunk chunk,
                         CoClicksIndex coClicksIndex) {
        this.chunk = chunk;
        this.coClicksIndex = coClicksIndex;
    }

//    public List<String> bestMatches(String thisProduct, int maxMatches) {
//        return coClicksIndex.bestMatches(thisProduct, maxMatches, chunk);
//    }

}
