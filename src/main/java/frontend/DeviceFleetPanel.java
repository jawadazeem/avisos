/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package frontend;

import core.SecurityHub;
import service.hub.api.SecurityHubService;
import devices.api.DataAcquisitionDevice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeviceFleetPanel extends JPanel {
    private final SecurityHub hub;
    private final SecurityHubService hubService;
    private final JPanel cardGrid;
    private final JTextField uuidInput;

    private final Map<UUID, DeviceCard> cardMap = new HashMap<>();

    // Theme Colors
    private final Color bgDark = new Color(30, 31, 34);
    private final Color footerDark = new Color(25, 26, 29);
    private final Color accentBlue = new Color(71, 131, 192);
    private final Color borderDark = new Color(45, 48, 51);

    public DeviceFleetPanel(SecurityHub hub, SecurityHubService hubService) {
        this.hub = hub;
        this.hubService = hubService;

        setLayout(new BorderLayout());
        setBackground(bgDark);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header Section
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("DEVICE FLEET");
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Subtitle/Counter
        JLabel sub = new JLabel("Monitoring hardware links across active zones");
        sub.setFont(new Font("Inter", Font.ITALIC, 12));
        sub.setForeground(Color.GRAY);
        header.add(sub, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        // Main Grid Area
        cardGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 25));
        cardGrid.setBackground(bgDark);

        JScrollPane scroll = new JScrollPane(cardGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Glass-morphism Footer for Registration
        JPanel registrationPanel = new JPanel(new BorderLayout());
        registrationPanel.setBackground(footerDark);
        registrationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, borderDark),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Input Wrapper
        JPanel inputWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        inputWrapper.setOpaque(false);

        JLabel regLabel = new JLabel("PROVISION UUID");
        regLabel.setForeground(accentBlue);
        regLabel.setFont(new Font("Inter", Font.BOLD, 11));

        uuidInput = new JTextField(25);
        styleTextField(uuidInput);

        JButton pairBtn = new JButton("PAIR DEVICE");
        stylePairButton(pairBtn);

        pairBtn.addActionListener(e -> executeRegistration());

        inputWrapper.add(regLabel);
        inputWrapper.add(uuidInput);
        inputWrapper.add(pairBtn);

        registrationPanel.add(inputWrapper, BorderLayout.WEST);
        add(registrationPanel, BorderLayout.SOUTH);

        refresh();
    }

    private void styleTextField(JTextField field) {
        field.setBackground(new Color(43, 45, 48));
        field.setForeground(Color.WHITE);
        field.setCaretColor(accentBlue);
        field.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Better for UUIDs
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderDark),
                new EmptyBorder(10, 15, 10, 15)
        ));
    }

    private void stylePairButton(JButton btn) {
        btn.setBackground(accentBlue);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 25, 12, 25));
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(accentBlue.brighter()); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(accentBlue); }
        });
    }

    private void executeRegistration() {
        String rawId = uuidInput.getText().trim();
        if (rawId.isEmpty()) return;

        try {
            UUID id = UUID.fromString(rawId);
            hub.pairNewDataAcquisitionDeviceById(id);
            uuidInput.setText("");
            refresh();
        } catch (IllegalArgumentException ex) {
            showError("Invalid UUID format. Verification failed.");
        }
    }

    private void showError(String msg) {
        UIManager.put("OptionPane.background", footerDark);
        UIManager.put("Panel.background", footerDark);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        JOptionPane.showMessageDialog(this, msg, "Security Error", JOptionPane.ERROR_MESSAGE);
    }

    public void refresh() {
        List<DataAcquisitionDevice> devices = hub.getDevices();
        Map<UUID, DeviceCard> stillPresent = new HashMap<>();

        for (DataAcquisitionDevice d : devices) {
            UUID id = d.getId();
            DeviceCard card = cardMap.get(id);
            if (card == null) {
                card = new DeviceCard(d, hubService);
                cardMap.put(id, card);
                cardGrid.add(card);
            } else {
                card.setDevice(d);
                card.updateFromDevice();
            }
            stillPresent.put(id, card);
        }

        for (UUID existingId : cardMap.keySet().toArray(new UUID[0])) {
            if (!stillPresent.containsKey(existingId)) {
                DeviceCard toRemove = cardMap.remove(existingId);
                if (toRemove != null) cardGrid.remove(toRemove);
            }
        }

        cardGrid.revalidate();
        cardGrid.repaint();
    }
}