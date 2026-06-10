# Runbook — Water Ingress and Leak Detection Response

## Document Purpose

This runbook covers the detection, triage, and response procedures for water ingress events at the Meridian Ridge facility. It applies to all alarms of type `LEAK_DETECTED` and to humidity or pressure anomalies that may indicate a developing water event before a leak node triggers. It is intended for use by the On-Call Engineer as a step-by-step response guide and by the Avisos AI Incident Analyst as the primary retrieval source for leak-related incident analysis.

Leak events at this facility range from minor condensate overflow at the Zone Beta drain manifold to serious structural water ingress near electrical distribution equipment. The correct initial response depends on which node triggered, what correlated telemetry looks like, and whether the signal pattern fits a known cause. This runbook addresses all of those cases.

---

## Symptom and Signal Patterns

### Pattern 1 — Isolated Condensate Overflow (Zone Beta)

**Primary signal:** `LEAK_DETECTED` on B-MECH-LEAK-01
**Supporting signals:** Humidity at Zone Beta CRAH nodes elevated above 65%; no leak alarms in other zones; Zone Alpha subfloor nodes clean

This is the most common leak event at the facility. The condensate drain manifold in Zone Beta is the highest-frequency source of minor water accumulation. A slow drain line blockage allows condensate to back up and overflow the collection pan before reaching the floor sensor. In most cases the volume of water is small and self-contained.

**Distinguishing characteristics:**
- Alarm fires during periods of high cooling load or high ambient humidity
- Humidity in Zone Beta has been climbing for one to several hours before the leak node triggers
- No correlated alarms in Zone Alpha, Zone Delta, or Zone Echo

---

### Pattern 2 — Zone Alpha Subfloor Ingress (Exterior Wall)

**Primary signal:** `LEAK_DETECTED` on A-SF-LEAK-01 (north subfloor node)
**Supporting signals:** Recent heavy rainfall; Zone Alpha cold-aisle humidity elevated; no Zone Beta anomaly; temperature readings in rows A1–A3 may show slight rise as wet subfloor disrupts airflow through perforated tiles

This pattern indicates water entering through the north exterior wall of Zone Alpha. This wall has a documented history of minor water intrusion during sustained heavy rainfall. The water enters at or below grade and pools on the subfloor surface before reaching the leak sensor.

**Distinguishing characteristics:**
- Alarm coincides with or follows a significant weather event
- A-SF-LEAK-02 (center subfloor) is not yet triggered, indicating water has not spread far
- Zone Beta CRAH readings are normal, ruling out condensate as the source

---

### Pattern 3 — Spreading Subfloor Water

**Primary signal:** `LEAK_DETECTED` on A-SF-LEAK-01 followed by A-SF-LEAK-02
**Supporting signals:** Humidity rising across Zone Alpha; temperature variance increasing in rows A1–A3 as airflow from perforated tiles is disrupted; Zone Delta corridor leak node (D-TRANSIT-LEAK-01) may activate as water migrates south

This is a higher-severity version of Pattern 2. Water has entered in sufficient volume to spread from the north subfloor access point toward the center of the room. If D-TRANSIT-LEAK-01 also activates, water has crossed into the cable transit corridor and is moving toward the electrical room.

**Distinguishing characteristics:**
- Two or more leak nodes activate in sequence with a time gap of minutes to tens of minutes
- The sequence follows the expected migration path: north subfloor → center subfloor → corridor floor → corridor west end
- Humidity and temperature readings in Zone Alpha begin to deviate from normal as subfloor conditions degrade

---

### Pattern 4 — Zone Gamma Floor Drain Backflow

**Primary signal:** `LEAK_DETECTED` on G-NET-LEAK-01
**Supporting signals:** Other zone leak nodes may also be active if this is a facility-level water event; Zone Gamma network equipment readings may begin to show temperature rise if water is near rack bases

This pattern occurs during facility-level flooding events when the floor drain backflow preventer in Zone Gamma fails or is overwhelmed. Because Zone Gamma houses the MQTT broker and core switching, water intrusion here carries a compounding risk: physical damage to network hardware can suppress telemetry from all other zones, making it harder to assess the full scope of the event.

**Distinguishing characteristics:**
- G-NET-LEAK-01 activates during or after an extreme weather event or a facility-level water event already active in other zones
- Zone Gamma network telemetry quality may degrade coincidentally with or shortly after the leak alarm
- The floor drain backflow preventer was last tested at the most recent annual maintenance; if that test is overdue, treat this pattern with higher suspicion

