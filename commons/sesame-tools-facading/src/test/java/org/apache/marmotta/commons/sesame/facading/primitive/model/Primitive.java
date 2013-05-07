package org.apache.marmotta.commons.sesame.facading.primitive.model;

import org.apache.marmotta.commons.sesame.facading.annotations.RDFPropertyBuilder;
import org.apache.marmotta.commons.sesame.facading.model.Facade;

@RDFPropertyBuilder(PropBuilder.class)
public interface Primitive extends Facade {

    public int getInt();
    public void setInt(int i);
    
    public float getFloat();
    public void setFloat(float f);
    
    public double getDouble();
    public void setDouble(double d);
    
    public long getLong();
    public void setLong(long l);
    
    public boolean getBoolean();
    public void setBoolean(boolean b);
    
    public char getChar();
    public void setChar(char c);
    
    public byte getByte();
    public void setByte(byte b);
    
}
