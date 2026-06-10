# Datacenter Facility Layout — Meridian Ridge Facility

## Facility Overview

The Meridian Ridge facility is a single-story, climate-controlled operations building serving as the primary monitored environment for the Avisos platform. The facility houses network and compute infrastructure across three primary equipment zones, supported by a dedicated cooling plant, an electrical distribution room, a cable transit corridor, and a staffed control room. Total conditioned floor space is approximately 4,200 square feet.

All zones are monitored by Avisos sensor nodes publishing telemetry via MQTT to the avisos-controller-service. Node placement was determined during the initial site survey and is documented in the Node Catalog. Physical access to all equipment zones requires a proximity badge. The control room is staffed during business hours; after-hours response relies on the on-call rotation defined in the Escalation Procedures document.

The facility operates under a cold-aisle/hot-aisle containment model in all three equipment zones. Cooling is delivered by two in-row computer room air handlers (CRAHs) in Zone Beta and supplemented by perimeter units in Zones Alpha and Gamma. The electrical distribution room feeds all zones from a single main panel with two upstream UPS units.

---

## Zone Definitions

### Zone Alpha — Primary Compute Racks

**Room designation:** A-FLOOR
**Location in facility:** North wing, interior
**Rack rows:** Rows A1 through A6 (24 two-post and four-post racks)

Zone Alpha contains the highest-density compute and storage equipment in the facility. Rows A1–A3 are cold-aisle-contained with blanking panels installed on all unused rack units. Rows A4–A6 are older open-frame racks with less consistent airflow management.

**Nodes deployed:**
- One environmental node per rack row (6 nodes total), mounted at mid-rack height on the cold-aisle face
- Two overhead leak detection nodes mounted above the raised subfloor access panels at the north and south ends of the room
- One ambient node mounted near the room's return-air grille

**Environmental risks:**
- Hot-aisle exhaust recirculation in rows A4–A6 due to absent containment curtains
- Subfloor water ingress risk at the north end; the building's exterior wall in that area has a history of minor water intrusion during heavy rainfall
- Battery backup for nodes in this zone runs from a secondary power rail that historically shows voltage sag during peak load

**Most serious alarms in this zone:**
- `LEAK_DETECTED` — Subfloor water near active compute racks is a critical condition requiring immediate response
- `TEMPERATURE_HIGH` — Elevated exhaust temperatures in rows A4–A6 can indicate cooling unit underperformance or blocked airflow
- `BATTERY_LOW` / `BATTERY_CRITICAL` — Voltage sag on the secondary rail may trigger false battery alarms; these must be correlated with PDU load data before assuming node hardware failure

**Zone relationships:**
Zone Alpha shares a return-air plenum with Zone Beta. A cooling failure in Zone Beta will manifest as rising temperatures in Zone Alpha within approximately 15–20 minutes. The cable transit corridor (Zone Delta) runs along the south wall of Zone Alpha and is the primary path for all power and network cabling entering this room.

---

### Zone Beta — Cooling Plant and In-Row Units

**Room designation:** B-MECH
**Location in facility:** Central, adjacent to Zone Alpha south wall
**Equipment:** Two Stalwart CR-9 in-row CRAH units, two perimeter supplemental units, primary condensate drain manifold

Zone Beta is not a rack equipment room. It is the mechanical heart of the facility's cooling infrastructure. The two primary CRAHs serve the shared plenum for Zones Alpha and Gamma. Condensate drain lines from all four cooling units terminate at the central drain manifold in this room before routing to the building's drain system.

**Nodes deployed:**
- One environmental node per CRAH unit (2 nodes), mounted on the equipment intake face
- One leak detection node positioned at the base of the condensate drain manifold
- One ambient node near the room's exterior exhaust louver

**Environmental risks:**
- Condensate overflow is the primary leak risk; the drain manifold is the most likely failure point
- The exterior louver introduces outdoor air pressure variation that can affect the ambient pressure sensor readings, particularly during storm fronts
- Humidity levels in this room run higher than other zones by design; baseline readings are intentionally elevated compared to equipment zones

**Most serious alarms in this zone:**
- `LEAK_DETECTED` — Condensate overflow or drain line blockage; a leak here can migrate to Zone Alpha via the shared subfloor
- `HUMIDITY_HIGH` — May indicate a failing CRAH unit or a drain line backing up before a visible leak occurs
- `PRESSURE_ANOMALY` — Unusual pressure readings may indicate louver obstruction or a duct breach; correlate with outdoor weather conditions before escalating

