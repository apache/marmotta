package org.apache.marmotta.commons.sesame.model;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class LiteralKey {

    private Object value;

    private String type;

    private String lang;

    public LiteralKey(Object value, String type, String lang) {
        this.value = value;
        this.type = type;
        this.lang = lang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LiteralKey that = (LiteralKey) o;

        if (lang != null ? !lang.equals(that.lang) : that.lang != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (lang != null ? lang.hashCode() : 0);
        return result;
    }
}
