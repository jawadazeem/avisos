# Runbook — Environmental Drift Detection and Response

## Document Purpose

This runbook defines environmental drift as it applies to the Avisos monitoring platform, describes how to distinguish gradual degradation from acute incidents, explains multi-sensor pattern recognition for the Meridian Ridge facility, and provides response and escalation guidance for drift conditions. It is intended for use by on-call engineers during triage and by the Avisos AI Incident Analyst when generating drift-related incident summaries.

Environmental drift alarms do not always produce a single loud alarm event. They often surface as a pattern of Severity 4 informational readings that individually appear unremarkable but collectively indicate a system moving outside its stable operating band. Recognizing drift early is the primary goal of this runbook.

---

## Definition of Environmental Drift

Environmental drift is a sustained, directional change in one or more telemetry readings over a period of time that is not explained by a known transient event such as a maintenance visit, a seasonal adjustment, or a temporary load change. Drift is distinguished from an acute incident by its rate of change: where an acute incident produces a threshold crossing within minutes, drift produces a threshold crossing over hours, days, or weeks.

Drift is significant because it often indicates a system component that is degrading rather than failing outright. A cooling unit losing refrigerant charge, a drain line developing a slow partial blockage, a door seal deteriorating, or a sensor cell aging will all produce drift signatures before they produce acute alarms. Detecting and responding to drift conditions extends the window for corrective action and reduces the probability of a Severity 1 event.

For the purposes of Avisos alarm evaluation, a drift condition is flagged when the controller service observes a directional trend in telemetry readings across a defined observation window. The default observation windows are one hour for short-term drift detection and 24 hours for long-term trend analysis. These windows are configurable per node group.

---

## Normal Versus Concerning Changes

Not all directional telemetry movement represents drift. The following guidance distinguishes expected variation from concerning trends.

### Expected and Normal Variation

**Diurnal temperature cycling:** Ambient temperature in Zone Delta and near exterior walls in Zone Alpha will shift with outdoor temperature across the day and night cycle. A 3–6°F rise during afternoon hours and a corresponding drop overnight is a baseline pattern, not drift.

**Post-maintenance thermal recovery:** After a site visit in which rack access panels were opened, equipment was moved, or lighting was on for an extended period, temperatures in the affected zone may be elevated for 30–90 minutes before returning to baseline. This is normal and should not be flagged as drift if it coincides with a logged maintenance event.

**Seasonal humidity variation:** Ambient humidity in Zone Alpha near the north exterior wall and in Zone Delta may shift by 5–8 percentage points between summer and winter months. This is a known site characteristic. Readings should be interpreted against the current season's expected baseline, not an absolute year-round figure.

**Pressure variation during weather events:** The Zone Beta ambient pressure node is sensitive to outdoor barometric pressure changes due to the proximity of the exterior louver. A pressure reading shift during a passing storm front or a significant weather system is expected and not a drift condition.

**Load-driven temperature rise:** A planned increase in compute load in Zone Alpha, such as during a batch processing period or a system migration, will produce a measurable temperature rise in that zone. If a load event is logged, temperature increases proportional to the event are expected variation.

### Concerning Trends

**Unidirectional movement without a known cause:** A reading that moves consistently in one direction across four or more consecutive telemetry intervals without an explanatory event is the primary drift indicator. The direction matters: a temperature that rises steadily without recovery, a humidity reading that climbs incrementally over 12 hours, or a pressure differential that widens over several days each represents a system trending away from equilibrium.

**Rate of change acceleration:** A drift condition that was progressing slowly and begins to accelerate is a strong signal that the underlying degradation is worsening. A temperature rising at 0.5°F per hour that shifts to 1.5°F per hour over the same observation window indicates the system is losing the ability to self-correct.

**Recovery failure after a transient:** If a known transient event (a maintenance visit, a brief load spike) causes a reading to rise and the reading does not return to baseline within the expected recovery window, the system may lack the corrective capacity it previously had. Failure to recover is a drift indicator even if the absolute reading has not crossed a threshold.

**Cross-sensor directional alignment:** When multiple sensors in the same zone all show directional movement in the same period, the pattern is more significant than any single sensor reading. See the multi-sensor pattern section below.

---

## Multi-Sensor Drift Patterns

The most diagnostically useful drift conditions involve correlated movement across two or more sensors. The following patterns are established for the Meridian Ridge facility and should be recognized during triage.

### Pattern A — Cooling Degradation

