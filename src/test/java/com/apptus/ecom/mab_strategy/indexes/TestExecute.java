package com.apptus.ecom.mab_strategy.indexes;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.apptus.ecom.mab_strategy.Execute;
import com.apptus.ecom.mab_strategy.Execute.Scheme;

public class TestExecute {
    private static String FILE_PATH = "/com/apptus/ecom/mab_strategy/indexes/";

    @Test
    public void testExecute() throws URISyntaxException, IOException {
        File events = new File(getClass().getResource(FILE_PATH + "test1.txt").toURI());
        Scheme scheme = Scheme.FIRST;

        Execute.main(new String[]{events.getAbsolutePath(), scheme.toString()});

    }
}
