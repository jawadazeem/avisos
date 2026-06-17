import { useEffect, useState } from "react";
import { api } from "../api/client";
import type { StaffRecord } from "../types/models";
import "./StaffPage.css";

export function StaffPage() {
  const [staff, setStaff] = useState<StaffRecord[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.getStaff().then(setStaff).catch((e) => setError(e.message));
  }, []);

  if (error) return <div className="page-error">Error: {error}</div>;

  return (
    <div className="staff-page">
      <div className="page-heading-row">
        <h2 className="page-title">Staff Directory</h2>
        <span className="page-subtitle">{staff.length} personnel</span>
      </div>
      <table className="scada-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Role</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Primary Zone</th>
            <th>Shift</th>
          </tr>
        </thead>
        <tbody>
          {staff.map((member) => (
            <tr key={member.staffId}>
              <td className="staff-name">{member.name}</td>
              <td className="staff-role">{member.role}</td>
              <td className="staff-email">
                <a href={`mailto:${member.email}`}>{member.email}</a>
              </td>
              <td className="staff-phone">{member.phone}</td>
              <td className="staff-zone">{member.primaryZone}</td>
              <td className="staff-shift">{member.shift}</td>
            </tr>
          ))}
          {staff.length === 0 && (
            <tr>
              <td colSpan={6} className="empty-row">
                No staff records found
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
