package com.apptus.ecom.mab_strategy;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;

import com.apptus.ecom.mab_strategy.indexes.Chunk;
import com.apptus.ecom.mab_strategy.indexes.ChunkHandler;
import com.apptus.ecom.mab_strategy.indexes.CoClicksIndex;
import com.apptus.ecom.mab_strategy.indexes.TopClicksIndex;
import com.apptus.esales.event.codec.Event;
import com.apptus.esales.event.db.EventDatabase;
import com.apptus.esales.event.db.EventReader;
import com.apptus.esales.event.db.replicate.Origin;
import com.apptus.esales.event.log_stream.EventLogStream;
import com.apptus.esales.event.log_stream.SessionLogEvent;
import com.apptus.esales.event.log_stream.SessionLogEvent.AddToCart;
import com.apptus.esales.event.log_stream.SessionLogEvent.Click;
import com.apptus.esales.event.log_stream.SessionLogEvent.Search;
import com.apptus.esales.event.signal.Payment;
import com.apptus.esales.filesystem.DiskFileNode;
import com.apptus.esales.init.runtime_settings.IntRuntimeSetting;

public class Execute {

    public enum Scheme {
        FIRST,
        LAST,
        EVENLY,
        AGEING_FIRST,
        AGEING_LAST,
    }

    static class Bandit {
        Chunk chunk;
        float alpha;
        float beta;

        public Bandit(Chunk chunk, int alpha, int beta) {
            this.chunk = chunk;
            this.alpha = alpha;
            this.beta = beta;
        }

        public String toString(CoClicksIndex coClicksIndex) {
            return "Bandit{" +
                   "chunk=" + coClicksIndex.toString(chunk) +
                   ", alpha=" + alpha +
                   ", beta=" + beta +
                   ", mean=" + alpha / (alpha + beta) +
                   ", variance:=" + alpha * beta / (Math.pow(alpha + beta, 2) * (alpha + beta + 1)) +
                   '}';
        }

        public Chunk getChunk() {
            return chunk;
        }

        public double sample() {
            BetaDistribution betaDistribution = new BetaDistribution(alpha, beta);
            return betaDistribution.sample();
        }

    }

    static class ClickInfo {
        String productKey;
        int banditId;
        Set<String> suggestions;

        public ClickInfo(String productKey, int banditId, Set<String> suggestions) {
            this.productKey = productKey;
            this.banditId = banditId;
            this.suggestions = suggestions;
        }

        public boolean match(String productKey) {
            return suggestions.contains(productKey);
        }
    }

    private final static IntRuntimeSetting activeDaysSetting = new IntRuntimeSetting(5, 2, 14);

