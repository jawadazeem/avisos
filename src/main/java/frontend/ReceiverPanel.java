/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package frontend;

import infrastructure.subscribers.Subscriber;
import service.receiver.ReceiverService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ReceiverPanel extends JPanel {
    private final JTable subscriberTable;
    private final DefaultTableModel tableModel;
    private final ReceiverService receiverService;

    public ReceiverPanel(ReceiverService receiverService) {
        this.receiverService = receiverService;

        setLayout(new BorderLayout());
        setBackground(new Color(30, 31, 34)); // Match Dashboard background
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // --- 1. TITLE SECTION ---
        JLabel title = new JLabel("RECEIVER NODES");
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        title.setFont(new Font("Inter", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        // --- 2. TABLE SECTION ---
        String[] columnNames = {"NODE ID", "RECEIVER TYPE", "CONNECTION STATUS"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        subscriberTable = new JTable(tableModel);
        styleTable(subscriberTable);

        JScrollPane scrollPane = new JScrollPane(subscriberTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(43, 45, 48)));
        scrollPane.getViewport().setBackground(new Color(30, 31, 34));
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. CONTROLS SECTION (BOTTOM) ---
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton refreshBtn = new JButton("REFRESH FLEET");
        styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> refreshTable());

        JButton enableReceiver = new JButton("ENABLE NEW RECEIVER");
        styleTertiaryButton(enableReceiver);
        enableReceiver.addActionListener(e -> receiverService.connectNewReceiver((int)(Math.random()*100)));

        JButton clearBtn = new JButton("CLEAR ALL");
        styleDangerButton(clearBtn);
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Decommission all receivers?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                receiverService.clearReceivers();
                refreshTable();
            }
        });

        controls.add(refreshBtn);
        controls.add(clearBtn);
        add(controls, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Subscriber> receivers = receiverService.getReceivers();

        for (Subscriber s : receivers) {
            if (s != null) {
                tableModel.addRow(new Object[]{
                        "#" + s.getId(),
                        s.getSubscriberType().toString().replace("_", " "),
                        s.getSubscriberStatus()
                });
            }
        }
    }

    // --- STYLING HELPERS (Matching DeviceFleetPanel) ---

    private void styleTable(JTable table) {
        table.setRowHeight(40);
        table.setBackground(new Color(43, 45, 48));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 63, 65));
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(71, 131, 192));
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(25, 26, 29));
        header.setForeground(new Color(71, 131, 192));
        header.setFont(new Font("Inter", Font.BOLD, 12));
        header.setBorder(BorderFactory.createEmptyBorder());
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setBackground(new Color(71, 131, 192));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTertiaryButton(JButton btn) {
        btn.setBackground(new Color(71, 131, 192));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleDangerButton(JButton btn) {
        btn.setBackground(new Color(239, 68, 68));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFont(new Font("Inter", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}