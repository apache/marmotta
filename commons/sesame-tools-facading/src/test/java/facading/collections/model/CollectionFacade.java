package facading.collections.model;

import at.newmedialab.sesame.facading.annotations.RDF;
import at.newmedialab.sesame.facading.model.Facade;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface CollectionFacade extends Facade {

    @RDF("http://www.example.com/rdf/vocab/date")
    public List<Date> getDates();
    public void setDates(List<Date> dates);
    public void addDate(Date date);
    public void deleteDates();

    @RDF("http://www.example.com/rdf/vocab/autor")
    public void addAutor(String autor);
    public void setAutors(Collection<String> authors);

}
