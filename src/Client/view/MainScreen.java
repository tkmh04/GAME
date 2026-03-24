package Client.view; 

import Client.RunClient;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class MainScreen extends JFrame{
    private static boolean isDialogOpen = false;
    
    private boolean canClick = true; 
    private String name;
    private JTextArea chatArea;
    private JTextField messageField;
    private int id1 = 1;
    private int id2 = 2;
    private Room room; //
    private RunClient client;
    private InfoRoom playerInfo;
    private InfoRoom opponentInfo;
    private String id;
    JLabel score1, score2;
    private boolean isPlayerTurn;
    JPanel smallGrid, largeGrid;
    private boolean isFirstTurn = true;
    private Timer currentTimer; // Biến lưu trữ Timer đang chạy


    public MainScreen(RunClient client,  InfoRoom playerInfo, InfoRoom opponentInfo, boolean turn) {
        this.client = client;
        this.playerInfo = playerInfo;
        this.opponentInfo = opponentInfo;
        
        setupGUI();
        handleTurn(turn);
    }

    private void setupGUI() {
        setTitle("Màn hình game phòng " + playerInfo.getId() + " của người chơi " +  playerInfo.getName());
        System.out.println("--Bảng người chơi: "+ playerInfo.getBoard().getShips());
        System.out.println("--Bảng đối thủ: "+ opponentInfo.getBoard());
        
        if(client.isConnected()) {
            System.out.println("đã kết nối !!!");
            
        } else System.out.println("không kết nối!");
        
       
        setSize(950, 550); // pack();
        setLocationRelativeTo(null); // Hiển thị giữa màn hình
        setResizable(false);
        setLayout(new BorderLayout());
        
        JButton button3 = new JButton();
        button3.setBounds(10, 10, 51, 49);
        ImageIcon B3 = new ImageIcon("src/Client/view/resources/Setting.png");
        button3.setIcon(B3); 
        button3.setBorderPainted(false);
        button3.setContentAreaFilled(false);
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Tạo JFrame Settings
                JFrame settingsFrame = new JFrame("Settings");
                settingsFrame.setSize(300, 200);
                settingsFrame.setUndecorated(true); // Không hiển thị thanh tiêu đề
                settingsFrame.setLayout(new GridLayout(5, 1));

                // Đặt vị trí hiển thị gần nút Settings
                Point location = button3.getLocationOnScreen();
                settingsFrame.setLocation(location.x + button3.getWidth(), location.y + button3.getHeight());


                // Tắt/Bật âm thanh
                JToggleButton soundToggle = new JToggleButton("Sound: OFF");
                soundToggle.addActionListener(ev -> {
                    soundToggle.setText(soundToggle.isSelected() ? "Sound: ON" : "Sound: OFF");
                });

                // Thoát
                JButton exitButton = new JButton("Exit");
                exitButton.addActionListener(ev -> {
                    try {
                        client.sendExit(); // Gửi tín hiệu thoát tới server
                    } catch (IOException ex) {
                        Logger.getLogger(SetupScreen.class.getName()).log(Level.SEVERE, null, ex);
                    }
            
                    System.exit(0); // Thoát chương trình
                });

                // Quay lại
                JButton backButton = new JButton("Back");
                backButton.addActionListener(ev -> {
                    JOptionPane.showMessageDialog(settingsFrame, "Back action triggered.");
                });

                // Hướng dẫn
                JButton guideButton = new JButton("Guide");
                guideButton.addActionListener(ev -> {
                    isDialogOpen = true; // Đặt cờ để ngăn JFrame đóng
                    JOptionPane.showMessageDialog(settingsFrame, "This is the guide for this class.");
                    isDialogOpen = false; // Reset cờ sau khi JOptionPane đóng
                });

                // Chức năng khác (Placeholder)
                JButton otherFunctionButton = new JButton("Other Functions");
                otherFunctionButton.addActionListener(ev -> {
                    isDialogOpen = true; // Đặt cờ để ngăn JFrame đóng
                    JOptionPane.showMessageDialog(settingsFrame, "Develop your own function here.");
                    isDialogOpen = false; // Reset cờ sau khi JOptionPane đóng
                });

                // Thêm các nút vào JFrame
                settingsFrame.add(soundToggle);
                settingsFrame.add(exitButton);
                settingsFrame.add(backButton);
                settingsFrame.add(guideButton);
                settingsFrame.add(otherFunctionButton);

                // Đóng Settings Frame khi mất focus (trừ khi JOptionPane đang mở)
                settingsFrame.addWindowFocusListener(new WindowFocusListener() {
                    @Override
                    public void windowGainedFocus(WindowEvent e) {
                        // Không làm gì khi lấy lại focus
                    }

                    @Override
                    public void windowLostFocus(WindowEvent e) {
                        if (!isDialogOpen) { // Chỉ đóng JFrame khi không có dialog mở
                            settingsFrame.dispose();
                        }
                    }
                });

                settingsFrame.setVisible(true);
            }
        });
        
        
        
        JPanel center = new JPanel();
        center.setLayout(null);
        center.setBackground(Color.WHITE);
        center.add(button3);
       
        JLabel player1Label = createLabel("Bảng của bạn: " + playerInfo.getName(), 174 - 100, 60, 300, 30);
        score1 = createLabel("0:00", 174, 90, 100, 30);//0:00
        JLabel player2Label = createLabel("Bảng của đối thủ: " + opponentInfo.getName(), 509 - 100, 60, 300, 30);
        score2 = createLabel("0:00", 509, 90, 100, 30);
        center.add(player1Label);
        center.add(score1);
        center.add(player2Label);
        center.add(score2);
        
        // Bảng lưới nhỏ (5x5)
        smallGrid = createGrid(10, 10, 220, 220, false); //22x22
        smallGrid.setBounds(114, 130, 220, 220);

        // Bảng lưới lớn (10x10)
        largeGrid = createGrid(10, 10, 350, 350, true);  //3chat.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));5x35
        largeGrid.setBounds(385, 130, 350, 350);
        
        center.add(largeGrid);
        center.add(smallGrid);
        JPanel chat = new JPanel(new BorderLayout());
        chat.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chat.setBackground(Color.WHITE);

        add(center, BorderLayout.CENTER);
        add(chat, BorderLayout.EAST);
        chat.setPreferredSize(new Dimension(190, getHeight()));
        center.setPreferredSize(new Dimension(760, getHeight()));
       
        chatArea = new JTextArea(20, 50);
        chatArea.setBorder(BorderFactory.createTitledBorder("Khu vực chat"));
        chatArea.setEditable(false);
        chat.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Gửi");
        panel.add(messageField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        chat.add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());
        
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private JLabel createLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setBounds(x, y, width, height);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        return label;
    }

        // Hàm tạo lưới ô vuông
    private JPanel createGrid(int rows, int cols, int width, int height, boolean isLargeGrid) {
    JPanel gridPanel = new JPanel();
    gridPanel.setLayout(new GridLayout(rows, cols));
    gridPanel.setPreferredSize(new Dimension(width, height));
    char[] rowLabels = "ABCDEFGHIJ".toCharArray();
    JPanel[][] cells = new JPanel[rows][cols];

    // Danh sách vị trí tàu
    ArrayList<String> shipPositions = new ArrayList<>();
    if (isLargeGrid) {
        for (Ship ship : opponentInfo.getBoard().getShips()) {
            shipPositions.addAll(ship.getPositions());
        }
    } else {
        for (Ship ship : playerInfo.getBoard().getShips()) {
            shipPositions.addAll(ship.getPositions());
        }
    }

    for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
            JPanel cell = new JPanel();
            cell.setLayout(null);
            cell.setBorder(BorderFactory.createLineBorder(Color.decode("#5656ef")));
            cell.setBackground(Color.decode("#d0ecf6")); // Màu mặc định
            gridPanel.add(cell);
            cells[row][col] = cell;

            String position = rowLabels[row] + String.valueOf(col + 1);

            // Hiển thị vị trí tàu của người chơi trên smallGrid
            if (!isLargeGrid && shipPositions.contains(position)) {
                cell.setBackground(Color.decode("#56aeff")); // Màu đỏ cho tàu của người chơi
            }

            // Thiết lập hành vi cho largeGrid
            if (isLargeGrid) { 
                cell.addMouseListener(new java.awt.event.MouseAdapter() {
                    Color originalColor = cell.getBackground();

                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        // Chỉ hover nếu ô chưa có ảnh trúng/miss
                        if (cell.getComponentCount() == 0) {
                            JLabel imageLabel = new JLabel(new ImageIcon("src/Client/view/resources/nham2.png")); // Đường dẫn ảnh nhắm
                            imageLabel.setBounds(0, 0, 35, 35);
                            cell.add(imageLabel);
                            cell.revalidate(); // Cập nhật giao diện
                            cell.repaint();
                        }
                    }

                    @Override
public void mouseExited(java.awt.event.MouseEvent evt) {
    // Kiểm tra nếu ô chỉ chứa ảnh "nhắm" (ảnh nham2.png) và không chứa ảnh hit/miss
    if (cell.getComponentCount() == 1) {
        // Kiểm tra xem ảnh trong ô có phải là ảnh "nhắm" không (dựa vào tên ảnh)
        JLabel label = (JLabel) cell.getComponent(0);
        if (label.getIcon() != null && label.getIcon().toString().contains("nham2.png")) {
            // Nếu đúng là ảnh "nhắm", xóa ảnh và khôi phục màu gốc
            cell.removeAll();
            cell.setBackground(originalColor); // Khôi phục màu gốc
            cell.revalidate(); // Cập nhật giao diện
            cell.repaint();
        }
    }
}


                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        if (!canClick) {
                            return; // Không cho phép bắn nếu không đến lượt
                        }
                        if (cell.getComponentCount() > 1) {
                            return;
                        }
                        boolean isHit = false;
                        if (shipPositions.contains(position)) {
                            // Hiển thị ảnh bắn trúng
                            JLabel hitLabel = new JLabel(new ImageIcon("src/Client/view/resources/hitlarge2.png")); // Đường dẫn ảnh bắn trúng
                            hitLabel.setBounds(0, 0, cell.getWidth(), cell.getHeight());
                            cell.removeAll(); // Xóa ảnh hover trước khi thêm ảnh trúng/miss
                            cell.add(hitLabel);
                            chatArea.append("Bạn đã bắn trúng vị trí: " + position + "\n");
                            isHit = true;
                        } else {
                            // Hiển thị ảnh bắn trượt
                            JLabel missLabel = new JLabel(new ImageIcon("src/Client/view/resources/misslarge2.png")); // Đường dẫn ảnh bắn trượt
                            missLabel.setBounds(0, 0, cell.getWidth(), cell.getHeight());
                            cell.removeAll(); // Xóa ảnh hover trước khi thêm ảnh trúng/miss
                            cell.add(missLabel);
                            chatArea.append("Bạn đã bắn trượt vị trí: " + position + "\n");
                        }

                        // Cập nhật lại giao diện ô
                        cell.revalidate();
                        cell.repaint();

                        // Gửi thông tin bắn lên server
                        try {
                            client.sendFirePosition(playerInfo, position, isHit);
                        } catch (IOException e) {
                            chatArea.append("Lỗi khi gửi thông tin bắn: " + e.getMessage() + "\n");
                        }

                        // Nếu bắn trúng, không tắt khả năng click
                        enableGridClicks(isHit);
                    }
                });
            }
        }
    }

    return gridPanel;
}
    
    public void startCountdown(JLabel scoreLabel) {
    if (currentTimer != null) {
        currentTimer.stop(); // Dừng Timer trước đó nếu tồn tại
    }

    currentTimer = new Timer(1000, new ActionListener() {
        int timeLeft = 30; // Thời gian bắt đầu từ 30 giây

        @Override
        public void actionPerformed(ActionEvent e) {
            timeLeft--; // Giảm 1 giây

            // Cập nhật thời gian vào JLabel theo định dạng "0:xx"
            int minutes = timeLeft / 60; // Tính phút
            int seconds = timeLeft % 60; // Tính giây
            scoreLabel.setText(String.format("%d:%02d", minutes, seconds));

            // Khi hết thời gian (0 giây)
            if (timeLeft <= 0) {
                ((Timer) e.getSource()).stop(); // Dừng Timer
                currentTimer = null; // Xóa tham chiếu đến Timer
                chatArea.append("Hết thời gian cho lượt bắn!\n");
            }
        }
    });
    currentTimer.start(); // Bắt đầu đếm ngược
}

        public void resetTimeDisplay(JLabel scoreLabel) {
    if (currentTimer != null) {
        currentTimer.stop(); // Dừng Timer hiện tại
        currentTimer = null; // Xóa tham chiếu đến Timer
    }
    scoreLabel.setText("0:00"); // Hiển thị "0:00"
}
  
    
    void handleTurn(Boolean turn) {
    isPlayerTurn = turn;

    if (isFirstTurn) {
        if (isPlayerTurn) {
            showCountdownDialog("Bạn bắt đầu trước!", score2);
        } else {
            enableGridClicks(false);//
            showWaitingDialog(score1);
        }
        isFirstTurn = false;
    }
    
}
    void enableGridClicks(boolean enable) {
    canClick = enable;
    }
   
    private void showCountdownDialog(String message, JLabel scoreLabel) {
    // Tạo dialog đếm ngược
    JDialog countdownDialog = new JDialog(this, message, true);
    countdownDialog.setSize(300, 150);
    countdownDialog.setLocationRelativeTo(this);

    // Tạo JLabel hiển thị thời gian
    JLabel countdownLabel = new JLabel("3 giây còn lại ...", SwingConstants.CENTER);
    countdownLabel.setFont(new Font("Arial", Font.BOLD, 20));
    countdownDialog.add(countdownLabel);

    // Timer đếm ngược trong dialog
    Timer countdownTimer = new Timer(1000, new ActionListener() {
        int timeLeft = 3; // Đếm ngược từ 5 giây

        @Override
        public void actionPerformed(ActionEvent e) {
            timeLeft--; // Giảm 1 giây
            countdownLabel.setText(timeLeft + " giây còn lại...");

            if (timeLeft <= 0) {
                ((Timer) e.getSource()).stop(); // Dừng Timer
                countdownDialog.dispose(); // Đóng dialog
                startCountdown(scoreLabel); // Bắt đầu đếm ngược chính thức
            }
        }
    });
    countdownTimer.start(); // Bắt đầu đếm ngược

    // Hiển thị dialog
    countdownDialog.setVisible(true);
}

    private void showWaitingDialog(JLabel scoreLabel) {
        // Tạo dialog đếm ngược chờ lượt đối thủ
        JDialog waitingDialog = new JDialog(this, "Đối thủ bắt đầu trước", true);
        waitingDialog.setSize(300, 150);
        waitingDialog.setLocationRelativeTo(this);

        // Tạo JLabel hiển thị thời gian
        JLabel waitingLabel = new JLabel("3 giây còn lại...", SwingConstants.CENTER);
        waitingLabel.setFont(new Font("Arial", Font.BOLD, 20));
        waitingDialog.add(waitingLabel);

        // Timer đếm ngược trong dialog
        Timer waitingTimer = new Timer(1000, new ActionListener() {
            int timeLeft = 3; // Đếm ngược từ 5 giây

            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--; // Giảm 1 giây
                waitingLabel.setText(timeLeft + " giây còn lại...");

                if (timeLeft <= 0) {
                    ((Timer) e.getSource()).stop(); // Dừng Timer
                    waitingDialog.dispose(); // Đóng dialog
                    startCountdown(scoreLabel); // Bắt đầu đếm ngược chính thức
                }
            }
        });
        waitingTimer.start(); // Bắt đầu đếm ngược

        // Hiển thị dialog
        waitingDialog.setVisible(true);
    }
       


