# Escalation Policy — Avisos Alarm Response

## Document Purpose

This document defines the alarm escalation policy for the Avisos monitoring platform at the Meridian Ridge facility. It establishes severity levels, notification responsibilities by role, expected response times, escalation triggers, and guidelines for alarm-related communications. It applies to all alarms raised by the avisos-controller-service, whether acknowledged by an on-site operator or received by the on-call engineer after hours.

The Avisos AI Incident Analyst may use this document to recommend an appropriate escalation path and draft notification messages. All externally directed communications and any message that names individuals, vendors, or regulatory bodies must be reviewed and approved by a human before sending.

---

## Severity Levels

Avisos uses four severity levels. Every alarm raised by the controller service is assigned one of these levels based on the alarm type and the conditions defined in the individual alarm definition documents. The severity may be upgraded during active triage if conditions worsen or if multiple alarms are found to share a root cause.

---

### Severity 1 — Critical

**Definition:**
A condition that poses an immediate risk to life safety, threatens the structural integrity of facility systems, or has a high probability of causing unrecoverable data loss or complete service outage within minutes to hours if not addressed.

**Criteria (any one is sufficient):**
- Leak detected in Zone Echo (electrical distribution room)
- Leak detected in Zone Alpha subfloor confirmed to be spreading (multiple leak nodes active)
- Temperature at UPS battery arrays exceeding 85°F (29°C)
- Simultaneous loss of both CRAH units in Zone Beta
- Complete loss of MQTT telemetry from all zones with no known maintenance window
- Any alarm condition that an attending engineer assesses as an immediate physical danger

**Expected response time:**
Immediate. The on-call engineer must acknowledge within five minutes of notification. Physical response or directed remote action must begin within fifteen minutes.

**Notification:**
- On-Call Engineer — immediate, automated via Avisos notification system
- Facility Lead — immediate, direct phone call from On-Call Engineer
- Infrastructure Lead — notified within ten minutes
- If life-safety risk: emergency services contacted by Facility Lead before any other action

**Escalation to external parties:**
Facility Lead authorizes contact with the building management company, electrical contractor, or emergency services as appropriate. On-Call Engineer does not contact external parties independently at Severity 1.

---

### Severity 2 — High

**Definition:**
A condition that is actively degrading facility operations, has a clear potential to escalate to Severity 1 within one to several hours, or affects a system with no available redundancy.

**Criteria (any one is sufficient):**
- Leak detected in any equipment zone (Alpha, Gamma, Delta) where water is near active cabling or hardware
- Temperature in Zone Alpha or Gamma exceeding threshold with no immediate recovery trend
- Single CRAH unit offline with no automatic failover confirmed
- Zone Gamma network anomaly causing partial telemetry loss from one or more zones
- UPS battery runtime estimated below 30 minutes due to a utility power event
- Simultaneous alarms in two or more zones that fit a known dependency chain

**Expected response time:**
The on-call engineer must acknowledge within fifteen minutes. Active investigation must begin within thirty minutes.

**Notification:**
- On-Call Engineer — automated notification via Avisos system
- Facility Lead — notified by On-Call Engineer within twenty minutes if root cause is not identified
- Infrastructure Lead — notified if the condition involves network infrastructure or compute availability

**Escalation criteria:**
Escalate to Severity 1 if the condition does not stabilize or improve within thirty minutes of active response, or if any Severity 1 criterion is met during investigation.

---

### Severity 3 — Moderate

**Definition:**
A condition outside normal operating parameters that requires investigation and corrective action but does not present an immediate risk to equipment or operations. Systems remain functional and redundancy is available.

**Criteria (examples, not exhaustive):**
- Single-zone temperature above upper threshold but trending stable
- Humidity exceeding normal range in one zone without a corresponding temperature anomaly
- Isolated `SIGNAL_DEGRADED` alarm on a single node with no correlated network events
- `BATTERY_LOW` on one or more nodes without evidence of rail voltage sag or UPS involvement
- Pressure anomaly in Zone Beta correlating with known weather conditions
- Leak detected in Zone Beta condensate area with no downstream spread confirmed

