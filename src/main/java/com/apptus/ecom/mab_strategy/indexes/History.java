package com.apptus.ecom.mab_strategy.indexes;

import java.util.Objects;

public class History {
    int productFrom;
    int productTo;
    String productToId;

    int counter;
    double success;
    double failure;

    private static long SIZE = 10_000_000;

    public History(int productFrom, int productTo, String productToId) {
        this.productFrom = productFrom;
        this.productTo = productTo;
        this.productToId = productToId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        History history = (History) o;
        return Objects.equals(productFrom, history.productFrom) &&
               Objects.equals(productTo, history.productTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productFrom, productTo);
    }

    public static int externalHashCode(String productFrom, String productTo) {
        return Objects.hash(productFrom, productTo);
    }

    public double confidence() {
        return success + failure;
    }

    public long rank() {
        if (confidence() == 0) {
            return counter * SIZE + productTo;
        } else {
            return (long) (success / (failure + success) * SIZE * SIZE + counter * SIZE + productTo);
        }
    }

}
