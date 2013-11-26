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

package org.apache.marmotta.kiwi.loader.generic;

import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apache.marmotta.kiwi.loader.util.UnitFormatter;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    private KiWiHandler handler;


    protected RrdDb statDB;
    protected Sample statSample;
    protected long statLastDump;

    protected long SAMPLE_INTERVAL = TimeUnit.SECONDS.toSeconds(5L);
    protected long DIAGRAM_INTERVAL = TimeUnit.MINUTES.toSeconds(5L);

    protected ScheduledExecutorService statSampler;

    private long start, previous;

    public Statistics(KiWiHandler handler) {
        this.handler = handler;
    }


    public void startSampling() {
        log.info("statistics gathering enabled; starting statistics database");

        this.start = System.currentTimeMillis();
        this.previous = System.currentTimeMillis();

        File statFile = new File("kiwiloader.rrd");
        if(statFile.exists()) {
            log.info("deleting old statistics database");
            statFile.delete();
        }

        RrdDef stCfg = new RrdDef("kiwiloader.rrd");
        stCfg.setStep(SAMPLE_INTERVAL);
        stCfg.addDatasource("triples", DsType.COUNTER, 600, Double.NaN, Double.NaN);
        stCfg.addDatasource("nodes", DsType.COUNTER, 600, Double.NaN, Double.NaN);
        stCfg.addDatasource("nodes-loaded", DsType.COUNTER, 600, Double.NaN, Double.NaN);
        stCfg.addDatasource("cache-hits", DsType.COUNTER, 600, Double.NaN, Double.NaN);
        stCfg.addDatasource("cache-misses", DsType.COUNTER, 600, Double.NaN, Double.NaN);
        stCfg.addArchive(ConsolFun.AVERAGE, 0.5, 1, 1440);  // every five seconds for 2 hours
        stCfg.addArchive(ConsolFun.AVERAGE, 0.5, 12, 1440); // every minute for 1 day
        stCfg.addArchive(ConsolFun.AVERAGE, 0.5, 60, 1440); // every five minutes for five days

        try {
            statDB = new RrdDb(stCfg);
            statSample = statDB.createSample();
            statLastDump = System.currentTimeMillis();

            // start a sampler thread to run at the SAMPLE_INTERVAL
            statSampler = Executors.newScheduledThreadPool(2);
            statSampler.scheduleAtFixedRate(new StatisticsUpdater(),0, SAMPLE_INTERVAL, TimeUnit.SECONDS);

            // create a statistics diagram every 5 minutes
            statSampler.scheduleAtFixedRate(new DiagramUpdater(),DIAGRAM_INTERVAL,DIAGRAM_INTERVAL,TimeUnit.SECONDS);
        } catch (IOException e) {
            log.warn("could not initialize statistics database: {}",e.getMessage());
        }

    }

    public void stopSampling() {
        DiagramUpdater du = new DiagramUpdater();
        du.run();

        if(statDB != null) {
            try {
                statDB.close();
            } catch (IOException e) {
                log.warn("could not close statistics database...");
            }
        }
        if(statSampler != null) {
            statSampler.shutdown();
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
                    log.info("imported {} triples; statistics: {}/sec, {}/sec (last min), {}/sec (last hour)", UnitFormatter.formatSize(handler.triples), UnitFormatter.formatSize((handler.config.getCommitBatchSize() * 1000) / (System.currentTimeMillis() - previous)), UnitFormatter.formatSize(triplesLastMin), UnitFormatter.formatSize(triplesLastHour));
                } else {
                    log.info("imported {} triples ({}/sec, no long-time averages available)", UnitFormatter.formatSize(handler.triples), UnitFormatter.formatSize((handler.config.getCommitBatchSize() * 1000) / (System.currentTimeMillis() - previous)));
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

            long cacheMisses = 0, cacheHits = 0;
            for(SelfPopulatingCache c : new SelfPopulatingCache[] { handler.literalCache, handler.uriCache, handler.bnodeCache }) {
                cacheHits   += c.getStatistics().getCacheHits();
                cacheMisses += c.getStatistics().getCacheMisses();
            }

            try {
                long time = System.currentTimeMillis() / 1000;

                synchronized (statSample) {
                    statSample.setTime(time);
                    statSample.setValues(handler.triples, handler.nodes, handler.nodesLoaded, cacheHits, cacheMisses);
                    statSample.update();
                }

            } catch (Exception e) {
                log.warn("could not update statistics database: {}", e.getMessage());
            }
        }
    }


    private class DiagramUpdater implements Runnable {
        @Override
        public void run() {
            try {
                File gFile = new File(handler.config.getStatisticsGraph());

                if(gFile.exists()) {
                    gFile.delete();
                }

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


                gDef.datasource("triples", "kiwiloader.rrd", "triples", ConsolFun.AVERAGE);
                gDef.datasource("nodes", "kiwiloader.rrd", "nodes", ConsolFun.AVERAGE);
                gDef.datasource("nodes-loaded", "kiwiloader.rrd", "nodes-loaded", ConsolFun.AVERAGE);
                gDef.datasource("cache-hits", "kiwiloader.rrd", "cache-hits", ConsolFun.AVERAGE);
                gDef.datasource("cache-misses", "kiwiloader.rrd", "cache-misses", ConsolFun.AVERAGE);

                gDef.line("triples", Color.BLUE, "Triples Written", 3F);
                gDef.line("nodes", Color.MAGENTA, "Nodes Written", 3F);
                gDef.line("nodes-loaded", Color.CYAN, "Nodes Loaded", 3F);
                gDef.line("cache-hits", Color.GREEN, "Node Cache Hits");
                gDef.line("cache-misses", Color.ORANGE, "Node Cache Misses");


                gDef.setImageFormat("png");
                gDef.gprint("triples", ConsolFun.AVERAGE, "average triples/sec: %,.0f\\l");
                gDef.gprint("nodes", ConsolFun.AVERAGE, "average nodes/sec: %,.0f\\l");

                RrdGraph graph = new RrdGraph(gDef);
                BufferedImage img = new BufferedImage(900,750, BufferedImage.TYPE_INT_RGB);
                graph.render(img.getGraphics());
                ImageIO.write(img, "png", gFile);

                log.info("updated statistics diagram generated in {}", handler.config.getStatisticsGraph());

                statLastDump = System.currentTimeMillis();
            } catch (Exception ex) {
                log.warn("error creating statistics diagram: {}", ex.getMessage());
            }
        }

    }
}
