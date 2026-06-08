#pragma once
#include <boost/uuid/uuid.hpp>
#include <string>

class NodeMetaData {
public:
    using UUID = boost::uuids::uuid;

    enum class CameraType {
        StandardFixed,
        RotatingPTZ,
        Depth3DStereo
    };

private:
    UUID uuid;
    std::string name;
    CameraType type;

public: 
    NodeMetaData(UUID uuid, const std::string& name, CameraType type);

    UUID getUuid() const;
    std::string getName() const;
    CameraType getType() const;

    void setUuid(UUID newUuid);
    void setName(const std::string& newName);
    void setType(CameraType newType);
};