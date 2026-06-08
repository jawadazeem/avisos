/**
 * @file NodeMetaData.cpp
 * @brief Construction and identity generation for simulated sensor nodes.
 */

#include "NodeMetaData.h"

#include <boost/uuid/uuid_generators.hpp>
#include <random>

namespace avisos::model {

NodeMetaData::NodeMetaData(const std::string& name, CameraType type)
    : uuid_(boost::uuids::random_generator()()),
      name_(name),
      type_(type) {}

// -- default --
NodeMetaData::NodeMetaData()
    : uuid_(boost::uuids::random_generator()()),
      type_(choose_random_camera_type()) {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<int> distr(1000, 9999);
    name_ = get_type_as_string() + "-" + std::to_string(distr(gen));
}

// -- accessors --

NodeMetaData::UUID NodeMetaData::uuid() const { return uuid_; }
std::string NodeMetaData::name() const { return name_; }
NodeMetaData::CameraType NodeMetaData::type() const { return type_; }

std::string NodeMetaData::get_type_as_string() const {
    switch (type_) {
        case CameraType::StandardFixed: return "STANDARD_FIXED_2D";
        case CameraType::RotatingPTZ:   return "ROTATING_PTZ";
        case CameraType::Depth3DStereo: return "DEPTH_3D_STEREO";
        default:                        return "UNKNOWN_HARDWARE_TYPE";
    }
}

// -- mutators --

void NodeMetaData::set_uuid(UUID value) { uuid_ = value; }
void NodeMetaData::set_name(const std::string& value) { name_ = value; }
void NodeMetaData::set_type(CameraType value) { type_ = value; }

// -- utility --

NodeMetaData::CameraType NodeMetaData::choose_random_camera_type() {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<int> distr(0, 2);

    switch (distr(gen)) {
        case 0:  return CameraType::StandardFixed;
        case 1:  return CameraType::RotatingPTZ;
        default: return CameraType::Depth3DStereo;
    }
}

} // namespace avisos::model
