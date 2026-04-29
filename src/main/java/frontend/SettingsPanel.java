/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package frontend;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {

    // Theme Colors
    private final Color bgDark = new Color(30, 31, 34);
    private final Color cardDark = new Color(25, 26, 29);
    private final Color accentBlue = new Color(71, 131, 192);
    private final Color borderDark = new Color(45, 48, 51);

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(bgDark);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header
        JLabel title = new JLabel("SYSTEM CONFIGURATION");
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Settings Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(createSection("Database Configuration", new String[]{"DB Connection String", "Schema Version", "Auto-Backup Interval (Min)"}));
        content.add(Box.createVerticalStrut(20));
        content.add(createSection("Security Parameters", new String[]{"Alarm Trigger Threshold", "Ping Timeout (ms)", "Max Failure Retries"}));
        content.add(Box.createVerticalStrut(20));
        content.add(createSection("User Preferences", new String[]{"Admin Username", "Notification Email", "UI Refresh Rate (ms)"}));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton saveBtn = createStyledButton("SAVE CONFIGURATION", accentBlue);
        JButton resetBtn = createStyledButton("DISCARD CHANGES", new Color(180, 70, 70));

        footer.add(resetBtn);
        footer.add(saveBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createSection(String title, String[] fields) {
        JPanel section = new JPanel(new GridBagLayout());
        section.setBackground(cardDark);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderDark),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Inter", Font.BOLD, 12),
                accentBlue
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            JLabel label = new JLabel(fields[i]);
            label.setForeground(Color.LIGHT_GRAY);
            label.setFont(new Font("Inter", Font.PLAIN, 13));
            section.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            JTextField input = new JTextField(20);
            styleTextField(input);
            section.add(input, gbc);
        }

        return section;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(new Color(43, 45, 48));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderDark),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}