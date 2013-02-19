package facading.locale.model;

import at.newmedialab.sesame.commons.model.Namespaces;
import at.newmedialab.sesame.facading.annotations.RDF;
import at.newmedialab.sesame.facading.model.Facade;

import java.util.Locale;

public interface LocaleFacade extends Facade {

    @RDF(Namespaces.NS_RDFS + "label")
    public String getLabel();
    public String getLabel(Locale loc);
    public void setLabel(String label);
    public void setLabel(String label, Locale loc);
    public void deleteLabel();
    public void deleteLabel(Locale loc);

}
