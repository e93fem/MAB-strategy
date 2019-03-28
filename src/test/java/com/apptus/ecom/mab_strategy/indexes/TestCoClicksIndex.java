package com.apptus.ecom.mab_strategy.indexes;

import java.util.List;

import org.junit.Test;

public class TestCoClicksIndex {

    @Test
    public void testConverters() {

        CoClicksIndex coClicksIndex = new CoClicksIndex();
        ChunkHandler chunkHandler = new ChunkHandler();
        Chunk expectChunk1 = new Chunk(0, 2, 0, 2, coClicksIndex);
        chunkHandler.addChunk(expectChunk1);
        Chunk expectChunk2 = new Chunk(2, Integer.MAX_VALUE, 0, 2, coClicksIndex);
        chunkHandler.addChunk(expectChunk2);
        Chunk expectChunk3 = new Chunk(2, Integer.MAX_VALUE, 2, Integer.MAX_VALUE, coClicksIndex);
        chunkHandler.addChunk(expectChunk3);

        coClicksIndex.addClicks("1", "2", chunkHandler);
        coClicksIndex.addClicks("1", "3", chunkHandler);
        coClicksIndex.addClicks("1", "3", chunkHandler);
        coClicksIndex.addClicks("1", "3", chunkHandler);
        coClicksIndex.addSuccess("1", "2", chunkHandler, 1);
        coClicksIndex.addSuccess("1", "2", chunkHandler, 1);
        coClicksIndex.addFailure("1", "2", chunkHandler, 1);

        List<String> matches = coClicksIndex.bestMatches("1", 5, chunkHandler, expectChunk1);
        System.out.println(matches);

    }

}
