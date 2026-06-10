# Node Maintenance History — Meridian Ridge Facility

## Document Purpose

This document records the installation details and cumulative maintenance history for Avisos sensor nodes deployed at the Meridian Ridge facility. It is maintained by the Infrastructure Lead and updated after every maintenance visit, hardware intervention, or configuration change affecting a node. Entries are listed by node ID in zone order.

The Avisos AI Incident Analyst should use this document to contextualize active alarms against a node's known history. A node with a documented pattern of intermittent connectivity, a recently replaced sensor, or a prior leak-adjacent alarm should be interpreted differently from a node with a clean service history. Known issues are flagged explicitly in each entry.

All dates use YYYY-MM-DD format. Technician references use role titles only.

---

## Node Fleet Summary

| Node ID | Common Name | Zone | Location Detail | Installed | Status |
|---|---|---|---|---|---|
| A-A2-ENV-01 | Row Two North | Zone Alpha, Row A2 | Mid-rack, cold-aisle face | 2022-03-14 | Active |
| A-A5-ENV-02 | Row Five South | Zone Alpha, Row A5 | Mid-rack, open frame | 2022-03-14 | Active — known issues |
| A-SF-LEAK-01 | North Subfloor | Zone Alpha, North subfloor | Underside of floor structure, north access point | 2022-03-14 | Active |
| A-SF-LEAK-02 | Center Subfloor | Zone Alpha, Center subfloor | Underside of floor structure, center access point | 2022-03-14 | Active — known issues |
| B-MECH-ENV-01 | CRAH East Face | Zone Beta, CR9-EAST | CRAH intake face, east unit | 2022-03-14 | Active |
| B-MECH-LEAK-01 | Drain Manifold | Zone Beta, drain manifold | Floor level, west wall | 2022-04-02 | Active |
| G-NET-ENV-01 | G1 Top Rail | Zone Gamma, Row G1 | Top-of-rack, core switching row | 2022-03-14 | Active |
| G-NET-LEAK-01 | Gamma Floor | Zone Gamma, floor drain | Adjacent to northeast floor drain | 2022-03-14 | Active — known issues |
| G-PATCH-ENV-01 | Patch Wall East | Zone Gamma, patch panel wall | East wall, mid-height | 2023-01-18 | Active |
| D-TRANSIT-LEAK-01 | Corridor Low Point | Zone Delta, west section | Corridor floor, lowest grade point | 2022-03-14 | Active |
| E-ELEC-ENV-01 | UPS Array Center | Zone Echo, UPS bay | Between UPS-A and UPS-B | 2022-03-14 | Active |
| E-ELEC-LEAK-01 | Echo Exterior Wall | Zone Echo, exterior wall | Floor level, base of exterior wall | 2022-03-14 | Active — monitor |

---

## Node Detail Records

---

### A-A2-ENV-01 — Row Two North

**Zone:** Alpha, Row A2 (contained cold aisle)
**Node type:** Environmental (temperature, humidity, pressure, battery, signal)
**Installed:** 2022-03-14
**Installation notes:** Mounted on mid-rack cold-aisle face bracket, row A2, rack position 14U. Initial calibration performed at installation. Baseline temperature established at 68°F, humidity at 47%.

**Maintenance history:**

2022-09-05 — Routine quarterly inspection. All readings within normal range. No issues noted. Firmware updated to v2.1.4.

2023-01-10 — Calibration check. Temperature sensor reading 1.2°F high compared to reference thermometer. Offset correction applied in controller configuration. Humidity within tolerance.

2023-06-21 — Routine inspection. Node enclosure dusty but lens and sensor ports clear. Enclosure cleaned. Readings stable. Battery at 97%.

2023-11-14 — Firmware updated to v2.3.0. No issues noted. Signal quality excellent at 94%.

2024-04-08 — Calibration check. Temperature offset from 2023-01-10 still appropriate; no adjustment needed. Humidity sensor within tolerance. Pressure sensor reading slightly low (within 1.5 hPa of reference); noted but within acceptable range; no correction applied.

