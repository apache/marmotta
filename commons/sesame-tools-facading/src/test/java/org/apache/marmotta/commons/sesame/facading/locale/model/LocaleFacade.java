package org.apache.marmotta.commons.sesame.facading.locale.model;


import java.util.Locale;

import org.apache.marmotta.commons.sesame.facading.annotations.RDF;
import org.apache.marmotta.commons.sesame.facading.model.Facade;
import org.apache.marmotta.commons.sesame.model.Namespaces;

public interface LocaleFacade extends Facade {

    @RDF(Namespaces.NS_RDFS + "label")
    public String getLabel();
    public String getLabel(Locale loc);
    public void setLabel(String label);
    public void setLabel(String label, Locale loc);
    public void deleteLabel();
    public void deleteLabel(Locale loc);

}
