/*
 * Copyright 2006 Nathanial X. Freitas, openvision.tv
 *
 * This code is currently released under the Mozilla Public License.
 * http://www.mozilla.org/MPL/
 *
 * Alternately you may apply the terms of the Apache Software License
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.rometools.feed.module.mediarss.io;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.rometools.feed.module.mediarss.MediaEntryModule;
import org.rometools.feed.module.mediarss.MediaModule;
import org.rometools.feed.module.mediarss.types.*;

import java.util.HashSet;
import java.util.Set;


//this class TBI
public class MediaModuleGenerator implements ModuleGenerator {
    private static final Namespace NS = Namespace.getNamespace("media",
            MediaModule.URI);
    private static final Set NAMESPACES = new HashSet();

    static {
        NAMESPACES.add(NS);
    }

    public String getNamespaceUri() {
        return MediaModule.URI;
    }

    public Set getNamespaces() {
        return NAMESPACES;
    }

    public void generate(Module module, Element element) {
        if (module instanceof MediaModule) {
            MediaModule m = (MediaModule) module;
            this.generateMetadata(m.getMetadata(), element);
            this.generatePlayer(m.getPlayer(), element);
        }

        if (module instanceof MediaEntryModule) {
            MediaEntryModule m = (MediaEntryModule) module;
            MediaGroup[] g = m.getMediaGroups();

            for (MediaGroup aG : g) {
                this.generateGroup(aG, element);
            }

            MediaContent[] c = m.getMediaContents();

            for (MediaContent aC : c) {
                this.generateContent(aC, element);
            }
        }
    }

    public void generateContent(MediaContent c, Element e) {
        Element mc = new Element("content", NS);
        this.addNotNullAttribute(mc, "medium", c.getMedium());
        this.addNotNullAttribute(mc, "channels", c.getAudioChannels());
        this.addNotNullAttribute(mc, "bitrate", c.getBitrate());
        this.addNotNullAttribute(mc, "duration", c.getDuration());
        this.addNotNullAttribute(mc, "expression", c.getExpression());
        this.addNotNullAttribute(mc, "fileSize", c.getFileSize());
        this.addNotNullAttribute(mc, "framerate", c.getFramerate());
        this.addNotNullAttribute(mc, "height", c.getHeight());
        this.addNotNullAttribute(mc, "lang", c.getLanguage());
        this.addNotNullAttribute(mc, "samplingrate", c.getSamplingrate());
        this.addNotNullAttribute(mc, "type", c.getType());
        this.addNotNullAttribute(mc, "width", c.getWidth());

        if (c.isDefaultContent()) {
            this.addNotNullAttribute(mc, "isDefault", "true");
        }

        if (c.getReference() instanceof UrlReference) {
            this.addNotNullAttribute(mc, "url", c.getReference());
            this.generatePlayer(c.getPlayer(), mc);
        } else {
            this.generatePlayer(c.getPlayer(), mc);
        }

        this.generateMetadata(c.getMetadata(), mc);
        e.addContent(mc);
    }

    public void generateGroup(MediaGroup g, Element e) {
        Element t = new Element("group", NS);
        MediaContent[] c = g.getContents();

        for (MediaContent aC : c) {
            this.generateContent(aC, t);
        }

        this.generateMetadata(g.getMetadata(), t);
        e.addContent(t);
    }

    public void generateMetadata(Metadata m, Element e) {
        if (m == null) {
            return;
        }

        Category[] cats = m.getCategories();

        for (Category cat : cats) {
            Element c = generateSimpleElement("category", cat.getValue());
            this.addNotNullAttribute(c, "scheme", cat.getScheme());
            this.addNotNullAttribute(c, "label", cat.getLabel());
            e.addContent(c);
        }

        Element copyright = addNotNullElement(e, "copyright", m.getCopyright());
        this.addNotNullAttribute(copyright, "url", m.getCopyrightUrl());

        Credit[] creds = m.getCredits();

        for (Credit cred : creds) {
            Element c = generateSimpleElement("credit", cred.getName());
            this.addNotNullAttribute(c, "role", cred.getRole());
            this.addNotNullAttribute(c, "scheme", cred.getScheme());
            e.addContent(c);
        }

        Element desc = addNotNullElement(e, "description", m.getDescription());
        this.addNotNullAttribute(desc, "type", m.getDescriptionType());

        if (m.getHash() != null) {
            Element hash = this.addNotNullElement(e, "hash",
                    m.getHash().getValue());
            this.addNotNullAttribute(hash, "algo", m.getHash().getAlgorithm());
        }

        String[] keywords = m.getKeywords();

        if (keywords.length > 0) {
            String keyword = keywords[0];

            for (int i = 1; i < keywords.length; i++) {
                keyword += (", " + keywords[i]);
            }

            this.addNotNullElement(e, "keywords", keyword);
        }

        Rating[] rats = m.getRatings();

        for (Rating rat1 : rats) {
            Element rat = this.addNotNullElement(e, "rating", rat1.getValue());
            this.addNotNullAttribute(rat, "scheme", rat1.getScheme());

            if (rat1.equals(Rating.ADULT)) {
                this.addNotNullElement(e, "adult", "true");
            } else if (rat1.equals(Rating.NONADULT)) {
                this.addNotNullElement(e, "adult", "false");
            }
        }

        Text[] text = m.getText();

        for (Text aText : text) {
            Element t = this.addNotNullElement(e, "text", aText.getValue());
            this.addNotNullAttribute(t, "type", aText.getType());
            this.addNotNullAttribute(t, "start", aText.getStart());
            this.addNotNullAttribute(t, "end", aText.getEnd());
        }

        Thumbnail[] thumbs = m.getThumbnail();

        for (Thumbnail thumb : thumbs) {
            Element t = new Element("thumbnail", NS);
            this.addNotNullAttribute(t, "url", thumb.getUrl());
            this.addNotNullAttribute(t, "width", thumb.getWidth());
            this.addNotNullAttribute(t, "height", thumb.getHeight());
            this.addNotNullAttribute(t, "time", thumb.getTime());
            e.addContent(t);
        }

        Element title = this.addNotNullElement(e, "title", m.getTitle());
        this.addNotNullAttribute(title, "type", m.getTitleType());

        Restriction[] r = m.getRestrictions();

        for (Restriction aR : r) {
            Element res = this.addNotNullElement(e, "restriction",
                    aR.getValue());
            this.addNotNullAttribute(res, "type", aR.getType());
            this.addNotNullAttribute(res, "relationship", aR.getRelationship());
        }
    }

    public void generatePlayer(PlayerReference p, Element e) {
        if (p == null) {
            return;
        }

        Element t = new Element("player", NS);
        this.addNotNullAttribute(t, "url", p.getUrl());
        this.addNotNullAttribute(t, "width", p.getWidth());
        this.addNotNullAttribute(t, "height", p.getHeight());
        e.addContent(t);
    }

    protected void addNotNullAttribute(Element target, String name, Object value) {
        if (target != null && value != null) {
            target.setAttribute(name, value.toString());
        }
    }

    protected Element addNotNullElement(Element target, String name,
        Object value) {
        if (value == null) {
            return null;
        }

        Element e = generateSimpleElement(name, value.toString());
        target.addContent(e);

        return e;
    }

    protected Element generateSimpleElement(String name, String value) {
        Element element = new Element(name, NS);
        element.addContent(value);

        return element;
    }
}
