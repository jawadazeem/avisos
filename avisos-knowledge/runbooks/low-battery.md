# Runbook — Low Battery and Power Degradation Alarms

## Document Purpose

This runbook provides triage and response procedures for `BATTERY_LOW` and `BATTERY_CRITICAL` alarms raised by the Avisos controller service. It covers normal battery behavior, threshold interpretation, likely causes by zone, the relationship between power state and other telemetry degradation, corrective actions, and maintenance guidance. It also defines what the AI Incident Analyst should include when generating a response to a battery-related alarm.

---

## Normal Battery Expectations

Avisos sensor nodes are powered primarily from zone sub-panels via low-voltage DC supply rails. Each node contains an integrated lithium-iron-phosphate (LiFePO4) backup cell that maintains node operation during brief supply interruptions and buffers against rail voltage sag. Under normal conditions, nodes operate continuously from the supply rail and the backup cell remains at or near full charge.

Expected battery levels under normal operating conditions are as follows.

| Condition | Expected Battery Level |
|---|---|
| Node on healthy supply rail | 95–100% |
| Node on rail with minor voltage sag | 80–94% |
| Node operating on backup cell only | Declining from last charged level |
| Node recovering after supply restoration | Climbing toward 95–100% over 2–4 hours |

Battery level is reported as a percentage in each telemetry payload. The controller service evaluates the reported level against the thresholds below and raises alarms accordingly. Battery level readings are sampled at the same interval as other vitals and are subject to the same signal quality constraints; a node with degraded signal may report stale battery values.

Nodes in Zone Echo are powered from a dedicated sensor infrastructure sub-panel rather than the general zone supply. Battery behavior in Zone Echo should be interpreted in that context; see the Zone Echo notes in the Datacenter Layout document.

---

## Alarm Thresholds

| Alarm Type | Trigger Condition | Default Severity |
|---|---|---|
| `BATTERY_LOW` | Battery level falls below 40% | Severity 3 — Moderate |
| `BATTERY_CRITICAL` | Battery level falls below 15% | Severity 2 — High |
| `BATTERY_CRITICAL` with node offline risk | Battery level below 15% and signal degraded | Severity 2 — High (consider upgrade to Severity 1 if zone is critical) |

Thresholds may be adjusted per node group in the controller service configuration. Check the current threshold settings before assuming a reading is anomalous relative to the defaults above.

A `BATTERY_LOW` alarm that is not acknowledged and does not recover within two hours should be manually reviewed for escalation to Severity 2, even if the level has not crossed the critical threshold.

---

## Likely Causes

### Supply Rail Voltage Sag

The most common cause of `BATTERY_LOW` alarms across multiple nodes in the same zone is a voltage sag on the supply rail serving that zone. When rail voltage drops below the node's charging threshold, the backup cell begins to discharge to compensate. The node continues operating normally from the combined supply, but battery level declines steadily.

Rail voltage sag is most likely during peak load periods, after a partial circuit failure in Zone Echo, or following a UPS switchover event. If multiple nodes in the same zone report low battery simultaneously, check Zone Echo panel status and UPS output before investigating individual nodes.

### Supply Rail Failure or Tripped Breaker

A fully interrupted supply to a node group will cause all affected nodes to begin drawing down their backup cells immediately. Battery levels will decline at a rate determined by the node's power consumption profile, typically 3–8 percentage points per hour under normal telemetry load. Nodes will reach critical battery levels within approximately four to six hours of supply loss and will go offline as cells deplete.

A tripped breaker in a zone sub-panel is the most common cause of supply interruption. The Facility Lead can inspect and reset sub-panel breakers within Zone Echo.

### Individual Node Hardware Degradation

An isolated `BATTERY_LOW` alarm on a single node with no correlated rail events suggests the node's backup cell is degrading. LiFePO4 cells have a finite cycle life. Nodes that have been deployed for more than two years or that have been through repeated deep discharge events may show reduced capacity. A degraded cell may report a level that declines faster than expected under backup operation or that never fully recovers to 95% even after extended time on a healthy rail.

### Charging Circuit Fault

In some cases, the node's onboard charging circuit fails while the supply rail remains healthy. The node operates normally from the rail but the backup cell is no longer being recharged. Battery level will decline slowly even while the node appears powered, and the first indication may be a `BATTERY_LOW` reading during a period when no supply interruption is known to have occurred. This fault requires node replacement; it cannot be resolved in the field.

### Environmental Factors Increasing Power Draw

