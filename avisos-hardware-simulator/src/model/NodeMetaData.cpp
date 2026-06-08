#include "NodeMetaData.h"
#include <boost/uuid/uuid_generators.hpp>

NodeMetaData::NodeMetaData(const std::string& name, CameraType type)
    : uuid(boost::uuids::random_generator()()),
      name(name), 
      type(type) {
}

NodeMetaData::UUID NodeMetaData::getUuid() const {
    return uuid;
}

std::string NodeMetaData::getName() const {
    return name;
}

NodeMetaData::CameraType NodeMetaData::getType() const {
    return type;
}

std::string NodeMetaData::getTypeAsString() const {
    switch (type) {
        case CameraType::StandardFixed: return "STANDARD_FIXED_2D";
        case CameraType::RotatingPTZ:   return "ROTATING_PTZ";
        case CameraType::Depth3DStereo: return "DEPTH_3D_STEREO";
        default:                        return "UNKNOWN_HARDWARE_TYPE";
    }
}

// Setters
void NodeMetaData::setName(const std::string& newName) {
    name = newName;
}

void NodeMetaData::setType(CameraType newType) {
    type = newType;
}