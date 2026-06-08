/**
 * @file NodeMetaData.h
 * @brief Identity and hardware classification for a simulated sensor node.
 *
 * Each simulator instance represents one physical node. On construction,
 * a random UUID is generated, a camera type is chosen, and a human-readable
 * name is derived (e.g. "ROTATING_PTZ-4821"). This metadata is immutable
 * for the lifetime of a simulation run.
 */

#pragma once

#include <boost/uuid/uuid.hpp>
#include <string>

namespace avisos::model {

class NodeMetaData {
public:
    using UUID = boost::uuids::uuid;

    /// Sensor hardware classification, maps to the node's NODE_TYPE in Java.
    enum class CameraType {
        StandardFixed,   ///< Fixed 2D camera
        RotatingPTZ,     ///< Pan-tilt-zoom camera
        Depth3DStereo    ///< Stereo depth sensor
    };

private:
    UUID uuid_;
    std::string name_;
    CameraType type_;

public:
    /** Constructs with explicit name and type; UUID is auto-generated. */
    NodeMetaData(const std::string& name, CameraType type);

    /** Default: random camera type, auto-generated name (e.g. "ROTATING_PTZ-7103"). */
    NodeMetaData();

    /** Returns a uniformly random CameraType. */
    CameraType choose_random_camera_type();

    /** Returns the camera type as an uppercase string (e.g. "STANDARD_FIXED_2D"). */
    [[nodiscard]] std::string get_type_as_string() const;

    // -- accessors --

    [[nodiscard]] UUID uuid() const;
    [[nodiscard]] std::string name() const;
    [[nodiscard]] CameraType type() const;

    void set_uuid(UUID value);
    void set_name(const std::string& value);
    void set_type(CameraType value);
};

} // namespace avisos::model