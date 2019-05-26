package com.apptus.ecom.mab_strategy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.distribution.BetaDistribution;

import com.apptus.ecom.mab_strategy.indexes.Chunk;
import com.apptus.ecom.mab_strategy.indexes.ChunkHandler;
import com.apptus.ecom.mab_strategy.indexes.CoClicksIndex;
import com.apptus.ecom.mab_strategy.indexes.History;
import com.apptus.ecom.mab_strategy.indexes.SearchCoClicksIndex;
import com.apptus.ecom.mab_strategy.indexes.TopClicksIndex;
import com.apptus.ecom.mab_strategy.indexes.TopSearchClicksIndex;

public class Execute {

    public enum Scheme {
        FIRST,
        LAST,
        EVENLY,
        AGEING_FIRST,
        AGEING_LAST
    }

    public enum ChunkType {
        MINI,
        SMALL,
        ALL
    }

    static class Bandit {
        CoClicksIndex coClicksIndex;
        float alpha;
        float beta;

        public Bandit(CoClicksIndex coClicksIndex, int alpha, int beta) {
            this.coClicksIndex = coClicksIndex;
            this.alpha = alpha;
            this.beta = beta;
        }

        public String toString() {
            return "Bandit{" +
                   "chunk=" + coClicksIndex.toString() +
                   ", alpha=" + alpha +
                   ", beta=" + beta +
                   ", mean=" + alpha / (alpha + beta) +
                   ", variance:=" + alpha * beta / (Math.pow(alpha + beta, 2) * (alpha + beta + 1)) +
                   '}';
        }

        public double sample() {
            BetaDistribution betaDistribution = new BetaDistribution(alpha, beta);
            return betaDistribution.sample();
        }

        public CoClicksIndex getCoClicksIndex() {
            return coClicksIndex;
        }
    }

    static class ClickInfo {
        Set<Pair<Integer, History>> suggestions = new HashSet<>();
        Set<String> products;

        public ClickInfo(int banditId, Set<History> suggestions) {
            suggestions.forEach(history -> this.suggestions.add(new ImmutablePair<>(banditId, history)));
            this.products = suggestions.stream().map(history -> history.getProductFrom()).collect(Collectors.toSet());
        }

        public ClickInfo(Set<Pair<Integer, History>> suggestions) {
            this.suggestions = suggestions;
            this.products = suggestions.stream().map(pair -> pair.getRight().getProductFrom()).collect(Collectors.toSet());
        }