2024-10-02 — Routine inspection. No issues. Battery at 96%. Node continues to be among the most stable in the fleet.

**Known issues:** None active. Minor temperature offset corrected by configuration in January 2023; no recurrence.

**AI analyst note:** This is a high-reliability node with a clean recent history. Alarms from this node should be weighted as credible. The documented temperature offset was corrected at the configuration level and does not affect current readings.

---

### A-A5-ENV-02 — Row Five South

**Zone:** Alpha, Row A5 (open-frame racks)
**Node type:** Environmental (temperature, humidity, pressure, battery, signal)
**Installed:** 2022-03-14
**Installation notes:** Mounted on open-frame rack at row A5, rack position 18U. This row lacks cold-aisle containment; baseline temperature established at 74°F to reflect the warmer open-frame environment. Humidity baseline 49%.

**Maintenance history:**

2022-09-05 — Routine inspection. Temperature reading trending slightly high relative to neighboring nodes in rows A4 and A6. Variance assessed as within expected range for the open-frame environment. No action taken.

2023-01-10 — Humidity sensor flagged as erratic. Reading cycling between 44% and 67% within short intervals without correlated temperature change. Sensor module replaced. Post-replacement readings stable at 51%.

2023-03-28 — `BATTERY_LOW` alarm investigated. Battery found at 34%. Supply rail voltage measured at 11.3V against a nominal 12V. Voltage sag confirmed on Zone Alpha secondary rail. No node hardware fault. Rail issue flagged to Facility Lead. Battery recovered to 89% within six hours after Facility Lead adjusted load distribution on the sub-panel.

2023-06-21 — Routine inspection. Battery at 91%. Signal at 81%. Temperature reading high relative to A-A4-ENV node by 4.2°F. Checked for blocked airflow at rack face; one blanking panel missing from adjacent rack. Blanking panel replaced by on-site technician. Temperature differential reduced to 1.8°F by end of visit.

2023-11-14 — Firmware updated to v2.3.0. Post-update signal quality improved from 81% to 86%. Battery at 88%.

2024-03-15 — `BATTERY_LOW` alarm. Battery at 38%. Investigated; no rail voltage issue found this time. Battery cell assessed as degrading. Backup cell replaced. Post-replacement battery climbed to 94% within three hours on a confirmed healthy rail.

2024-07-22 — Humidity sensor showing intermittent spike behavior again, similar to the pattern seen before the January 2023 replacement. Spikes are less frequent (two to three per week rather than continuous). Sensor module flagged for replacement at next site visit. Workaround: controller configured to require two consecutive out-of-range humidity readings before raising a humidity alarm for this node.

2024-10-02 — Humidity sensor module replaced (second replacement). Post-replacement readings stable. Controller alarm filter from 2024-07-22 retained as a precaution for 90-day monitoring period.

**Known issues:** History of humidity sensor instability; two sensor modules replaced. History of battery events, one supply-rail-caused and one cell-degradation-caused. The alarm filter requiring two consecutive humidity readings remains active as of last inspection.

**AI analyst note:** Treat humidity readings from this node with moderate caution. The two-consecutive-reading filter reduces false positives but means a single out-of-range reading will not raise an alarm. A confirmed humidity alarm from this node has cleared the filter and is credible. Battery alarms from this node have had two distinct causes in the past; check rail voltage before assuming cell degradation.

---

### A-SF-LEAK-01 — North Subfloor

**Zone:** Alpha, north subfloor access point
**Node type:** Leak detection, environmental (temperature, humidity)
**Installed:** 2022-03-14
**Installation notes:** Mounted on underside of floor structure above north subfloor access panel. Positioned to detect pooling water before it contacts cable pathways. This is the highest-priority leak node in the facility given its proximity to the exterior wall water intrusion history.

**Maintenance history:**