**Sensors involved:** Temperature nodes in Zone Alpha (rows A1–A6), CRAH intake nodes in Zone Beta, Zone Gamma top-of-rack nodes

**Signature:**
Zone Beta CRAH intake temperature begins rising. Within 30–60 minutes, Zone Alpha cold-aisle temperature begins rising as the supply air temperature increases. Zone Gamma top-of-rack readings may follow within 60–90 minutes if the root cause is central cooling plant underperformance rather than a Zone Alpha-specific issue.

Humidity in Zone Beta may simultaneously drift upward if the CRAH unit is losing refrigerant charge, as reduced cooling capacity causes the unit to pass more moisture into the supply stream rather than condensing it out.

**Distinguishing cooling degradation from an acute cooling failure:**
In an acute failure, the CRAH unit goes offline and temperatures in downstream zones rise sharply within 15–20 minutes. In cooling degradation drift, the CRAH unit remains operational but its output is diminishing. Temperature rises are gradual, measured in fractions of a degree per interval rather than large jumps. The CRAH intake node may show a pattern of readings that were previously stable now creeping upward by small amounts across a multi-hour window.

**Significance:** Cooling degradation drift is a precursor condition to a Severity 1 cooling failure. Early detection allows the Facility Lead to schedule a refrigerant check or CRAH servicing before a failure occurs.

### Pattern B — Humidity Rise and Condensate Buildup

**Sensors involved:** Zone Beta humidity nodes, Zone Alpha humidity nodes, Zone Beta leak detection node (B-MECH-LEAK-01)

**Signature:**
Humidity in Zone Beta begins rising incrementally above its established baseline. Zone Alpha humidity follows, rising more slowly because the rise must propagate through the supply plenum. The Zone Beta leak detection node remains clean initially, but as humidity continues to rise, condensate accumulation at the drain manifold may eventually produce a `LEAK_DETECTED` event.

If the humidity rise is caused by a partially blocked drain line, the progression is: Zone Beta humidity rises → drain flow slows → condensate accumulation increases → eventual overflow. The drift window between initial humidity rise and visible overflow may be several hours, providing an actionable detection window.

**Distinguishing humidity drift from an acute moisture event:**
An acute moisture event (a burst pipe, a sudden condensate overflow) produces a rapid humidity spike and a near-simultaneous `LEAK_DETECTED` alarm. Humidity drift from a drain restriction produces a slow climb with the leak alarm appearing late in the progression, if at all during the early detection window.

**Significance:** A humidity drift pattern in Zone Beta is one of the most reliable early indicators of an impending drain issue. Responding to the humidity trend before the leak node triggers prevents water from reaching Zone Alpha subfloor.

### Pattern C — Pressure Differential Drift

**Sensors involved:** Zone Beta ambient pressure node, Zone Alpha environmental nodes, Zone Delta corridor nodes

**Signature:**
Barometric pressure readings from Zone Beta begin deviating from outdoor pressure trends. Where the Zone Beta pressure node would normally track roughly with ambient barometric changes, it begins to read differently from outdoor conditions in a way that cannot be explained by weather. Simultaneously, Zone Alpha subfloor pressure indicators (inferred from temperature stratification patterns between cold-aisle and room-level nodes) may suggest reduced airflow.

Pressure differential drift between Zone Beta and Zone Alpha can indicate a duct breach, a CRAH fan motor beginning to lose efficiency, or a subfloor obstruction reducing airflow through perforated tiles.

**Distinguishing pressure drift from weather-driven variation:**
Weather-driven pressure changes affect all pressure-sensing nodes in a broadly similar direction and track with known meteorological patterns. Pressure drift that is isolated to Zone Beta or that diverges from outdoor barometric trends is not weather-driven. Check outdoor weather data when evaluating Zone Beta pressure readings before classifying a reading as drift.

**Significance:** Pressure differential drift in Zone Beta is a leading indicator of airflow capacity reduction. Addressing airflow issues before they manifest as temperature problems is preferable to responding after temperatures have risen.

### Pattern D — Signal Quality Degradation Across a Zone

**Sensors involved:** All nodes in a single zone reporting signal quality metrics

**Signature:**
Signal quality readings for multiple nodes in the same zone show a slow downward trend across an observation window measured in days. No single node drops below the `SIGNAL_DEGRADED` threshold, but the zone-wide average is moving steadily downward. Individual nodes may show brief recoveries that mask the overall trend unless the full observation window is reviewed.

