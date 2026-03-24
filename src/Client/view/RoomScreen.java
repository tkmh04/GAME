/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.view;

import Client.RunClient;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomScreen extends JFrame {
    private Point initialClick;
    
    private List<Player> players = new ArrayList<>();
    private String name;
    private List<Room> rooms;
    private JLabel statusLabel;
    DefaultTableModel playerModel;
    JTable playerTable;
    private RunClient client;
    DefaultTableModel roomModel;
    JTable roomTable;
    String roomId;
    JButton quickMatch;
    JButton exitButton;
    SetupScreen sup;
    MainScreen main;

    public RoomScreen(String username, RunClient client) {         
        this.name = username;
        this.client = client;
        initComponent();
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

        setUndecorated(true); 
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//hide//visblefalse//sys0?
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 15);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        exitButton = new JButton();
        exitButton.setIcon(new ImageIcon("src/Client/view/resources/exit.png"));
        exitButton.setBorderPainted(false);
        exitButton.setFocusPainted(false);
        exitButton.setContentAreaFilled(false);
        exitButton.addActionListener(e -> {     
            System.exit(0);    
        });

        topPanel.add(exitButton, BorderLayout.EAST);

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));

        JButton nameButton = new JButton("Tên: " + name);
        nameButton.setPreferredSize(new Dimension(150, 34));
        nameButton.setFont(defaultFont);
        nameButton.setBackground(new Color(173, 216, 230));
        nameButton.setFocusPainted(false);
        nameButton.setBorderPainted(false);    

        rightPanel.add(nameButton);

        rightPanel.setBorder(new EmptyBorder(15, 10, 15, 10));
        topPanel.add(rightPanel, BorderLayout.WEST);
        rightPanel.setOpaque(false);

        // Table Style
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", Color.DARK_GRAY);
        UIManager.put("Table.selectionBackground", new Color(220, 240, 255));
        UIManager.put("Table.gridColor", Color.LIGHT_GRAY);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("TableHeader.background", new Color(245, 245, 245));
        UIManager.put("TableHeader.foreground", Color.BLACK);

        // Khởi tạo bảng với các cột
        String[] playerColumns = {"Mã ID", "Tên người chơi", "Trạng thái"};
        playerModel = new DefaultTableModel(playerColumns, 0);

        playerTable = new JTable(playerModel);
        playerTable.setRowHeight(25);
        // Thêm bảng vào panel
        JScrollPane scrollPane = new JScrollPane(playerTable);
        add(scrollPane, BorderLayout.CENTER);

        // Room Table
        String[] roomColumns = {"Mã phòng", "Xem"};
        roomModel = new DefaultTableModel(roomColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Chỉ cho cột "Xem" được bấm
            }
        };

        roomTable = new JTable(roomModel);
        roomTable.setRowHeight(25);

        // nút xem, Cell Renderer & Editor for the "Xem" column
        roomTable.getColumn("Xem").setCellRenderer(new EyeButtonRenderer());
        roomTable.getColumn("Xem").setCellEditor(new EyeButtonEditor());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(4, 10, 20, 10));

        statusLabel = new JLabel("Hãy tìm đối thủ!");
        statusLabel.setFont(defaultFont);
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        // Panel chứa 2 bảng
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        centerPanel.setBackground(Color.WHITE);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new TitledBorder("Người chơi online"));
        leftPanel.add(new JScrollPane(playerTable), BorderLayout.CENTER);

        JButton sendRequestButton = new JButton("Mời ghép đấu");
        sendRequestButton.addActionListener(e -> handleSendFriendRequest());

        leftPanel.add(sendRequestButton, BorderLayout.SOUTH);

        JPanel rightTablePanel = new JPanel(new BorderLayout());
        rightTablePanel.setBackground(Color.WHITE);
        rightTablePanel.setBorder(new TitledBorder("Phòng đang đấu"));
        rightTablePanel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        centerPanel.add(leftPanel);
        centerPanel.add(rightTablePanel);

        // Bottom
        quickMatch = new JButton("Ghép nhanh");
        quickMatch.setFont(defaultFont);
        quickMatch.setBackground(new Color(173, 216, 230));
        quickMatch.setFocusPainted(false);
        quickMatch.setBorderPainted(false);
        quickMatch.setPreferredSize(new Dimension(150, 34)); 
        quickMatch.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Kiểm tra trạng thái của nút quickMatch và gửi yêu cầu tương ứng tới server
                if (quickMatch.getText().equals("Huỷ ghép nhanh")) {
                    // Gửi yêu cầu huỷ ghép nhanh tới server
                    client.sendCancelQuickMatch();
                    quickMatch.setText("Ghép nhanh");  // Cập nhật lại text của nút
                    statusLabel.setText("Hãy tìm đối thủ!");
                    exitButton.setEnabled(true);
                } else {
                    // Gửi yêu cầu ghép nhanh tới server
                    client.sendQuickMatchRequest();
                    quickMatch.setText("Huỷ ghép nhanh");  // Cập nhật lại text của nút
                    statusLabel.setText("Đang chờ ghép...");  // Cập nhật lại label trạng thái
                    exitButton.setEnabled(false);
                }
            } catch (IOException ex) {
                Logger.getLogger(RoomScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    });
        quickMatch.setFont(defaultFont);
        bottomPanel.add(quickMatch, BorderLayout.EAST);

        // Add to frame
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        new Thread(this::listenToServer).start();
    }

