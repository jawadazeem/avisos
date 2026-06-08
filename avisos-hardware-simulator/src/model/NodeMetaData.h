#pragma once
#include <boost/uuid/uuid.hpp>
#include <string>

namespace avisos::model {

class NodeMetaData {
public:
    using UUID = boost::uuids::uuid;

    enum class CameraType {
        StandardFixed,
        RotatingPTZ,
        Depth3DStereo
    };

private:
    UUID uuid_;
    std::string name_;
    CameraType type_;

public:
    NodeMetaData(const std::string& name, CameraType type);
    
    NodeMetaData();

    CameraType choose_random_camera_type();

    // -- accessors --

    [[nodiscard]] UUID uuid() const;
    [[nodiscard]] std::string name() const;
    [[nodiscard]] CameraType type() const;

    void set_uuid(UUID value);
    void set_name(const std::string& value);
    void set_type(CameraType value);
};

} // namespace avisos::model