2022-09-05 — Routine inspection. Node accessed via subfloor panel. No moisture. Node mounting secure. Sensor surface clean.

2022-11-08 — `LEAK_DETECTED` alarm. Heavy rainfall event preceding alarm by approximately four hours. Water intrusion confirmed at base of north exterior wall. Standing water approximately 0.3 inches deep at subfloor level near wall. Facility Lead notified. Water removed using absorbent matting. Exterior wall joint inspected; minor sealant gap identified at the base of the wall panel. Sealant gap temporarily sealed with hydraulic cement pending permanent repair. Node confirmed functional after event.

2023-02-14 — Permanent exterior wall sealant repair completed by building contractor. Facility Lead signed off. Node inspection confirmed clean.

2023-06-21 — Routine inspection. No moisture. Node position verified. No sealant deterioration visible at repaired wall joint.

2023-11-14 — Firmware updated to v2.3.0.

2024-04-08 — Routine inspection. Minor surface condensation noted on the underside of the floor structure during inspection; assessed as transient from a cold morning and warm subfloor air interaction. Sensor surface dry. No alarm was raised, confirming the sensor is positioned above the level at which condensation forms on the structure.

2024-10-02 — Routine inspection. No moisture. Sealant repair from 2023-02-14 remains intact.

**Known issues:** Prior confirmed water intrusion event in November 2022. Root cause addressed by permanent sealant repair. No recurrence since repair.

**AI analyst note:** This node has a confirmed prior leak event with a documented root cause and repair. A future `LEAK_DETECTED` alarm from this node should prompt immediate cross-reference with current weather conditions (heavy rainfall increases probability of recurrence if sealant has deteriorated) and visual inspection of the repaired north wall joint.

---

### A-SF-LEAK-02 — Center Subfloor

**Zone:** Alpha, center subfloor access point
**Node type:** Leak detection, environmental (temperature, humidity)
**Installed:** 2022-03-14
**Installation notes:** Mounted on underside of floor structure above center subfloor access panel, near row A3. Primary function is early detection of condensate migration from Zone Beta via shared subfloor.

**Maintenance history:**

2022-09-05 — Routine inspection. Clean. No issues.

2023-01-10 — Node mount found loose during inspection. Mounting bracket had partially separated from the floor structure due to vibration from CRAH units. Node repositioned and re-secured with additional fastener points. Sensor position verified.

2023-06-21 — Routine inspection. Mounting secure. Clean. No issues.

2023-08-17 — `LEAK_DETECTED` alarm. Investigated. Small amount of moisture found on the subfloor directly below the node. Source traced to a brief condensate overflow at Zone Beta drain manifold (B-MECH-LEAK-01 had also alarmed 22 minutes earlier). Moisture was limited; no cable contact. Subfloor dried. Zone Beta drain cleared of partial blockage. Both nodes cleared.

2023-11-14 — Firmware updated to v2.3.0.

2024-04-08 — Routine inspection. Clean. Mounting secure.

2024-10-02 — Slight humidity elevation noted in sensor reading compared to A-SF-LEAK-01 at the same time of day. Difference is 4 percentage points. Assessed as within expected variation given proximity to Zone Beta shared wall. Flagged for trend monitoring; no action taken.

**Known issues:** Prior confirmed moisture event in August 2023 linked to Zone Beta drain overflow. Mounting bracket history; last secured January 2023, confirmed secure since. Humidity reading slightly elevated relative to neighboring subfloor node; under trend monitoring.

**AI analyst note:** When this node alarms, immediately check B-MECH-LEAK-01 status. The August 2023 event demonstrates that Zone Beta condensate overflow can reach this node. A 22-minute lag between the Zone Beta alarm and this node's alarm was observed; if B-MECH-LEAK-01 is already alarming, this node may follow. The humidity elevation noted in October 2024 is under observation and should be factored into humidity alarm interpretation from this node.

---

### B-MECH-ENV-01 — CRAH East Face

