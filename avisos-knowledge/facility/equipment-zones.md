# Equipment Zones — Meridian Ridge Facility

## Document Purpose

This document provides detailed operational profiles for each named equipment zone and sub-zone within the Meridian Ridge facility. It is intended as a reference for on-call engineers responding to alarms and as a retrieval source for the Avisos AI Incident Analyst. Each section describes normal operating expectations, telemetry interpretation guidance, and cross-zone dependencies relevant to incident triage.

Where zone names appear in node IDs or alarm payloads, this document is the authoritative source for what those names mean physically and operationally.

---

## Zone Alpha — Primary Compute Racks

### Sub-Zone: Row Cluster A1–A3 (Contained Cold Aisle)

**Physical description:**
Rows A1 through A3 form the northern half of Zone Alpha. These three rows operate under a full cold-aisle containment system with rigid polycarbonate panels at each row end and brush-strip cable pass-throughs. Blanking panels are installed on all unused rack units. Cooling air enters from the raised subfloor via perforated tiles placed at regular intervals along the cold aisle. Hot exhaust exits into the contained hot aisle and returns to the Zone Beta cooling plant via the overhead return-air plenum.

**Normal operating expectations:**
- Cold-aisle intake temperature: 65–72°F (18–22°C)
- Hot-aisle exhaust temperature: 85–100°F (29–38°C)
- Relative humidity: 40–55%
- Subfloor static pressure: slightly positive relative to room
- Node battery levels: stable above 80% under normal rail voltage

Telemetry from environmental nodes in this cluster is the most stable in the facility due to containment discipline. Readings outside the ranges above are meaningful signals and should not be dismissed as baseline variation.

**Telemetry changes that may indicate trouble:**
- Cold-aisle temperature rising above 75°F (24°C): suggests containment breach, perforated tile displacement, or Zone Beta cooling underperformance
- Humidity climbing above 60%: may precede a condensate-related event in Zone Beta or indicate a subfloor moisture ingress at the north wall
- Any `LEAK_DETECTED` event: treat as critical; subfloor water near contained racks has no safe threshold
- Battery readings dropping simultaneously on multiple nodes: indicates voltage sag on the secondary power rail feeding sensor infrastructure, not individual node failure

**Keywords:** cold aisle, hot aisle, containment, perforated tile, subfloor, return plenum, rack temperature, humidity, blanking panel

---

### Sub-Zone: Row Cluster A4–A6 (Open-Frame Racks)

**Physical description:**
Rows A4 through A6 occupy the southern half of Zone Alpha. These rows use older open-frame racks without cold-aisle containment. Hot exhaust from equipment in these rows is not channeled and mixes with room air. Cooling is provided by two perimeter supplemental units mounted on the south wall, which supply cooled air across the open floor. Airflow efficiency in this cluster is lower than rows A1–A3, and temperature stratification is common, with higher readings at the top of racks.

**Normal operating expectations:**
- Ambient room temperature: 68–76°F (20–24°C)
- Top-of-rack temperature at environmental nodes: up to 82°F (28°C) is within tolerance given airflow limitations
- Relative humidity: 42–58%
- Some reading variance between nodes in this cluster is expected and normal

**Telemetry changes that may indicate trouble:**
- Top-of-rack temperature exceeding 85°F (29°C): indicates perimeter unit underperformance, a blocked supply grille, or equipment in adjacent racks generating abnormal exhaust
- Wide temperature spread between nodes in the same row (greater than 8°F difference): suggests a localized hotspot, possibly caused by a failed rack fan tray or a high-density device added without airflow review
- Humidity rising while temperature remains stable: possible indication of condensation forming on cooler surfaces due to perimeter unit overcooling; inspect supply air temperature at the unit face
- `SIGNAL_DEGRADED` on nodes in rows A4–A6: cabling for these nodes routes through a less-managed section of the Zone Delta corridor; signal issues may be cable-related rather than node hardware

**Keywords:** open frame, perimeter cooling, hotspot, top-of-rack, airflow stratification, temperature variance, rack fan, supplemental cooling unit

---

### Sub-Zone: Alpha Subfloor Access Points

