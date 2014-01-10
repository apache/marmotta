/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.marmotta.splash;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.awt.*;

/**
 * Base class for listeners updating the splash screen on application startup.
 * Offers methods for displaying text and updating a progress bar.
 * <p/>
 * Author: Sebastian Schaffert
 */
public abstract class SplashScreenUpdaterBase {

    protected static Log log = LogFactory.getLog(SplashScreenUpdaterBase.class);

    private static final int status_pos_x = 25;
    private static final int status_pos_y = 467;

    private static final int splash_border = 3;
    private static final int progress_bar_y = 480;
    private static final int progress_bar_height = 3;

    private static final Color color_background = new Color(245,244,230);
    private static final Color color_progress_bg = new Color(252,117,51);
    private static final Color color_progress_fg = Color.BLUE;

    protected void showStatus(String text) {
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null) {
            log.error("SplashScreen.getSplashScreen() returned null");
            return;
        }
        Graphics2D g = (Graphics2D)splash.createGraphics();
        if (g == null) {
            log.error("g is null");
            return;
        }

        // clear existing area
        Rectangle splashBounds = splash.getBounds();
        g.setColor(color_background);
        g.fillRect(splash_border+0,splash_border+status_pos_y-40,splashBounds.width-2*splash_border,50);

        // draw string on splash screen
        g.setColor(Color.GRAY);
        g.setFont(new Font("ARIAL",Font.BOLD,14));
        g.drawString(text,splash_border+status_pos_x,splash_border+status_pos_y);

        splash.update();

    }

    /**
     * Update color_progress bar to show the color_progress (between 0 and 100) as provided as argument.
     *
     * @param progress
     */
    protected void showProgress(int progress) {
        final SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null) {
            log.error("SplashScreen.getSplashScreen() returned null");
            return;
        }
        Graphics2D g = (Graphics2D)splash.createGraphics();
        if (g == null) {
            log.error("g is null");
            return;
        }

        // clear existing area
        Rectangle splashBounds = splash.getBounds();
        g.setColor(color_progress_bg);
        g.fillRect(splash_border+0,splash_border+progress_bar_y,splashBounds.width-2*splash_border,progress_bar_height);

        // fill with new color_progress
        g.setColor(color_progress_fg);
        g.fillRect(splash_border+0,splash_border+progress_bar_y,(int)((splashBounds.width-2*splash_border) * progress / 100),progress_bar_height);

        splash.update();
    }

}
