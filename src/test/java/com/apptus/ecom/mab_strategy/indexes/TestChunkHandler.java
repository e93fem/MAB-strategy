package com.apptus.ecom.mab_strategy.indexes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestChunkHandler {

    @Test
    public void testChunkHandler() {
        CoClicksIndex coClicksIndex = new CoClicksIndex();

        ChunkHandler chunkHandler = new ChunkHandler();
        Chunk expectChunk1 = new Chunk(0, 2, 0, 2, coClicksIndex);
        chunkHandler.addChunk(expectChunk1);
        Chunk expectChunk2 = new Chunk(2, Integer.MAX_VALUE, 0, 2, coClicksIndex);
        chunkHandler.addChunk(expectChunk2);
        Chunk expectChunk3 = new Chunk(2, Integer.MAX_VALUE, 2, Integer.MAX_VALUE, coClicksIndex);
        chunkHandler.addChunk(expectChunk3);

        Chunk chunk1 = chunkHandler.getChunk(1, 1);
        assertEquals(expectChunk1, chunk1);
        Chunk chunk3 = chunkHandler.getChunk(100, 100);
        assertEquals(expectChunk3, chunk3);
    }
}