**Physical description:**
The raised subfloor in Zone Alpha is accessible via two lift-panel access points: one at the north end of the room (near the exterior wall) and one near the center of the room adjacent to row A3. Both points are covered by standard floor tiles when not in use. Overhead leak detection nodes (A-SF-LEAK-01 and A-SF-LEAK-02) are mounted on the underside of the floor structure above these access points at heights that allow them to detect pooling water before it reaches cable pathways.

**Normal operating expectations:**
- No moisture detected; both leak nodes should report a clean status at all times
- Subfloor space should be clear of debris; maintenance visits occasionally leave materials in this space, which can obstruct airflow through perforated tiles
- Periodic condensation on cold pipes is not a leak event; however, persistent or increasing condensation that contacts cabling or flooring supports should be flagged

**Telemetry changes that may indicate trouble:**
- `LEAK_DETECTED` on A-SF-LEAK-01 (north access point): highest priority; this node is closest to the exterior wall where water intrusion has occurred historically; correlate with recent weather conditions and inspect exterior wall joint before assuming internal source
- `LEAK_DETECTED` on A-SF-LEAK-02 (center access point): may indicate condensate migration from Zone Beta via the shared subfloor; check Zone Beta drain manifold status simultaneously
- Both subfloor leak nodes triggering within a short time window: confirms water spread under the floor; escalate immediately and consider powering down affected rack rows pending inspection

**Keywords:** subfloor, raised floor, leak detection, water intrusion, exterior wall, condensate migration, floor tile, access panel

---

## Zone Beta — Cooling Plant

### Sub-Zone: CRAH Units (Stalwart CR-9)

**Physical description:**
Two Stalwart CR-9 computer room air handlers are installed in Zone Beta, designated CR9-EAST and CR9-WEST. Each unit draws return air from the overhead plenum, conditions it, and delivers cooled air to the subfloor supply plenum shared with Zone Alpha and Zone Gamma. Environmental nodes are mounted on the intake face of each unit. Each CR-9 has an internal condensate collection pan that drains to the central manifold via a gravity-fed line.

**Normal operating expectations:**
- Intake air temperature at CRAH face: 75–88°F (24–31°C); this is return air and will be warmer than equipment zone readings
- Supply air temperature leaving the unit: 55–65°F (13–18°C); not directly measured by Avisos nodes but can be inferred from subfloor pressure and Zone Alpha cold-aisle readings
- Relative humidity at intake face: 45–65%; higher than equipment zones because return air carries moisture from the equipment environment
- Both units should be running simultaneously under normal conditions; single-unit operation is a degraded mode and should be flagged to the Facility Lead

**Telemetry changes that may indicate trouble:**
- Intake temperature rising above 90°F (32°C) on either CRAH node: indicates higher-than-expected heat load returning from equipment zones; correlate with Zone Alpha and Zone Gamma temperature trends
- Humidity at intake face exceeding 70%: possible refrigerant undercharge causing the unit to overcool and condense excessive moisture, or a Zone Alpha/Gamma humidity source entering the return stream
- Pressure anomaly at the Zone Beta ambient node: the room houses large air-moving equipment; sudden pressure changes may indicate a fan motor failure, a duct separation, or the exterior louver opening unexpectedly
- `SIGNAL_DEGRADED` on either CRAH node: these nodes are in a mechanically noisy environment; verify physical node attachment before assuming RF interference

**Keywords:** CRAH, computer room air handler, Stalwart CR-9, intake temperature, return air, supply plenum, refrigerant, cooling unit, fan motor, CR9-EAST, CR9-WEST

---

### Sub-Zone: Condensate Drain Manifold

**Physical description:**
The condensate drain manifold is a PVC junction assembly mounted at floor level on the west wall of Zone Beta. Gravity-feed lines from CR9-EAST, CR9-WEST, and both perimeter supplemental units connect here before routing to the building drain system via a single exit line. A leak detection node (B-MECH-LEAK-01) is positioned on the floor directly below the manifold junction. A secondary catch tray is installed beneath the manifold but is not instrumented.

**Normal operating expectations:**
- No moisture detected at the floor level under normal operation; condensate should flow continuously through the lines without accumulation
- During high-humidity periods or high cooling loads, condensate flow volume increases; this is normal but increases the probability of a slow drain line blockage
- The manifold is inspected during quarterly maintenance visits; partial blockages may develop between visits