class EyeButtonRenderer extends JPanel implements TableCellRenderer {

    public EyeButtonRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0)); // Giữa ô
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        removeAll();

        JButton button = new JButton(new ImageIcon("src/Client/view/resources/eye.png"));
        button.setPreferredSize(new Dimension(24, 24));
        button.setBorder(null);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);

        add(button);

        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
        }

        return this;
    }
}
class EyeButtonEditor extends AbstractCellEditor implements TableCellEditor {

    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    private final JButton button;

    public EyeButtonEditor() {
        button = new JButton(new ImageIcon("src/Client/view/resources/eye.png"));
        button.setPreferredSize(new Dimension(24, 24));
        button.setBorder(null);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setOpaque(true);
        panel.add(button);
        button.addActionListener(e -> {
    int row = roomTable.getSelectedRow();
           
    if (row < 0) {
      JOptionPane.showMessageDialog(null, "Vui lòng chọn một phòng.");
      return;
    }
    String idroom = roomTable.getValueAt(row, 0).toString();
    Room found = findRoomById(idroom);
    if (found != null) {
      // Hiện dialog
      JOptionPane.showMessageDialog(
        null,
        "2 người chơi trong phòng " + idroom + " là: "
        + found.getPlayerOne().getPlayerName() + " và "
        + found.getPlayerTwo().getPlayerName()
      );
     
    } else {
      JOptionPane.showMessageDialog(null, "Không tìm thấy phòng: " + idroom);
    }
    fireEditingStopped();
});
    }

