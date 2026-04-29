/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package frontend;

import devices.model.DeviceStatus;
import service.hub.api.SecurityHubService;
import devices.api.DataAcquisitionDevice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

class DeviceCard extends JPanel {
    private DataAcquisitionDevice dataAcquisitionDevice;
    private final JLabel typeLabel, idLabel, signalLabel, batteryLabel;
    private final JProgressBar batteryBar;
    private final JButton activateBtn;

    // Theme Colors
    private final Color cardBg = new Color(25, 26, 29);
    private final Color accentBlue = new Color(71, 131, 192);
    private final Color statusGreen = new Color(46, 204, 113);
    private final Color btnBg = new Color(43, 45, 48);

    public DeviceCard(DataAcquisitionDevice d, SecurityHubService hubService) {
        this.dataAcquisitionDevice = d;

        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(300, 240)); // Slightly larger for breathing room
        setBackground(cardBg);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 48, 51), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // --- TOP: HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        typeLabel = new JLabel(formatType(d.getDeviceType().toString()));
        typeLabel.setFont(new Font("Inter", Font.BOLD, 14));
        typeLabel.setForeground(accentBlue);

        // Custom Status Indicator with Label
        JPanel statusBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        statusBox.setOpaque(false);
        JLabel statusText = new JLabel(d.getDeviceStatus().toString());
        statusText.setFont(new Font("Inter", Font.BOLD, 10));
        statusText.setForeground(Color.GRAY);

        statusBox.add(statusText);
        statusBox.add(createStatusDot());

        header.add(typeLabel, BorderLayout.WEST);
        header.add(statusBox, BorderLayout.EAST);

        // --- CENTER: DATA & STATS ---
        JPanel body = new JPanel(new GridLayout(3, 1, 0, 5));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 0, 10, 0));

        idLabel = new JLabel("UUID: " + d.getId().toString().substring(0, 18) + "...");
        idLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        idLabel.setForeground(Color.LIGHT_GRAY);

        signalLabel = new JLabel("SIGNAL STRENGTH: " + d.getSignalStrength() + " dBm");
        signalLabel.setFont(new Font("Inter", Font.BOLD, 11));
        signalLabel.setForeground(Color.GRAY);

        // Battery Section in Body
        JPanel batteryPanel = new JPanel(new BorderLayout(10, 0));
        batteryPanel.setOpaque(false);
        batteryLabel = new JLabel("⚡ " + (int)d.getBatteryLife() + "%");
        batteryLabel.setFont(new Font("Inter", Font.BOLD, 11));
        batteryLabel.setForeground(Color.LIGHT_GRAY);

        batteryBar = new JProgressBar(0, 100);
        batteryBar.setValue((int) d.getBatteryLife());
        batteryBar.setPreferredSize(new Dimension(100, 6)); // Thinner, sleeker bar
        batteryBar.setBackground(new Color(40, 42, 45));
        batteryBar.setBorderPainted(false);

        batteryPanel.add(batteryLabel, BorderLayout.WEST);
        batteryPanel.add(batteryBar, BorderLayout.CENTER);

        body.add(idLabel);
        body.add(signalLabel);
        body.add(batteryPanel);

        // --- BOTTOM: ACTION BUTTONS ---
        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.setOpaque(false);

        JButton pingBtn = createActionButton("PING");
        JButton rmBtn = createActionButton("REMOVE");

        String activationStatus = (hubService.getDeviceStatus(d) == DeviceStatus.AWAY ? "ACTIVATE" : "DEACTIVATE");
        activateBtn = createActionButton(activationStatus);

        // Logic (Threads as you had them)
        pingBtn.addActionListener(e -> new Thread(d::ping).start());
        rmBtn.addActionListener(e -> new Thread(() -> hubService.removeDevice(d)).start());
        activateBtn.addActionListener(e -> handleActivation(hubService));

        actions.add(pingBtn);
        actions.add(activateBtn);
        actions.add(rmBtn);

        // Assemble
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        refreshBatteryColor();
    }

    private String formatType(String type) {
        String cleaned = type.replace("_", " ");
        if (cleaned.length() > 6) {
            cleaned = cleaned.substring(0, cleaned.length() - 6);
        }
        return cleaned.toUpperCase();
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 9));
        btn.setBackground(btnBg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 66)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createStatusDot() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = switch (dataAcquisitionDevice.getDeviceStatus()) {
                    case OPERATIONAL -> statusGreen;
                    case RECOVERY_MODE -> Color.ORANGE;
                    case DECOMMISSIONED -> Color.RED;
                    default -> Color.GRAY;
                };
                g2.setColor(c);
                g2.fillOval(2, 6, 8, 8);
                // Subtle Glow Effect
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(0, 4, 12, 12);
            }
        };
    }

    private void handleActivation(SecurityHubService hubService) {
        new Thread(() -> {
            if (hubService.getDeviceStatus(dataAcquisitionDevice) == DeviceStatus.AWAY) {
                hubService.activateDevice(dataAcquisitionDevice);
            } else {
                hubService.deactivateDevice(dataAcquisitionDevice);
            }
            updateFromDevice();
        }).start();
    }

    public void updateFromDevice() {
        SwingUtilities.invokeLater(() -> {
            typeLabel.setText(formatType(dataAcquisitionDevice.getDeviceType().toString()));
            signalLabel.setText("SIGNAL STRENGTH: " + dataAcquisitionDevice.getSignalStrength() + " dBm");
            batteryBar.setValue((int) dataAcquisitionDevice.getBatteryLife());
            batteryLabel.setText("⚡ " + (int)dataAcquisitionDevice.getBatteryLife() + "%");

            String status = (dataAcquisitionDevice.getDeviceStatus() == DeviceStatus.AWAY ? "ACTIVATE" : "DEACTIVATE");
            activateBtn.setText(status);

            refreshBatteryColor();
            repaint();
        });
    }

    private void refreshBatteryColor() {
        int life = (int) dataAcquisitionDevice.getBatteryLife();
        if (life < 20) batteryBar.setForeground(Color.RED);
        else if (life < 50) batteryBar.setForeground(Color.YELLOW);
        else batteryBar.setForeground(statusGreen);
    }

    public void setDevice(DataAcquisitionDevice d) {
        this.dataAcquisitionDevice = d;
        updateFromDevice();
    }
}