**Telemetry changes that may indicate trouble:**
- `LEAK_DETECTED` on B-MECH-LEAK-01: indicates condensate overflow from the manifold or a failed drain line fitting; this is the most common leak source in the facility and should be treated as medium-priority unless water volume is significant or Zone Alpha subfloor nodes confirm downstream spread
- Humidity rising steadily in Zone Beta while CRAH intake readings remain stable: can precede a condensate backup event before visible overflow occurs; early indicator for preventive drain inspection
- `LEAK_DETECTED` in Zone Beta followed within 10–20 minutes by `LEAK_DETECTED` in Zone Alpha subfloor: confirms active water migration via the shared subfloor; escalate incident severity

**Keywords:** condensate drain, manifold, PVC, drain line, blockage, overflow, catch tray, water migration, cooling plant drain, B-MECH-LEAK-01

---

## Zone Gamma — Network and Edge Infrastructure

### Sub-Zone: Network Rack Rows G1–G3

**Physical description:**
Zone Gamma contains three rack rows designated G1, G2, and G3. Row G1 holds core switching and firewall appliances. Row G2 holds distribution switching, patch panels, and out-of-band management equipment. Row G3 holds edge compute nodes and ancillary network hardware. Environmental nodes are mounted at top-of-rack in each row. Equipment in this zone generates less heat per rack than Zone Alpha, but the criticality of the network infrastructure makes temperature management important.

**Normal operating expectations:**
- Ambient temperature: 66–74°F (19–23°C)
- Top-of-rack temperature: up to 79°F (26°C) at peak load
- Relative humidity: 40–55%
- Node signal quality: high; these nodes are physically closest to the MQTT broker, which is hosted on a device in row G2

**Telemetry changes that may indicate trouble:**
- Temperature rising above 80°F (27°C) at G1 top-of-rack: switching and firewall hardware in this row is thermally sensitive; sustained elevated temperature may trigger hardware throttling before any physical alarm is visible
- `SIGNAL_DEGRADED` on Zone Gamma nodes: counterintuitively, signal degradation on nodes in the zone hosting the MQTT broker may indicate a broker or network issue rather than a node RF problem; check broker service status before investigating node hardware
- Multiple nodes across the facility showing `SIGNAL_DEGRADED` simultaneously: strong indicator of a Zone Gamma network event; do not treat as coincidental node failures
- Humidity rising in G-NET room while Zone Beta reads normal: may indicate the room's supplemental HVAC unit is underperforming; this unit is independent of the main cooling plant

**Keywords:** network rack, MQTT broker, switching, firewall, top-of-rack temperature, signal quality, edge compute, out-of-band management, G1, G2, G3

---

### Sub-Zone: Zone Gamma Floor Drain and Patch Panel Wall

**Physical description:**
Zone Gamma contains the facility's only passive floor drain, located in the northeast corner of the room near rack row G1. This drain has a backflow preventer valve that was last tested during the previous annual maintenance cycle. A leak detection node (G-NET-LEAK-01) is positioned adjacent to the drain. The patch panel wall runs along the east side of the room and houses high-density fiber and copper terminations for all inter-zone cabling. A dedicated environmental node (G-PATCH-ENV-01) monitors conditions at this wall due to the localized heat generated by dense cable bundles.

**Normal operating expectations:**
- No moisture at floor drain under normal conditions; the drain is a passive overflow point and should be dry
- Patch panel wall ambient temperature: up to 78°F (26°C) due to cable density; this is higher than open-rack areas in the same room and is a known baseline condition
- Relative humidity at patch panel wall: may read 2–4 percentage points higher than room average due to localized airflow restriction

**Telemetry changes that may indicate trouble:**
- `LEAK_DETECTED` on G-NET-LEAK-01: likely backflow through the floor drain during a facility-level water event, or water ingress from the utility chase below the room; determine whether other zones are also showing leak alarms before scoping the response
- Temperature at G-PATCH-ENV-01 exceeding 82°F (28°C): suggests cabling additions have increased heat density at the wall without a corresponding airflow adjustment; schedule a cabling audit
- Simultaneous humidity spike at G-PATCH-ENV-01 and temperature rise: indicates restricted airflow is trapping heat and moisture; consider temporary supplemental airflow

