package Server;

import Client.view.*;
import encryption.EncryptionUtil;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RunServer {
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static Queue<ClientHandler> quickMatchQueue = new LinkedList<>();
    private static int playerCounter = 0;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server: Đang chạy trên cổng 12345...");
            // Tạo một luồng riêng để quản lý đầu vào từ console (như nhận lệnh từ admin)
            new Thread(RunServer::manageConsoleInput).start();
            while (true) {
                Socket socket = serverSocket.accept();
            // Mỗi khi có client kết nối, tạo một luồng mới để xử lý client đó riêng biệt
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server: Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }
    }
//đối tượng có thể chạy trên một luồng (thread)
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String name;
        private Player player; //người gửi, client gửi
        private EncryptionUtil encryptionUtil;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public String getName() {
            return name;
        }

        public void sendUpdate() {
            try {
                if (isConnected()) {
                    List<Player> players = new ArrayList<>();
                    synchronized (clients) {
                        for (ClientHandler clientHandler : clients.values()) {
                            players.add(clientHandler.player);
                        }
                    }
                    //System.out.println("Server: Danh sách players trước khi gửi: " + players);
                    out.writeObject(encryptionUtil.encrypt(Type.PLAYER_LIST));
                    out.writeObject(encryptionUtil.encrypt(players));
                    out.flush();
                } else {
                    //System.out.println("Server: Không thể gửi dữ liệu, kết nối đã bị ngắt cho " + name);
                }
            } catch (Exception e) {
                System.out.println("Server: Lỗi khi gửi thông tin cho " + name + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void sendRoomList() {
            try {
                if (isConnected()) {
                    List<Room> roomList = new ArrayList<>(rooms.values());
                    out.writeObject(encryptionUtil.encrypt(Type.ROOM_LIST));
                    out.writeObject(encryptionUtil.encrypt(roomList));
                    out.flush();
                } else {
                    //System.out.println("Server: Không thể gửi danh sách phòng, kết nối đã bị ngắt cho " + name);
                }
            } catch (Exception e) {
                System.out.println("Server: Lỗi gửi danh sách phòng cho " + name + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void broadcastUpdate() {
            synchronized (clients) {
                for (ClientHandler client : clients.values()) {
                    client.sendRoomList();
                }
            }
        }

        private void broadcastUpdateP() {
            synchronized (clients) {
                for (ClientHandler client : clients.values()) {
                    client.sendUpdate();
                }
            }
        }

        private void processFriendRequest(String toUser) {
            ClientHandler targetClient = clients.get(toUser);
            if (targetClient != null && targetClient.isConnected()) {
                try {
                    // Sử dụng encryptionUtil của targetClient để mã hóa thông điệp
                    targetClient.out.writeObject(targetClient.encryptionUtil.encrypt("FRIEND_REQUEST:" + this.name));
                    targetClient.out.flush();
                    //System.out.println("Server: Đã gửi yêu cầu kết bạn từ " + this.name + " tới " + toUser);
                } catch (Exception e) {
                    System.out.println("Server: Lỗi gửi yêu cầu kết bạn tới " + toUser + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                try {
                    out.writeObject(encryptionUtil.encrypt("FRIEND_REQUEST_FAILED: Người dùng " + toUser + " không trực tuyến."));
                    out.flush();
                    //System.out.println("Server: Không thể gửi yêu cầu kết bạn, " + toUser + " không trực tuyến.");
                } catch (Exception e) {
                    System.out.println("Server: Lỗi gửi thông báo thất bại tới " + this.name + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        private void handleFriendResponse(String fromUser, boolean accepted) {
            ClientHandler targetClient = clients.get(fromUser);
            if (targetClient != null && targetClient.isConnected()) {
                try {
                    if (accepted) {
                        synchronized (rooms) {
                            String countR = createRID();
                            Map<Integer, Board> boards = new HashMap<>();
                            boards.put(player.getPlayerId(), null);
                            boards.put(targetClient.player.getPlayerId(), null);

                            Room newRoom = new Room(countR, player, targetClient.player, boards);
                            rooms.put(countR, newRoom);

                            player.setStatus(false);
                            targetClient.player.setStatus(false);

                            targetClient.out.writeObject(targetClient.encryptionUtil.encrypt("FRIEND_RESPONSE:" + this.name + ":Đồng ý"));
                            targetClient.out.writeObject(targetClient.encryptionUtil.encrypt("ROOM_CURR:" + countR));
                            targetClient.out.writeObject(targetClient.encryptionUtil.encrypt(newRoom));

                            this.out.writeObject(encryptionUtil.encrypt("FRIEND_RESPONSE:" + fromUser + ":Đồng ý"));
                            this.out.writeObject(encryptionUtil.encrypt("ROOM_CURR:" + countR));
                            this.out.writeObject(encryptionUtil.encrypt(newRoom));

                            targetClient.out.flush();
                            this.out.flush();
                        }
                    } else {
                        player.setStatus(true);
                        targetClient.player.setStatus(true);

                        targetClient.out.writeObject(targetClient.encryptionUtil.encrypt("FRIEND_RESPONSE:" + this.name + ":isDenied"));
                        targetClient.out.flush();
                        this.out.writeObject(encryptionUtil.encrypt("FRIEND_RESPONSE:" + fromUser + ":Deny"));
                        this.out.flush();
                    }
                } catch (Exception e) {
                    System.out.println("Server: Lỗi xử lý phản hồi kết bạn từ " + fromUser + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            broadcastUpdate();
        }

        private boolean isConnected() {
            return socket != null && !socket.isClosed() && socket.isConnected();
        }

        @Override
        public void run() {
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                //System.out.println("Server: Khởi tạo luồng I/O cho client");

                encryptionUtil = new EncryptionUtil(out, in, false);
                //System.out.println("Server: Trao đổi khóa RSA/AES hoàn tất");

                while (true) {
                    String encryptedName = (String) in.readObject();
                    if (encryptedName == null) {
                        //System.out.println("Server: Nhận tên người dùng null từ client.");
                        break;
                    }
                    try {
                        this.name = (String) encryptionUtil.decrypt(encryptedName);
                        //System.out.println("Server: Nhận tên người dùng: " + name);
                    } catch (javax.crypto.BadPaddingException e) {
                        System.out.println("Server: Lỗi giải mã tên người dùng (Base64): " + encryptedName);
                        System.out.println("Server: Lỗi: " + e.getMessage());
                        continue;
                    }

                    if (name != null) {
                        synchronized (clients) {
                            if (clients.containsKey(name)) {
                                out.writeObject(encryptionUtil.encrypt("NAME_EXIST"));
                                out.flush();
                                //System.out.println("Server: Tên " + name + " đã tồn tại");
                            } else {
                                registerPlayer(name);
                                break;
                            }
                        }
                    }
                }

                if (name != null) {
                    System.out.println("Server: " + player.toString() + " đã kết nối.");
                    broadcastUpdateP();

                    while (true) {
                        String encryptedObj = (String) in.readObject();
                        if (encryptedObj == null) {
                            //System.out.println("Server: Nhận dữ liệu null từ " + name);
                            break;
                        }
                        Object obj;
                        try {
                            obj = encryptionUtil.decrypt(encryptedObj);
                        } catch (javax.crypto.BadPaddingException e) {
                            System.out.println("Server: Lỗi giải mã dữ liệu (Base64): " + encryptedObj);
                            System.out.println("Server: Lỗi: " + e.getMessage());
                            continue;
                        }
                        if (obj instanceof Board) {
                            handleBoard((Board) obj);
                        } else if (obj instanceof String) {
                            processStringMessage((String) obj);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(
                    player != null
                        ? "Server: " + player.getPlayerId() + "-" + player.getPlayerName() + " đã ngắt kết nối."
                        : "Server: Người chơi không xác định chưa nhập tên đã ngắt kết nối!"
                );
                
                handleGameExit();
            } finally {
                cleanupPlayer();
            }
        }

        private void registerPlayer(String name) {
            clients.put(name, this);
            playerCounter++;
            player = new Player(playerCounter, name, true);
            try {
                out.writeObject(encryptionUtil.encrypt("OK"));
                out.flush();
                //System.out.println("Server: Gửi phản hồi OK cho " + name);
            } catch (Exception e) {
                System.out.println("Server: Lỗi gửi phản hồi OK: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void processStringMessage(String message) throws IOException {
            try {
                //System.out.println("Server: Xử lý thông điệp: " + message);
                if (message.startsWith("FRIEND_REQUEST:")) {
                    processFriendRequest(message.split(":")[1]);
                } else if (message.startsWith("FRIEND_RESPONSE:")) {
                    String[] parts = message.split(":");
                    handleFriendResponse(parts[1], "Đồng ý".equals(parts[2]));
                    sendUpdate();
                } else if (message.startsWith("FIRE")) {
                    handleFireMessage(message);
                } else if (message.startsWith("CHAT")) {
                    handleChat(message);
                } else if (message.startsWith("QUICK_MATCH")) {
                    handleQuickMatch();
                    sendUpdate();
                } else if (message.startsWith("CANCEL_QM")) {
                    handleCancelQuickMatch();
                } else if (message.startsWith("GAME_EXIT")) {
                    handleGameExit();
                }
            } catch (Exception e) {
                throw new IOException("Server: Lỗi xử lý thông điệp: " + e.getMessage());
            }
        }

        private void cleanupPlayer() {
            synchronized (clients) {
                if (name != null && clients.containsKey(name)) {
                    clients.remove(name);
                }
            }
            broadcastUpdateP();
        }

        private void handleGameExit() {
            synchronized (rooms) {
                Room room = null;
                for (Room r : rooms.values()) {
                    if (r.getPlayerByName(name) != null) {
                        room = r;
                        break;
                    }
                }

                if (room != null) {
                    if (room.getGameTimer() != null) {
                        room.getGameTimer().stop();
                        //System.out.println("Server: Đã dừng đồng hồ đếm giờ cho phòng " + room.getRoomId());
                    }

                    String opponentName = room.getOpponentNameById(player.getPlayerId());
                    ClientHandler opponentHandler = clients.get(opponentName);
                    if (opponentHandler != null && opponentHandler.isConnected()) {
                        try {
                            opponentHandler.out.writeObject(opponentHandler.encryptionUtil.encrypt("OPP_EXIT"));
                            opponentHandler.out.flush();
                        } catch (Exception e) {
                            System.out.println("Server: Lỗi khi gửi thông báo OPP_EXIT: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    System.out.println("Server: Phòng " + room.getRoomId() + " đã được xóa sau khi người chơi thoát.");
                }
            }

            cleanupPlayer();
        }

        private void handleBoard(Board board) {
            synchronized (rooms) {
                Room room = rooms.get(board.getIdRoom());
                if (room != null) {
                    int playerId = room.getPlayerIdByName(name);
                    if (playerId != -1) {
                        room.setBoardForPlayer(playerId, board);
                        //System.out.println("Server: Đã lưu board cho người chơi " + name + " trong phòng " + board.getIdRoom());

                        if (room.getBoardByPlayerId(room.getPlayerOne().getPlayerId()) != null &&
                            room.getBoardByPlayerId(room.getPlayerTwo().getPlayerId()) == null) {
                            ClientHandler opponentHandler = clients.get(room.getPlayerTwo().getPlayerName());
                            if (opponentHandler != null && opponentHandler.isConnected()) {
                                try {
                                    opponentHandler.out.writeObject(opponentHandler.encryptionUtil.encrypt("ONE_READY"));
                                    opponentHandler.out.flush();
                                    //System.out.println("Server: Gửi ONE_READY đến " + room.getPlayerTwo().getPlayerName());
                                } catch (Exception e) {
                                    System.out.println("Server: Lỗi gửi ONE_READY: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        } else if (room.getBoardByPlayerId(room.getPlayerOne().getPlayerId()) == null &&
                                   room.getBoardByPlayerId(room.getPlayerTwo().getPlayerId()) != null) {
                            ClientHandler opponentHandler = clients.get(room.getPlayerOne().getPlayerName());
                            if (opponentHandler != null && opponentHandler.isConnected()) {
                                try {
                                    opponentHandler.out.writeObject(opponentHandler.encryptionUtil.encrypt("ONE_READY"));
                                    opponentHandler.out.flush();
                                    //System.out.println("Server: Gửi ONE_READY đến " + room.getPlayerOne().getPlayerName());
                                } catch (Exception e) {
                                    System.out.println("Server: Lỗi gửi ONE_READY: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (room.getBoardByPlayerId(room.getPlayerOne().getPlayerId()) != null &&
                            room.getBoardByPlayerId(room.getPlayerTwo().getPlayerId()) != null) {
                            notifyPlayersRoomReady(room);
                        }
                    } else {
                        System.out.println("Server: Không tìm thấy người chơi " + name + " trong phòng " + board.getIdRoom());
                    }
                } else {
                    System.out.println("Server: Không tìm thấy phòng với ID: " + board.getIdRoom());
                }
            }
        }

        private void handleQuickMatch() {
            synchronized (quickMatchQueue) {
                quickMatchQueue.add(this);
                System.out.println("Server: " + name + " đã bấm ghép nhanh.");

                if (quickMatchQueue.size() >= 2) {
                    ClientHandler player1 = quickMatchQueue.poll();
                    ClientHandler player2 = quickMatchQueue.poll();

                    if (player1.isConnected() && player2.isConnected()) {
                        String roomId = createRID();
                        Map<Integer, Board> boards = new HashMap<>();
                        boards.put(player1.player.getPlayerId(), null);
                        boards.put(player2.player.getPlayerId(), null);

                        Room newRoom = new Room(roomId, player1.player, player2.player, boards);

                        synchronized (rooms) {
                            rooms.put(roomId, newRoom);
                        }
                        player1.player.setStatus(false);
                        player2.player.setStatus(false);

                        try {
                            System.out.println("Server: Ghép nhanh thành công: " + player1.player.getPlayerName() + " và " + player2.player.getPlayerName() + " trong phòng " + roomId);

                            player1.out.writeObject(player1.encryptionUtil.encrypt("ROOM_CURR:" + roomId));
                            player1.out.writeObject(player1.encryptionUtil.encrypt(newRoom));
                            player1.out.flush();

                            player2.out.writeObject(player2.encryptionUtil.encrypt("ROOM_CURR:" + roomId));
                            player2.out.writeObject(player2.encryptionUtil.encrypt(newRoom));
                            player2.out.flush();
                        } catch (Exception e) {
                            System.out.println("Server: Lỗi gửi thông tin phòng ghép nhanh: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Server: Không thể ghép nhanh, một trong hai người chơi đã ngắt kết nối.");
                        if (player1.isConnected()) quickMatchQueue.add(player1);
                        if (player2.isConnected()) quickMatchQueue.add(player2);
                    }
                } else {
                    try {
                        out.writeObject(encryptionUtil.encrypt("WAITING_FOR_MATCH"));
                        out.flush();
                    } catch (Exception e) {
                        System.out.println("Server: Lỗi gửi WAITING_FOR_MATCH: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            broadcastUpdate();
        }

        private void handleCancelQuickMatch() {
            synchronized (quickMatchQueue) {
                if (quickMatchQueue.contains(this)) {
                    quickMatchQueue.remove(this);
                    try {
                        out.writeObject(encryptionUtil.encrypt("CANCEL_QM: Bạn đã hủy ghép nhanh."));
                        out.flush();
                    } catch (Exception e) {
                        System.out.println("Server: Lỗi gửi CANCEL_QM: " + e.getMessage());
                        e.printStackTrace();
                    }
                    System.out.println("Server: " + name + " đã huỷ ghép nhanh.");
                } else {
                    try {
                        out.writeObject(encryptionUtil.encrypt("CANCEL_QM: Bạn chưa tham gia ghép nhanh."));
                        out.flush();
                    } catch (Exception e) {
                        System.out.println("Server: Lỗi gửi CANCEL_QM: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            broadcastUpdate();
        }

        private void notifyPlayersRoomReady(Room room) {
            try {
                ClientHandler playerOneHandler = clients.get(room.getPlayerOne().getPlayerName());
                ClientHandler playerTwoHandler = clients.get(room.getPlayerTwo().getPlayerName());

                if (playerOneHandler != null && playerOneHandler.isConnected() &&
                    playerTwoHandler != null && playerTwoHandler.isConnected()) {
                    playerOneHandler.out.writeObject(playerOneHandler.encryptionUtil.encrypt("GAME_READY"));
                    playerTwoHandler.out.writeObject(playerTwoHandler.encryptionUtil.encrypt("GAME_READY"));

                    String playerOneName = room.getPlayerOne().getPlayerName();
                    String playerTwoName = room.getPlayerTwo().getPlayerName();

                    int playerOneId = room.getPlayerIdByName(playerOneName);
                    int playerTwoId = room.getPlayerIdByName(playerTwoName);

                    playerOneHandler.out.writeObject(playerOneHandler.encryptionUtil.encrypt(room.getBoardByPlayerId(playerTwoId)));
                    playerOneHandler.out.writeObject(playerOneHandler.encryptionUtil.encrypt(playerTwoName));
                    playerOneHandler.out.writeObject(playerOneHandler.encryptionUtil.encrypt(playerTwoId));

                    playerTwoHandler.out.writeObject(playerTwoHandler.encryptionUtil.encrypt(room.getBoardByPlayerId(playerOneId)));
                    playerTwoHandler.out.writeObject(playerTwoHandler.encryptionUtil.encrypt(playerOneName));
                    playerTwoHandler.out.writeObject(playerTwoHandler.encryptionUtil.encrypt(playerTwoId));

                    playerOneHandler.out.writeObject(playerOneHandler.encryptionUtil.encrypt("GAME_TURN_T"));
                    playerTwoHandler.out.writeObject(playerTwoHandler.encryptionUtil.encrypt("GAME_TURN_F"));

                    GameTimer gameTimer = new GameTimer(33, () -> {
                        System.out.println("Server: Người chơi không hành động trong thời gian quy định. Chuyển lượt.");
                        switchTurn(room, playerOneId);
                    });
                    room.setGameTimer(gameTimer);
                    gameTimer.start();

                    playerOneHandler.out.flush();
                    playerTwoHandler.out.flush();
                } else {
                    System.out.println("Server: Không thể gửi GAME_READY, một trong hai người chơi không trực tuyến.");
                }
            } catch (Exception e) {
                System.out.println("Server: Lỗi gửi thông báo GAME_READY: " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void handleFireMessage(String message) {
            System.out.println("Server: Nhận thông tin bắn: " + message);
            String[] parts = message.split(" ");
            if (parts.length == 5 && "FIRE".equals(parts[0])) {
                String roomId = parts[1];
                int playerId = Integer.parseInt(parts[2]);
                String position = parts[3];
                boolean isHit = "HIT".equals(parts[4]);

                synchronized (rooms) {
                    Room room = rooms.get(roomId);
                    if (room != null) {
                        Board opponentBoard = room.getBoardByPlayerId(
                            playerId == room.getPlayerOne().getPlayerId()
                                ? room.getPlayerTwo().getPlayerId()
                                : room.getPlayerOne().getPlayerId()
                        );

                        if (opponentBoard != null) {
                            if (isHit) {
                                opponentBoard.getHits().add(position);
                            } else {
                                opponentBoard.getMisses().add(position);
                            }

                            checkShipsSunk(opponentBoard, playerId, room, position);

                            ClientHandler opponentHandler = clients.get(room.getOpponentNameById(playerId));
                            if (opponentHandler != null && opponentHandler.isConnected()) {
                                try {
                                    opponentHandler.out.writeObject(opponentHandler.encryptionUtil.encrypt("FIRE_UPDATE " + position + " " + (isHit ? "HIT" : "MISS")));
                                    opponentHandler.out.flush();
                                } catch (Exception e) {
                                    System.out.println("Server: Lỗi gửi FIRE_UPDATE: " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                            if (!isHit) {
                                switchTurn(room, playerId);
                            }
                        }
                    }
                }
            }
        }

        private void switchTurn(Room room, int currentPlayerId) {
            int nextPlayerId = (currentPlayerId == room.getPlayerOne().getPlayerId())
                ? room.getPlayerTwo().getPlayerId()
                : room.getPlayerOne().getPlayerId();

            room.setCurrentTurnPlayerId(nextPlayerId);

            if (room.getGameTimer() != null) {
                room.getGameTimer().stop();
            }

            GameTimer gameTimer = new GameTimer(30, () -> {
                System.out.println("Server: Người chơi không hành động trong thời gian quy định. Chuyển lượt.");
                switchTurn(room, nextPlayerId);
            });

            room.setGameTimer(gameTimer);
            gameTimer.start();

            ClientHandler nextPlayerHandler = clients.get(room.getPlayerById(nextPlayerId).getPlayerName());
            if (nextPlayerHandler != null && nextPlayerHandler.isConnected()) {
                try {
                    nextPlayerHandler.out.writeObject(nextPlayerHandler.encryptionUtil.encrypt("TURN_T"));
                    nextPlayerHandler.out.flush();
                } catch (Exception e) {
                    System.out.println("Server: Lỗi gửi TURN_T: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            ClientHandler currentPlayerHandler = clients.get(room.getPlayerById(currentPlayerId).getPlayerName());
            if (currentPlayerHandler != null && currentPlayerHandler.isConnected()) {
                try {
                    currentPlayerHandler.out.writeObject(currentPlayerHandler.encryptionUtil.encrypt("TURN_F"));
                    currentPlayerHandler.out.flush();
                } catch (Exception e) {
                    System.out.println("Server: Lỗi gửi TURN_F: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Server: Chuyển lượt sang người chơi " + nextPlayerId);
        }

        private void checkShipsSunk(Board opponentBoard, int playerId, Room room, String position) {
            Map<Character, String> shipNameMap = new HashMap<>();
            shipNameMap.put('B', "Battleship");
            shipNameMap.put('S', "Submarine");
            shipNameMap.put('C', "Carrier");
            shipNameMap.put('D', "Destroyer");
            shipNameMap.put('P', "Patrol Boat");

            ArrayList<Ship> ships = opponentBoard.getShips();
            boolean sentShipSunkMessage = false;

            for (Ship ship : ships) {
                List<String> shipPositions = ship.getPositions();
                if (shipPositions.contains(position) && opponentBoard.getHits().containsAll(shipPositions)) {
                    char symbol = ship.getSymbol();
                    String shipName = shipNameMap.getOrDefault(symbol, "Unknown Ship");

                    if (!sentShipSunkMessage) {
                        ClientHandler currentPlayerHandler = clients.get(room.getPlayerById(playerId).getPlayerName());
                        if (currentPlayerHandler != null && currentPlayerHandler.isConnected()) {
                            try {
                                System.out.println("Server: In tàu chìm: " + "SHIP_SUNK " + shipName);
                                currentPlayerHandler.out.writeObject(currentPlayerHandler.encryptionUtil.encrypt("SHIP_SUNK " + shipName));
                                currentPlayerHandler.out.flush();
                            } catch (Exception e) {
                                System.out.println("Server: Lỗi gửi SHIP_SUNK: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        sentShipSunkMessage = true;
                    }
                    break;
                }
            }

            boolean allShipsSunk = ships.stream().allMatch(ship -> opponentBoard.getHits().containsAll(ship.getPositions()));
            if (allShipsSunk) {
                ClientHandler currentPlayerHandler = clients.get(room.getPlayerById(playerId).getPlayerName());
                ClientHandler opponentPlayerHandler = clients.get(room.getOpponentNameById(playerId));

                if (currentPlayerHandler != null && currentPlayerHandler.isConnected() &&
                    opponentPlayerHandler != null && opponentPlayerHandler.isConnected()) {
                    try {
                        currentPlayerHandler.out.writeObject(currentPlayerHandler.encryptionUtil.encrypt("GAME_WIN"));
                        opponentPlayerHandler.out.writeObject(opponentPlayerHandler.encryptionUtil.encrypt("GAME_LOSE"));
                        currentPlayerHandler.out.flush();
                        opponentPlayerHandler.out.flush();

                        if (room.getGameTimer() != null) {
                            room.getGameTimer().stop();
                        }
                        System.out.println("Server: " + room.getRoomId() + " đã kết thúc trò chơi với người thắng: " +
                                           currentPlayerHandler.getName() + "- người thua: " + opponentPlayerHandler.getName());
                        room.setWinner(currentPlayerHandler.getName());
                        room.setLoser(opponentPlayerHandler.getName());
                    } catch (Exception e) {
                        System.out.println("Server: Lỗi gửi GAME_WIN/GAME_LOSE: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        private void handleChat(String message) {
            System.out.println("Server: Nhận thông tin chat: " + message);
            String[] parts = message.split(" ", 4);
            if (parts.length == 4 && "CHAT".equals(parts[0])) {
                String roomId = parts[1];
                int senderId = Integer.parseInt(parts[2]);
                String chatMessage = parts[3];

                synchronized (rooms) {
                    Room room = rooms.get(roomId);
                    if (room != null) {
                        int recipientId = senderId == room.getPlayerOne().getPlayerId()
                            ? room.getPlayerTwo().getPlayerId()
                            : room.getPlayerOne().getPlayerId();

                        String senderName = room.getPlayerById(senderId).getPlayerName();
                        ClientHandler recipientHandler = clients.get(room.getPlayerById(recipientId).getPlayerName());

                        if (recipientHandler != null && recipientHandler.isConnected()) {
                            try {
                                recipientHandler.out.writeObject(recipientHandler.encryptionUtil.encrypt("CHAT " + senderName + ": " + chatMessage));
                                recipientHandler.out.flush();
                                System.out.println("Server: " + senderName + " đã gửi tin nhắn tới Player " + recipientId + ": " + chatMessage);
                            } catch (Exception e) {
                                System.out.println("Server: Lỗi gửi tin nhắn chat: " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                this.out.writeObject(encryptionUtil.encrypt("CHAT Đối thủ không trực tuyến."));
                                this.out.flush();
                            } catch (Exception e) {
                                System.out.println("Server: Lỗi gửi thông báo đối thủ không trực tuyến: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            this.out.writeObject(encryptionUtil.encrypt("CHAT Không tìm thấy phòng."));
                            this.out.flush();
                        } catch (Exception e) {
                            System.out.println("Server: Lỗi gửi thông báo phòng không tồn tại: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                try {
                    this.out.writeObject(encryptionUtil.encrypt("CHAT Cú pháp tin nhắn không hợp lệ."));
                    this.out.flush();
                } catch (Exception e) {
                    System.out.println("Server: Lỗi gửi thông báo cú pháp không hợp lệ: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        public static String createRID() {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            Random random = new Random();
            String randomStr;

            synchronized (rooms) {
                do {
                    StringBuilder sb = new StringBuilder(6);
                    for (int i = 0; i < 6; i++) {
                        int index = random.nextInt(chars.length());
                        sb.append(chars.charAt(index));
                    }
                    randomStr = sb.toString();
                } while (rooms.containsKey(randomStr));
            }

            return randomStr;
        }
    }

    private static void manageConsoleInput() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Bạn muốn xem danh sách nào không? Bấm 1 để xem phòng, 2 để xem người chơi, 0 để thoát:");
            String input = scanner.nextLine();
            if ("1".equals(input)) {
                printRoomsWithDetails(scanner);
            } else if ("2".equals(input)) {
                printPlayer();
            } else if ("0".equals(input)) {
                System.out.println("Thoát chế độ xem.");
                break;
            } else {
                System.out.println("Lựa chọn không hợp lệ. Vui lòng thử lại.");
            }
        }
        scanner.close();
    }

    private static void printRoomsWithDetails(Scanner scanner) {
        synchronized (rooms) {
            if (rooms.isEmpty()) {
                System.out.println("Hiện không có phòng nào.");
                return;
            }

            System.out.println("Danh sách các phòng hiện tại:");
            List<String> roomKeys = new ArrayList<>(rooms.keySet());
            for (int i = 0; i < roomKeys.size(); i++) {
                String roomId = roomKeys.get(i);
                System.out.println((i + 1) + ". Phòng: " + roomId);
            }

            System.out.println("Nhập STT của phòng để xem chi tiết hoặc 0 để quay lại:");
            String input = scanner.nextLine();

            try {
                int selectedIndex = Integer.parseInt(input);
                if (selectedIndex == 0) {
                    System.out.println("Quay lại danh sách phòng.");
                    return;
                }
                if (selectedIndex >= 1 && selectedIndex <= roomKeys.size()) {
                    String selectedRoomId = roomKeys.get(selectedIndex - 1);
                    printRoomDetails(selectedRoomId);
                } else {
                    System.out.println("STT không hợp lệ. Vui lòng thử lại.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Lựa chọn không hợp lệ. Vui lòng nhập số.");
            }
        }
    }

    private static void printPlayer() {
        synchronized (clients) {
            if (clients.isEmpty()) {
                System.out.println("Hiện không có người chơi nào.");
                return;
            }
            System.out.println("Số lượng người online - đang chơi: " + clients.size());
            System.out.println("Danh sách người chơi hiện tại (ID - Tên - Trạng thái):");
            for (ClientHandler clientHandler : clients.values()) {
                Player player = clientHandler.player;
                String status = player.getStatus() ? "Online" : "Đang đấu";
                System.out.println(player.getPlayerId() + " - " + player.getPlayerName() + " - " + status);
            }
        }
    }

    private static void printRoomDetails(String roomId) {
        synchronized (rooms) {
            System.out.println("Số lượng phòng: " + rooms.size());
            Room room = rooms.get(roomId);
            if (room != null) {
                System.out.println("Chi tiết phòng: " + roomId);
                System.out.println("   Người chơi 1: " + (room.getPlayerOne() != null ? room.getPlayerOne().getPlayerName() : "Chưa có"));
                System.out.println("   Người chơi 2: " + (room.getPlayerTwo() != null ? room.getPlayerTwo().getPlayerName() : "Chưa có"));

                Board board1 = room.getBoardByPlayerId(room.getPlayerOne() != null ? room.getPlayerOne().getPlayerId() : -1);
                Board board2 = room.getBoardByPlayerId(room.getPlayerTwo() != null ? room.getPlayerTwo().getPlayerId() : -1);

                System.out.println("   Bảng người chơi 1: " + (board1 != null ? board1 : "Chưa gửi"));
                System.out.println("   Bảng người chơi 2: " + (board2 != null ? board2 : "Chưa gửi"));

                if (room.getWinner() != null && room.getLoser() != null) {
                    System.out.println("   Kết quả trò chơi: ");
                    System.out.println("      Người thắng: " + room.getWinner());
                    System.out.println("      Người thua: " + room.getLoser());
                } else {
                    System.out.println("   Kết quả trò chơi: Chưa có kết quả");
                }
            } else {
                System.out.println("Phòng không tồn tại.");
            }
        }
    }
}