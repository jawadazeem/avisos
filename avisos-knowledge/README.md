# Avisos Knowledge Base

## Overview

This folder contains the operational knowledge base for the Avisos monitoring and alarm system. Avisos is an on-premises datacenter monitoring platform designed for facility and infrastructure environments. It collects telemetry from distributed sensor nodes, detects anomalies, and raises alarms when hardware vitals fall outside acceptable thresholds.

The knowledge base serves two purposes. First, it acts as a human-readable reference for the operations and infrastructure team. Second, it is the document corpus used by the Avisos AI Incident Analyst, a Retrieval-Augmented Generation (RAG) component built on a locally hosted Ollama model. When an alarm fires, the controller service queries this corpus to retrieve relevant context, then passes that context to the language model to produce an interpreted alarm summary, a recommended response procedure, and a draft incident message suitable for internal communication.

---

## System Components

| Component | Role |
|---|---|
| **avisos-node-service** | Runs on each datacenter sensor node; collects and publishes hardware vitals via MQTT |
| **avisos-controller-service** | Ingests telemetry, evaluates thresholds, raises alarms, triggers RAG queries |
| **Avisos Web Console** | React-based dashboard; displays node status, live vitals, and active alarms |
| **Avisos AI Incident Analyst** | RAG pipeline (Ollama + this knowledge base); produces incident interpretations |

Monitored vitals include battery level, ambient temperature, relative humidity, barometric pressure, leak detection status, and RF signal quality.

---

## What Lives in This Folder

Documents in this knowledge base fall into the following categories.

**Alarm Definitions and Thresholds**
Descriptions of each alarm type, the conditions that trigger it, severity levels, and initial triage guidance. These are the primary retrieval targets when an alarm fires.

**Zone and Room Profiles**
Physical layout of the monitored environment, including rack zones, sensor placement, airflow patterns, and known environmental quirks that affect normal baseline readings.

**Node Catalog**
Reference entries for node types deployed in the environment, including hardware specifications, expected telemetry ranges, and common failure modes.

**Operational Runbooks**
Step-by-step procedures for responding to specific alarm conditions. Written for on-call engineers who may not be familiar with a particular zone or node type.

**Escalation and Contact Procedures**
Role-based escalation paths, on-call rotation guidance, and criteria for when to escalate versus resolve locally.

**Incident Message Templates**
Boilerplate and example drafts for internal incident communications. The AI Incident Analyst uses these as style references when drafting messages.

**Maintenance and Change Logs**
Records of scheduled maintenance windows, hardware replacements, threshold adjustments, and known anomalies introduced by planned changes.

**Glossary**
Definitions of terms, abbreviations, and internal naming conventions used across Avisos documentation.

---

## How the AI Incident Analyst Should Use These Documents

When the controller service detects an alarm condition, it constructs a retrieval query using the alarm type, the affected node ID, and the current vital readings. The RAG pipeline retrieves the most relevant chunks from this corpus and passes them to the Ollama model along with the alarm payload.

The model is expected to do the following with the retrieved context.

1. **Interpret the alarm.** Use the alarm definitions and zone profiles to explain what the condition means in plain language and why it is significant in the specific location where it occurred.

2. **Recommend next actions.** Use the relevant runbook to suggest an ordered list of response steps appropriate to the severity and context of the alarm.

3. **Assess urgency.** Cross-reference the escalation procedures to indicate whether the condition warrants immediate escalation, a watchful wait, or a deferred ticket.

4. **Draft an incident message.** Using the incident message templates as a style guide, produce a concise human-readable summary suitable for posting to the team's internal communications channel.

The model should treat retrieved document content as authoritative for this environment. If retrieved chunks conflict, prefer the more specific document (a runbook for a specific zone over a general alarm definition). If no relevant chunk is retrieved, the model should state that explicitly rather than speculate.

---

## Rules for Safe Documentation

All documents in this knowledge base are written to be safe for public demonstration and open-source repository hosting. Authors must follow these rules when contributing.

- **No real credentials.** Do not include passwords, API keys, tokens, SNMP community strings, or any form of authentication secret.
- **No real personal information.** Use role names (e.g., *On-Call Engineer*, *Facility Lead*) rather than real names. Fictional names may be used for personas in examples.
- **No real network addresses.** Use RFC 5737 documentation ranges (192.0.2.x, 198.51.100.x) or clearly fictional hostnames. Do not include real IP addresses, MAC addresses, or DNS records from any live environment.
- **No real phone numbers.** Use the 555-xxxx convention or role-based contact references only.
- **No real physical addresses.** Use fictional site names and zone labels. Do not reference real building addresses or geographic coordinates.
- **No vendor-specific exploitable detail.** Runbooks may reference vendor categories (e.g., *rack PDU*, *in-row cooling unit*) but should not include real model numbers combined with unpatched vulnerability context.
- **Fictional names for all zones, rooms, racks, and devices.** Use the naming conventions established in the Zone and Room Profiles documents.

---

## Suggested Folder Structure

```
avisos-knowledge/
├── README.md                        # This file
├── alarms/
│   ├── alarm-overview.md            # Alarm severity model and general triage logic
│   ├── alarm-battery.md
│   ├── alarm-temperature.md
│   ├── alarm-humidity.md
│   ├── alarm-pressure.md
│   ├── alarm-leak.md
│   └── alarm-signal.md
├── zones/
│   ├── zone-overview.md             # Site layout and zone naming conventions
│   ├── zone-alpha.md
│   ├── zone-beta.md
│   └── zone-gamma.md
├── nodes/
│   ├── node-catalog.md              # Node types, hardware specs, telemetry ranges
│   └── node-failure-modes.md
├── runbooks/
│   ├── runbook-high-temperature.md
│   ├── runbook-leak-detected.md
│   ├── runbook-battery-critical.md
│   ├── runbook-node-offline.md
│   └── runbook-signal-degraded.md
├── escalation/
│   └── escalation-procedures.md
├── templates/
│   └── incident-message-templates.md
├── maintenance/
│   └── change-log.md
└── glossary.md
```

---

## Public Demo Notice

This knowledge base is sanitized for public use. It contains no real credentials, no real personal information, no real network addresses, and no proprietary operational data from any live environment. All zone names, rack identifiers, node IDs, role names, and scenario descriptions are fictional and created for demonstration purposes only. The document corpus is suitable for committing to a public GitHub repository as part of the Avisos proof-of-concept project.