**Keywords:** floor drain, backflow preventer, patch panel wall, cable density, fiber termination, G-NET-LEAK-01, G-PATCH-ENV-01, utility chase, water ingress

---

## Zone Delta — Cable Transit Corridor

### Sub-Zone: Corridor Midpoint and East Section

**Physical description:**
The eastern half of the Zone Delta corridor runs from the Zone Alpha south wall entry panel to the midpoint junction where the overhead cable trays change direction. This section carries the densest cable load in the corridor, as all Zone Alpha rack cabling merges into the tray system here. An environmental node (D-TRANSIT-ENV-01) is mounted on the ceiling at the midpoint. Ambient temperature in this section is passively influenced by Zone Alpha conditions through the shared wall.

**Normal operating expectations:**
- Temperature: 70–82°F (21–28°C); wider acceptable range than equipment zones because this is an uncontrolled space
- Humidity: 38–60%; will track Zone Alpha conditions loosely
- No moisture at floor level; this section of the corridor has no floor penetrations and sits above grade

**Telemetry changes that may indicate trouble:**
- Temperature above 85°F (29°C) sustained for more than 15 minutes: heat is building from cable tray conductor resistance; may indicate a new high-current circuit was added to a tray already near fill capacity; flag to Facility Lead
- Humidity above 65%: unusual for this section unless Zone Alpha is also showing humidity rise; correlate the two readings

**Keywords:** cable tray, conductor resistance, tray fill, corridor temperature, passive thermal coupling, D-TRANSIT-ENV-01

---

### Sub-Zone: Corridor West Section and Low Point

**Physical description:**
The western half of Zone Delta runs from the midpoint to the Zone Echo entry panel. This section has a slight floor grade, approximately 1.5 inches of drop from east to west, which was introduced during a past facility modification. Any water entering the corridor from Zone Alpha via the shared subfloor or wall will tend to flow westward toward this end. A leak detection node (D-TRANSIT-LEAK-01) is positioned at the lowest point of the corridor floor, approximately 20 feet from the Zone Echo entry panel. An environmental node (D-TRANSIT-ENV-02) is mounted on the ceiling at the Zone Echo end.

**Normal operating expectations:**
- No moisture at any point on the corridor floor
- Temperature: 68–80°F (20–27°C); slightly cooler than the east section because this end is closer to the climate-controlled Zone Echo room
- Humidity: 38–58%

**Telemetry changes that may indicate trouble:**
- `LEAK_DETECTED` on D-TRANSIT-LEAK-01 without a prior Zone Alpha leak alarm: may indicate water entering from the south exterior wall rather than migrating from Zone Alpha; inspect the south exterior wall base and the below-grade building perimeter in this area
- `LEAK_DETECTED` on D-TRANSIT-LEAK-01 following a Zone Alpha leak alarm: confirms active water spread westward; immediate escalation required; proximity to Zone Echo electrical room makes this a secondary life-safety risk
- Temperature at D-TRANSIT-ENV-02 rising while Zone Echo temperature is stable: suggests heat is entering from the corridor side rather than the electrical room; investigate cable tray load in the west section

**Keywords:** floor grade, water migration, south wall, west corridor, Zone Echo proximity, D-TRANSIT-LEAK-01, D-TRANSIT-ENV-02, electrical room approach

---

## Zone Echo — Electrical Distribution Room

### Sub-Zone: UPS Battery Arrays

**Physical description:**
Two Paragon UPS-8000 units are installed on the west wall of Zone Echo, designated UPS-A and UPS-B. Each unit houses its own sealed lead-acid battery array. UPS-A serves Zone Alpha and Zone Beta. UPS-B serves Zone Gamma and the control room. An environmental node (E-ELEC-ENV-01) is mounted on the rack frame between the two units to monitor the ambient conditions of the battery arrays.

**Normal operating expectations:**
- Ambient temperature at battery array node: 68–77°F (20–25°C); battery arrays are sensitive to heat and their service life degrades measurably above 77°F
- Relative humidity: 35–50%; sealed batteries do not tolerate high humidity environments over extended periods
- No unusual odor or visible swelling; these are inspected during quarterly maintenance visits