Signal quality drift across a zone can indicate RF interference from new equipment introduced to the zone, a failing access point or MQTT relay device, or physical changes to the zone environment that affect RF propagation, such as additional cable trays or equipment enclosures that create RF shadows.

**Distinguishing signal drift from individual node hardware degradation:**
Individual node cell degradation produces signal quality decline on a single node, often correlated with battery level decline on the same node. Zone-wide signal drift with no corresponding battery trend suggests an environmental RF cause rather than individual node hardware issues.

**Significance:** Zone-wide signal drift, if unaddressed, will eventually produce `SIGNAL_DEGRADED` alarms across multiple nodes simultaneously. By the time individual alarms fire, the underlying cause may have been present for days. Reviewing zone-wide signal trends during routine checks can surface this condition earlier.

### Pattern E — Battery Level Drift Across a Zone

**Sensors involved:** All nodes in a zone with active battery monitoring

**Signature:**
Battery levels across a zone show a slow decline that is not explained by a known power event. Individually, each node may appear within its normal band, but the zone-wide average has moved from a stable 90–95% range down to a 70–80% range over a period of days or weeks. No single node has triggered a `BATTERY_LOW` alarm.

This pattern indicates a persistent supply rail voltage issue that is insufficient to fully charge node backup cells but is not causing acute battery discharge. The rail may be delivering power at the low end of the acceptable input voltage range for the charging circuit.

**Significance:** A zone experiencing battery drift is operating with reduced resilience. If a brief supply interruption occurs, nodes will begin from a lower charge state and will reach critical battery levels sooner than expected. This condition should be resolved before it compounds with another event.

---

## How to Distinguish a Sudden Incident from Slow Degradation

The following decision criteria help classify an active alarm condition as an acute incident or a drift condition during initial triage.

| Factor | Acute Incident | Drift Condition |
|---|---|---|
| Rate of threshold crossing | Within one to two telemetry intervals | Across many intervals over hours or days |
| Number of sensors affected | Typically one or a coherent group simultaneously | May be one sensor initially, spreading slowly |
| Alarm severity at first detection | Severity 2 or higher often fires directly | Begins at Severity 4 informational readings |
| Recovery behavior | No self-recovery without intervention | May show partial recovery that masks progression |
| Correlated alarms | Often multiple alarms near-simultaneously | Few or no correlated alarms early in progression |
| Known preceding events | Often no warning | Trend visible in telemetry history in retrospect |
| Rate of change | Large delta per interval | Small delta per interval |

When an alarm fires and the telemetry history shows that readings had been moving directionally toward the threshold for an extended period before the alarm was raised, the condition should be documented as drift-origin even if the alarm itself is an acute threshold crossing. This distinction affects both the response (address root cause, not just the threshold exceedance) and the post-incident review (assess why drift was not detected earlier).

---

## Recommended Operator Actions

### During Active Drift Detection

**Step 1 — Pull the telemetry history for the affected node or nodes.** Review readings across the longest available observation window, not just the current alarm state. Identify when the directional movement began, the rate of change, and whether the rate is stable or accelerating.

**Step 2 — Identify the drift pattern.** Compare the observed telemetry behavior against the multi-sensor patterns described in this runbook. Determine whether the drift matches a known pattern (cooling degradation, humidity rise, pressure differential, signal degradation, battery drift) or whether it is an uncharacterized combination that warrants deeper investigation.

**Step 3 — Check for correlated readings in adjacent zones.** Use the zone dependency relationships in the Equipment Zones document to determine whether the drift is localized or spreading. A drift condition confined to one zone suggests a zone-specific cause. A drift pattern spreading across zones suggests a shared system (cooling, power, network) is involved.

**Step 4 — Review the maintenance log.** Determine whether any recent maintenance activity, equipment addition, configuration change, or load change in the zone or in a dependent system could explain the onset of the drift. If a cause is found, document it and assess whether it is self-resolving or requires corrective action.

**Step 5 — Set a monitoring interval.** For a drift condition that has not yet crossed a Severity 3 threshold, establish an explicit monitoring interval appropriate to the rate of change. A slowly drifting reading may warrant a check every two hours; a faster drift may require continuous attention. Document the monitoring interval in the alarm record.

**Step 6 — Notify the Facility Lead for mechanical drift patterns.** If the drift pattern is consistent with cooling degradation or humidity rise, notify the Facility Lead for awareness even before a Severity 2 alarm fires. The Facility Lead may elect to schedule a CRAH inspection or drain check as a preventive measure.