**Zone relationships:**
Zone Beta is the thermal dependency for both Zone Alpha and Zone Gamma. Any cooling anomaly detected here should be treated as a potential precursor to temperature alarms in both equipment zones. The Facility Lead should be notified any time a Zone Beta alarm is active for more than ten minutes without a clear cause.

---

### Zone Gamma — Network and Edge Infrastructure

**Room designation:** G-NET
**Location in facility:** South wing, interior
**Rack rows:** Rows G1 through G3 (12 racks, mixed two-post and wall-mount)

Zone Gamma houses network switching, firewall appliances, patch infrastructure, and edge compute nodes. Equipment density is lower than Zone Alpha, but this zone is operationally critical because it carries all inter-zone and external traffic. A loss of cooling in Zone Gamma affects the entire facility's connectivity, including the MQTT broker and the avisos-controller-service host.

**Nodes deployed:**
- One environmental node per rack row (3 nodes), mounted at top-of-rack
- One leak detection node near the room's single floor drain
- One node mounted near the primary patch panel wall to monitor conditions at the densest cabling point

**Environmental risks:**
- This zone has the facility's only floor drain, which is a potential backflow risk during building flooding events
- The room sits above a basement-level utility chase; pressure differentials can occur when the chase HVAC system cycles
- Cable density at the patch panel wall restricts airflow and creates localized hot spots

**Most serious alarms in this zone:**
- `LEAK_DETECTED` — Floor drain backflow during facility-level water events; treat as critical given operational dependency of this zone
- `TEMPERATURE_HIGH` at top-of-rack nodes — Localised hot spot at the patch panel wall; may require targeted airflow intervention rather than a facility-wide cooling response
- `SIGNAL_DEGRADED` — MQTT connectivity for all nodes routes through this zone; degraded signal on Zone Gamma nodes may indicate a network-layer problem affecting broader telemetry reliability

**Zone relationships:**
Zone Gamma is the network dependency for all other zones. A connectivity failure here will prevent telemetry from reaching the controller service, which will trigger node-offline alarms across the facility. When multiple nodes across multiple zones appear offline simultaneously, check Zone Gamma physical status before assuming a widespread node hardware failure.

---

### Zone Delta — Cable Transit Corridor

**Room designation:** D-TRANSIT
**Location in facility:** Runs east–west along the south wall of Zone Alpha, connecting all zones to the electrical distribution room (Zone Echo)
**Dimensions:** Approximately 8 feet wide, 95 feet long

Zone Delta is a managed cable corridor, not a staffed room. Overhead cable trays carry power and data cabling between all equipment zones and the electrical distribution room. Access is via keyed panels at the Zone Alpha end and the Zone Echo end. This corridor is not climate-controlled but is enclosed and shares some conditioned air via passive transfer from adjacent zones.

**Nodes deployed:**
- Two environmental nodes mounted on the corridor ceiling at the midpoint and at the Zone Echo end
- One leak detection node at the lowest point of the corridor floor (center section, where the floor has a slight grade)

**Environmental risks:**
- Uncontrolled temperature; readings in this corridor will be higher than equipment zones during summer months and lower in winter
- The corridor's lowest floor point collects any water that migrates from Zone Alpha's subfloor via the shared wall; the leak node here acts as an early downstream indicator
- Cable tray fill levels in some sections are at capacity, which generates localized heat from conductor resistance

**Most serious alarms in this zone:**
- `LEAK_DETECTED` — Water in the corridor floor suggests migration from Zone Alpha subfloor or a building envelope breach along the south wall
- `TEMPERATURE_HIGH` — Sustained elevated temperature in the corridor can degrade cable insulation over time and may indicate a blocked passive air transfer point

**Zone relationships:**
Zone Delta is downstream of Zone Alpha for both water migration and thermal conditions. A leak alarm in Zone Delta that follows a Zone Alpha leak alarm confirms water is spreading and elevates the incident severity. Zone Delta connects directly to Zone Echo at its western terminus.

---

### Zone Echo — Electrical Distribution Room

**Room designation:** E-ELEC
**Location in facility:** West end of facility, accessible from Zone Delta corridor
**Equipment:** Main distribution panel, two Paragon UPS-8000 units, sub-panel for each equipment zone, transfer switch

Zone Echo is the facility's single point of electrical distribution. Both UPS units are maintained under a hardware support agreement with quarterly preventive maintenance visits. Battery replacement cycles are tracked in the Maintenance Change Log. Access to this room is restricted to the Facility Lead and authorized electrical contractors.

