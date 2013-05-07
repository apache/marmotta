package org.apache.marmotta.commons.sesame.facading.concurrent.model;

import org.apache.marmotta.commons.sesame.facading.annotations.RDF;
import org.apache.marmotta.commons.sesame.facading.model.Facade;
import org.openrdf.model.vocabulary.DCTERMS;

public interface FooFacade extends Facade {
    
    @RDF(DCTERMS.NAMESPACE + "string")
    public void setString(String string);
    public String getString();

}
