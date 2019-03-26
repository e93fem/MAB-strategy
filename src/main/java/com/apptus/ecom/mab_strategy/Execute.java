package com.apptus.ecom.mab_strategy;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;

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

    private final static IntRuntimeSetting activeDaysSetting = new IntRuntimeSetting(5, 2, 14);

    public static void main(String[] args) throws IOException {
        String logDir = args[0];

        System.out.println("Validate logs " + logDir);
        EventDatabase db = new EventDatabase(new DiskFileNode(new File(logDir)),
                EventLogStream.streams(),
                Origin.random(), activeDaysSetting);
        EventReader reader = db.replicated().readEvents();

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



                totalEvents += sessionLogEvent.events().size();
                for (Object o : sessionLogEvent.events()) {
                    if (o instanceof Search) {
                        totalSearches++;
                    } else if (o instanceof Click) {
                        totalClicks++;
                    } else if (o instanceof AddToCart) {
                        totalAddToCart++;
                    } else if (o instanceof Payment) {
                        totalPayments++;
                    }
                }

                if (++sessionLogCount % 100000 == 0) {
                    System.out.println(
                            new Date() + ": Read " + sessionLogCount + " sessionlog events and totally " + totalEvents
                            + " events. Searches: " + totalSearches + ", clicks: " + totalClicks + ", addToCarts: "
                            + totalAddToCart + ", payments: " + totalPayments);
                    System.out.println("Logs: " + dates);
                    System.out.println("Markets: " + marketCounts);
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
}
