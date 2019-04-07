package com.apptus.ecom.mab_strategy.indexes;

public class SearchHistory extends History {

    String searchPhrase;

    public SearchHistory(String searchPhrase, String productFrom, String productTo, int productToId) {
        super(productFrom, productTo, productToId);
        this.searchPhrase = searchPhrase;
    }


}