**Nodes deployed:**
- One environmental node mounted near the UPS battery arrays
- One ambient temperature node mounted at ceiling height to detect heat from the distribution panels
- One leak detection node on the floor near the exterior wall (this wall faces a below-grade exterior stairwell that has a history of surface water pooling)

**Environmental risks:**
- Battery array heat is normal but elevated readings may indicate a failing UPS battery bank or a thermal runaway precursor
- The exterior stairwell adjacent to this room is the highest water ingress risk in the facility; heavy rain events have previously resulted in minor water intrusion along the base of the exterior wall
- Electrical arcing or panel faults may register as anomalous temperature spikes before any other indicator

**Most serious alarms in this zone:**
- `LEAK_DETECTED` — Water near electrical distribution equipment is a life-safety concern; this alarm in Zone Echo triggers mandatory immediate escalation regardless of time of day
- `TEMPERATURE_HIGH` — Near the UPS battery arrays, elevated temperature may indicate battery thermal event; do not enter the room alone if this alarm is active
- `BATTERY_LOW` on the node itself — Ironic but operationally significant; if the monitoring node in the electrical room is reporting low battery, it suggests the sub-panel feeding sensor infrastructure may have an issue

**Zone relationships:**
Zone Echo feeds power to all other zones. A fault here affects the entire facility. The transfer switch in this room connects to an external generator inlet; generator connection procedures are documented in the Escalation Procedures document under Facility Lead responsibilities.

---

### Control Room — Operations Center

**Room designation:** OPS-CTR
**Location in facility:** East end of facility, adjacent to main entrance
**Function:** Staffed monitoring station, Avisos Web Console workstations, badge access control panel, visitor reception

The control room is not an equipment zone but is included here because it is the primary hub for alarm response. The Avisos Web Console runs on two workstations in this room. During staffed hours, an operator monitors the console and acknowledges alarms. After hours, alarms route to the on-call engineer via the notification system.

**Nodes deployed:**
- One environmental node mounted near the workstation cluster to monitor operator environment
- No leak detection node (room is on a raised interior floor section with no subfloor and no plumbing overhead)

**Environmental risks:**
- Low risk zone; primary concern is workstation availability for alarm response
- HVAC for this room is independent of the main cooling plant; a comfort cooling failure here does not indicate a risk to equipment zones

**Most serious alarms in this zone:**
- `TEMPERATURE_HIGH` — In a staffed environment, elevated temperature is a comfort and health concern, not an equipment risk; response is to check the room's standalone HVAC unit
- `SIGNAL_DEGRADED` — If the control room node loses MQTT connectivity, it may indicate a problem with the broker or Zone Gamma network infrastructure

---

## Zone Relationship Summary

The following relationships are operationally significant when interpreting alarms across zones.

| Dependency | Detail |
|---|---|
| Zone Beta → Alpha, Gamma | Cooling plant serves both equipment zones via shared plenum |
| Zone Alpha → Delta | Subfloor water migration path runs south into the corridor |
| Zone Delta → Echo | Corridor connects to electrical room; shared south wall water risk |
| Zone Gamma → All zones | Network zone; loss of connectivity here affects all MQTT telemetry |
| Zone Echo → All zones | Electrical distribution; a fault here can cause facility-wide power events |

---

## AI Incident Analyst Reference Notes

When interpreting an alarm, the zone designation in the node ID indicates where the node is physically located. Node IDs follow the format `[ZONE]-[ROW]-[TYPE]-[SEQUENCE]`, for example `A-A3-ENV-01` (Zone Alpha, Row A3, environmental node, unit 1) or `B-MECH-LEAK-01` (Zone Beta, leak detection node, unit 1).

Use the zone's known environmental risks and zone relationships to contextualize whether an alarm reading is likely a genuine condition or a known baseline anomaly. For example, elevated humidity in Zone Beta is a baseline characteristic of the cooling plant room and should be interpreted against a higher normal range than Zone Alpha. A single `TEMPERATURE_HIGH` in Zone Alpha row A4–A6 may be an airflow management issue rather than a cooling plant failure, whereas the same alarm in Zone Beta warrants immediate cooling plant inspection.

Correlate alarms across zones before concluding a condition is isolated. Simultaneous temperature rise in Zone Alpha and a humidity spike in Zone Beta suggests a shared cooling failure. A `LEAK_DETECTED` in Zone Alpha followed within minutes by `LEAK_DETECTED` in Zone Delta confirms active water spread and should elevate the incident severity.