---

### Pattern 5 — Zone Echo Exterior Wall Ingress

**Primary signal:** `LEAK_DETECTED` on E-ELEC-LEAK-01
**Supporting signals:** Recent heavy rainfall; Zone Delta corridor west-end node may also be active; no Zone Beta anomaly

This is the highest-severity leak pattern in the facility. Water near the electrical distribution panels and UPS battery arrays constitutes a life-safety event. The below-grade stairwell adjacent to Zone Echo is the primary ingress vector. Even a small volume of water at this location warrants immediate escalation.

**Distinguishing characteristics:**
- Alarm fires during or after heavy rain, particularly if the below-grade stairwell has had surface water pooling
- Zone Delta west-end leak node may have activated in the same event, confirming water is approaching from the corridor direction
- Zone Echo temperature and humidity readings may shift as outside air enters with the water

---

## Likely Causes by Zone

| Zone | Node | Most Likely Cause | Secondary Cause |
|---|---|---|---|
| Zone Beta | B-MECH-LEAK-01 | Condensate drain blockage or manifold overflow | Drain line fitting failure |
| Zone Alpha (north) | A-SF-LEAK-01 | Exterior wall water intrusion during rainfall | Subfloor pipe condensation accumulation |
| Zone Alpha (center) | A-SF-LEAK-02 | Spread from north subfloor | Zone Beta condensate migrating via shared subfloor |
| Zone Delta (west) | D-TRANSIT-LEAK-01 | Water migrating from Zone Alpha via floor grade | South exterior wall ingress |
| Zone Gamma | G-NET-LEAK-01 | Floor drain backflow during facility water event | Overhead pipe condensation above network racks |
| Zone Echo | E-ELEC-LEAK-01 | Below-grade stairwell surface water intrusion | West corridor water migration reaching electrical room |

---

## Severity Assessment

Apply the following criteria to assign or confirm the severity of a leak-related alarm. These criteria supplement the general Escalation Policy and are specific to water ingress events.

**Assign Severity 1 if any of the following are true:**
- E-ELEC-LEAK-01 is active (water near electrical distribution)
- Two or more subfloor or corridor leak nodes are active simultaneously, confirming active spread
- G-NET-LEAK-01 is active and Zone Gamma network telemetry is degrading concurrently
- An attending engineer or Facility Lead has physically confirmed visible standing water in any equipment zone

**Assign Severity 2 if any of the following are true:**
- A-SF-LEAK-01 is active and water source is not yet confirmed to be contained
- D-TRANSIT-LEAK-01 is active without a confirmed upstream source that is already controlled
- B-MECH-LEAK-01 is active and Zone Alpha subfloor nodes have not yet been confirmed clean
- Any single leak node is active in an equipment zone and the on-call engineer has not yet been able to physically assess the area

**Assign Severity 3 if all of the following are true:**
- Only B-MECH-LEAK-01 is active
- Zone Alpha and Zone Delta nodes are clean
- The alarm is consistent with Pattern 1 (isolated condensate) based on Zone Beta humidity trends
- The Facility Lead is aware and a drain inspection is being scheduled

**Do not downgrade a severity assessment based solely on the absence of corroborating alarms.** A single active leak node in a critical zone is sufficient for Severity 1 or 2 regardless of what other nodes report. Sensor positioning means water can be present without yet reaching a secondary node.

---

## Immediate Actions

The following steps apply as soon as a `LEAK_DETECTED` alarm is acknowledged. Execute them in order unless a specific pattern above directs otherwise.

**Step 1 — Identify the triggering node.**
Note the node ID, zone, and physical location from the alarm payload. Confirm which pattern from the Symptom and Signal Patterns section best fits the current alarm.

**Step 2 — Check correlated telemetry.**
Review current humidity readings for the triggering zone and adjacent zones. Check whether any other leak nodes are active or have recently transitioned. Note whether rainfall or a weather event is ongoing. Check Zone Beta humidity trend for the past two hours to distinguish condensate from structural ingress.

**Step 3 — Assess severity.**
Apply the severity criteria above. Do not wait for physical confirmation before assigning an initial severity. Err toward higher severity when uncertain.

