package org.apache.marmotta.commons.sesame.facading.concurrent.model;

import org.apache.marmotta.commons.sesame.facading.annotations.RDF;
import org.apache.marmotta.commons.sesame.facading.annotations.RDFType;
import org.apache.marmotta.commons.sesame.facading.model.Facade;

@RDFType(TypeFacade.TYPE)
public interface TypeFacade extends Facade {

    public static final String TITLE = "http://foo.bar/title";
    public static final String TYPE = "http://foo.bar/Type";

    @RDF(TITLE)
    String getTitle();
    void setTitle(String title);

}
