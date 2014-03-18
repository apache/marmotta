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
package org.apache.marmotta.maven.plugins.marmotta;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Maven Plugin to print the version number on the splash screen.
 * @author Jakob Frank <jakob@apache.org>
 *
 */
@Mojo(name="createSplash")
public class SplashScreenBuilderMojo extends AbstractMojo {

    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile(".*-SNAPSHOT$", Pattern.CASE_INSENSITIVE);

    @Parameter(property="createSplash.input", required=true)
    private File input;

    @Parameter(property="createSplash.output", required=true)
    private File output;

    @Parameter(property="createSplash.versionString", required=true)
    private String versionString;

    @Parameter(property="createSplash.xPos", defaultValue="467")
    private int xPos;

    @Parameter(property="createSplash.yPos", defaultValue="333")
    private int yPos;

    @Parameter(property="createSplash.vAlign", defaultValue="base")
    private String vAlign;

    @Parameter(property="createSplash.hAlign", defaultValue="right")
    private String hAlign;

    @Parameter(property="createSplash.color", defaultValue="#1d1d1b")
    private String color;

    @Parameter(property="createSplash.snapshotColor")
    private String snapshotColor;

    @Parameter(property="createSplash.font", defaultValue="Arial bold 18")
    private String font;

    public SplashScreenBuilderMojo() {
        super();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // load the template image
        BufferedImage splash;
        try {
            splash = ImageIO.read(input);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not read input file", e);
        }

        final Graphics2D g = (Graphics2D) splash.getGraphics();

        if (font != null) {
            // set the font, this will use the default as fallback.
            g.setFont(Font.decode(font));
        }
        // for alignment, we need to do some calculations
        final FontMetrics fm = g.getFontMetrics();

        // if the position is not set, use the center of the template
        if (xPos < 0) {
            xPos = splash.getWidth()/2;
        }
        if (yPos < 0) {
            yPos = splash.getHeight()/2;
        }

        // adjust print-position based on the alignment (vertical)
        if ("top".equalsIgnoreCase(vAlign)) {
            yPos = yPos + fm.getAscent();
        } else if ("middle".equalsIgnoreCase(vAlign)) {
            yPos = yPos + (fm.getAscent()/2);
        } else if ("bottom".equalsIgnoreCase(vAlign)) {
            yPos = yPos - fm.getDescent(); 
        } else if ("base".equalsIgnoreCase(vAlign)) {
            // nop;
            // yPos = yPos;
        } else {
            getLog().warn(String.format("invalid param value for \"vAlign\": \"%s\", using fallback \"base\"", vAlign));
            // nop;
            // yPos = yPos;
        }

        // adjust print-position based on the alignment (horizontal)
        final int w = fm.stringWidth(versionString);
        if ("left".equalsIgnoreCase(hAlign)) {
            // nop;
            // xPos = xPos;
        } else if ("right".equalsIgnoreCase(hAlign)) {
            xPos = xPos - w;
        } else if ("center".equalsIgnoreCase(hAlign)) {
            xPos = xPos - (w/2);
        } else {
            getLog().warn(String.format("invalid param value for \"hAlign\": \"%s\", using fallback \"center\"", hAlign));
            // nop;
            xPos = xPos - (w/2);
        }

        // parse the color to print in
        Color c = getColor(color, Color.BLACK);
        // if the version string ends with "-SNAPSHOT", we might want to use a different color
        if (SNAPSHOT_PATTERN.matcher(versionString).matches()) {
            c = getColor(snapshotColor, c);
        }
        g.setColor(c);

        // we want pretty printing...
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // this is where the version is printed!
        g.drawString(versionString, xPos, yPos);

        // save the resulting splash image.
        try {
            final String fName = output.getName();
            ImageIO.write(splash, fName.substring(fName.lastIndexOf('.') + 1), output);
        } catch (IOException e) {
            throw new MojoExecutionException("Could not write output file", e);
        }
    }

    private Color getColor(String color, Color fallback) {
        if (color != null) {
            if (color.startsWith("#") && color.length() == 7) {
                return new Color(Integer.parseInt(color.substring(1), 16));
            } else if (color.length() == 6) {
                getLog().warn("color should be provided in html notation: '#rrggbb'");
                return new Color(Integer.parseInt(color, 16));
            } else {
                getLog().warn("invalid color definition: '" + color + "'. Using fallback: black");
                return fallback;
            }
        } else {
            return fallback;
        }
    }

}