**Zone:** Beta, CR9-EAST intake face
**Node type:** Environmental (temperature, humidity, pressure, battery, signal)
**Installed:** 2022-03-14
**Installation notes:** Mounted on the intake face of the CR9-EAST unit. Baseline temperature established at 82°F (return air is warmer than room ambient). Humidity baseline 54%.

**Maintenance history:**

2022-09-05 — Routine inspection. Readings consistent with CRAH operating parameters. Signal quality 88%.

2023-01-10 — Pressure sensor flagged. Reading showing sustained offset of approximately 3 hPa above reference. Calibration adjustment applied. Post-calibration readings stable.

2023-06-21 — Node enclosure vibrating loosely against the CRAH mounting surface. Foam isolation pad added between node and CRAH surface. Vibration reduced. Signal quality improved from 84% to 91% after vibration reduction.

2023-11-14 — Firmware updated to v2.3.0. Pressure sensor recalibrated as part of firmware update procedure.

2024-04-08 — Routine inspection. Temperature trend over the past 90 days reviewed. Readings have been stable; no drift detected. Humidity at 57%, slightly above baseline but within tolerance for current season.

2024-07-03 — `HUMIDITY_HIGH` alarm. Reading reached 71%. Investigated; CR9-EAST found operating with a partially blocked condensate drain line. Drain cleared by Facility Lead. Humidity returned to 55% within 90 minutes. No downstream moisture detected at B-MECH-LEAK-01.

2024-10-02 — Routine inspection. Post-drain-clearing readings stable. Temperature at 83°F, humidity at 55%. Drain manifold visually inspected and clear.

**Known issues:** Prior pressure calibration offset in January 2023, corrected. Prior humidity event in July 2024 linked to drain line blockage; resolved. Vibration isolation pad installed June 2023.

**AI analyst note:** Humidity readings from this node are the primary early indicator for Zone Beta drain issues. The July 2024 event demonstrated that humidity rising above 68% preceded a drain overflow condition. Use this node's humidity trend as a leading indicator when evaluating Zone Beta leak risk.

---

### B-MECH-LEAK-01 — Drain Manifold

**Zone:** Beta, condensate drain manifold
**Node type:** Leak detection
**Installed:** 2022-04-02
**Installation notes:** Installed two weeks after initial fleet deployment following an early condensate overflow event during commissioning. Positioned on the floor directly below the drain manifold junction.

**Maintenance history:**

2022-09-05 — Routine inspection. Clean. No issues.

2023-08-17 — `LEAK_DETECTED` alarm. Condensate overflow from partially blocked drain manifold. Overflow volume was minor; catch tray contained most of it. Small amount reached the floor sensor. Zone Beta humidity had been elevated for approximately three hours before overflow. Drain cleared. A-SF-LEAK-02 alarmed 22 minutes after this node.

2023-11-14 — Firmware updated to v2.3.0.

2024-04-08 — Routine inspection. Drain manifold flow tested visually with water. Clear. No issues with node.

2024-10-02 — Routine inspection. Clean. No issues. Drain line inspected and clear.

**Known issues:** One prior alarm event in August 2023. Drain line is inspected at every routine visit following this event.

**AI analyst note:** Alarm history on this node is directly linked to drain line blockage. A `LEAK_DETECTED` from this node should immediately trigger a check of Zone Beta humidity trend and A-SF-LEAK-02 status, following the established 2023 event sequence.

---

### G-NET-ENV-01 — G1 Top Rail

**Zone:** Gamma, Row G1, top-of-rack
**Node type:** Environmental (temperature, humidity, pressure, battery, signal)
**Installed:** 2022-03-14
**Installation notes:** Mounted at top-of-rack on Row G1, which houses core switching and firewall appliances. Baseline temperature 71°F. Humidity baseline 44%.

**Maintenance history:**

2022-09-05 — Routine inspection. Readings normal. Signal quality 96%; excellent due to proximity to MQTT broker in G2.

