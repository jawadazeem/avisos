# Runbook — Camera Obstruction and Visual Anomaly Alarms

## Document Purpose

This runbook provides triage and response procedures for camera-related alarms raised by the Avisos controller service when a vision analysis service returns labels indicating obstruction, unexpected objects, environmental hazards, or unclassified anomalies. It covers symptom interpretation, confidence score guidance, security versus maintenance classification, verification steps, false positive patterns, and rules for AI-generated summaries of visual alarm events.

Avisos nodes equipped with imaging capability submit periodic frame captures to a locally hosted vision service. The vision service returns a set of labels, each accompanied by a confidence score expressed as a percentage. The controller service evaluates returned labels against a configured watch list and raises alarms when a label match exceeds the minimum confidence threshold defined in the node group configuration.

This runbook applies to all camera-equipped nodes in the Meridian Ridge facility. Not all nodes carry imaging hardware; consult the Node Catalog to confirm whether a specific node ID includes a camera module.

---

## Symptom Reference

The following symptoms indicate a camera obstruction or visual anomaly alarm condition. One or more may be present simultaneously.

**Obstruction symptoms:**
- Vision service returns an `obstruction` or `covered` label with confidence above threshold
- Image analysis returns no labels at all on a node that previously returned consistent labels, suggesting the camera lens is fully blocked
- Label confidence scores drop uniformly across all categories in a single frame capture, indicating degraded image quality rather than a specific object detection

**Unexpected object symptoms:**
- Labels such as `person`, `tool`, `equipment`, `cable`, `bag`, or `container` appear in a zone where human presence is not expected at the time of the capture
- Labels such as `water`, `liquid`, or `puddle` appear in a zone where no leak alarm is currently active
- A label designated as `unknown object` or `unclassified` appears with confidence above 60%

**Environmental hazard symptoms:**
- Labels such as `smoke`, `haze`, `dust`, or `particulate` appear in any equipment zone
- `Fire` or `flame` label returned at any confidence level above the minimum threshold; treat as Severity 1 immediately regardless of score

**Visual degradation symptoms:**
- Returned labels include `blur`, `low light`, `overexposure`, or similar image quality descriptors
- Label set is significantly different from the established baseline for that node's location and time of day
- Frame capture returns an error or empty payload from the vision service

---

## Common Causes

### Physical Obstruction of Camera Lens

The most frequent cause of obstruction alarms. A label, cable tie, piece of tape, sticker, or other object has been placed directly on or immediately in front of the camera lens. This may be accidental (a technician placing a work tag on the nearest surface) or deliberate. The vision service will typically return low-confidence labels or an `obstruction` label. Image quality descriptors will score high while content labels score low or return empty.

### Condensation or Moisture on Lens

In zones with elevated humidity or near cooling equipment, condensation can form on the node enclosure lens cover. This produces a diffuse, low-contrast image. The vision service may return `blur` or `haze` labels. Condensation obstruction events tend to resolve on their own as ambient conditions stabilize, but a persistent condensation event suggests the node is experiencing humidity outside its rated operating range.

### Dust or Particulate Accumulation

Nodes deployed in the Zone Delta corridor or near cable tray fill points may accumulate dust on the lens cover over time. Dust obstruction produces a gradual degradation of image quality across multiple capture intervals rather than a sudden change. The vision service may return `particulate` or `dust` labels or simply show declining confidence across all object categories over days or weeks.

### Technician or Authorized Personnel Presence

An engineer or contractor working in a zone will produce a `person` label in frame captures taken during that activity. This is the most common source of unexpected-person labels and is almost always a false alarm in the security context. However, it must be cross-referenced against the maintenance schedule before being dismissed.

### Environmental Water Visible in Frame

A `water` or `liquid` label may appear in frame captures before a corresponding `LEAK_DETECTED` alarm fires, particularly if the water is visible to the camera but has not yet reached the leak detection node's sensor position. This is a useful early indicator and should prompt a check of adjacent leak nodes rather than being dismissed as a vision artifact.

### Smoke or Airborne Particulate Event

