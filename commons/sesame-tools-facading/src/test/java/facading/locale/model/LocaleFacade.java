package facading.locale.model;

import at.newmedialab.sesame.facading.annotations.RDF;
import at.newmedialab.sesame.facading.model.Facade;

import java.util.Locale;

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