2023-01-10 — No issues. Firmware at current version.

2023-06-21 — Temperature reading 2.1°F above neighboring node G-NET-ENV-02 (row G2). Investigated; a new high-density switch installed in G1 during the previous month had not been flagged to the monitoring team. Load increase is the cause. Temperature within tolerance. Baseline updated in controller to reflect new load profile. New baseline top-of-rack temperature 73°F.

2023-11-14 — Firmware updated to v2.3.0.

2024-04-08 — Routine inspection. Readings stable at updated baseline. No issues. Battery at 97%.

2024-10-02 — Routine inspection. No issues. Signal quality 94%.

**Known issues:** Baseline temperature was updated in June 2023 following equipment addition. Previous baseline is no longer valid.

**AI analyst note:** Alarm thresholds for this node were recalibrated in June 2023. Alarms should be evaluated against the updated baseline (top-of-rack normal up to 79°F) rather than the original installation baseline. Equipment additions to Row G1 should always be communicated to the monitoring team for baseline review.

---

### G-NET-LEAK-01 — Gamma Floor

**Zone:** Gamma, northeast floor drain
**Node type:** Leak detection
**Installed:** 2022-03-14
**Installation notes:** Positioned adjacent to the northeast floor drain. Backflow preventer valve in the drain is the primary mechanical protection; this node is the monitoring layer.

**Maintenance history:**

2022-09-05 — Routine inspection. Clean. No issues.

2023-01-10 — `LEAK_DETECTED` alarm. Investigated; false positive caused by cleaning crew using a wet mop in the zone. Water from mopping reached the sensor. Cleaning crew access to equipment zones flagged to Operations Supervisor. Cleaning procedures updated to prohibit wet mopping in equipment zones; dry or damp microfiber only.

2023-06-21 — Routine inspection. Clean. No issues. Cleaning procedure change confirmed with Facility Lead.

2023-11-14 — Firmware updated to v2.3.0.

2024-01-29 — `LEAK_DETECTED` alarm during a heavy rainfall event. Water found pooling at the floor drain; backflow preventer valve assessed as partially fouled with debris, reducing its effectiveness. Approximately 0.5 inches of standing water at drain. No water reached cabling. Facility Lead cleared the drain and flushed the backflow preventer. Node cleared after water removed.

2024-04-08 — Backflow preventer valve replaced during routine maintenance visit. Old valve showed debris accumulation and partial seat wear. New valve installed and tested. Node clean.

2024-10-02 — Routine inspection. Clean. Backflow preventer visually inspected; clear. No issues.

**Known issues:** One wet-mop false positive in January 2023 (procedural, not hardware). One genuine backflow event in January 2024 linked to debris in the drain backflow preventer; valve replaced April 2024. Heavy rainfall events should prompt heightened attention to this node.

**AI analyst note:** This node has two distinct alarm causes in its history: a procedural false positive and a genuine backflow event. When evaluating a `LEAK_DETECTED` alarm from this node, cross-reference with current weather conditions and check whether any cleaning or maintenance activity was logged for Zone Gamma. A heavy rainfall event significantly increases the probability that this alarm is genuine.

---

### G-PATCH-ENV-01 — Patch Wall East

**Zone:** Gamma, patch panel wall, east wall
**Node type:** Environmental (temperature, humidity, battery, signal)
**Installed:** 2023-01-18
**Installation notes:** Installed as an addition to the original fleet following a temperature anomaly discovered during a cabling project in late 2022. Baseline temperature established at 76°F to reflect the elevated ambient near the high-density cable termination wall. No pressure sensor on this node model.

**Maintenance history:**

2023-06-21 — First routine inspection since installation. Readings stable at baseline. Signal quality 89%.

2023-11-14 — Firmware updated to v2.3.0. Baseline humidity noted at 47%, slightly higher than room average consistent with restricted airflow at the cable wall.