// Phương thức cập nhật lưới
void updateGrid(String position, String result) {
    // Cập nhật bảng lưới nhỏ
    updateGridForBoard(smallGrid, position, result);

   
}



private void updateGridForBoard(JPanel gridPanel, String position, String result) {
    char[] rowLabels = "ABCDEFGHIJ".toCharArray();
    Component[] cells = gridPanel.getComponents(); // Lấy tất cả các ô trong lưới

    for (int row = 0; row < 10; row++) {
        for (int col = 0; col < 10; col++) {
            String cellPosition = rowLabels[row] + String.valueOf(col + 1);

            if (cellPosition.equals(position)) {
                JPanel cell = (JPanel) cells[row * 10 + col];

                // Đặt màu sắc dựa trên kết quả
                if ("HIT".equalsIgnoreCase(result)) {
                    cell.setBackground(Color.RED); // Trúng
                    chatArea.append("Đối thủ bắn trúng tại: " + position + "\n");
                } else if ("MISS".equalsIgnoreCase(result)) {
                    cell.setBackground(Color.WHITE); // Trượt
                    chatArea.append("Đối thủ bắn trượt tại: " + position + "\n");
                }

                cell.revalidate(); 
                cell.repaint(); // Tái vẽ lại ô sau khi thay đổi màu

                return; // Dừng sau khi tìm thấy và cập nhật vị trí
            }
        }
    }
}

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                // Tạo chuỗi theo định dạng yêu cầu
                String formattedMessage = String.format("CHAT %s %s %s", 
                                                        playerInfo.getId(),      // idroom
                                                        playerInfo.getPlayerId(),    // id người chơi
                                                        message);                // nội dung

                // Gửi chuỗi lên server
                client.sendMessage(formattedMessage);

                // Hiển thị tin nhắn trên khu vực chat
                chatArea.append("Bạn: " + message + "\n");
                messageField.setText("");

            } catch (IOException e) {
                chatArea.append("Lỗi khi gửi tin nhắn: " + e.getMessage() + "\n");
            }
        }
    }
    void appendToChatArea(String message) {
        // Phương thức để thêm thông điệp vào chatArea
        chatArea.append(message + "\n"); // Giả định chatArea là một JTextArea
    }
    
    public void showResultDialog(String message) {
        if (currentTimer != null) {
        currentTimer.stop();
        currentTimer = null; // Xóa tham chiếu đến Timer
        }
        // Tạo JDialog
        JDialog dialog = new JDialog(this, "Kết Quả Trận Đấu", true); // 'true' để modal
        dialog.setSize(400, 200);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Tạo JPanel cho nội dung
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout());

        // Thêm thông báo
        JLabel messageLabel = new JLabel(message);
            // Thay đổi font nếu thông báo phù hợp
        if ("Đối thủ của bạn đã rời trận!".equals(message)) {
            messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        } else {
            messageLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        }
        messageLabel.setForeground(Color.RED);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(messageLabel, BorderLayout.CENTER);

        // Tạo nút OK
        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Arial", Font.PLAIN, 14));
        okButton.addActionListener(e -> System.exit(0));

        // Thêm nút vào JPanel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Thêm panel vào dialog
        dialog.add(panel);

        // Hiển thị dialog ở giữa màn hình
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}