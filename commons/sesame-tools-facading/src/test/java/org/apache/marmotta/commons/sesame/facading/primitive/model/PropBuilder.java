package org.apache.marmotta.commons.sesame.facading.primitive.model;

import org.apache.marmotta.commons.sesame.facading.model.AbstractNamespacePropBuilder;

public class PropBuilder extends AbstractNamespacePropBuilder {

    @Override
    protected String getNamespace() {
        return "http://persistence.text/prop#";
    }

}