2024-04-08 — Temperature reading at 78°F, which is within the elevated baseline range. Humidity at 51%. Slight upward humidity drift noted over the past 90-day trend (from 47% to 51%). Flagged for continued monitoring; no action taken.

2024-07-15 — Humidity reached 58% without a corresponding temperature spike. Investigated; a cabling project earlier that month had added a dense bundle of new fiber runs along the east wall, further restricting airflow at the sensor location. Airflow partially restored by rerouting two cable bundles to a less congested tray path. Humidity settled at 53% post-adjustment.

2024-10-02 — Routine inspection. Temperature at 77°F. Humidity at 52%. Stable following July cable adjustment.

**Known issues:** Humidity baseline has shifted upward over the node's deployment life, from 47% at installation to a current stable point near 52%, driven by progressive cable density increases. Future cabling additions to the east wall should be reviewed for airflow impact before installation.

**AI analyst note:** This node's humidity baseline has drifted upward by approximately 5 percentage points since installation, with a documented cause (cable density). Humidity alarm thresholds may need to be reviewed against the current baseline rather than the 2023 installation baseline. Temperature readings from this node are inherently higher than room average; evaluate against the 76–79°F normal range for this location, not the general Zone Gamma threshold.

---

### D-TRANSIT-LEAK-01 — Corridor Low Point

**Zone:** Delta, west section, floor low point
**Node type:** Leak detection, environmental (temperature, humidity)
**Installed:** 2022-03-14
**Installation notes:** Positioned at the lowest point of the Zone Delta corridor floor. This is a downstream indicator node; water reaching this point has already passed through the corridor and is approaching Zone Echo.

**Maintenance history:**

2022-09-05 — Routine inspection. Clean. No issues. Corridor floor dry.

2023-06-21 — Routine inspection. Minor dust accumulation on sensor housing. Housing cleaned. Sensor surface intact.

2023-08-17 — No alarm on this date, but review conducted following the Zone Beta and Zone Alpha subfloor events. Corridor floor inspected; dry. Water from the Zone Beta event did not reach the corridor. Node confirmed functional.

2023-11-14 — Firmware updated to v2.3.0.

2024-04-08 — Routine inspection. Clean. No issues.

2024-10-02 — Routine inspection. Clean. Corridor floor dry. No issues.

**Known issues:** None. This node has a clean alarm history. It has never triggered in service.

**AI analyst note:** This node's clean history means its alarm baseline is well-established as consistently dry. A `LEAK_DETECTED` alarm from this node has no prior false positive pattern to consider and should be treated as credible immediately. Given its position as the last node before Zone Echo, any alarm here is high priority. Cross-reference with Zone Alpha subfloor nodes and Zone Beta to determine whether water is migrating from known upstream sources.

---

### E-ELEC-ENV-01 — UPS Array Center

**Zone:** Echo, between UPS-A and UPS-B
**Node type:** Environmental (temperature, humidity, pressure, battery, signal)
**Installed:** 2022-03-14
**Installation notes:** Mounted on rack frame between the two Paragon UPS-8000 units. Monitors ambient conditions of the battery arrays. Access to Zone Echo is restricted; all maintenance visits require Facility Lead presence.

**Maintenance history:**

2022-09-05 — Routine inspection. Temperature at 72°F. Humidity at 41%. Signal quality 79%; lower than equipment zone nodes due to the shielding effect of the UPS metal enclosures. This is a known baseline characteristic.

2023-01-10 — Signal quality dropped to 61% on two consecutive telemetry intervals; `SIGNAL_DEGRADED` alarm raised. On-site inspection found no hardware fault. Signal recovered to 77% without intervention. Assessed as transient RF interference; no further investigation required at the time.

2023-06-21 — Routine inspection. Temperature at 73°F. Humidity at 43%. Signal at 76%. Battery at 94%.

2023-09-04 — `BATTERY_LOW` alarm. Battery at 37%. Facility Lead inspected Zone Echo sub-panel. Sensor infrastructure breaker found tripped. Cause undetermined; no fault load identified. Breaker reset. Battery recovered to 91% within four hours. Breaker and wiring inspected by Facility Lead; no fault found.

