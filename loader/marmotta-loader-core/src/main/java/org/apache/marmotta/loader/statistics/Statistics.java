/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.loader.statistics;

import org.apache.commons.configuration.Configuration;
import org.apache.marmotta.loader.api.LoaderOptions;
import org.apache.marmotta.loader.util.UnitFormatter;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.*;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Collect statistics from a KiWiHandler by sampling at given time intervals and logging to a RRD database.
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class Statistics {

    private static Logger log = LoggerFactory.getLogger(Statistics.class);

    private StatisticsHandler handler;

    protected Path statFile;
    protected RrdDb statDB;
    protected Sample statSample;
    protected long statLastDump;

    protected long SAMPLE_INTERVAL = TimeUnit.SECONDS.toSeconds(5L);
    protected long DIAGRAM_INTERVAL = TimeUnit.MINUTES.toSeconds(5L);

    protected ScheduledExecutorService statSampler;

    private long start, previous;

    private Configuration configuration;
    private DiagramUpdater diagramUpdater;

    public Statistics(StatisticsHandler handler, Configuration configuration) {
        this.handler       = handler;
        this.configuration = configuration;
    }


    public void startSampling() {
        log.info("statistics gathering enabled; starting statistics database");

        this.start = System.currentTimeMillis();
        this.previous = System.currentTimeMillis();

        try {
            statFile = Files.createTempFile("kiwiloader.", ".rrd");
            Path gFile;
            if (configuration.containsKey(LoaderOptions.STATISTICS_GRAPH)) {
                gFile = Paths.get(configuration.getString(LoaderOptions.STATISTICS_GRAPH));
            } else {
                gFile = Files.createTempFile("marmotta-loader.", ".png");
            }


            RrdDef stCfg = new RrdDef(statFile.toString());
            stCfg.setStep(SAMPLE_INTERVAL);
            stCfg.addDatasource("triples", DsType.COUNTER, 600, Double.NaN, Double.NaN);
            stCfg.addArchive(ConsolFun.AVERAGE, 0.5, 1, 1440);  // every five seconds for 2 hours
            stCfg.addArchive(ConsolFun.AVERAGE, 0.5, 12, 1440); // every minute for 1 day
            stCfg.addArchive(ConsolFun.AVERAGE, 0.5, 60, 1440); // every five minutes for five days

            statDB = new RrdDb(stCfg);
            statSample = statDB.createSample();
            statLastDump = System.currentTimeMillis();

            // start a sampler thread to run at the SAMPLE_INTERVAL
            statSampler = Executors.newScheduledThreadPool(2);
            statSampler.scheduleAtFixedRate(new StatisticsUpdater(),0, SAMPLE_INTERVAL, TimeUnit.SECONDS);

            // create a statistics diagram every 5 minutes
            diagramUpdater = new DiagramUpdater(gFile);
            statSampler.scheduleAtFixedRate(diagramUpdater,DIAGRAM_INTERVAL,DIAGRAM_INTERVAL,TimeUnit.SECONDS);
        } catch (IOException e) {
            log.warn("could not initialize statistics database: {}",e.getMessage());
        }

    }

    public void stopSampling() {
        if(statSampler != null) {
            statSampler.shutdown();
        }

        if (diagramUpdater != null) {
            diagramUpdater.run();
        }

        if(statDB != null) {
            try {
                statDB.close();
            } catch (IOException e) {
                log.warn("could not close statistics database...");
            }
        }

        try {
            Files.deleteIfExists(statFile);
        } catch (IOException e) {
            log.warn("could not cleanup statistics database: {}",e.getMessage());
        }
    }

    public void printStatistics() {
        if(statSample != null) {
            try {
                long time = System.currentTimeMillis() / 1000;

                FetchRequest minRequest = statDB.createFetchRequest(ConsolFun.AVERAGE, time - 60  , time);
                FetchData minData = minRequest.fetchData();
                double triplesLastMin = minData.getAggregate("triples", ConsolFun.AVERAGE);

                FetchRequest hourRequest = statDB.createFetchRequest(ConsolFun.AVERAGE, time - (60 * 60) , time);
                FetchData hourData = hourRequest.fetchData();
                double triplesLastHour = hourData.getAggregate("triples", ConsolFun.AVERAGE);

                if(triplesLastMin != Double.NaN) {
                    log.info("imported {} triples; statistics: {}/sec (last min), {}/sec (last hour)", UnitFormatter.formatSize(handler.triples), UnitFormatter.formatSize(triplesLastMin), UnitFormatter.formatSize(triplesLastHour));
                }
                previous = System.currentTimeMillis();

            } catch (IOException e) {
                log.warn("error updating statistics: {}", e.getMessage());
            }
        } else {
        }


    }


    private class StatisticsUpdater implements Runnable {
        @Override
        public void run() {

            try {
                long time = System.currentTimeMillis() / 1000;

                synchronized (statSample) {
                    statSample.setTime(time);
                    statSample.setValues(handler.triples);
                    statSample.update();
                }

            } catch (Exception e) {
                log.warn("could not update statistics database: {}", e.getMessage());
            }
        }
    }


    private class DiagramUpdater implements Runnable {

        private final Path gFile;

        public DiagramUpdater(Path gFile) {
            this.gFile = gFile;
        }

        @Override
        public void run() {
            try {
                // generate PNG diagram
                RrdGraphDef gDef = new RrdGraphDef();
                gDef.setFilename("-");
                gDef.setWidth(800);
                gDef.setHeight(600);
                gDef.setStartTime(start / 1000);
                gDef.setEndTime(System.currentTimeMillis() / 1000);
                gDef.setTitle("KiWiLoader Performance");
                gDef.setVerticalLabel("number/sec");
                gDef.setAntiAliasing(true);


                gDef.datasource("triples", statFile.toString(), "triples", ConsolFun.AVERAGE);

                gDef.line("triples", Color.BLUE, "Triples Written", 3F);


                gDef.setImageFormat("png");
                gDef.gprint("triples", ConsolFun.AVERAGE, "average triples/sec: %,.0f\\l");

                RrdGraph graph = new RrdGraph(gDef);
                BufferedImage img = new BufferedImage(900,750, BufferedImage.TYPE_INT_RGB);
                graph.render(img.getGraphics());

                try (OutputStream stream = Files.newOutputStream(gFile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                    ImageIO.write(img, "png", stream);
                }

                log.info("updated statistics diagram generated in {}", gFile);

                statLastDump = System.currentTimeMillis();
            } catch (Exception ex) {
                log.warn("error creating statistics diagram: {}", ex.getMessage());
            }
        }

    }
}