A `smoke` or `haze` label returned from an equipment zone is a high-priority alarm condition. Sources include equipment overheating, an electrical arc event, or material combustion. Do not assume a `smoke` label is a false positive without physical verification. This condition escalates to Severity 1 if the label confidence exceeds 50% or if it is returned on two consecutive frame captures.

### Lighting Changes

Equipment zones are not continuously lit. Lights may be switched off between site visits or may cycle on motion-detection timers. A node capturing a frame in low-light conditions may return `low light` or `dark environment` labels or produce unexpected object detections as the vision model attempts to classify shapes in poor illumination. Compare the capture timestamp against the facility lighting schedule before assessing an unexpected label as substantive.

### Vision Service Processing Error

Occasionally the vision service returns malformed, empty, or error payloads. The controller service logs the raw response alongside the alarm. If the alarm is accompanied by a service error in the log rather than a label set, treat it as a technical fault rather than a genuine visual anomaly and investigate the vision service health.

---

## Interpreting Confidence Scores and Repeated Labels

### Confidence Score Ranges

Confidence scores from the vision service are not certainty ratings. They reflect the model's statistical assessment of label fit given the image content. The following ranges provide practical interpretation guidance for Avisos alarm triage.

| Score Range | Interpretation |
|---|---|
| 90–100% | High confidence; treat as a reliable detection for common labels in clear images |
| 70–89% | Moderate-high confidence; substantive but warrants verification before action |
| 50–69% | Moderate confidence; possible detection; consider image quality and context before escalating |
| 30–49% | Low confidence; treat as a weak signal; correlate with other alarms before acting |
| Below 30% | Very low confidence; likely noise or image quality artifact; log and monitor for recurrence |

The minimum confidence threshold for raising an alarm is configured per node group. Default thresholds are 60% for object and person labels, 50% for environmental hazard labels, and 40% for `fire` or `flame` labels. Operators may adjust these thresholds in the controller service configuration.

### Labels That Override Confidence Scoring

The following labels trigger immediate escalation regardless of confidence score, because the cost of ignoring a low-confidence true positive is unacceptable.

- `fire`
- `flame`
- `smoke` (when returned on two or more consecutive captures)
- Any label returned simultaneously with a `LEAK_DETECTED` alarm in the same zone

### Repeated Labels Across Captures

A label that appears in a single frame capture is a single data point. A label that appears across multiple consecutive captures is a pattern and should be weighted significantly more heavily. The controller service tracks label recurrence per node and flags repeated detections in the alarm metadata.

**Interpretation guidance for repeated labels:**

- Same label at similar confidence across three or more captures: treat as a persistent condition; physical verification is warranted regardless of confidence score
- Label appearing then disappearing then reappearing: suggests an intermittent condition (a person moving through frame, water pooling and partially draining, condensation cycling); document the pattern and review capture timestamps
- Confidence score rising across successive captures for the same label: the condition is intensifying; escalate accordingly
- Confidence score declining across successive captures for the same label: condition may be resolving; continue monitoring but defer physical dispatch if no other alarms are correlated

---

## When to Treat as Security Versus Maintenance

The distinction between a security event and a maintenance event determines notification path and response urgency. Apply the following criteria.

### Treat as a Security Event When:

- A `person` label is detected outside scheduled maintenance hours with no active work order for the affected zone
- A `person` label is detected in a restricted zone (Zone Echo or Zone Alpha rack rows) without a corresponding badge access record
- An `obstruction` label appears suddenly on a node that was reporting normally, with no maintenance activity scheduled; deliberate camera covering is a physical security indicator
- Any label combination suggesting unauthorized equipment introduction (`bag`, `container`, `unknown object`) in a restricted zone outside business hours
- Multiple camera nodes in the same zone return unexpected labels simultaneously without a corresponding maintenance event

**Security event response:** Notify the Security Officer immediately as a parallel action alongside normal alarm triage. Do not wait for physical verification before notifying. Preserve the frame capture images and alarm log entries as they may be needed for a security review.

### Treat as a Maintenance Event When:

- A `person` label appears during a confirmed scheduled maintenance window for that zone
- A `tool` or `equipment` label is present alongside a `person` label during business hours
- An `obstruction` label appears immediately following a logged maintenance visit, suggesting a work item was left near the node
- A `cable` label appears after a known cabling change in the zone
- Image quality degradation labels (`blur`, `dust`, `low light`) appear without any object or person detection

