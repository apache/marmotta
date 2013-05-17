package org.apache.marmotta.commons.sesame.facading.primitive.model;

import java.util.Date;
import java.util.Locale;

import org.apache.marmotta.commons.sesame.facading.annotations.RDFPropertyBuilder;
import org.apache.marmotta.commons.sesame.facading.model.Facade;

@RDFPropertyBuilder(PropBuilder.class)
public interface Boxed extends Facade {

    public Integer getInteger();
    public void setInteger(Integer integer);
    public Long getLong();
    public void setLong(Long l);
    public Float getFloat();
    public void setFloat(Float f);
    public Double getDouble();
    public void setDouble(Double d);
    public Character getCharacter();
    public void setCharacter(Character character);
    public Byte getByte();
    public void setByte(Byte b);
    public Boolean getBoolean();
    public void setBoolean(Boolean b);
    public Locale getLocale();
    public void setLocale(Locale locale);
    public String getString();
    public void setString(String string);
    public Date getDate();
    public void setDate(Date date);
    
    
}
