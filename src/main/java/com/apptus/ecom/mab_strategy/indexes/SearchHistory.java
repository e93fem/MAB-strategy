package com.apptus.ecom.mab_strategy.indexes;

public class SearchHistory extends History {

    String searchPhrase;

    public SearchHistory(String searchPhrase, String productFrom, String productTo, int productToId) {
        super(productFrom, productTo, productToId);
        this.searchPhrase = searchPhrase;
    }

    public static String getKey(String searchPhrase, String productFrom, String productTo) {
        return searchPhrase + ":" + productFrom + ":" + productTo;
    }

    public static String getChunkKey(String searchPhrase, String productFrom) {
        return searchPhrase + ":" + productFrom;
    }

    public String getChunkKey() {
        return getChunkKey(searchPhrase, productFrom);
    }

}