**Maintenance event response:** Log the event, correlate with the maintenance schedule, and assign a follow-up item to the on-site team to inspect and clear the obstruction or anomaly during the next site visit. Notify the On-Call Engineer for awareness; no immediate Security Officer involvement is required unless other indicators suggest unauthorized access.

---

## Verification Steps

### Step 1 — Retrieve Frame Capture

Retrieve the frame capture associated with the alarm from the controller service log. Review the image directly before taking any action. Many alarm conditions are immediately interpretable from the image: an obstruction is visible, a person is clearly a technician in a work vest, condensation is evident on the lens. Human visual review of the capture is always the first verification step and frequently resolves ambiguity that confidence scores alone cannot.

### Step 2 — Review Capture Metadata

Check the capture timestamp, the node ID, and the zone against the following sources.

- Current maintenance schedule: is a work order active for this zone at this time?
- Badge access log: was an entry recorded for this zone near the capture time?
- Other active alarms: is a correlated alarm (leak, temperature, power) active in the same zone?
- Prior captures from this node: what did the last several frames show, and does the current frame represent a significant change?

### Step 3 — Check Adjacent Node Telemetry

If the visual alarm may indicate a physical environmental condition (water visible, smoke, haze), immediately check the telemetry from adjacent environmental and leak detection nodes in the same zone. A camera detecting water before the leak node confirms it is a useful early warning. A camera detecting haze with no corresponding temperature rise narrows the possible causes.

### Step 4 — Dispatch Physical Inspection

Physical verification is required for any of the following conditions.

- `fire` or `smoke` label at any confidence level
- `person` label outside scheduled hours without badge record
- `obstruction` label with no maintenance activity logged
- `water` or `liquid` label without a corresponding leak alarm, to determine whether the condition is real and the leak node simply has not yet been reached
- Any high-confidence label that cannot be explained by maintenance, lighting, or image quality factors

During physical inspection, the engineer should photograph the node and surrounding area, note whether any obstruction is present and its likely source, and clear any accidental obstruction before departing. Any suspected deliberate obstruction must not be removed until the Security Officer has been notified and has authorized preservation or collection.

### Step 5 — Update Alarm Status

After verification, update the alarm record in the controller service with the verification finding and disposition. Close the alarm if the condition is resolved. Flag for follow-up if the cause is identified but not yet corrected. Escalate if physical inspection reveals a condition worse than the alarm initially indicated.

---

## False Positive Patterns

The following patterns produce frequent false positive visual alarms at Meridian Ridge and should be recognized during triage to avoid unnecessary escalation.

**Lighting cycle transitions:** Frame captures taken in the first 30–60 seconds after the facility motion-sensing lights activate from a dark state often produce low-quality images with unexpected label returns. The vision model may label ceiling-mounted equipment, overhead cable trays, or indicator lights as `unknown object`. This pattern is identifiable by the low-light or transitional exposure quality of the capture and the absence of any correlated alarms.

**Reflective surfaces near Zone Beta CRAH units:** The polished enclosures of the Stalwart CR-9 units produce reflections that the vision model occasionally labels as `person` or `figure` at low confidence. This is a known artifact of the node placement in Zone Beta. Frame captures from B-MECH-ENV nodes showing a `person` label below 65% confidence during off-hours should be correlated with badge access before escalation.

**Cable bundle movement:** High-voltage cable bundles in the Zone Delta corridor can sway slightly when large equipment is moved nearby or when the building HVAC cycles. A moving cable bundle may produce a `cable` label at moderate confidence alongside an `unknown object` label. This is a corridor-specific false positive and is distinguishable from a genuine anomaly by the absence of any badge or maintenance activity.

**Seasonal temperature contrast:** During winter months, the exterior wall in Zone Alpha's north section generates a visible thermal gradient near the subfloor access panel. Frame captures near this area may produce `haze` or `blur` labels due to the visible air movement from temperature differential. This is a seasonal baseline condition, not a smoke or environmental event.

