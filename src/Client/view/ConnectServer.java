package Client.view;

import Client.RunClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectServer extends JFrame {
    public ConnectServer() {
        initComponent();
        setVisible(true);
    }

    void initComponent() {
        setTitle("Connect");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        JLabel label1 = new JLabel("KẾT NỐI SERVER", JLabel.CENTER);
        label1.setFont(new Font("Arial", Font.BOLD, 20));
        label1.setPreferredSize(new Dimension(350, 50));
        add(label1, BorderLayout.NORTH);

        JPanel panelCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JLabel ipLabel = new JLabel("IP:");
        ipLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panelCenter.add(ipLabel);

        JTextField ipField = new JTextField("127.0.0.1", 9);
        panelCenter.add(ipField);

        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panelCenter.add(portLabel);

        JTextField portField = new JTextField("12345", 4);
        panelCenter.add(portField);

        add(panelCenter, BorderLayout.CENTER);

        JPanel panelSouth = new JPanel();
        JButton connectButton = new JButton("Kết nối");
        connectButton.setFont(new Font("Arial", Font.PLAIN, 14));
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = ipField.getText().trim();
                String portText = portField.getText().trim();

                try {
                    int port = Integer.parseInt(portText);
                    if (port < 1 || port > 65535) {
                        JOptionPane.showMessageDialog(null, "Port phải nằm trong khoảng 1-65535.", "SAI PORT", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Khởi động client
                    new Thread(() -> new RunClient(ip, port)).start();
                    dispose();

                    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Port không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panelSouth.add(connectButton);
        panelSouth.setBorder(BorderFactory.createEmptyBorder(5, 0, 22, 0));
        add(panelSouth, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        new ConnectServer();
    }
}
