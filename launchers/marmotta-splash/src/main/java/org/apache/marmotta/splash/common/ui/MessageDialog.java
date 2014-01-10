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
package org.apache.marmotta.splash.common.ui;

import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.WordUtils;

public class MessageDialog {
    
    public static final String MARMOTTA_ICON = "/org/apache/marmotta/splash/systray/systray.png";
    
    private MessageDialog() {
        // static access only!
    }

    public static void info(String message) {
        show("Info", message, "");
    }
    
    public static void info(String title, String message) {
        show(title, message, "");
    }
    
    public static void show(String title, String message, String description) {
        final JDialog dialog = new JDialog((Frame)null, title);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        
        final JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.getRootPane().setContentPane(root);

        final JButton close = new JButton("OK");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        GridBagConstraints cClose = new GridBagConstraints();
        cClose.gridx = 0;
        cClose.gridy = 2;
        cClose.gridwidth = 2;
        cClose.weightx = 1;
        cClose.weighty = 0;
        cClose.insets = new Insets(5, 5, 5, 5);
        
        root.add(close, cClose);
        dialog.getRootPane().setDefaultButton(close);
        
        Icon icon = loadIcon(MARMOTTA_ICON) ;
        if (icon != null) {
            JLabel lblIcn = new JLabel(icon);

            GridBagConstraints cIcon = new GridBagConstraints();
            cIcon.gridx = 1;
            cIcon.gridy = 0;
            cIcon.gridheight = 2;
            cIcon.fill = GridBagConstraints.NONE;
            cIcon.weightx = 0;
            cIcon.weighty = 1;
            cIcon.anchor = GridBagConstraints.NORTH;
            cIcon.insets = new Insets(10, 5, 5, 0);
            root.add(lblIcn, cIcon);
        }
        
        JLabel lblMsg = new JLabel("<html>"+StringEscapeUtils.escapeHtml3(message).replaceAll("\\n", "<br>"));
        lblMsg.setFont(lblMsg.getFont().deriveFont(Font.BOLD, 16f));
        GridBagConstraints cLabel = new GridBagConstraints();
        cLabel.gridx = 0;
        cLabel.gridy = 0;
        cLabel.fill = GridBagConstraints.BOTH;
        cLabel.weightx = 1;
        cLabel.weighty = 0.5;
        cLabel.insets = new Insets(5, 5, 5, 5);
        root.add(lblMsg, cLabel);
        
        JLabel lblDescr = new JLabel("<html>"+StringEscapeUtils.escapeHtml3(description).replaceAll("\\n", "<br>"));
        cLabel.gridy++;
        cLabel.insets = new Insets(0, 5, 5, 5);
        root.add(lblDescr, cLabel);
        
        
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        
        dialog.setVisible(true);
        dialog.dispose();
    }
    
    static Icon loadIcon() {
        return loadIcon(MARMOTTA_ICON);
    }

    static Icon loadIcon(String icon) {
        if (icon == null) return null;
        final URL rsc = MessageDialog.class.getResource(icon);
        if (rsc != null) {
            return new ImageIcon(rsc);
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        show("Hello", "This is a test", 
                WordUtils.wrap("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.", 55));
    }
}