### After Drift Crosses a Severity Threshold

Once a drift condition produces a Severity 2 or Severity 3 alarm, apply the relevant alarm-specific runbook (high temperature, humidity, battery, etc.) in addition to the drift response guidance in this document. Document in the alarm record that the condition was drift-origin and include the duration and rate of the observed trend.

---

## Escalation Criteria

| Condition | Escalation Action |
|---|---|
| Drift rate accelerating without an identified cause | Escalate to Severity 2; notify Facility Lead |
| Drift crossing into Severity 2 territory without recovery trend | Notify Infrastructure Lead; consider physical inspection |
| Cooling degradation pattern confirmed (Pattern A) | Notify Facility Lead immediately; schedule CRAH inspection |
| Humidity rise pattern with no drain inspection in past 30 days (Pattern B) | Notify Facility Lead; schedule drain inspection within 24 hours |
| Zone-wide battery drift to below 75% average (Pattern E) | Notify Facility Lead; request Zone Echo panel voltage check |
| Drift condition present on Zone Echo nodes | Treat as Severity 2 minimum; notify Facility Lead regardless of individual readings |
| Drift condition that has been stable for more than 72 hours without correction | Escalate to Infrastructure Lead for root cause review |

---

## Sample AI Incident Summary

The following is an example of an AI Incident Analyst summary for a cooling degradation drift event. It illustrates the expected format, content boundaries, and language calibration for drift conditions.

---

**AVISOS AI INCIDENT ANALYST — Drift Condition Summary**
**Generated for human review — not sent automatically**

**Condition type:** Environmental drift — suspected cooling degradation
**Pattern match:** Pattern A (Cooling Degradation)
**Zones involved:** Zone Beta, Zone Alpha
**Observation window:** Past 18 hours
**Severity recommendation:** Severity 3 — Moderate; escalation to Severity 2 recommended if trend continues

---

**Summary:**

Telemetry from Zone Beta CRAH intake node B-MECH-ENV-01 shows a sustained upward temperature trend over the past 18 hours. The reading has moved from a stable baseline of 81°F at the start of the observation window to 87°F at the most recent interval. The rate of change has been approximately 0.33°F per hour, which is consistent with gradual cooling capacity reduction rather than an acute equipment failure.

Zone Alpha cold-aisle nodes in rows A1 and A2 show a corresponding upward trend beginning approximately 75 minutes after the Zone Beta movement, which is consistent with the expected thermal propagation time through the shared supply plenum. Zone Alpha readings have moved from 69°F to 73°F over the same period. No threshold alarms have fired yet in Zone Alpha, but the current trajectory projects a Severity 3 threshold crossing in approximately four hours at the present rate.

Zone Beta humidity at B-MECH-ENV-01 has also risen incrementally, from 52% to 61% over the 18-hour window. Elevated humidity alongside rising intake temperature is consistent with a CRAH unit operating at reduced refrigerant charge or with reduced airflow through the unit. This combination increases the confidence that the drift source is in the Zone Beta cooling plant.

No `LEAK_DETECTED` alarms are active in Zone Beta. No maintenance activity has been logged in Zone Beta in the past seven days.

---

**What this summary cannot confirm:**

This summary is based on telemetry trends and pattern matching against documented drift signatures. It cannot confirm the specific mechanical cause of the CRAH underperformance. It cannot determine whether one or both CRAH units are affected. Physical inspection of Zone Beta by a qualified technician is required to confirm root cause.

---

**Recommended actions:**

1. Notify the Facility Lead of the cooling degradation pattern and request a Zone Beta inspection at the earliest opportunity.
2. Continue monitoring Zone Alpha temperature trends at a 30-minute check interval.
3. If Zone Alpha temperature reaches 76°F before Zone Beta inspection occurs, escalate to Severity 2 and notify the Infrastructure Lead.
4. Check Zone Gamma top-of-rack temperatures for early involvement; Zone Gamma readings have not shown drift in the current observation window but should be confirmed.
5. Review the CRAH maintenance log; if the last refrigerant check was more than six months ago, include this in the briefing to the Facility Lead.

**This summary is a draft for operator review. Verify current telemetry values before forwarding to any recipient.**

---

*Runbook owner: Infrastructure Lead. Review cycle: semi-annual or following any confirmed drift-origin Severity 1 event.*