    @Override
public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
    // ✅ Chủ động cập nhật lại vùng chọn khi click vào nút
    table.changeSelection(row, column, false, false);
    panel.setBackground(table.getSelectionBackground());
    return panel;
}

    @Override
    public Object getCellEditorValue() {
        return null;
    }
}
    
    private void handleSendFriendRequest() {
        int row = playerTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người chơi để gửi lời mời.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedName = (String) playerModel.getValueAt(row, 1); // nếu cột 1 là tên
        String status       = (String) playerModel.getValueAt(row, 2); // nếu cột 0 là trạng thái

        // Kiểm tra null và điều kiện
        if (!selectedName.equals(name) && "Online".equals(status)) {
            try {
                client.sendFriendRequest(selectedName);
                statusLabel.setText("Đang chờ " + selectedName + " đồng ý...");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gửi yêu cầu thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Không thể gửi yêu cầu: bạn chọn người đó đang đấu với người khác.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    void setTextWait() {
        statusLabel.setText("Đợi server tìm bạn muốn ghép...");
    }
    
    private void listenToServer() {
        new Thread(() -> {
            try {
                while (true) {
                    // Nhận đối tượng từ server
                    Object obj = client.receive();
                    if (obj == null) {
                        System.err.println("Dữ liệu nhận được là null từ server.");
                        continue;
                    }

                    if (obj instanceof String) {
                        String type = (String) obj;
                        if ("null".equals(type)) {
                            System.err.println("Chuỗi nhận được là 'null', không hợp lệ.");
                            continue;
                        }

                        switch (type) {
                            case "PLAYER_LIST":
                                // Nhận danh sách người chơi
                                players = client.getPlayerList();
                                if (players != null && !players.isEmpty()) {
                                    System.out.println("Danh sách người chơi: " + players.size() + " người.");
                                    for (Player player : players) {
                                        System.out.println(player.toString());
                                    }
                                    updatePlayerList(players); // Cập nhật danh sách người chơi
                                }
                                break;

                            case "ROOM_LIST":
                                // Nhận danh sách phòng
                                rooms = client.getRoomList();
                                if (rooms != null && !rooms.isEmpty()) {
                                    for (Room room : rooms) {
                                        System.out.println("Room ID: " + room.getRoomId());
                                    }
                                    updateRoomList(rooms); // Cập nhật danh sách phòng
                                }
                                break;
                                // Xử lý các loại thông điệp khác
                            default:
                                handleMessage(type);
                                break;
                        }
                    } else {
                        System.err.println("Dữ liệu không xác định từ server: " + obj);
                    }
                }
            } catch (Exception e) {
                System.err.println("Mất kết nối đến server!");
//                e.printStackTrace();
            }
        }).start();
    }

    /**
     * xử lý từ chối thì hiện status/option hay đồng ý hiện đang đấu và vô phòng
     * @param message 
     */
    private void handleMessage(String message) throws IOException, ClassNotFoundException, InterruptedException, Exception {
        if (message.startsWith("FRIEND_REQUEST:")) {
            String fromUser = message.split(":")[1];
            int response = JOptionPane.showOptionDialog(this,
                    fromUser + " gửi yêu cầu muốn ghép với bạn",
                    "Lời mời ghép đấu",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, null, null);
            try {
                client.sendFriendResponse(fromUser, response == JOptionPane.YES_OPTION);                
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (message.startsWith("FRIEND_RESPONSE:")) {
            String[] parts = message.split(":");
                String fromUser = parts[1];
                String status   = parts[2];
                System.out.println(status);
                if ("Đồng ý".equalsIgnoreCase(status)) {
                // Gửi yêu cầu tạo phòng
                    dispose();   
            } else if ("Deny".equalsIgnoreCase(status)) {
                // Gửi yêu cầu tạo phòng
                statusLabel.setText("Hãy tìm đối thủ!");
            }
            else if ("isDenied".equalsIgnoreCase(status)) {
                // Gửi yêu cầu tạo phòng
                statusLabel.setText(fromUser + " đã từ chối! Vui lòng thử lại!");
            }
                int row = findRowByName(fromUser);
                if (row != -1) {
                    playerModel.setValueAt("Online", row, 2);
                }            
        }
        
        /*
         * chưa fix xong
         */
        
        else if (message.startsWith("ROOM_CURR:")) {            
            dispose();
        // Xử lý thông điệp ROOM_CURR
        String roomIdString = message.substring("ROOM_CURR:".length());        
        try {
            // Chuyển đổi ID phòng từ String sang Integer
            roomId = roomIdString;   
            System.out.println("Chuyển vào màn hình SetupScreen với Room ID: " + roomId);
            Object obj = client.receive(); //Tìm phòng từ danh sách
            
             if (obj instanceof Room) {
            Room room = (Room) obj;
                // Truyền đối tượng phòng vào SetupScreen
                sup = new SetupScreen(client ,room, name);                
             }
        }catch (NumberFormatException e) {
            // Xử lý nếu không thể chuyển đổi ID thành số
            System.out.println("ID phòng không hợp lệ: " + roomIdString);
        }}
        
        if ("GAME_READY".equals(message)) {

            try {
                // Nhận bảng và thông tin của đối thủ từ server

                Object boardObj = client.receive();  // Nhận bảng đối thủ
                Object opponentNameObj = client.receive();  // Nhận tên đối thủ
                Object opponentIdObj = client.receive();  // Nhận mã đối thủ
                Object turnObj = client.receive();

                if (boardObj instanceof Board && opponentNameObj instanceof String && opponentIdObj instanceof Integer && turnObj instanceof String) {
                    Board opponentBoard = (Board) boardObj;  // Ép kiểu bảng đối thủ
                    String opponentName = (String) opponentNameObj;  // Ép kiểu tên đối thủ
                    int opponentId = (Integer) opponentIdObj;  // Ép kiểu mã đối thủ
                    String turn = (String) turnObj;  // Ép kiểu lượt chơi

                    // Kiểm tra và xác định lượt chơi

                    Boolean turnTrue = null;
                    if (turn != null) {
                        if ("GAME_TURN_T".equals(turn)) {
                            turnTrue = true;
                        } else if ("GAME_TURN_F".equals(turn)) {
                            turnTrue = false;

                        }

                    } else {
                        System.err.println("Lượt chơi không hợp lệ (null)");
                    }

                    // Hiển thị thông tin phòng
                    System.out.println("Thông tin phòng mã phòng bạn chơi: " + roomId);
                    System.out.println("Bảng của đối thủ: " + opponentBoard);
                    System.out.println("Tên đối thủ: " + opponentName);
                    System.out.println("Mã đối thủ: " + opponentId);
                    System.out.println("Lượt của bạn: " + turnTrue);

                    // Xử lý logic trò chơi dựa trên thông tin đã nhận
                    sup.handleGameReady(roomId, opponentBoard, opponentName, opponentId, turnTrue);
                } else {
                    System.err.println("Dữ liệu không hợp lệ. Vui lòng kiểm tra lại!");
                }

                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Lỗi khi nhận dữ liệu từ server: " + e.getMessage());
                        e.printStackTrace();
                    }
        }
        else if (message.startsWith("ONE_READY")) {
            sup.setTextReady();
        }
        else if (message.startsWith("WAITING_FOR_MATCH")) {
            setTextWait();
        }
        else if (message.startsWith("OPP_EXIT")) {
            main = sup.getMainScreen();
            // Kiểm tra nơi hiển thị thông báo và gọi phương thức thích hợp
            if (sup != null) {
                // Nếu sup đang hiển thị
                sup.showExitDialog("Oh no! Đối thủ của bạn đã rời trận.");
            
            } else if (main != null && main.isVisible()) {
                // Nếu main đang hiển thị
                main.showResultDialog("Đối thủ của bạn đã rời trận!");
            }
        }

        else if (message.startsWith("GAME_")) {
            main = sup.getMainScreen();
            String command = (String) message;
            switch (command) {                
            case "GAME_WIN":
                SwingUtilities.invokeLater(() -> {
                System.err.println("---------KẾT THÚC GAME---------");
                main.showResultDialog("🏆 YOU WIN 🏆");
                });
                break;
                
            case "GAME_LOSE":
                SwingUtilities.invokeLater(() -> {
                System.err.println("---------KẾT THÚC GAME---------");
                main.showResultDialog("💔 YOU LOSE 💔");
                });
                break;
            }
    }
    else if (message.startsWith("FIRE_UPDATE")) {
        main = sup.getMainScreen();
            String[] parts = message.split(" ");
                    if (parts.length == 3) {
                        String position = parts[1]; // Vị trí, ví dụ: A1
                        String result = parts[2];  // Kết quả, ví dụ: MISS

                        System.out.println("--Đã cập nhật lưới");
                        // Cập nhật lưới theo vị trí và kết quả
                        SwingUtilities.invokeLater(() -> {
                            main.updateGrid(position, result);
                        });
                    }
    }
        else if (message.startsWith("SHIP_SUNK")) {
    main = sup.getMainScreen();
    String[] parts = message.split(" ", 2);
    System.out.println("Parts length: " + parts.length);
    if (parts.length == 2) {
        String shipName = parts[1].trim();
        System.out.println("Ship name to display: " + shipName);
        SwingUtilities.invokeLater(() -> {
            main.appendToChatArea("Tàu " + shipName + " đã bị đánh chìm!");
            System.out.println("Displayed message for: " + shipName);
        });
    }
}

        else if (message.startsWith("TURN_")) {
            main = sup.getMainScreen();
            Boolean turn2 = null;
                if ("TURN_F".equals(message)) {
                    turn2 = false;
                    SwingUtilities.invokeLater(() -> {
            main.appendToChatArea("Lượt đối thủ!");
            main.appendToChatArea("Hãy chờ đến lượt bạn");
            main.enableGridClicks(false);
            main.resetTimeDisplay(main.score1);
            main.resetTimeDisplay(main.score2);
            main.startCountdown(main.score1);
            
        });
                }
                else if ("TURN_T".equals(message)) {
                    turn2 = true;    
                    SwingUtilities.invokeLater(() -> {
            main.resetTimeDisplay(main.score1);
            main.resetTimeDisplay(main.score2);
            main.startCountdown(main.score2);            
            main.enableGridClicks(true);
            main.appendToChatArea("Lượt của bạn!");
        });
                }               
                }
        else if (message.startsWith("CHAT")) {
    // Lấy nội dung tin nhắn từ server
    main = sup.getMainScreen();
    String chatMessage = message.substring(5); // Bỏ tiền tố "CHAT " để lấy nội dung

    // Tách tên người gửi và nội dung
    int colonIndex = chatMessage.indexOf(":"); // Tìm vị trí dấu ":" để phân biệt tên và nội dung
    if (colonIndex != -1) {
        String senderName = chatMessage.substring(0, colonIndex).trim(); // Lấy tên người gửi
        String messageContent = chatMessage.substring(colonIndex + 1).trim(); // Lấy nội dung tin nhắn

        // Hiển thị tên người gửi và nội dung tin nhắn
        SwingUtilities.invokeLater(() -> {
        main.appendToChatArea(senderName + ": " + messageContent);
        });

    } else {
        System.out.println("Cú pháp tin nhắn không hợp lệ.");
    }
}
 else {
        // Xử lý các loại tin nhắn khác từ server
        System.out.println("Thông điệp khác: " + message);
    }
    }
        
    private void updatePlayerList(List<Player> players) {
    SwingUtilities.invokeLater(() -> {

        // Xóa tất cả các hàng cũ
        playerModel.setRowCount(0);
        // Cập nhật lại đang đấu
        // Thêm lại từng người chơi vào bảng
        int stt = 1;
        for (Player player : players) {
            // Kiểm tra nếu tên người chơi không phải là bạn
            if (!player.getPlayerName().equals(name)) {
                String status = player.getStatus() ? "Online" : "Đang đấu";  // Trạng thái của người chơi
                playerModel.addRow(new Object[]{stt++, player.getPlayerName(), status});
            }
        }
    });
    
}
// ko xoá chỉ thêm hàng dưới
    private void updateRoomList(List<Room> rooms) {
    SwingUtilities.invokeLater(() -> {
        roomModel.setRowCount(0); // Xóa danh sách cũ
        if (rooms == null || rooms.isEmpty()) {
            System.out.println("Danh sách phòng rỗng.");
            return;
        }

        for (Room room : rooms) {
            roomModel.addRow(new Object[]{room.getRoomId()});
        }
    });
}

    
    private int findRowByName(String name) {
        for (int i = 0; i < playerModel.getRowCount(); i++) {
            if (playerModel.getValueAt(i, 1).equals(name)) {
                return i;
            }
        }
        return -1;
    }
    private Room findRoomById(String roomId) {
    // Duyệt qua danh sách các phòng (giả sử bạn đang sử dụng Map hoặc List)
    for (Room room : rooms) {  // Nếu rooms là List<Room>
        if(room.getRoomId().equals(roomId)) {
    // Thực hiện các hành động khi roomId khớp với idroom
}

            return room; // Trả về phòng khi tìm thấy ID trùng khớp
        }
    return null;
    }
}