    public static void main(String[] args) throws IOException {
        String logDir = args[0];
        Scheme scheme = Scheme.valueOf(args[1]);

        System.out.println("Execute logs " + logDir);
        EventDatabase db = new EventDatabase(new DiskFileNode(new File(logDir)),
                EventLogStream.streams(),
                Origin.random(), activeDaysSetting);
        EventReader reader = db.replicated().readEvents();

        CoClicksIndex coClicksIndex = new CoClicksIndex();
        TopClicksIndex topClicksIndex = new TopClicksIndex();

        ChunkHandler chunkHandler = new ChunkHandler();
//        chunkHandler.addChunk(new Chunk(0, 4, 0, 4, topClicksIndex));
//        chunkHandler.addChunk(new Chunk(4, Integer.MAX_VALUE, 0, 4, topClicksIndex));
//        chunkHandler.addChunk(new Chunk(4, Integer.MAX_VALUE, 4, Integer.MAX_VALUE, topClicksIndex));
//        chunkHandler.addChunk(new Chunk(0, 4, 0, 4, coClicksIndex));
//        chunkHandler.addChunk(new Chunk(4, Integer.MAX_VALUE, 0, 4, coClicksIndex));
//        chunkHandler.addChunk(new Chunk(4, Integer.MAX_VALUE, 4, Integer.MAX_VALUE, coClicksIndex));

        chunkHandler.addChunk(new Chunk(0, 2, 0, 2, coClicksIndex));
        chunkHandler.addChunk(new Chunk(2, 8, 0, 2, coClicksIndex));
        chunkHandler.addChunk(new Chunk(2, 8, 2, 4, coClicksIndex));
        chunkHandler.addChunk(new Chunk(2, 8, 4, 8, coClicksIndex));
        chunkHandler.addChunk(new Chunk(8, 16, 0, 2, coClicksIndex));
        chunkHandler.addChunk(new Chunk(8, 16, 2, 4, coClicksIndex));
        chunkHandler.addChunk(new Chunk(8, 16, 4, 8, coClicksIndex));
        chunkHandler.addChunk(new Chunk(8, 16, 8, 16, coClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 0, 2, coClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 2, 4, coClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 4, 8, coClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 8, 16, coClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 16, 32, coClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 32, Integer.MAX_VALUE, coClicksIndex));
        chunkHandler.addChunk(new Chunk(0, 2, 0, 2, topClicksIndex));
        chunkHandler.addChunk(new Chunk(2, 8, 0, 2, topClicksIndex));
        chunkHandler.addChunk(new Chunk(2, 8, 2, 4, topClicksIndex));
        chunkHandler.addChunk(new Chunk(2, 8, 4, 8, topClicksIndex));
        chunkHandler.addChunk(new Chunk(8, 16, 0, 2, topClicksIndex));
        chunkHandler.addChunk(new Chunk(8, 16, 2, 4, topClicksIndex));
        chunkHandler.addChunk(new Chunk(8, 16, 4, 8, topClicksIndex));
        chunkHandler.addChunk(new Chunk(8, 16, 8, 16, topClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 0, 2, topClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 2, 4, topClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 4, 8, topClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 8, 16, topClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 16, 32, topClicksIndex));
        chunkHandler.addChunk(new Chunk(16, Integer.MAX_VALUE, 32, Integer.MAX_VALUE, topClicksIndex));

        List<Bandit> banditList = new ArrayList<>();
        chunkHandler.getChunkList().forEach(chunk -> banditList.add(new Bandit(chunk, 1, 1)));

        double prevAlpha = 0;
        double prevBeta = 0;

        int count = 0;
        int sessionLogCount = 0;
        int totalEvents = 0;
        int totalSearches = 0;
        int totalClicks = 0;
        int totalAddToCart = 0;
        int totalPayments = 0;
        Set<String> dates = new HashSet<>();
        MutableObjectIntMap<String> marketCounts = new ObjectIntHashMap<>();
        while (reader.read()) {
            Event event = reader.event();
            if (event instanceof SessionLogEvent) {
                SessionLogEvent sessionLogEvent = (SessionLogEvent) event;
                Timestamp timestamp = new Timestamp(sessionLogEvent.timestamp);
                Date date = new Date(timestamp.getTime());
                String dateString = new SimpleDateFormat("yyyy-MM-dd").format(date);
                dates.add(dateString);
                marketCounts.addToValue(sessionLogEvent.market, 1);

                List<ClickInfo> clickInfos = new ArrayList<>();

                totalEvents += sessionLogEvent.events().size();
                for (Object o : sessionLogEvent.events()) {
                    if (o instanceof Search) {
                        totalSearches++;
                    } else if (o instanceof Click) {
                        totalClicks++;
                        String productKey = ((Click) o).productKey;

                        // Check if part of previous click suggestions
                        ClickInfo firstClickInfo = null;
                        ClickInfo lastClickInfo = null;
                        List<ClickInfo> allClickInfo = new ArrayList<>();

                        for (ClickInfo clickInfo : clickInfos) {
                            if (clickInfo.match(productKey)) {
                                allClickInfo.add(clickInfo);
                                if (firstClickInfo == null) {
                                    firstClickInfo = clickInfo;
                                }
                                lastClickInfo = clickInfo;
                            }
                        }
                        if (lastClickInfo != null) {
                            if (scheme == Scheme.FIRST) {
                                updateReward(chunkHandler, banditList, productKey,
                                        firstClickInfo, 1);
                            } else if (scheme == Scheme.LAST) {
                                updateReward(chunkHandler, banditList, productKey, lastClickInfo, 1);
                            } else if (scheme == Scheme.EVENLY) {
                                double reward = 1 / allClickInfo.size();
                                if (allClickInfo.size() > 1) {
                                    int i = 0;
                                }
                                for (ClickInfo clickInfo : allClickInfo) {
                                    updateReward(chunkHandler, banditList, productKey, clickInfo, reward);
                                }
                            }
                        }

                        // Generate candidates
                        double bestValue = -1;
                        int bestId = -1;
                        for (int i = 0; i < banditList.size(); i++) {
                            Bandit bandit = banditList.get(i);
                            if (bandit.getChunk().getCoClicksIndex().containsData(bandit.chunk)) {
                                double sample = bandit.sample();
                                if (sample > bestValue) {
                                    bestValue = sample;
                                    bestId = i;
                                }
                            }
                        }
                        if (bestId != -1) {
                            Bandit bandit = banditList.get(bestId);
                            List<String> candidates = bandit.getChunk().getCoClicksIndex().bestMatches(productKey, 1,
                                    chunkHandler, bandit.chunk);
                            bandit.beta++;
                            clickInfos.add(new ClickInfo(productKey, bestId, new HashSet<>(candidates)));
                        }

                    } else if (o instanceof AddToCart) {
                        totalAddToCart++;
                    } else if (o instanceof Payment) {
                        totalPayments++;
                    }
                }

                Set<Click> clicks = new HashSet<>();
                for (Object o : sessionLogEvent.events()) {

                    if (o instanceof Search) {
                    } else if (o instanceof Click) {
                        clicks.add((Click) o);
                    } else if (o instanceof AddToCart) {
                    } else if (o instanceof Payment) {
                    }

                }
                List<Click> list = new ArrayList<>(clicks);
                for (int i = 0; i < list.size(); i++) {
                    for (int j = 0; j < list.size(); j++) {
                        if (i != j) {
                            coClicksIndex.addClicks(list.get(i).productKey, list.get(j).productKey, chunkHandler);
                            topClicksIndex.addClicks(list.get(i).productKey, list.get(j).productKey, chunkHandler);
                        }
                    }
                }

                if (++sessionLogCount % 100000 == 0) {

                    System.out.println(
                            new Date() + ": Read " + sessionLogCount + " sessionlog events and totally " + totalEvents
                            + " events. Searches: " + totalSearches + ", clicks: " + totalClicks + ", addToCarts: "
                            + totalAddToCart + ", payments: " + totalPayments);
                    System.out.println("Logs: " + dates);
                    System.out.println("Markets: " + marketCounts);

                    banditList.forEach(r -> System.out.println(r.toString(r.getChunk().getCoClicksIndex())));
                    double alphaSum = banditList.stream().mapToDouble(bandit -> bandit.alpha).sum();
                    double betaSum = banditList.stream().mapToDouble(bandit -> bandit.beta).sum();
                    System.out.println(
                            "Total result: alpha: " + alphaSum + ", beta: " + betaSum + ", matches: " + (alphaSum / (
                                    alphaSum + betaSum)));
                    System.out.println(
                            "Total tmp result: alpha: " + (alphaSum - prevAlpha) + ", beta: " + (betaSum - prevBeta)
                            + ", matches: " + ((alphaSum - prevAlpha) / (float) (
                                    (alphaSum - prevAlpha) + (betaSum - prevBeta))));
                    prevAlpha = alphaSum;
                    prevBeta = betaSum;
                }
            }
            if (++count % 100000 == 0) {
                System.out.println(new Date() + ": Read " + count + " events.");
            }
        }
        System.out.println(
                new Date() + ": Totally read " + count + " events, " + sessionLogCount + " sessionlog events and totally "
                + totalEvents
                + " events. Searches: " + totalSearches + ", clicks: " + totalClicks + ", addToCarts: "
                + totalAddToCart + ", payments: " + totalPayments);
        System.out.println("Logs: " + dates);
        System.out.println("Markets: " + marketCounts);
        db.close();

    }

    private static void updateReward(ChunkHandler chunkHandler, List<Bandit> banditList,
                                     String productKey, ClickInfo clickInfo, double reward) {
        Bandit bandit = banditList.get(clickInfo.banditId);
        bandit.alpha += reward;
        bandit.beta -= reward;
        bandit.getChunk().getCoClicksIndex().addSuccess(clickInfo.productKey, productKey, chunkHandler, reward);
    }
}
