package org.apache.marmotta.commons.sesame.facading.builder.model;


import java.util.Set;

import org.apache.marmotta.commons.sesame.facading.annotations.RDFPropertyBuilder;
import org.apache.marmotta.commons.sesame.facading.model.Facade;

@RDFPropertyBuilder(ExamplePropBuilder.class)
public interface ExampleFacade extends Facade {

    public static final String NS = "http://www.example.com/vocab/test#";

    void setTitle(String title);
    String getTitle();

    void setTags(Set<String> tags);
    void addTag(String tag);
    Set<String> getTags();

}