**Step 4 — Notify per the Escalation Policy.**
For Severity 1: contact the Facility Lead by direct phone call immediately. Do not rely on automated notification alone.
For Severity 2: notify the Facility Lead within twenty minutes if root cause is not confirmed and contained.
For Severity 3: inform the Facility Lead at the next scheduled check-in.

**Step 5 — For Zone Echo alarms only.**
Do not enter Zone Echo if E-ELEC-LEAK-01 is active. Wait for the Facility Lead to assess the room and confirm it is safe. If any doubt exists about standing water near the distribution panel, treat this as a life-safety event and follow the Facility Lead's direction on whether to de-energize circuits.

**Step 6 — Document the start of the incident.**
Record the time the alarm fired, the time it was acknowledged, the initial severity assessment, and who was notified. This record becomes the basis for the incident log entry.

---

## Verification Steps

Once immediate notifications are complete, the On-Call Engineer verifies the alarm and scopes the event. Complete these steps before closing or downgrading an alarm.

**Verify the node is functioning correctly.**
Check node signal quality and battery level. A node with a weak signal or low battery that also reports a leak may be producing a degraded reading. If signal quality is below acceptable threshold, note this as a false positive risk factor but do not dismiss the alarm until physical inspection confirms the area is dry.

**Check upstream nodes.**
For any active leak node, review whether the node upstream in the expected migration path has also triggered or is approaching threshold. Upstream nodes that are clean support a contained or early-stage assessment. Upstream nodes that are also triggered or trending upward support an active spread assessment.

**Correlate with weather data.**
For exterior wall ingress patterns (A-SF-LEAK-01, D-TRANSIT-LEAK-01, G-NET-LEAK-01, E-ELEC-LEAK-01), cross-reference with current weather conditions. Rainfall events are a significant context factor. Note weather conditions in the incident record.

**Request physical inspection.**
Remote telemetry confirms that a node has triggered; it does not confirm the volume, source, or direction of water. A physical inspection by the Facility Lead or On-Call Engineer (where safe to do so) is required to fully scope the event. Document the inspection findings in the incident record.

**Confirm Zone Beta drain status.**
For any leak event in Zone Alpha subfloor or Zone Delta, confirm whether the Zone Beta condensate drain manifold is functioning normally. A drain backup in Zone Beta can contribute to or cause subfloor moisture in Zone Alpha through the shared subfloor boundary.

---

## False Positive Considerations

Not every `LEAK_DETECTED` alarm represents a significant water event. The following known conditions can produce false or low-significance alarms.

**Condensation on cold surfaces near a leak node.**
During high-humidity periods, condensation can form on metal subfloor supports, pipe exteriors, or CRAH unit frames near a leak sensor. This condensation may drip onto or near the sensor without constituting an active water ingress event. Physical inspection distinguishes condensation drips from pooling water.

**Post-maintenance residual moisture.**
After any maintenance activity involving cooling equipment, drain lines, or subfloor access, residual moisture may trigger a leak node. If a maintenance window was recently completed, note this as a likely cause and verify with the attending technician before treating the alarm as an independent event.

**High ambient humidity without a water source.**
In rare conditions during extreme outdoor humidity, the Zone Beta ambient node or CRAH intake nodes may read near or above threshold without any liquid water present. If the leak node reading is borderline and all surrounding humidity readings are elevated but stable, and no visual confirmation of water is found, document as a possible humidity-induced trigger and monitor closely.

**Node sensitivity drift over time.**
Leak detection nodes at this facility use resistive moisture sensors. These sensors can develop sensitivity drift if exposed to mineral deposits from previous minor condensation events. A node with a history of repeated low-level moisture exposure may trigger at lower actual moisture levels than its calibration intends. If a specific node has a history of repeated borderline alarms, flag this to the Facility Lead for hardware inspection or sensor replacement.

**In all cases, a false positive assessment requires physical confirmation that the area is dry. Do not dismiss a leak alarm based on telemetry alone.**

---

## When to Escalate

Escalate beyond the On-Call Engineer in the following situations.

**Escalate to Facility Lead immediately:**
- Any Zone Echo leak alarm (E-ELEC-LEAK-01)
- Any two-node simultaneous or sequential leak pattern confirming spread
- Physical inspection confirms standing water in any equipment zone
- Source of water cannot be identified within thirty minutes of initial alarm

**Escalate to Infrastructure Lead:**
- Zone Gamma leak alarm is active and network telemetry degradation is concurrent
- A water event is assessed as likely to cause equipment damage or service interruption

