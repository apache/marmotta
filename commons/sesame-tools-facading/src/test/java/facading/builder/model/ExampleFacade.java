package facading.builder.model;

import at.newmedialab.sesame.facading.annotations.RDFPropertyBuilder;
import at.newmedialab.sesame.facading.model.Facade;

import java.util.Set;

@RDFPropertyBuilder(ExamplePropBuilder.class)
public interface ExampleFacade extends Facade {

    public static final String NS = "http://www.example.com/vocab/test#";

    void setTitle(String title);
    String getTitle();

    void setTags(Set<String> tags);
    void addTag(String tag);
    Set<String> getTags();

}