**Expected response time:**
Acknowledge within one hour. Investigation and corrective action plan within four hours during business hours, or at the start of the next business day if the alarm triggers outside staffed hours and conditions are stable.

**Notification:**
- On-Call Engineer — automated notification
- Facility Lead — informed during next scheduled check-in or earlier if the engineer assesses a risk of escalation

**Escalation criteria:**
Escalate to Severity 2 if the condition worsens, if a second related alarm activates, or if investigation reveals a root cause with potential for rapid deterioration.

---

### Severity 4 — Informational

**Definition:**
A condition that is within or near the boundary of normal operating parameters, logged for awareness, trend monitoring, or record-keeping purposes. No immediate action is required.

**Criteria (examples, not exhaustive):**
- Telemetry reading approaching but not crossing a threshold
- Node returning to normal status after a prior alarm
- Successful node reconnection after a brief signal loss
- Scheduled maintenance window alarm suppression active
- Humidity or temperature reading elevated by a small margin with a clear environmental explanation (weather, door propped open, post-maintenance thermal recovery)

**Expected response time:**
No immediate response required. Review during the next scheduled operations check. Log the event and note any trend.

**Notification:**
- Recorded in the Avisos alarm log
- Visible on the Web Console dashboard
- No automated push notification unless configured by the operator for specific node groups

**Escalation criteria:**
Escalate to Severity 3 if the informational condition persists beyond 24 hours or if a pattern of recurring informational alarms suggests an underlying issue.

---

## Role Definitions

The following roles are referenced throughout this policy. Role assignments are maintained by the Infrastructure Lead and updated in the team directory. No real names appear in this document.

| Role | Responsibility in Alarm Response |
|---|---|
| **On-Call Engineer** | First responder for all automated notifications; owns incident triage and initial response; escalates to other roles per this policy |
| **Facility Lead** | Physical authority over the building; authorizes access, contractor engagement, and emergency services contact; owns life-safety decisions |
| **Infrastructure Lead** | Technical authority over compute, network, and monitoring systems; brought in for Severity 1 and Severity 2 events affecting infrastructure availability |
| **Operations Supervisor** | Business continuity owner; notified for any Severity 1 event and any Severity 2 event expected to exceed two hours of impact |
| **Security Officer** | Notified if an alarm condition involves physical access anomalies, tampering indicators, or any situation where unauthorized access to a restricted zone is suspected |
| **External Contractor** | Engaged by Facility Lead for mechanical or electrical work beyond internal team capability; not contacted directly by On-Call Engineer |

---

## Escalation Paths by Condition Type

### Environmental and Cooling

Cooling-related alarms (temperature, humidity, CRAH status) escalate from On-Call Engineer to Facility Lead when the root cause involves physical plant equipment. The On-Call Engineer handles software-side investigation and alarm correlation; the Facility Lead owns the physical response to mechanical equipment. If a cooling alarm is assessed as Severity 1, the Infrastructure Lead is notified simultaneously.

### Water and Leak Events

All leak alarms begin at Severity 2 or higher. The On-Call Engineer assesses spread using correlated node data and determines whether Zone Echo or life-safety involvement elevates the event to Severity 1. The Facility Lead is always notified for any active leak, regardless of severity. The External Contractor (plumbing or building envelope specialist) is engaged by the Facility Lead if the source is structural or involves facility plumbing.

### Power and Electrical

Any alarm involving Zone Echo, UPS status, or sub-panel anomalies is escalated to the Facility Lead immediately, regardless of initial severity assessment. The On-Call Engineer does not perform any physical intervention in Zone Echo. If a power event is causing downstream node-offline alarms across multiple zones, the Infrastructure Lead is notified to assess service continuity.

### Network and Telemetry

Zone Gamma network anomalies are escalated to the Infrastructure Lead as the technical authority. The On-Call Engineer documents the scope of telemetry loss and correlates whether the loss pattern fits a Zone Gamma dependency event or individual node failures. The Operations Supervisor is notified if telemetry loss exceeds 30 minutes without a recovery path identified.

