# Avisos Staff Jurisdictions

This file describes the datacenter staff model used by the Avisos.

## Facility Coverage Model

Avisos divides staff responsibility by physical zone, operational discipline, and escalation level. A single alarm can
involve more than one team when a node sits near a boundary between security, facilities, and network infrastructure.

The controller should treat staff jurisdiction as guidance for routing and natural-language response generation. Final
dispatch decisions still depend on alarm severity, active shift, and escalation policy.

## Staff Directory

| Staff ID       | Name           | Email                         | Phone       | Role                    | Primary Jurisdiction                     | Datacenter Coverage                                        | Shift |
|----------------|----------------|-------------------------------|-------------|-------------------------|------------------------------------------|------------------------------------------------------------|-------|
| staff-ops-001  | Jawad Azeem    | jawad.azeem@outlook.com       | +1-555-0001 | Operations Supervisor   | All operations and facility oversight    | All zones                                                  | Day   |
| staff-sec-001  | Maya Chen      | maya.chen@avisos.example      | +1-555-0101 | Security Shift Lead     | Physical security and access control     | Lobby, exterior fence, mantrap, loading corridor           | Day   |
| staff-sec-002  | Omar Patel     | omar.patel@avisos.example     | +1-555-0102 | Security Responder      | Floor security patrol                    | Rack halls A and B, service floor, emergency exits         | Swing |
| staff-sec-003  | Lena Brooks    | lena.brooks@avisos.example    | +1-555-0103 | Night Security Lead     | After-hours security response            | All zones, priority on exterior and electrical rooms       | Night |
| staff-fac-001  | Andre Wallace  | andre.wallace@avisos.example  | +1-555-0111 | Facilities Lead         | Mechanical, cooling, and water ingress   | Mechanical room B, service floor, chilled water corridors  | Day   |
| staff-fac-002  | Priya Nair     | priya.nair@avisos.example     | +1-555-0112 | Facilities Technician   | Environmental sensor response            | Rack halls A and B, HVAC return aisles, underfloor sensors | Swing |
| staff-net-001  | Sofia Martinez | sofia.martinez@avisos.example | +1-555-0121 | Network Operations Lead | Network rooms and patching areas         | Network room G, patch panels, transit corridor D           | Day   |
| staff-net-002  | Ethan Reed     | ethan.reed@avisos.example     | +1-555-0122 | Network Technician      | Node communications and telemetry triage | Network room G, rack rows A1-A6, controller uplink         | Night |
| staff-elec-001 | Hannah Kim     | hannah.kim@avisos.example     | +1-555-0131 | Electrical Safety Lead  | Electrical rooms and power events        | Electrical room E, UPS area, generator transfer corridor   | Day   |

## Jurisdiction Notes

### Operations Oversight

The operations supervisor holds facility-wide jurisdiction. All critical alarms (Sev 1) escalate to the operations
supervisor after the primary zone owner acknowledges. The operations supervisor may override routing decisions and
reassign incidents across teams.

### Rack Halls A and B

Rack hall alarms usually route first to the on-shift facilities technician when the alarm involves temperature,
humidity, water, or battery drift. Security joins when camera labels indicate unauthorized presence, forced access,
obstruction, or unusual movement.

Network operations joins rack hall incidents when the affected node reports repeated telemetry failures, poor signal
quality, or controller heartbeat loss.

### Service Floor and Mechanical Room B

Water ingress, humidity spike, pressure anomaly, or cooling drift near the service floor belongs to facilities. Security
should be notified when the event coincides with door access anomalies, camera obstruction, or an unauthorized person
label.

### Network Room G and Transit Corridor D

Network room events belong to network operations first. Security becomes a secondary responder for access anomalies.
Facilities becomes secondary for environmental conditions that threaten network hardware.

Transit corridor D is a shared jurisdiction because it connects network, electrical, and service-floor paths.

### Electrical Room E

Electrical room alarms should route to the electrical safety lead first. Security should be notified for unauthorized
presence or open-panel camera labels. Facilities should be included for water, humidity, or smoke-adjacent environmental
alarms.

### Lobby, Exterior Fence, and Mantrap

Physical security owns these areas. Facilities should only be included if environmental sensors indicate water, smoke,
obstruction, or infrastructure damage.

## Demo Routing Guidance

- Low battery on deterministic demo nodes: notify the staff member responsible for the node zone and include network
  operations if telemetry is degraded.
- Water ingress in service floor or mechanical zones: notify facilities immediately and copy security if a camera alarm
  also exists.
- Unauthorized person or intrusion labels: notify security first, then include the zone owner if equipment may be
  affected.
- Electrical panel, smoke, or power-room labels: notify electrical safety first and copy security.
- Camera obstruction: notify security for access-sensitive zones; notify network operations if the camera is in network
  room G.