        public Pair<Integer, History> match(String productKey) {
            if (products.contains(productKey)) {
                return suggestions.stream().filter(
                        pair -> pair.getValue().getProductFrom().equals(productKey)).findFirst().get();
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return "ClickInfo{" +
                   "suggestions=" + suggestions +
                   ", products=" + products +
                   '}';
        }
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        String logFile = args[0];
        Scheme scheme = Scheme.valueOf(args[1]);
        int numberOfCandidates = Integer.parseInt(args[2]);
        ChunkType chunkType = ChunkType.valueOf(args[3]);
        boolean withContext = Boolean.parseBoolean(args[4]);
        boolean useMAB = Boolean.parseBoolean(args[5]);

        ChunkHandler chunkHandler = new ChunkHandler();
        addChunk(chunkHandler, chunkType);

//        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(new File(logFile).getParentFile().getParentFile()
//                                                                       + "/logs",
//                "log_" + new File(logFile).getName().substring(0, new File(logFile).getName().length() - 4) + "_" + scheme + "_"
//                + numberOfCandidates + "_" + smallChucks + "_"
//                + withContext + "_" + useMAB + ".txt")));

        CoClicksIndex coClicksIndex = new CoClicksIndex("coClicksIndex", chunkHandler);
        TopClicksIndex topClicksIndex = new TopClicksIndex("topClicksIndex", chunkHandler);
        CoClicksIndex clickPaymentIndex = new CoClicksIndex("clickPaymentIndex", chunkHandler);
        CoClicksIndex coPaymentIndex = new CoClicksIndex("coPaymentIndex", chunkHandler);
        SearchCoClicksIndex searchCoClicksIndex = new SearchCoClicksIndex("searchCoClicksIndex", chunkHandler);
        TopSearchClicksIndex topSearchClicksIndex = new TopSearchClicksIndex("topSearchClicksIndex", chunkHandler);
        SearchCoClicksIndex searchClickPaymentIndex = new SearchCoClicksIndex("searchClickPaymentIndex", chunkHandler);
        SearchCoClicksIndex searchCoPaymentIndex = new SearchCoClicksIndex("searchCoPaymentIndex", chunkHandler);

        List<Bandit> banditList = new ArrayList<>();
        banditList.add(new Bandit(coClicksIndex, 1, 1));
        banditList.add(new Bandit(topClicksIndex, 1, 1));
        banditList.add(new Bandit(clickPaymentIndex, 1, 1));
        banditList.add(new Bandit(coPaymentIndex, 1, 1));
        if (withContext) {
            banditList.add(new Bandit(searchCoClicksIndex, 1, 1));
            banditList.add(new Bandit(topSearchClicksIndex, 1, 1));
            banditList.add(new Bandit(searchClickPaymentIndex, 1, 1));
            banditList.add(new Bandit(searchCoPaymentIndex, 1, 1));
        }

        System.out.println(
                "Execute logs " + logFile + ". Uses " + numberOfCandidates + " candidates and " + scheme + " scheme and " +
                chunkHandler.getChunkList().size() + " chunks and " +
                banditList.size() + " bandits and " + chunkType + " chunkType and " + withContext + " withContext and " +
                useMAB + " useMAB");

        double prevAlpha = 0;
        double prevBeta = 0;
        List<String> events = new ArrayList<>();

        int count = 0;
        int sessionLogCount = 0;
        int totalEvents = 0;
        int totalSearches = 0;
        int totalClicks = 0;
        int totalAddToCart = 0;
        int totalPayments = 0;
        int totalMatchingClicks = 0;

        BufferedReader br = new BufferedReader(new FileReader(logFile));
        String row = br.readLine();
        while (row != null) {
            if (row.startsWith("START SESSION")) {
                events = new ArrayList<>();
            } else if (row.startsWith("END SESSION")) {
                String searchWord = "";
                List<String> clicks = new ArrayList<>();
                List<ClickInfo> clickInfos = new ArrayList<>();

                for (String event : events) {
                    if (event.startsWith("Search:")) {
                        totalSearches++;
                        searchWord = event.substring(7);
                    } else if (event.startsWith("Click:")) {
                        totalClicks++;
                        String productKey = event.substring(6);
                        clicks.add(productKey);

                        // Check if part of previous click suggestions
                        Pair<Integer, History> firstHistory = null;
                        Pair<Integer, History> lastHistory = null;
                        List<Pair<Integer, History>> allHistories = new ArrayList<>();

                        for (ClickInfo clickInfo : clickInfos) {
                            Pair<Integer, History> history = clickInfo.match(productKey);
                            if (history != null) {
                                allHistories.add(history);
                                if (firstHistory == null) {
                                    firstHistory = history;
                                }
                                lastHistory = history;
                            }
                        }
                        if (lastHistory != null) {
                            totalMatchingClicks++;
                            if (scheme == Scheme.FIRST) {
                                updateReward(banditList, firstHistory, 1);
                            } else if (scheme == Scheme.LAST) {
                                updateReward(banditList, lastHistory, 1);
                            } else if (scheme == Scheme.EVENLY) {
                                double reward = 1 / (double) allHistories.size();
                                for (Pair<Integer, History> history : allHistories) {
                                    updateReward(banditList, history, reward);
                                }
                            } else if (scheme == Scheme.AGEING_FIRST) {
                                int sum = 0;
                                for (int i = 0; i < allHistories.size(); i++) {
                                    sum += i + 1;
                                }
                                for (int i = 0; i < allHistories.size(); i++) {
                                    Pair<Integer, History> history = allHistories.get(i);
                                    updateReward(banditList, history, (i + 1) / (double) sum);

                                }
                            } else if (scheme == Scheme.AGEING_LAST) {
                                int sum = 0;
                                for (int i = 0; i < allHistories.size(); i++) {
                                    sum += i + 1;
                                }
                                for (int i = 0; i < allHistories.size(); i++) {
                                    Pair<Integer, History> history = allHistories.get(allHistories.size() - i - 1);
                                    updateReward(banditList, history, (i + 1) / (double) sum);
                                }
                            }
                        }

                        clickInfos.stream().forEach(clickInfo -> clickInfo.suggestions.forEach(suggestion -> banditList.get(
                                suggestion.getKey()).getCoClicksIndex().addFailure(suggestion.getRight(),
                                1 / (float) numberOfCandidates)));

                        if (!useMAB) {
                            // Generate candidates
                            double bestValue = -1;
                            int bestId = -1;
                            for (int i = 0; i < banditList.size(); i++) {
                                Bandit bandit = banditList.get(i);
                                CoClicksIndex coClicksIndex1 = bandit.getCoClicksIndex();
                                if (coClicksIndex1.hasMatches(searchWord, productKey, new HashSet<>())) {
                                    double sample = bandit.sample();
                                    if (sample > bestValue) {
                                        bestValue = sample;
                                        bestId = i;
                                    }
                                }
                            }
                            if (bestId != -1) {
                                Bandit bandit = banditList.get(bestId);
                                CoClicksIndex coClicksIndex1 = bandit.getCoClicksIndex();
                                List<History> candidates = coClicksIndex1.bestMatches(searchWord, productKey, numberOfCandidates,
                                        new HashSet<>(), numberOfCandidates);
                                bandit.beta += 1 / (float) numberOfCandidates;
                                clickInfos.add(new ClickInfo(bestId, new HashSet<>(candidates)));
//                                bw.write(clickInfos.toString() + "\n");
                            }
                        } else {
                            Set<String> addedSuggestions = new HashSet<>();
                            Set<Pair<Integer, History>> suggestions = new HashSet<>();
                            for (int j = 0; j < numberOfCandidates; j++) {
                                // Generate candidates
                                double bestValue = -1;
                                int bestId = -1;
                                for (int i = 0; i < banditList.size(); i++) {
                                    Bandit bandit = banditList.get(i);
                                    CoClicksIndex coClicksIndex1 = bandit.getCoClicksIndex();
                                    if (coClicksIndex1.hasMatches(searchWord, productKey, addedSuggestions)) {
                                        double sample = bandit.sample();
                                        if (sample > bestValue) {
                                            bestValue = sample;
                                            bestId = i;
                                        }
                                    }
                                }
                                if (bestId != -1) {
                                    Bandit bandit = banditList.get(bestId);
                                    CoClicksIndex coClicksIndex1 = bandit.getCoClicksIndex();
                                    List<History> candidates = coClicksIndex1.bestMatches(searchWord, productKey, 1,
                                            addedSuggestions, numberOfCandidates);
                                    bandit.beta += 1 / (float) numberOfCandidates;
                                    suggestions.add(new ImmutablePair<>(bestId, candidates.get(0)));
                                    addedSuggestions.add(candidates.get(0).getProductTo());
                                } else {
                                    break;
                                }
                            }
                            if (!suggestions.isEmpty()) {
                                clickInfos.add(new ClickInfo(suggestions));
//                                bw.write(clickInfos.toString() + "\n");
                            }
                        }

                    } else if (event.startsWith("AddToCart:")) {
                        totalAddToCart++;
                    } else if (event.startsWith("Payment:")) {
                        totalPayments++;
                    }

                }

                // Update indexes
                String prevClick = null;
                Set<String> allClicks = new HashSet<>();
                Set<String> allPayments = new HashSet<>();

                Map<String, Set<String>> searchClicks = new HashMap<>();
                Map<String, Set<String>> searchPayments = new HashMap<>();

                searchWord = null;
                for (String event : events) {
                    if (event.startsWith("Search:")) {
                        searchWord = event.substring(7);
                    } else if (event.startsWith("Click:")) {
                        String click = event.substring(6);
                        allClicks.add(click);

                        if (prevClick != null) {
                            coClicksIndex.addClicks(prevClick, click);
                            topClicksIndex.addClicks(prevClick, click);
                            if (searchWord != null) {
                                final String word = searchWord;
                                if (searchClicks.containsKey(searchWord)) {
                                    searchClicks.get(searchWord).forEach(c -> searchCoClicksIndex.addClicks(word, c, click));
                                    searchClicks.get(searchWord).forEach(c -> topSearchClicksIndex.addClicks(word, c, click));
                                }
                            }
                        }
                        prevClick = click;
                        if (searchWord != null) {
                            searchClicks.computeIfAbsent(searchWord, k -> new HashSet<>()).add(click);
                        }
                    } else if (event.startsWith("Payment:")) {
                        Set<String> payments = Arrays.stream(event.substring(8).replace("[", "").replace("]", "")
                                                                  .replaceAll(" ", "").split(",")).collect(
                                Collectors.toSet());
                        allPayments.addAll(payments);
                        if (searchWord != null && searchClicks.containsKey(searchWord)) {
                            searchPayments.computeIfAbsent(searchWord, k -> new HashSet<>()).addAll(searchClicks.get(searchWord));
                        }
                        for (String oldPayment : allPayments) {
                            for (String oldPayment2 : allPayments) {
                                if (!oldPayment.equals(oldPayment2)) {
                                    coPaymentIndex.addClicks(oldPayment, oldPayment2);
                                }
                            }
                        }
                        for (String click : allClicks) {
                            payments.forEach(payment -> clickPaymentIndex.addClicks(click, payment));
                        }
                        if (searchWord != null) {
                            if (searchClicks.containsKey(searchWord)) {
                                for (String click : searchClicks.get(searchWord)) {
                                    final String word = searchWord;
                                    payments.forEach(payment -> searchClickPaymentIndex.addClicks(word, click, payment));
                                }
                            }
                            if (searchPayments.containsKey(searchWord)) {
                                for (String payment : searchPayments.get(searchWord)) {
                                    final String word = searchWord;
                                    payments.forEach(p -> searchCoPaymentIndex.addClicks(word, payment, p));
                                }
                            }
                        }
                    }

                }

                if (++sessionLogCount % 100000 == 0) {
                    System.out.println(
                            new Date() + ": Read " + sessionLogCount + " sessionlog events and totally " + totalEvents
                            + " events. Searches: " + totalSearches + ", clicks: " + totalClicks + ", addToCarts: "
                            + totalAddToCart + ", payments: " + totalPayments);

                    banditList.forEach(
                            r -> System.out.println(r.toString()));
                    double alphaSum = banditList.stream().mapToDouble(bandit -> bandit.alpha).sum();
                    double betaSum = banditList.stream().mapToDouble(bandit -> bandit.beta).sum();
                    System.out.println(
                            "Total result: alpha: " + alphaSum + ", beta: " + betaSum + ", matches: " + (alphaSum / (
                                    alphaSum + betaSum)));
                    System.out.println(
                            "Total tmp result: alpha: " + (alphaSum - prevAlpha) + ", beta: " + (betaSum - prevBeta)
                            + ", matches: " + ((alphaSum - prevAlpha) / (float) (
                                    (alphaSum - prevAlpha) + (betaSum - prevBeta))));
                    System.out.println("Matching clicks (" + totalMatchingClicks + ", " + totalClicks + "): "
                                       + totalMatchingClicks / (double) totalClicks);
                    prevAlpha = alphaSum;
                    prevBeta = betaSum;
                }

            } else {
                events.add(row);
            }
            row = br.readLine();
        }

        System.out.println(
                new Date() + ": Totally read " + count + " events, " + sessionLogCount + " sessionlog events and totally "
                + totalEvents
                + " events. Searches: " + totalSearches + ", clicks: " + totalClicks + ", addToCarts: "
                + totalAddToCart + ", payments: " + totalPayments);
        System.out.println(
                new Date() + ": Read " + sessionLogCount + " sessionlog events and totally " + totalEvents
                + " events. Searches: " + totalSearches + ", clicks: " + totalClicks + ", addToCarts: "
                + totalAddToCart + ", payments: " + totalPayments);

        banditList.forEach(
                r -> System.out.println(r.toString()));
        double alphaSum = banditList.stream().mapToDouble(bandit -> bandit.alpha).sum();
        double betaSum = banditList.stream().mapToDouble(bandit -> bandit.beta).sum();
        System.out.println(
                "Total result: alpha: " + alphaSum + ", beta: " + betaSum + ", matches: " + (alphaSum / (
                        alphaSum + betaSum)));
        System.out.println(
                "Total tmp result: alpha: " + (alphaSum - prevAlpha) + ", beta: " + (betaSum - prevBeta)
                + ", matches: " + ((alphaSum - prevAlpha) / (float) (
                        (alphaSum - prevAlpha) + (betaSum - prevBeta))));
        System.out.println("Matching clicks (" + totalMatchingClicks + ", " + totalClicks + "): "
                           + totalMatchingClicks / (double) totalClicks);
        br.close();
        long endTime = System.currentTimeMillis();

        System.out.println("Done! Took " + (endTime - startTime) / 1000 / 60 + " minutes.");
//        bw.close();
    }

