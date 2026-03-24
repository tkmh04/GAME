package Client.view;

import Client.RunClient;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.EmptyBorder;

public class SetupScreen extends JFrame {
    private Point initialClick;
    
    private static boolean isDialogOpen = false;
    private JButton reset, randomButton, start;
    private ArrayList<JPanel> cells; // List of grid cells
    private ArrayList<String> placedShips;
    private JRadioButton horizontalButton, verticalButton; // ngang, dọc
    private DefaultListModel<String> shipListModel;
    private JList<String> shipList;
    JLabel text;
    int playerId;
    Board board;
    private Map<Character, List<String>> shipPositions = new LinkedHashMap<>();
    String name;
    String opponentName;
    Room room;
    private RunClient client;
    private MainScreen main;
    
    public SetupScreen(RunClient client, Room room, String name) { 
        this.room = room;
        this.name = name;
        this.client = client;
        cells = new ArrayList<>();
        placedShips = new ArrayList<>();
        int currentPlayerId = room.getPlayerByName(name).getPlayerId();
        opponentName = room.getOpponentNameById(currentPlayerId);
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
    
        setUndecorated(true); 
        setSize(910, 633);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
        ImageIcon backgroundIcon = new ImageIcon("src/Client/view/resources/SetupScreen.jpg");
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        setContentPane(backgroundLabel);
        setLayout(new BorderLayout());

        // Settings button
        JButton settingsButton = createIconButton("src/Client/view/resources/Setting.png");
        settingsButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
        // Tạo JFrame Settings
        JFrame settingsFrame = new JFrame("Settings");
        settingsFrame.setSize(200, 200);
        settingsFrame.setUndecorated(true); // Không hiển thị thanh tiêu đề
        settingsFrame.setLayout(new GridLayout(4, 1)); // Chỉnh sửa lại số hàng để phù hợp

        // Đặt vị trí hiển thị gần nút Settings
        Point location = settingsButton.getLocationOnScreen();
        settingsFrame.setLocation(location.x + settingsButton.getWidth() - 50, location.y + settingsButton.getHeight());
        
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
        exitButton.setFocusPainted(false);
        exitButton.setContentAreaFilled(false);

        // Tạo các JLabel thay thế cho JButton
        JLabel idroom = new JLabel("Phòng: " + room.getRoomId());
        JLabel curr = new JLabel("Tên người chơi: " + name);
        JLabel opp = new JLabel("Tên đối thủ: " + opponentName);

        // Tùy chỉnh để không có màu nền và viền
        idroom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Thêm padding nếu cần
        curr.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Thêm padding nếu cần
        opp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Thêm padding nếu cần

        // Tùy chỉnh font nếu cần
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 15);
        idroom.setFont(defaultFont);
        curr.setFont(defaultFont);
        opp.setFont(defaultFont);

        idroom.setOpaque(true);
        idroom.setBackground(Color.WHITE);
        curr.setOpaque(true);
        curr.setBackground(Color.WHITE);
        opp.setOpaque(true);
        opp.setBackground(Color.WHITE);

        // Thêm các thành phần vào JFrame
        settingsFrame.add(idroom);
        settingsFrame.add(curr);
        settingsFrame.add(opp);
        settingsFrame.add(exitButton);
        
