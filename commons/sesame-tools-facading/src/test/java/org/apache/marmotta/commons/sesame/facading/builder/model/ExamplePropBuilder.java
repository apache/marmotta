package org.apache.marmotta.commons.sesame.facading.builder.model;

import org.apache.marmotta.commons.sesame.facading.model.AbstractNamespacePropBuilder;

public class ExamplePropBuilder extends AbstractNamespacePropBuilder {

    @Override
    protected String getNamespace() {
        return ExampleFacade.NS;
    }

}
