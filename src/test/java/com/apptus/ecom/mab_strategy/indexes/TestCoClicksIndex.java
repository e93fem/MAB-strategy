package com.apptus.ecom.mab_strategy.indexes;

import java.util.List;

import org.junit.Test;

public class TestCoClicksIndex {

    @Test
    public void testConverters()  {

        CoClicksIndex coClicksIndex = new CoClicksIndex();

        coClicksIndex.addClicks("p1", "p2");
        coClicksIndex.addClicks("p1", "p3");
        coClicksIndex.addClicks("p1", "p3");
        coClicksIndex.addClicks("p1", "p3");

        List<String> matches = coClicksIndex.bestMatches("p1", 5);
        System.out.println(matches);

    }

}
