//
// Created by wastl on 15.02.16.
//

#ifndef MARMOTTA_LDPATH_MODEL_H
#define MARMOTTA_LDPATH_MODEL_H

#include <vector>
#include <string>

#include "model/rdf_model.h"

namespace marmotta {
namespace ldpath {
namespace model {

class RDFBackend {
 public:
    virtual std::vector<rdf::Value> listObjects(const rdf::Value& subject, const rdf::Value& property) const = 0;

    virtual std::vector<rdf::Value> listSubjects(const rdf::Value& property, const rdf::Value& object) const = 0;
};


class NodeSelector {
 public:
    virtual std::vector<rdf::Value> select(const RDFBackend* backend, const rdf::Value& context) const = 0;

    virtual std::string getPathExpression() const = 0;
};

class PropertySelector : public NodeSelector {
 public:
    PropertySelector(const rdf::URI& property) : property(property) {}

    std::vector<rdf::Value> select(const RDFBackend* backend, const rdf::Value& context) const override;
    std::string getPathExpression() const override;

 private:
    rdf::URI property;
};
}
}
}

#endif //MARMOTTA_LDPATH_MODEL_H
