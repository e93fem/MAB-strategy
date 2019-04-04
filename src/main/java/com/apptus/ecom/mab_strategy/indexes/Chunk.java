package com.apptus.ecom.mab_strategy.indexes;

import java.util.Objects;

public class Chunk {

    int minConf;
    int maxConf;
    int minSuccess;
    int maxSuccess;

    public Chunk(int minConf, int maxConf, int minSuccess, int maxSuccess) {
        this.minConf = minConf;
        this.maxConf = maxConf;
        this.minSuccess = minSuccess;
        this.maxSuccess = maxSuccess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Chunk chunk = (Chunk) o;
        return minConf == chunk.minConf &&
               maxConf == chunk.maxConf &&
               minSuccess == chunk.minSuccess &&
               maxSuccess == chunk.maxSuccess;
    }


    @Override
    public int hashCode() {
        return Objects.hash(minConf, maxConf, minSuccess, maxSuccess);
    }

    @Override
    public String toString() {
        return "Chunk{" +
               "minConf=" + minConf +
               ", maxConf=" + maxConf +
               ", minSuccess=" + minSuccess +
               ", maxSuccess=" + maxSuccess +
               '}';
    }
}