**Post-maintenance recovery period:** For approximately 30–60 minutes after a maintenance visit, nodes in the visited zone may capture residual visual anomalies: an open rack panel, a tool bag not yet fully removed from frame, a propped door changing the lighting angle. These are expected in the 60-minute window following a logged maintenance visit and should be treated with a lower escalation threshold during that period.

---

## What the AI Incident Analyst Should and Should Not Claim

When the Avisos AI Incident Analyst generates a summary for a camera obstruction or visual anomaly alarm, it must operate within the following boundaries.

### The AI Should:

- State the label or labels returned by the vision service, the associated confidence scores, and the number of consecutive captures on which the label appeared
- Identify the node, zone, and physical location context for the alarm
- Cross-reference the capture timestamp against maintenance schedule context if available
- Note any correlated alarms in the same zone and explain their relationship to the visual alarm
- Apply the false positive pattern guidance to assess whether the alarm fits a known benign pattern
- Recommend whether the event should be treated as a security event or a maintenance event, with reasoning
- List recommended verification steps from this runbook in order of priority
- Draft a candidate notification message

### The AI Must Not:

- Claim to identify a specific individual from a frame capture
- Assert that a `person` label confirms unauthorized access without badge record corroboration
- Claim that a `smoke` or `fire` label confirms an active fire without physical verification
- Dismiss a `smoke`, `fire`, or `water` label as a false positive without stating the basis for that assessment explicitly
- State a definitive root cause as confirmed fact when the evidence is a confidence score below 80%
- Recommend that a deliberate obstruction be physically removed before Security Officer notification
- Include the raw image, frame capture path, or any internal file system reference in an externally directed notification draft

### Confidence Language the AI Should Use

The AI should match its language certainty to the confidence score and recurrence count. The following phrasing guide applies.

| Condition | Appropriate phrasing |
|---|---|
| Single capture, score below 50% | "A possible detection of [label] was returned; confidence is low and physical verification is recommended before treating this as confirmed." |
| Single capture, score 50–79% | "The vision service returned a [label] detection at moderate confidence. Correlated alarms and maintenance records should be reviewed before escalating." |
| Single capture, score 80%+ | "A high-confidence [label] detection was returned. Physical verification is recommended promptly." |
| Repeated label, any score | "The [label] label has appeared on [N] consecutive captures. Recurring detections increase the likelihood of a genuine condition regardless of individual confidence scores." |
| `fire` or `smoke`, any score | "A [fire/smoke] label was returned. This requires immediate physical verification regardless of confidence score." |

---

## Sample Notification Draft

The following is an example of an AI-generated candidate notification for a `person` label alarm outside scheduled hours. This format should be adapted to the specific label, node, zone, and conditions active at the time of the incident.

---

**CANDIDATE DRAFT — Requires human review before sending**

**To:** On-Call Engineer, Security Officer
**Severity:** 2 — High (Security evaluation required)
**Alarm:** Visual Anomaly — Unexpected Person Detection
**Node:** A-A2-CAM-01
**Zone:** Alpha, Row A2 Cold Aisle
**Capture time:** 02:14 local

The vision service returned a `person` label at 81% confidence on node A-A2-CAM-01 at 02:14. The label appeared on two consecutive frame captures separated by approximately 90 seconds. No maintenance window is active for Zone Alpha at this time.

Badge access records for Zone Alpha have not been reviewed at the time of this draft. This review is a required verification step before escalation determination.

No correlated temperature, leak, or power alarms are active in Zone Alpha. Signal quality on A-A2-CAM-01 is within normal range.

**Recommended actions:**
1. Review Zone Alpha badge access log for entries between 01:45 and 02:30.
2. Retrieve and visually inspect the frame captures from A-A2-CAM-01 at 02:14 and 02:16.
3. Notify Security Officer for awareness pending badge log review.
4. If no authorized access record exists, dispatch for physical inspection of Zone Alpha per Security Officer direction.
5. Do not dismiss this alarm as a false positive until badge records have been confirmed negative.

**This draft has not been sent. Verify badge access records and confirm Security Officer notification before forwarding.**

---

*Runbook owner: Infrastructure Lead and Security Officer (joint). Review cycle: semi-annual or following any confirmed unauthorized access event.*