**Telemetry changes that may indicate trouble:**
- Temperature at E-ELEC-ENV-01 exceeding 80°F (27°C): priority alert; elevated battery array temperature can shorten battery life and in worst cases precede thermal runaway; notify Facility Lead and do not enter the room alone
- Humidity above 55% in this zone: investigate source; this room has no plumbing and humidity should be low; elevated readings suggest either water intrusion from the exterior stairwell or a failed door seal allowing corridor air infiltration
- Node battery level on E-ELEC-ENV-01 reading low: the monitoring node in the electrical room is powered from the Zone Echo sub-panel; if this node is reporting low battery while UPS units appear healthy, check the sub-panel breaker feeding sensor infrastructure

**Keywords:** UPS, battery array, Paragon UPS-8000, thermal runaway, sealed lead-acid, UPS-A, UPS-B, battery temperature, battery life, E-ELEC-ENV-01

---

### Sub-Zone: Main Distribution Panel and Exterior Wall

**Physical description:**
The main distribution panel is mounted on the north wall of Zone Echo. Sub-panels for each zone branch from this panel. The transfer switch for the external generator inlet is also in this room, mounted adjacent to the main panel. A ceiling-mounted temperature node (E-ELEC-TEMP-01) monitors the upper air in the room, where heat from panel components accumulates. The exterior wall of Zone Echo faces a below-grade stairwell, and the base of this wall is the primary water ingress risk in the electrical room. A floor-level leak detection node (E-ELEC-LEAK-01) is positioned at the base of the exterior wall.

**Normal operating expectations:**
- Ceiling temperature at E-ELEC-TEMP-01: 70–80°F (21–27°C) during normal operation; panels generate low-level waste heat that stratifies upward
- No moisture at E-ELEC-LEAK-01 under any conditions; water near electrical distribution equipment is always abnormal

**Telemetry changes that may indicate trouble:**
- `LEAK_DETECTED` on E-ELEC-LEAK-01: immediate mandatory escalation; water near active electrical distribution panels is a life-safety event; do not enter the room until the Facility Lead and, if necessary, emergency services have been notified
- Ceiling temperature above 85°F (29°C): may indicate a failing breaker generating arc heat, an overloaded circuit, or general room cooling failure; correlate with any recent load changes
- Simultaneous high temperature and leak alarm in Zone Echo: treat as a critical combined event; initiate controlled facility shutdown procedure per the relevant runbook

**Keywords:** main distribution panel, sub-panel, transfer switch, generator inlet, exterior stairwell, water ingress, arc heat, overloaded circuit, E-ELEC-LEAK-01, E-ELEC-TEMP-01, life-safety

---

## Cross-Zone Dependency Summary

The following dependency chains are the most operationally significant and should guide multi-alarm triage decisions.

**Cooling chain:**
Zone Beta (CRAH units) → shared plenum → Zone Alpha subfloor supply → Zone Gamma supplemental cooling. A Zone Beta cooling failure will produce rising temperatures in Zone Alpha within 15–20 minutes and in Zone Gamma within 20–30 minutes depending on thermal load.

**Water migration chain:**
Zone Alpha subfloor (north wall ingress) → Zone Alpha center subfloor → Zone Delta corridor floor (grade-assisted westward flow) → Zone Echo exterior wall base. A confirmed leak at any point in this chain should prompt inspection of all downstream nodes.

**Network dependency chain:**
Zone Gamma (MQTT broker, core switching) → all MQTT telemetry paths → avisos-controller-service. Zone Gamma network failures will suppress telemetry from all other zones, causing node-offline alarms that are an artifact of the outage, not independent hardware failures.

**Power dependency chain:**
Zone Echo (main panel, UPS-A, UPS-B) → all zone sub-panels → all powered equipment including Avisos nodes. A Zone Echo event that affects UPS output will produce battery alarms and eventual node-offline alarms across the facility as node batteries are drawn down.

**AI retrieval note:** When two or more alarms across different zones are active simultaneously, always consult this dependency summary before treating the alarms as independent events. Correlated alarms that fit a known dependency chain indicate a single root cause and should be managed as one incident.