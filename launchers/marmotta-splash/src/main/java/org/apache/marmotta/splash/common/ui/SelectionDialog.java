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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public class SelectionDialog {

    
    public static int select(String title, String message, String description, List<Option> options, int defaultOption) {
        final JDialog dialog = new JDialog((Frame)null, title);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        
        final AtomicInteger result = new AtomicInteger(Math.max(defaultOption,-1));
        
        JButton defaultBtn = null;
        
        final JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.getRootPane().setContentPane(root);

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

        // All the options
        cLabel.ipadx = 10;
        cLabel.ipady = 10;
        cLabel.insets = new Insets(5, 15, 0, 15);
        for (int i = 0; i < options.size(); i++) {
            cLabel.gridy++;
            
            final Option o = options.get(i);
            final JButton btn = new JButton("<html>"+StringEscapeUtils.escapeHtml3(o.label).replaceAll("\\n", "<br>"), MessageDialog.loadIcon(o.icon));
            if (StringUtils.isNotBlank(o.info)) {
                btn.setToolTipText("<html>"+StringEscapeUtils.escapeHtml3(o.info).replaceAll("\\n", "<br>"));
            }
            
            btn.setHorizontalAlignment(AbstractButton.LEADING);
            btn.setVerticalTextPosition(AbstractButton.CENTER);
            btn.setHorizontalTextPosition(AbstractButton.TRAILING);
            
            final int myAnswer = i;
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    result.set(myAnswer);
                    dialog.setVisible(false);
                }
            });
            
            root.add(btn, cLabel);
            if (i == defaultOption) {
                dialog.getRootPane().setDefaultButton(btn);
                defaultBtn = btn;
            }
        }
        
        final Icon icon = MessageDialog.loadIcon() ;
        if (icon != null) {
            JLabel lblIcn = new JLabel(icon);

            GridBagConstraints cIcon = new GridBagConstraints();
            cIcon.gridx = 1;
            cIcon.gridy = 0;
            cIcon.gridheight = 2 + options.size();
            cIcon.fill = GridBagConstraints.NONE;
            cIcon.weightx = 0;
            cIcon.weighty = 1;
            cIcon.anchor = GridBagConstraints.NORTH;
            cIcon.insets = new Insets(10, 5, 5, 0);
            root.add(lblIcn, cIcon);
        }
        
        final JButton close = new JButton("Cancel");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result.set(-1);
                dialog.setVisible(false);
            }
        });
        GridBagConstraints cClose = new GridBagConstraints();
        cClose.gridx = 0;
        cClose.gridy = 2 + options.size();
        cClose.gridwidth = 2;
        cClose.weightx = 1;
        cClose.weighty = 0;
        cClose.insets = new Insets(15, 5, 5, 5);
        
        root.add(close, cClose);
        if (defaultOption < 0) {
            dialog.getRootPane().setDefaultButton(close);
            defaultBtn = close;
        }
        

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        defaultBtn.requestFocusInWindow();
        
        dialog.setVisible(true);
        dialog.dispose();

        return result.get();
    }
    
    public static class Option {
        private String label, info;
        private String icon;
        
        public Option(String label) {
            this(label, "", null);
        }
        
        public Option(String label, String info) {
            this(label, info, null);
        }

        public Option(String label, String info, String icon) {
            this.label = label;
            this.info = info;
            this.icon = icon;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
    
    public static void main(String[] args) {
        final List<Option> options = Arrays.asList(
                new Option("Option1\nfoo"),
                new Option("Option2", "this is \nalso valid"), 
                new Option("Option3", "", MessageDialog.MARMOTTA_ICON));
        final int choice = select("Question", "Please select", "what do you prefer?", options, 1);
        if (choice < 0) {
            System.out.println("No coice was made!");
        } else {
            System.out.printf("Your choice was %d%n%s (%s)", choice, options.get(choice).label, options.get(choice).info);
        }
    }
}