2023-11-14 — Firmware updated to v2.3.0. Facility Lead present per access policy.

2024-04-08 — Routine inspection. Signal quality 78%. Temperature 71°F. Humidity 40%. Battery 96%. No issues. UPS units visually inspected; no indicator faults.

2024-08-21 — Transient signal quality drop to 58%; alarm raised and cleared within 20 minutes without intervention. Pattern similar to January 2023 event. Infrastructure Lead flagged the recurrence for investigation. RF environment survey conducted; an older UPS cooling fan motor in UPS-A identified as a possible intermittent RF emission source. Fan motor replacement scheduled for next quarterly maintenance.

2024-10-02 — UPS-A cooling fan motor replaced during quarterly maintenance visit (scheduled following August investigation). Signal quality stable at 81% post-replacement; improvement from 78% pre-replacement average. Battery 95%.

**Known issues:** History of transient signal quality drops; root cause identified as UPS-A fan motor RF emissions and resolved with fan motor replacement in October 2024. Prior `BATTERY_LOW` event in September 2023 caused by tripped breaker; no recurring breaker trips since.

**AI analyst note:** Signal quality on this node runs lower than the fleet average due to UPS enclosure shielding. The historic low baseline (76–79%) should be used when assessing whether a signal reading is anomalous. Post fan motor replacement, the baseline has improved to approximately 81%. A `SIGNAL_DEGRADED` alarm from this node that clears within 30 minutes fits the prior transient pattern and should be documented but may not require immediate physical response. A sustained degradation is a different condition and warrants investigation. The September 2023 battery event history means that a future battery alarm from this node should prompt a Zone Echo breaker check early in the triage sequence.

---

### E-ELEC-LEAK-01 — Echo Exterior Wall

**Zone:** Echo, exterior wall base
**Node type:** Leak detection
**Installed:** 2022-03-14
**Installation notes:** Positioned at the base of the exterior wall in Zone Echo. This wall faces the below-grade stairwell. A `LEAK_DETECTED` alarm from this node is a mandatory immediate escalation event due to proximity to live electrical distribution equipment.

**Maintenance history:**

2022-09-05 — Routine inspection. Clean. Exterior stairwell drain visually confirmed clear from window.

2023-01-10 — Routine inspection. Clean. No issues.

2023-06-21 — Routine inspection. Minor efflorescence (salt deposit) noted on the interior face of the exterior wall approximately 8 inches above floor level. Efflorescence indicates prior moisture movement through the wall structure. Facility Lead notified. Assessed as historic residue from construction period; wall surface dry and no active moisture detected. Flagged for monitoring.

2023-11-14 — Efflorescence area re-examined. No change; no new deposits. No moisture at sensor. Firmware updated to v2.3.0.

2024-04-08 — Routine inspection. Efflorescence area stable. Exterior stairwell drain inspected from stairwell access; minor debris accumulation cleared by Facility Lead. Stairwell drain now free-flowing.

2024-10-02 — Routine inspection. Clean. Efflorescence area unchanged; no active moisture. Stairwell drain confirmed clear.

**Known issues:** Efflorescence on interior wall face indicating historic moisture migration. Under monitoring since June 2023; no active moisture detected. Stairwell drain cleared April 2024.

**AI analyst note:** This node has never alarmed in service. The efflorescence finding indicates the wall has experienced moisture in the past; this increases the credibility of any future `LEAK_DETECTED` alarm from this node compared to a location with no moisture history. A `LEAK_DETECTED` from this node is mandatory immediate escalation regardless of other conditions. Do not treat as a probable false positive. Check the stairwell drain status and current weather conditions as part of the initial assessment.

---

*Document owner: Infrastructure Lead. Updated after every maintenance event. Review cycle: quarterly, or immediately following any alarm event involving a listed node.*