**Escalate to Operations Supervisor:**
- Any Severity 1 water event
- Any event expected to require equipment shutdown or significant service disruption

**Escalate to External Contractor (via Facility Lead):**
- Source is confirmed to be structural (exterior wall, roof, floor penetration, building plumbing)
- Drain line blockage cannot be cleared with facility tools and resources
- Any Zone Echo event that requires electrical work beyond de-energizing a circuit breaker

**Escalate to Emergency Services (via Facility Lead):**
- Standing water is confirmed adjacent to or in contact with the Zone Echo distribution panel
- Any situation where an attending person assesses an immediate personal safety risk

---

## What the AI Incident Analyst Should Include in a Summary

When the Avisos AI Incident Analyst generates a summary for a water ingress event, the summary should address the following points using retrieved telemetry and document context.

**Triggering node and location.** Name the node ID, its zone, and its physical position within that zone (subfloor, floor level, manifold area, exterior wall base). Explain what that location means in terms of likely water source.

**Signal pattern match.** Identify which pattern from this runbook the current alarm most closely matches. State the pattern name and explain why the telemetry fits it. If the pattern is ambiguous, name the two most likely candidates and explain the distinguishing factor that would differentiate them.

**Correlated telemetry summary.** Describe the humidity and temperature context in the triggering zone and any adjacent zones. Note whether other leak nodes are active or were recently active.

**Severity recommendation.** State the recommended severity level with a one-sentence justification grounded in the criteria above.

**Immediate action recommendation.** List the next two or three actions the On-Call Engineer should take, in priority order, drawn from the Immediate Actions section of this runbook.

**Escalation recommendation.** State which roles should be notified based on the current severity and pattern, citing the Escalation Policy.

**False positive note.** If the telemetry pattern is consistent with a known false positive condition, note that as a possibility and state what physical verification would confirm or rule it out.

**Draft notification.** Provide a draft notification message for the appropriate recipient role, using the wording guidelines in the Escalation Policy. Label the draft clearly as a suggested starting point requiring human review before sending.

---

## Sample Internal Notification Drafts

The following drafts are style references and starting points. They must be reviewed and edited by the On-Call Engineer before sending. Do not send AI-generated drafts without human review.

---

**Draft A — Severity 1, Zone Echo Leak (Facility Lead notification)**

> SEVERITY 1 ALARM — Meridian Ridge Facility
> Zone: Echo — Electrical Distribution Room
> Node: E-ELEC-LEAK-01
> Time detected: [TIME]
> Duration active: [DURATION]
>
> Leak detection alarm active at the base of the Zone Echo exterior wall. Water near electrical distribution panels and UPS battery arrays. This is a life-safety condition.
>
> Action required: Immediate physical assessment of Zone Echo. Do not enter the room alone. Do not energize or de-energize circuits without confirming safety.
>
> On-Call Engineer is standing by. Awaiting your direction on whether to initiate controlled shutdown procedure.

---

**Draft B — Severity 2, Zone Alpha Subfloor Spreading (Facility Lead notification)**

> SEVERITY 2 ALARM — Meridian Ridge Facility
> Zone: Alpha — Subfloor
> Nodes active: A-SF-LEAK-01, A-SF-LEAK-02
> Time of first alarm: [TIME]
> Time of second alarm: [TIME]
>
> Two subfloor leak nodes have activated in sequence. Water appears to be spreading from the north end of Zone Alpha toward the center of the room. Zone Delta corridor node is currently clean but is being monitored.
>
> Active compute racks in rows A1–A3 are in the affected area.
>
> Requesting physical inspection of Zone Alpha subfloor access points. On-Call Engineer has notified Infrastructure Lead. Condensate source in Zone Beta has been checked and is not contributing.

---

**Draft C — Severity 3, Zone Beta Condensate (Facility Lead awareness)**

> SEVERITY 3 ALARM — Meridian Ridge Facility
> Zone: Beta — Cooling Plant
> Node: B-MECH-LEAK-01
> Time detected: [TIME]
>
> Condensate leak alarm at the Zone Beta drain manifold. Pattern consistent with partial drain line blockage. Zone Alpha subfloor nodes are clean. No downstream spread detected.
>
> No immediate equipment risk identified. Requesting drain inspection at next available opportunity during business hours.
>
> Will continue monitoring. Will escalate if Zone Alpha subfloor nodes activate or if Zone Beta humidity continues to rise.