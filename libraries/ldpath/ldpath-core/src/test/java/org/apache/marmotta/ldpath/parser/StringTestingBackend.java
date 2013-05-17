package org.apache.marmotta.ldpath.parser;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.marmotta.ldpath.model.backend.AbstractBackend;

public class StringTestingBackend extends AbstractBackend<String> {

    private static final Pattern LANG_PATTERN = Pattern.compile("@(\\w+)"),
            TYPE_PATTERN = Pattern.compile("\\^\\^([\\w:/.#%-]+)");
    
    @Override
    public boolean isLiteral(String n) {
        return n.startsWith("\"");
    }

    @Override
    public boolean isURI(String n) {
        return n.startsWith("<");
    }

    @Override
    public boolean isBlank(String n) {
        return n.startsWith("_");
    }

    @Override
    public Locale getLiteralLanguage(String n) {
        final Matcher m = LANG_PATTERN.matcher(n);
        if (m.find()) {
            return new Locale(m.group(1));
        }
        return null;
    }

    @Override
    public URI getLiteralType(String n) {
        final Matcher m = TYPE_PATTERN.matcher(n);
        if (m.find()) {
            return URI.create(m.group(1));
        }
        return null;
    }

    @Override
    public String createLiteral(String content) {
        return "\""+content+"\"";
    }

    @Override
    public String createLiteral(String content, Locale language, URI type) {
        StringBuilder sb = new StringBuilder('"');
        sb.append(content).append('"');
        if (language != null) {
            sb.append("@").append(language.getLanguage());
        }
        if (type != null) {
            sb.append("^^").append(type.toString());
        }
        return sb.toString();
    }

    @Override
    public String createURI(String uri) {
        return "<" + uri + ">";
    }

    @Override
    public String stringValue(String node) {
        if (node.startsWith("<")) {
            return node.substring(1, node.length()-1);
        } else if (node.startsWith("\"")) {
            return node.substring(1, node.indexOf('"', 1));
        } else
            return node;
    }

}
