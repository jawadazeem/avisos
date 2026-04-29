/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package frontend;

import service.hub.api.SecurityHubService;
import service.system.SystemHealthService;
import service.system.SystemSnapshot;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class SystemReportPanel extends JPanel {
    private final SystemHealthService healthService;
    private final SecurityHubService hubService;
    private final JLabel healthVal, alarmVal, densityVal, modeVal, timeVal;

    // We make this a field so updateReport can change its color
    private JButton maintBtn;

    // Theme Colors
    private final Color bgDark = new Color(30, 31, 34);
    private final Color cardDark = new Color(25, 26, 29);
    private final Color accentBlue = new Color(71, 131, 192);
    private final Color accentAmber = new Color(200, 150, 50);
    private final Color borderDark = new Color(45, 48, 51);
    private final Color btnDefault = new Color(45, 48, 51);

    public SystemReportPanel(SecurityHubService hubService, SystemHealthService healthService) {
        this.healthService = healthService;
        this.hubService = hubService;

        setLayout(new BorderLayout());
        setBackground(bgDark);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("SYSTEM OPERATIONAL REPORT");
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        statsGrid.setOpaque(false);

        healthVal = createStatCard(statsGrid, "FLEET HEALTH");
        alarmVal = createStatCard(statsGrid, "ACTIVE ALARMS");
        densityVal = createStatCard(statsGrid, "ALARM DENSITY");
        modeVal = createStatCard(statsGrid, "SYSTEM MODE");
        timeVal = createStatCard(statsGrid, "LAST SNAPSHOT");

        setupActionCard(statsGrid);

        add(statsGrid, BorderLayout.CENTER);
        updateReport();
    }

    private void setupActionCard(JPanel parent) {
        JPanel card = createBaseCard("SYSTEM ACTIONS");
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        JButton rebootBtn = createStyledButton("Reboot Hub", new Color(180, 70, 70));
        JButton checkBtn = createStyledButton("Fleet Check", accentBlue);

        // Initialize our field
        maintBtn = createStyledButton("Toggle Maintenance", accentAmber);

        rebootBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to reboot the hardware fleet?", "System Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                hubService.resetDataAcquisitionDevices();
                hubService.resolveAllAlarms();
                updateReport();
            }
        });

        checkBtn.addActionListener(e -> {
            hubService.initiateFleetCheck();
            updateReport();
        });

        maintBtn.addActionListener(e -> {
            hubService.toggleIsMaintenanceMode();
            updateReport(); // This will trigger the color change
        });

        btnPanel.add(rebootBtn);
        btnPanel.add(checkBtn);
        btnPanel.add(maintBtn);

        card.add(btnPanel, BorderLayout.CENTER);
        parent.add(card);
    }

    private JButton createStyledButton(String text, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(btnDefault);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 66)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(hoverColor); }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Only go back to gray if it's NOT the maintenance button while maintenance is active
                if (btn == maintBtn && hubService.isMaintenanceMode()) {
                    btn.setBackground(accentAmber);
                } else {
                    btn.setBackground(btnDefault);
                }
            }
        });

        return btn;
    }

    public void updateReport() {
        SystemSnapshot snap = healthService.getSystemSnapshot();

        // Update values
        healthVal.setText((int)(snap.getFleetHealthPercentage() * 100) + "%");
        alarmVal.setText(String.valueOf(snap.getActiveAlarmCount()));
        densityVal.setText(String.format("%.2f", snap.getAlarmDensity()));
        modeVal.setText(snap.getSystemMode().toString());
        timeVal.setText(snap.getTimestamp().atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // Persist Maintenance Color
        if (hubService.isMaintenanceMode()) {
            maintBtn.setBackground(accentAmber);
            modeVal.setForeground(accentAmber); // Bonus: make the text yellow too!
        } else {
            maintBtn.setBackground(btnDefault);
            modeVal.setForeground(accentBlue);
        }
    }

    // Helper methods (createStatCard, createBaseCard) remain the same...
    private JLabel createStatCard(JPanel parent, String title) {
        JPanel card = createBaseCard(title);
        JLabel val = new JLabel("--");
        val.setForeground(accentBlue);
        val.setFont(new Font("Inter", Font.BOLD, 32));
        val.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(val, BorderLayout.CENTER);
        parent.add(card);
        return val;
    }

    private JPanel createBaseCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(cardDark);
        card.setBorder(BorderFactory.createLineBorder(borderDark));
        JLabel t = new JLabel(title);
        t.setForeground(Color.GRAY);
        t.setFont(new Font("Inter", Font.PLAIN, 12));
        t.setBorder(new EmptyBorder(10, 15, 0, 0));
        card.add(t, BorderLayout.NORTH);
        return card;
    }
}