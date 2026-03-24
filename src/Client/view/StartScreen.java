/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 *
 * @author Admin
 */
public class StartScreen extends JFrame{
    private boolean ready = false;
    private String name;
    private static boolean isDialogOpen = false;
    private ArrayList<Player> players = new ArrayList<>();
    private Point initialClick;
    public StartScreen() { 
        initComponent();
        setVisible(true);
    }
    private void initComponent() {
// Thêm sự kiện để di chuyển JFrame
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint(); // Lấy tọa độ khi nhấn
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Cập nhật vị trí JFrame khi kéo chuột
                setLocation(getX() + e.getX() - initialClick.x, getY() + e.getY() - initialClick.y);
            }
        });
        JLabel label = new JLabel("Kéo để di chuyển JFrame", SwingConstants.CENTER);
        add(label);
    
        setUndecorated(true); // bỏ thanh tiêu đề
        setSize(800, 450); // ngang,doc
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Hiển thị giữa màn hình                
        ImageIcon backgroundIcon = new ImageIcon("src/Client/view/resources/StartScreen.png");
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        setLayout(new BorderLayout());
        add(backgroundLabel);
        
        JButton button = new JButton("PLAY");
        JButton button2 = new JButton("EXIT");
        button2.addActionListener(ev -> System.exit(0));                
        button.setBounds(75, 200, 200, 44); // x, y, width, height
        ImageIcon B = new ImageIcon("src/Client/view/resources/StartButton.png");
        button.setIcon(B); 
        button.setBorderPainted(false);
        button.setHorizontalTextPosition(JButton.CENTER); //ngang
        button.setVerticalTextPosition(JButton.CENTER);
        button.setFont(new Font("", Font.BOLD, 17));
        button.addActionListener(new ActionListener() { 
        @Override
        public void actionPerformed(ActionEvent e) {
            name = JOptionPane.showInputDialog(null, "Nhập tên của bạn:");
            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Vui lòng nhập tên để bắt đầu chơi", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                ready = true;
            }
        }
    
});
        button2.setBounds(75, 270, 200, 44);
        ImageIcon B2 = new ImageIcon("src/Client/view/resources/StartButton.png");
        button2.setIcon(B2); 
        button2.setBorderPainted(false);
        button2.setHorizontalTextPosition(JButton.CENTER);
        button2.setVerticalTextPosition(JButton.CENTER);
        button2.setFont(new Font("", Font.BOLD, 17)); 
                     
        backgroundLabel.add(button); 
        backgroundLabel.add(button2);
    }
    
    public String getUsername() {
        while (!ready) {
            try {
                Thread.sleep(100); // Chờ người dùng nhập tên
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return name;
    }

    public void showNotification(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    public void resetForRetry() {
        // Đặt lại cờ sẵn sàng để nhận tên mới
        ready = false;
        name = null;
    }
}
