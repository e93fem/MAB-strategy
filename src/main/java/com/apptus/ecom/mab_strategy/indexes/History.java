package com.apptus.ecom.mab_strategy.indexes;

import java.util.Objects;

public class History {
    String productTo;
    String productFrom;
    int productToId;

    int counter;
    double success;
    double failure;

    private static long SIZE = 10_000_000;

    public History(String productFrom, String productTo, int productToId) {
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

    public static String getKey(String productFrom, String productTo) {
        return productFrom + ":" + productTo;
    }

    public String getProductFrom() {
        return productFrom;
    }

    public String getKey() {
        return getKey(productFrom, productTo);
    }

    public double confidence() {
        return success + failure;
    }

    public long rank() {
        if (confidence() == 0) {
            return counter * SIZE + productToId;
        } else {
            return (long) (success / (failure + success) * SIZE * SIZE + counter * SIZE + productToId);
        }
    }

    @Override
    public String toString() {
        return "History{" +
               "productTo='" + productTo + '\'' +
               ", productFrom='" + productFrom + '\'' +
               ", productToId=" + productToId +
               ", counter=" + counter +
               ", success=" + success +
               ", failure=" + failure +
               '}';
    }
}