    private static void updateReward(List<Bandit> banditList, Pair<Integer, History> history, double reward) {
        Bandit bandit = banditList.get(history.getKey());
        bandit.alpha += reward;
        bandit.beta -= reward;
        bandit.beta = Math.max(1, bandit.beta);
        bandit.getCoClicksIndex().addSuccess(history.getValue(), reward);
    }

    private static void addChunk(ChunkHandler chunkHandler, ChunkType chunkType) {
        if (chunkType == ChunkType.MINI) {
            chunkHandler.addChunk(new Chunk(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE));
        } else if (chunkType == ChunkType.SMALL) {
            chunkHandler.addChunk(new Chunk(0, 4, 0, 4));
            chunkHandler.addChunk(new Chunk(4, Integer.MAX_VALUE, 0, 4));
            chunkHandler.addChunk(new Chunk(4, Integer.MAX_VALUE, 4, Integer.MAX_VALUE));
        } else if (chunkType == ChunkType.ALL) {
            chunkHandler.addChunk(new Chunk(0, 2, 0, 2));
            chunkHandler.addChunk(new Chunk(2, 8, 0, 2));
            chunkHandler.addChunk(new Chunk(2, 8, 2, 4));
            chunkHandler.addChunk(new Chunk(2, 8, 4, 8));
            chunkHandler.addChunk(new Chunk(8, 16, 0, 2));
            chunkHandler.addChunk(new Chunk(8, 16, 2, 4));
            chunkHandler.addChunk(new Chunk(8, 16, 4, 8));
            chunkHandler.addChunk(new Chunk(8, 16, 8, 16));
            chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 0, 2));
            chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 2, 4));
            chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 4, 8));
            chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 8, 16));
            chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 16, 32));
            chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 32, Integer.MAX_VALUE));
        } else {
            throw new RuntimeException("Invalid chunk type: " + chunkType);
        }

        chunkHandler.reverse();

    }

}
