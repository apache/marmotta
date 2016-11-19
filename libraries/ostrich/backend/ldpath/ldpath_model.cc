//
// Created by wastl on 15.02.16.
//

#include "ldpath_model.h"

namespace marmotta {
namespace ldpath {
namespace model {

std::vector<rdf::Value> PropertySelector::select(
        const RDFBackend *backend, const rdf::Value &context) const {
    return backend->listObjects(context, property);
}

std::string PropertySelector::getPathExpression() const {
    std::string s = "<";
    s += property.getUri();
    s += ">";
    return s;
}

}
}
}