Nodes operating in extreme temperature conditions outside their rated range draw more power to maintain sensor operation. Sustained high temperatures in a zone (Zone Alpha rows A4–A6, Zone Delta corridor) can marginally increase node power consumption. This is a minor contributing factor in most cases but can accelerate battery drain in a node already operating on a degraded cell.

---

## Relationship Between Battery Level and Signal Quality

Battery state and signal quality are operationally linked. As a node's battery level declines, the RF transmitter is among the first subsystems the node's power management firmware deprioritizes to extend operating time. The result is a predictable pattern of co-degradation.

**Typical progression as battery depletes:**

1. Battery level begins declining; telemetry payloads remain normal frequency and quality
2. Battery level crosses 40%; `BATTERY_LOW` alarm raised; signal quality may begin to show minor degradation
3. Battery level crosses 25%; transmit power is reduced by firmware; `SIGNAL_DEGRADED` alarm may fire
4. Battery level crosses 15%; `BATTERY_CRITICAL` alarm raised; telemetry interval may extend as the node attempts to conserve power
5. Battery level crosses 5%; node enters minimum-operation mode; telemetry becomes infrequent or stops entirely
6. Battery depleted; node goes offline; `NODE_OFFLINE` alarm fires

When a `SIGNAL_DEGRADED` alarm and a `BATTERY_LOW` or `BATTERY_CRITICAL` alarm are active simultaneously on the same node, treat the signal degradation as a symptom of the power condition rather than an independent RF problem. Do not dispatch for signal investigation until the power issue is resolved or ruled out.

Conversely, if `SIGNAL_DEGRADED` fires on a node with no battery alarm and no known rail event, the signal issue is more likely to have a network or RF cause and should be investigated separately.

---

## Immediate Actions

### Step 1 — Assess Scope

Identify whether the battery alarm is isolated to a single node or affects multiple nodes. Use the Avisos Web Console to filter active alarms by zone. If two or more nodes in the same zone are reporting low battery simultaneously, treat the event as a likely rail or panel issue and proceed to Step 3 before investigating individual nodes.

### Step 2 — Check for Correlated Alarms

Review the active alarm list for any of the following conditions active in the same time window as the battery alarm.

- `NODE_OFFLINE` on any node in the same zone
- `SIGNAL_DEGRADED` on the affected node or neighboring nodes
- Any Zone Echo temperature or leak alarm
- Any alarm flagged as related to a maintenance window or recent configuration change

Correlated alarms provide root cause context and may change the escalation path.

### Step 3 — Check Zone Echo Panel Status

For any battery alarm affecting multiple nodes in the same zone, or any `BATTERY_CRITICAL` alarm on a single node, the on-call engineer should verify Zone Echo sub-panel status. If physical access to Zone Echo is not available, request that the Facility Lead perform a visual check of the sub-panel breakers and UPS output indicators.

Document what is found, including which breakers are on, which if any are tripped, and the current UPS status display readings.

### Step 4 — Attempt Remote Node Diagnostics

If the node is still online and reporting telemetry, review the last several telemetry payloads for the affected node in the controller service logs. Note the rate of battery decline, whether the level is stable or actively dropping, and whether any other vitals show anomalies. A stable low reading (node holding at 35% without further decline) suggests the node is on a partially degraded rail but is not in immediate danger of going offline. An actively declining reading requires faster response.

### Step 5 — Dispatch Physical Inspection

For any `BATTERY_CRITICAL` alarm, or any `BATTERY_LOW` alarm that has not stabilized after two hours, dispatch the on-call engineer or a site-present operator to physically inspect the affected node and its power supply connection. During inspection, verify the following.

- Supply cable is seated and not visibly damaged
- Node indicator lights match expected behavior for battery state
- No visible physical damage to the node enclosure
- Rack PDU or wall outlet supplying the node is active
- No recent cabling work in the zone that may have disturbed the supply run

### Step 6 — Restore Supply or Replace Node

If a supply fault is found, restore the circuit under Facility Lead oversight. Do not reset Zone Echo breakers without Facility Lead authorization. If the node hardware is assessed as faulty (degraded cell, charging circuit fault, physical damage), place the node in a maintenance state in the controller service and arrange for replacement per the node replacement criteria below.

---

## Maintenance Recommendations

**Quarterly:**
Check the battery level trend history for all nodes via the controller service telemetry log. Any node that has shown a `BATTERY_LOW` event in the past 90 days should be flagged for inspection during the next site visit, even if it recovered without intervention.