        // Đóng Settings Frame khi mất focus (trừ khi JOptionPane đang mở)
        settingsFrame.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                // Không làm gì khi lấy lại focus
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                // Đóng JFrame nếu không có dialog đang mở
                settingsFrame.dispose();
            }
        });

        settingsFrame.setVisible(true);
    }
});

        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(new EmptyBorder(21, 0, 20, 0));
        north.setOpaque(false);
        north.add(settingsButton, BorderLayout.WEST);
        backgroundLabel.add(north, BorderLayout.NORTH);        
        
        // Tạo JPanel rightNorth
        JPanel rightNorth = new JPanel(new GridBagLayout());
        rightNorth.setPreferredSize(new Dimension(900, 50)); // Đặt kích thước cho rightNorth
        north.add(rightNorth, BorderLayout.EAST);
        
        text = new JLabel("Chờ đối thủ của bạn đặt tàu");
        text.setFont(new Font("", Font.BOLD, 19)); // Đặt kích thước font chữ
        rightNorth.add(text);
        
        // East panel for ship list and controls
        rightNorth.setOpaque(false);
        JPanel east = createEastPanel();
        backgroundLabel.add(east, BorderLayout.EAST);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);
        backgroundLabel.add(mainPanel, BorderLayout.CENTER);
        
        JPanel rowLabelsPanel = createRowLabels();
        JPanel colLabelsPanel = createColumnLabels();
        JPanel gridPanel = createGridPanel();

        mainPanel.add(rowLabelsPanel);
        mainPanel.add(colLabelsPanel);
        mainPanel.add(gridPanel);
    }
    
    void setTextReady() {
        text.setText("Nhanh nào!!! Đối thủ đã sẵn sàng!");
    }

    private JButton createIconButton(String iconPath) {
        JButton button = new JButton();
        ImageIcon icon = new ImageIcon(iconPath);
        button.setIcon(icon);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

    private JPanel createEastPanel() {
        JPanel east = new JPanel();
        east.setPreferredSize(new Dimension(400, getHeight()));
        east.setOpaque(false);
        east.setLayout(null);

        JPanel dock = new JPanel();
        dock.setBounds(20, 60, 330, 150);
        
        dock.setLayout(new BoxLayout(dock, BoxLayout.Y_AXIS));
        east.add(dock);
        dock.setBackground(Color.decode("#d0ecf6"));
        
        JPanel option = new JPanel();
        option.setBounds(15, 250, 150, 200);
        option.setLayout(new BoxLayout(option, BoxLayout.Y_AXIS));
        east.add(option);
        
        
        start = createIconButton("src/Client/view/resources/Start.png");
        start.setEnabled(false);
        start.setBounds(188, 270, 150, 150);
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();//setvisible(f)
                sendSetupToServer();
            }
        }); 
        
        east.add(start);
        
        // Thêm khoảng trống phía trên
        option.add(Box.createVerticalGlue());

        // Nút RESET
        reset = new JButton("RESET");
        reset.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa theo trục ngang
        option.add(reset);

        // Đặt kích thước nút
        reset.setPreferredSize(new Dimension(90, 40)); // Chiều rộng: 100, Chiều cao: 40

        // Đặt màu nền và màu chữ
        reset.setBackground(Color.decode("#d0ecf6")); // Màu nền (xanh lá cây nhạt)
        reset.setForeground(Color.BLACK);             // Màu chữ (trắng)

        // Đặt kiểu và kích thước chữ
        reset.setFont(new Font("", Font.BOLD, 12)); // Font: Arial, Đậm, kích thước 14

        // Bỏ viền nếu muốn nút phẳng hơn
        reset.setFocusPainted(false); // Bỏ đường viền focus
        reset.setBorderPainted(false);


        // Thêm khoảng cách cố định giữa hai nút
        option.add(Box.createVerticalStrut(20));
        option.setOpaque(false);

        // Nút RANDOM
        randomButton = new JButton("RANDOM");
        randomButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa theo trục ngang
        option.add(randomButton);
        // Đặt kích thước nút
        randomButton.setPreferredSize(new Dimension(90, 40)); // Chiều rộng: 100, Chiều cao: 40

        // Đặt màu nền và màu chữ
        randomButton.setBackground(Color.decode("#d0ecf6")); // Màu nền (xanh lá cây nhạt)
        randomButton.setForeground(Color.BLACK);            

        // Đặt kiểu và kích thước chữ
        randomButton.setFont(new Font("", Font.BOLD, 12)); // Font: Arial, Đậm, kích thước 14

        // Bỏ viền nếu muốn nút phẳng hơn
        randomButton.setFocusPainted(false); // Bỏ đường viền focus
        randomButton.setBorderPainted(false);
        // Thêm khoảng trống phía dưới
        option.add(Box.createVerticalGlue());

        reset.addActionListener(e -> resetBoard());
        randomButton.addActionListener(e -> randomizeShips());
        ButtonGroup directionGroup = new ButtonGroup();

        horizontalButton = new JRadioButton("Ngang");
        horizontalButton.setFocusPainted(false);
        horizontalButton.setBorderPainted(false);
        horizontalButton.setContentAreaFilled(false);
        verticalButton = new JRadioButton("Dọc");
        verticalButton.setFocusPainted(false);
        verticalButton.setBorderPainted(false);
        verticalButton.setContentAreaFilled(false);
        directionGroup.add(horizontalButton);
        directionGroup.add(verticalButton);
        dock.add(horizontalButton);
        dock.add(verticalButton);
        horizontalButton.setSelected(true);

        shipListModel = new DefaultListModel<>();
        populateShipList();
        shipList = new JList<>(shipListModel);
        JScrollPane shipScrollPane = new JScrollPane(shipList);
        dock.add(shipScrollPane);

        shipListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                checkShipList();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                checkShipList();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                checkShipList();
            }
        });

        return east;
    }

    private void checkShipList() {
        // Kích hoạt nút start chỉ khi có ít nhất một tàu trong shipListModel
        start.setEnabled(shipListModel.getSize() == 0);
    }

    private JPanel createRowLabels() {
        JPanel rowLabelsPanel = new JPanel(new GridLayout(10, 1));
        rowLabelsPanel.setBounds(40, 60, 20, 420);
        rowLabelsPanel.setOpaque(false);
        for (char c = 'A'; c <= 'J'; c++) {
            JLabel label = new JLabel(String.valueOf(c), SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            rowLabelsPanel.add(label);
        }
        return rowLabelsPanel;
    }

    private JPanel createColumnLabels() {
        JPanel colLabelsPanel = new JPanel(new GridLayout(1, 10));
        colLabelsPanel.setBounds(60, 40, 420, 20);
        colLabelsPanel.setOpaque(false);
        for (int i = 1; i <= 10; i++) {
            JLabel label = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 14));
            colLabelsPanel.add(label);
        }
        return colLabelsPanel;
    }

    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel(new GridLayout(10, 10));
        gridPanel.setBounds(60, 60, 420, 420);
        gridPanel.setOpaque(false);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                JPanel cell = new JPanel();
                cell.setBackground(Color.decode("#d0ecf6"));
                cell.setBorder(BorderFactory.createLineBorder(Color.decode("#5656ef")));
                cells.add(cell);

                int r = row;
                int c = col;
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleCellClick(r, c, gridPanel);
                    }
                });

                gridPanel.add(cell);
            }
        }
        return gridPanel;
    }

    private void handleCellClick(int row, int col, JPanel gridPanel) {
        int selectedIndex = shipList.getSelectedIndex();

        // Check if there are no ships left to place
        if (shipListModel.getSize() == 0) {
            JOptionPane.showMessageDialog(this, "Không còn tàu nào để đặt. Chơi thôi!");
            return;
        }

        // Check if a ship is selected
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tàu để đặt!");
            return;
        }

        boolean isHorizontal = horizontalButton.isSelected();
        String selectedShip = shipListModel.getElementAt(selectedIndex);
        int shipSize = Integer.parseInt(selectedShip.replaceAll("[^0-9]", ""));
        char shipType = selectedShip.replaceAll("[^A-Z]", "").charAt(0);

        ArrayList<String> shipCells = new ArrayList<>();
        boolean isValid = true;

        // Check if the ship can fit in the selected position
        for (int i = 0; i < shipSize; i++) {
            int targetRow = isHorizontal ? row : row + i;
            int targetCol = isHorizontal ? col + i : col;

            // Check if the target position is out of bounds
            if (targetRow >= 10 || targetCol >= 10) {
                isValid = false;
                break;
            }

            // Check if the cell is already occupied by another ship
            String position = (char) ('A' + targetRow) + String.valueOf(targetCol + 1);
            if (placedShips.contains(position)) {
                isValid = false;
                break;
            }

            shipCells.add(position);
        }

        // If valid, place the ship
        if (isValid) {
            for (String position : shipCells) {
                int targetRow = position.charAt(0) - 'A';
                int targetCol = Integer.parseInt(position.substring(1)) - 1;

                // Ensure that the correct cell is updated
                JPanel targetCell = cells.get(targetRow * 10 + targetCol);
                targetCell.setBackground(Color.decode("#56aeff")); // Set ship color
            }

            // Add the placed ship's positions to the map
            shipPositions.put(shipType, new ArrayList<>(shipCells));

            // Remove the placed ship from the ship list
            shipListModel.remove(selectedIndex);

            // Rebuild placedShips to maintain the order B, S, C, D, P
            placedShips.clear();
            for (char shipSymbol : new char[]{'B', 'S', 'C', 'D', 'P'}) {
                List<String> positions = shipPositions.get(shipSymbol);
                if (positions != null) {
                    placedShips.addAll(positions);
                }
            }

            // Check if all ships have been placed and print final positions
            if (shipListModel.getSize() == 0) {
                System.out.println("Final Ship positions: " + shipPositions);
                System.out.println("Final placedShips : " + placedShips);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vị trí tàu không hợp lệ! Hãy thử vị trí khác.");
        }
    }


    private void populateShipList() {
        int[] shipSizes = {5, 4, 3, 3, 2};
        String[] shipNames = {"Battleship", "Submarine", "Carrier", "Destroyer", "Patrol Boat"};
        for (int i = 0; i < shipSizes.length; i++) {
            shipListModel.addElement(shipNames[i] + " (Size: " + shipSizes[i] + ")");
        }
    }

    private void resetBoard() {
            for (JPanel cell : cells) {
                cell.setBackground(Color.decode("#d0ecf6"));
            }

            if (placedShips == null) {
        placedShips = new ArrayList<>();
    } else {
        placedShips.clear();
    }
            shipListModel.clear();
            populateShipList();
    }

    void handleGameReady(String id, Board opponentBoard, String opponentName, int opponentId, Boolean turn) {
        System.out.println("Đã nhận thông báo GAME_READY từ server!");
        System.out.println("Phòng: " + id);
        // In thông tin đối thủ
        System.out.println("Tên đối thủ: " + opponentName);
        System.out.println("Mã đối thủ: " + opponentId);
        System.out.println("Bảng của đối thủ: " + opponentBoard);
        System.out.println("Tên bạn: " + name);
        System.out.println("Mã bạn: " + playerId);
        System.out.println("Bảng của bạn: " + board);
        
        InfoRoom myInfo = new InfoRoom(id, name, playerId, board);
        InfoRoom opponentInfo = new InfoRoom(id, opponentName, opponentId, opponentBoard);
        // In thông tin chung
        System.out.println("Thông tin của bạn: " + myInfo);
        System.out.println("Thông tin của đối thủ: " + opponentInfo);

    //    Player playerOne = new Player(playerId, name, false);
    //    Player playerTwo = new Player(opponentId, opponentName, false);
    //    
    //    Map<Integer, Board> boards = new HashMap<>();
    //    boards.put(playerId, board);
    //    boards.put(opponentId, opponentBoard);
    //    
    //    Room roomfinal = new Room(id, playerOne, playerTwo, boards);
    //    
    //    System.out.println("In bảng người chơi 1:");
    //Board boardOne = roomfinal.getBoardByPlayerId(playerOne.getPlayerId());
    //if (boardOne != null) {
    //    System.out.println(boardOne.toString());
    //} else {
    //    System.out.println("Không có bảng cho người chơi 1.");
    //}
    //
    //System.out.println("In bảng người chơi 2:");
    //Board boardTwo = roomfinal.getBoardByPlayerId(playerTwo.getPlayerId());
    //if (boardTwo != null) {
    //    System.out.println(boardTwo.toString());
    //} else {
    //    System.out.println("Không có bảng cho người chơi 2.");
    //}

        main = new MainScreen(client, myInfo, opponentInfo, turn);
    
    }
    public MainScreen getMainScreen() {
        return main;
    }
    
    private void randomizeShips() {
    resetBoard();  // Reset lại bảng trước khi randomize tàu
    int[] shipSizes = {5, 4, 3, 3, 2}; // Kích thước tàu
    char[] shipSymbols = {'B', 'S', 'C', 'D', 'P'}; // Ký hiệu tàu

    // Lặp qua từng tàu và randomize vị trí
    for (int i = 0; i < shipSizes.length; i++) {
        boolean placed = false;
        char currentShipSymbol = shipSymbols[i];
        int shipSize = shipSizes[i];

        while (!placed) {
            int row = (int) (Math.random() * 10);
            int col = (int) (Math.random() * 10);
            boolean isHorizontal = Math.random() < 0.5;

            ArrayList<String> shipCells = new ArrayList<>();
            boolean isValid = true;

            // Kiểm tra xem tàu có thể đặt vào vị trí này không
            for (int j = 0; j < shipSize; j++) {
                int targetRow = isHorizontal ? row : row + j;
                int targetCol = isHorizontal ? col + j : col;

                if (targetRow >= 10 || targetCol >= 10) {
                    isValid = false;
                    break;
                }

                String position = (char) ('A' + targetRow) + String.valueOf(targetCol + 1);
                if (placedShips.contains(position)) {
                    isValid = false;
                    break;
                }

                shipCells.add(position);
            }

            // Nếu hợp lệ, đánh dấu tàu và thêm vào danh sách
            if (isValid) {
                for (String position : shipCells) {
                    int targetRow = position.charAt(0) - 'A';
                    int targetCol = Integer.parseInt(position.substring(1)) - 1;

                    JPanel targetCell = cells.get(targetRow * 10 + targetCol);
                    targetCell.setBackground(Color.decode("#56aeff"));
                    placedShips.add(position);
                }
                // Lưu vị trí tàu vào shipPositions
                shipPositions.put(currentShipSymbol, shipCells);
                placed = true;
            }
        }
    }
    System.out.println("Final Ship positions: " + shipPositions);
    shipListModel.clear();  // Xóa danh sách tàu đã chọn
}
    
    private void sendSetupToServer() {        
    try {
        System.out.println("Final placedShips 2: " + placedShips);
        // Giả sử bạn đã có playerId và roomId từ đâu đó
        playerId = room.getPlayerIdByName(name);
        int[] shipSizes = {5, 4, 3, 3, 2};
        char[] shipSymbols = {'B', 'S', 'C', 'D', 'P'}; // Ký hiệu tàu
        System.out.println("Mã phòng: " + room.getRoomId() + " và " + playerId);
        board = new Board(room.getRoomId(), playerId);

        // Khởi tạo danh sách tàu
        for (int i = 0; i < shipSizes.length; i++) {
            board.addShip(new Ship(shipSizes[i], shipSymbols[i]));
        }
        
        // Phân loại vị trí các tàu
        int shipIndex = 0;
        for (String position : placedShips) {
            Ship currentShip = board.getShips().get(shipIndex);
            currentShip.addPosition(position);
            if (currentShip.getPositions().size() == currentShip.getSize()) {
                shipIndex++;
            }
        }

        // Tạo ma trận 10x10
        char[][] grid = new char[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                grid[i][j] = '-'; // Ô trống
            }
        }

        // Điền vị trí các tàu vào ma trận từ shipPositions
        for (Map.Entry<Character, List<String>> entry : shipPositions.entrySet()) {
            char shipSymbol = entry.getKey();
            List<String> positions = entry.getValue();
            for (String position : positions) {
                int row = position.charAt(0) - 'A'; // Chuyển từ ký tự A-J thành số 0-9
                int col = Integer.parseInt(position.substring(1)) - 1; // Cột 1-10 thành chỉ số 0-9
                grid[row][col] = shipSymbol; // Điền ký tự đại diện của tàu
            }
        }

        // Hiển thị ma trận
        System.out.println("\n   1 2 3 4 5 6 7 8 9 10");
        for (int i = 0; i < 10; i++) {
            StringBuilder row = new StringBuilder((char) ('A' + i) + " ");
            for (int j = 0; j < 10; j++) {
                row.append(" ").append(grid[i][j]); // Ghép ký tự từng ô
            }
            System.out.println(row.toString());
        }

        // Thông báo hoàn tất
        client.sendBoard(board);
                
        System.out.println("in" + board);

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }    
}

    public void showExitDialog(String message) {
        // Tạo JDialog
        JDialog dialog = new JDialog(this, "Thông báo", true); // 'true' để ko bấm
        dialog.setSize(400, 200);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Tạo JPanel cho nội dung
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BorderLayout());

        // Thêm thông báo
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
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