### Security and Access

If an alarm condition includes evidence of unauthorized physical access (door held open, unexpected badge event near a restricted zone, tampered node), the Security Officer is notified by the On-Call Engineer as a parallel action alongside normal alarm response. Do not wait for alarm resolution to notify the Security Officer of a potential access violation.

---

## Response Time Summary

| Severity | Acknowledgement | Initial Response | Escalation Review |
|---|---|---|---|
| 1 — Critical | 5 minutes | 15 minutes | Continuous until resolved |
| 2 — High | 15 minutes | 30 minutes | Every 30 minutes |
| 3 — Moderate | 1 hour | 4 hours (business hours) | At next check-in |
| 4 — Informational | Next scheduled review | As needed | If persistent after 24 hours |

---

## Notification Wording Guidelines

### General Principles

Alarm notifications should be accurate, concise, and unambiguous. They should state what is happening, where it is happening, and what action is expected of the recipient. Avoid technical jargon in notifications directed at non-engineering roles. Avoid catastrophizing language in early-stage notifications when the situation is not yet fully assessed.

### Required Elements in a Severity 1 or 2 Notification

Every notification for a Severity 1 or Severity 2 event should include the following elements.

- **What:** The alarm type and a plain-language description of the condition
- **Where:** The zone, node ID, and physical location description
- **When:** The time the alarm fired and how long it has been active
- **Current status:** Whether the condition is stable, worsening, or improving
- **Action requested:** What the recipient is being asked to do

### Tone by Severity

**Severity 1:** Direct and urgent. State the risk plainly. Do not soften language in a way that might delay the recipient's response.

> Zone Echo leak alarm active. Water detected near electrical distribution panels. Immediate Facility Lead response required. Do not enter room alone.

**Severity 2:** Clear and informative. Convey seriousness without alarm language that overstates the current condition.

> High temperature alarm in Zone Alpha, rows A4–A6. Readings at 87°F and trending up. Investigating cooling source. Facility Lead awareness requested.

**Severity 3:** Factual and procedural. State the condition and the planned response.

> Humidity above threshold in Zone Beta. Reading at 68%, normal ceiling is 65%. Condensate drain inspection scheduled. No downstream spread detected.

**Severity 4:** Brief and logged. No push notification in most cases; record in alarm log.

### Avoid in All Notifications

- Speculation about root cause before investigation is complete
- Language that assigns blame to specific personnel or vendors
- Specific financial estimates of impact
- Statements about regulatory exposure or legal liability
- Any credential, hostname, IP address, or internal system path

---

## Rules for AI-Generated Notifications

The Avisos AI Incident Analyst may draft recommended notification messages as part of its incident analysis output. The following rules govern how those drafts are used.

**The AI may:**
- Draft a candidate notification message for any severity level
- Recommend which roles should be notified based on the alarm type and zone
- Suggest an escalation path based on retrieved policy context
- Flag if an active alarm matches a dependency chain that implies a higher severity than the initial classification

**The AI must not:**
- Send any notification autonomously
- Address a notification to a specific named individual
- Include speculation presented as confirmed fact
- Override a severity classification made by an attending engineer
- Draft external communications to vendors, building management, or emergency services without explicit human instruction to do so

**Human approval is required before:**
- Any notification is sent to a role outside the On-Call Engineer
- Any draft is forwarded outside the internal operations team
- A severity is escalated based solely on AI recommendation without engineer corroboration
- Any AI-drafted message is used verbatim in an official incident record

The AI Incident Analyst is an advisory tool. Its output is a starting point for human decision-making, not a substitute for it.

---

## Policy Maintenance

This policy is reviewed by the Infrastructure Lead and Operations Supervisor on a semi-annual basis or following any Severity 1 incident. Changes to role assignments, response time targets, or escalation thresholds are recorded in the Maintenance Change Log before taking effect.