**Annually:**
Nodes that have been deployed for more than 24 months should undergo a battery capacity verification during a scheduled maintenance window. A node that does not reach 90% charge within four hours of being confirmed on a healthy supply rail is a candidate for cell replacement or full node replacement.

**After any deep discharge event:**
Any node that reached a critically low battery level (below 10%) should be monitored closely for the following 72 hours. LiFePO4 cells that have experienced deep discharge may show reduced capacity or erratic reporting in subsequent cycles.

**After UPS switchover or power event:**
Following any facility-level power event, review battery levels for all nodes within 30 minutes of power restoration. Nodes that do not begin recovering toward 95% within two hours of supply restoration should be flagged for inspection.

---

## Node Replacement Criteria

A node should be scheduled for replacement under any of the following conditions.

- Battery level does not recover above 70% within four hours of confirmed healthy supply restoration
- Node has experienced three or more `BATTERY_CRITICAL` events within a 90-day period without a confirmed external power cause
- Physical inspection reveals cell swelling, enclosure damage, or connector corrosion on the power input
- Node telemetry shows a charging circuit fault pattern: level declining while on a confirmed live rail
- Node is more than 36 months old and has a documented history of battery events, even if currently stable
- The node's battery level is consistently reported at an implausible fixed value (a stuck sensor reading), indicating a failed battery monitoring circuit

Node replacements should be logged in the Maintenance Change Log with the date, zone, node ID, reason for replacement, and the ID of the replacement unit.

---

## What the AI Incident Analyst Should Include in Its Response

When the Avisos AI Incident Analyst is invoked for a battery-related alarm, its response should address the following elements using retrieved context from this runbook and the relevant zone and equipment documents.

**Alarm interpretation:**
State what the battery level reading means in plain language. Note whether the level is at the warning or critical threshold and whether it is stable or declining based on available telemetry history. Note the node's zone and any known environmental or power context for that zone.

**Scope assessment:**
State whether the alarm is isolated to a single node or whether other nodes in the same zone or facility are showing related conditions. Identify any correlated alarms (signal degradation, node offline, Zone Echo events) and explain their likely relationship to the battery condition.

**Likely cause:**
Based on the number of affected nodes and correlated alarms, identify the most probable cause from the likely causes listed in this runbook. State the confidence level plainly (for example, "consistent with a rail voltage event" versus "cause unclear; individual node fault possible").

**Recommended actions:**
List the immediate actions from this runbook in order, tailored to the specific alarm conditions. Do not recommend actions that require physical access to Zone Echo without noting that Facility Lead authorization is required.

**Escalation recommendation:**
State the current severity and whether conditions warrant escalation. If the battery alarm is co-occurring with a critical-zone dependency event, recommend severity upgrade and identify the role to notify.

**Draft notification:**
Produce a candidate notification message using the format described below. Label the draft clearly as a candidate for human review before sending.

---

## Sample Notification Draft

The following is an example of an AI-generated candidate notification for a `BATTERY_CRITICAL` alarm. This format should be adapted to the specific node, zone, and conditions at the time of the incident.

---

**CANDIDATE DRAFT — Requires human review before sending**

**To:** On-Call Engineer
**Severity:** 2 — High
**Alarm:** BATTERY_CRITICAL
**Node:** A-A5-ENV-02
**Zone:** Alpha, Row A5

Battery level on node A-A5-ENV-02 has dropped to 11% and is continuing to decline. Two neighboring nodes in Zone Alpha (A-A4-ENV-01 and A-A6-ENV-01) are also reporting below 40%, which is consistent with a supply rail event rather than an isolated node fault.

No Zone Echo alarms are currently active. Signal quality on the affected nodes remains within acceptable range but is showing early degradation consistent with low-battery transmit reduction.

**Recommended actions:**
1. Request Facility Lead visual inspection of the Zone Alpha sub-panel in Zone Echo.
2. Review UPS-A output status; UPS-A serves Zone Alpha.
3. Do not treat signal degradation on these nodes as an independent issue until power is restored.
4. If supply is confirmed healthy and battery levels continue to decline, dispatch for physical node inspection in rows A4–A6.

Node A-A5-ENV-02 is estimated to go offline within two to three hours at the current discharge rate if supply is not restored.

**This draft has not been sent. Review content, verify accuracy against current telemetry, and approve before forwarding.**

---

*Runbook owner: Infrastructure Lead. Review cycle: semi-annual or following any Severity 1 battery-related event.*