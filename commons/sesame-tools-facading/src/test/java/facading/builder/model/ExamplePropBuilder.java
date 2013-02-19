package facading.builder.model;

import at.newmedialab.sesame.facading.model.AbstractNamespacePropBuilder;

public class ExamplePropBuilder extends AbstractNamespacePropBuilder {

    @Override
    protected String getNamespace() {
        return ExampleFacade.NS;
    }

}
