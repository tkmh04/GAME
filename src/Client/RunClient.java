    package Client;

    import Client.view.*;
    import encryption.EncryptionUtil;
    import java.io.*;
    import java.net.*;
    import java.util.*;
    import javax.swing.JOptionPane;

    public class RunClient {
        private String serverIp;
        private int serverPort;
        private boolean connected = false;
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private EncryptionUtil encryptionUtil;

        public RunClient(String serverIp, int serverPort) {
            this.serverIp = serverIp;
            this.serverPort = serverPort;
            connectToServer();
        }

        private void connectToServer() {
            while (!connected) {
                try {
                    System.out.println("Client: Đang thử kết nối tới server " + serverIp + ":" + serverPort);
                    socket = new Socket(serverIp, serverPort);
                    //System.out.println("Client: Kết nối socket thành công");
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                    //System.out.println("Client: Khởi tạo luồng I/O thành công");

                    encryptionUtil = new EncryptionUtil(out, in, true);
                    //System.out.println("Client: Trao đổi khóa RSA/AES hoàn tất");

                    connected = true;
                    //System.out.println("Client: Kết nối và trao đổi khóa thành công!");
                    handleConnection();
                } catch (ConnectException e) {
                    System.out.println("Client: Không thể kết nối tới server. Server chưa được bật hoặc không nghe trên cổng " + serverPort);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.out.println("Client: Thread bị gián đoạn khi đang chờ.");
                    }
                } catch (Exception e) {
                    System.out.println("Client: Lỗi kết nối hoặc trao đổi khóa: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.out.println("Client: Thread bị gián đoạn khi đang chờ.");
                    }
                }
            }
        }

        private void handleConnection() throws IOException {
            try {
                StartScreen startScreen = new StartScreen();
                String username;
                String response = "";

                do {
                    username = startScreen.getUsername();
                    if (username == null || username.trim().isEmpty()) {
                        startScreen.showNotification("Tên không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }

                    System.out.println("Gửi tên người dùng: " + username);
                    out.writeObject(encryptionUtil.encrypt(username));
                    out.flush();

                    System.out.println("Đang chờ phản hồi từ server...");
                    response = (String) encryptionUtil.decrypt((String) in.readObject());
                    System.out.println("Nhận phản hồi từ server: " + response);

                    if ("NAME_EXIST".equals(response)) {
                        startScreen.showNotification("Tên này đang online, vui lòng chọn tên khác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        startScreen.resetForRetry();
                    } else if (!"OK".equals(response)) {
                        startScreen.showNotification("Lỗi không xác định từ server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                } while (!"OK".equals(response));

                System.out.println("Đã gửi tên " + username + " tới server!");
                startScreen.dispose();
                RoomScreen roomScreen = new RoomScreen(username, this);
                roomScreen.setVisible(true);

            } catch (Exception e) {
                System.out.println("Lỗi trong quá trình xử lý kết nối: " + e.getMessage());
                e.printStackTrace();
                connected = false;
            }
        }

        public boolean isConnected() {
            return connected;
        }

        public void disconnect() throws IOException {
            if (socket != null && !socket.isClosed()) {
                sendMessage("DISCONNECT");
                socket.close();
                connected = false;
                System.out.println("Đã ngắt kết nối với server.");
            }
        }
// Gửi yêu cầu ghép đôi với người nhận đến server
        public void sendFriendRequest(String toUser) throws IOException {
            try {
                out.writeObject(encryptionUtil.encrypt("FRIEND_REQUEST:" + toUser));
                out.flush();
                System.out.println("Đã gửi yêu cầu kết bạn tới " + toUser);
            } catch (Exception e) {
                throw new IOException("Lỗi gửi yêu cầu kết bạn: " + e.getMessage());
            }
        }
// Gửi hồi đáp ghép đôi từ người nhận 
        public void sendFriendResponse(String fromUser, boolean accepted) throws IOException {
            try {
                out.writeObject(encryptionUtil.encrypt("FRIEND_RESPONSE:" + fromUser + ":" + (accepted ? "Đồng ý" : "Từ chối")));
                out.flush();
            } catch (Exception e) {
                throw new IOException("Lỗi gửi phản hồi kết bạn: " + e.getMessage());
            }
        }

        public synchronized Object receive() throws IOException, ClassNotFoundException, Exception {
            try {
                String encryptedData = (String) in.readObject();
                if (encryptedData == null) {
                    System.out.println("Nhận được dữ liệu null từ server.");
                    return null;
                }
                try {
                    Object data = encryptionUtil.decrypt(encryptedData);
                    System.out.println("Đã nhận được object: " + data);
                    return data;
                } catch (javax.crypto.BadPaddingException e) {
                    System.out.println("Client: Lỗi giải mã dữ liệu (Base64): " + encryptedData);
                    System.out.println("Lỗi khi nhận thông điệp: " + e.getMessage());
                    e.printStackTrace();
                    // Trả về thông điệp lỗi để giao diện xử lý
                    return "ERROR: Dữ liệu mã hóa không hợp lệ";
                }
            } catch (IOException e) {
                System.out.println("Lỗi khi nhận thông điệp: " + e.getMessage());
                e.printStackTrace();
                throw e;
            } catch (ClassNotFoundException e) {
                System.out.println("Lỗi khi nhận thông điệp: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        public void close() throws IOException {
            disconnect();
        }

        public List<Room> getRoomList() throws IOException, ClassNotFoundException {
            try {
                out.writeObject(encryptionUtil.encrypt("ROOM_LIST"));
                out.flush();
                Object response = encryptionUtil.decrypt((String) in.readObject());
                if (response instanceof List) {
                    return (List<Room>) response;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                throw new IOException("Lỗi lấy danh sách phòng: " + e.getMessage());
            }
        }

        public List<Player> getPlayerList() throws IOException, ClassNotFoundException {
            try {
                Object response = encryptionUtil.decrypt((String) in.readObject());
                if (response instanceof List) {
                    return (List<Player>) response;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                throw new IOException("Lỗi lấy danh sách người chơi: " + e.getMessage());
            }
        }

        public void sendQuickMatchRequest() throws IOException {
            try {
                out.writeObject(encryptionUtil.encrypt("QUICK_MATCH"));
                out.flush();
                System.out.println("Đã gửi yêu cầu ghép nhanh đến server.");
            } catch (Exception e) {
                throw new IOException("Lỗi gửi yêu cầu ghép nhanh: " + e.getMessage());
            }
        }

        public void sendCancelQuickMatch() throws IOException {
            try {
                out.writeObject(encryptionUtil.encrypt("CANCEL_QM"));
                out.flush();
                System.out.println("Đã hủy yêu cầu ghép nhanh đến server.");
            } catch (Exception e) {
                throw new IOException("Lỗi hủy yêu cầu ghép nhanh: " + e.getMessage());
            }
        }

        public void sendExit() throws IOException {
            try {
                out.writeObject(encryptionUtil.encrypt("GAME_EXIT"));
                out.flush();
            } catch (Exception e) {
                throw new IOException("Lỗi gửi yêu cầu thoát game: " + e.getMessage());
            }
        }

        public void sendBoard(Board board) throws IOException {
            try {
                out.writeObject(encryptionUtil.encrypt(board));
                out.flush();
                System.out.println("Đã gửi board lên server: " + board.getIdRoom());
            } catch (Exception e) {
                throw new IOException("Lỗi gửi board: " + e.getMessage());
            }
        }

        public void sendMessage(String message) throws IOException {
            try {
                out.writeObject(encryptionUtil.encrypt(message));
                out.flush();
            } catch (Exception e) {
                throw new IOException("Lỗi gửi tin nhắn: " + e.getMessage());
            }
        }

        public void sendFirePosition(InfoRoom playerInfo, String position, boolean isHit) throws IOException {
            try {
                String roomId = playerInfo.getId();
                int playerId = playerInfo.getPlayerId();
                String message = "FIRE " + roomId + " " + playerId + " " + position + " " + (isHit ? "HIT" : "MISS");
                out.writeObject(encryptionUtil.encrypt(message));
                out.flush();
                System.out.println("Đã gửi vị trí bắn: " + position + ", Kết quả: " + (isHit ? "Trúng" : "Trượt") +
                        ", ID phòng: " + roomId + ", ID người chơi: " + playerId);
            } catch (Exception e) {
                throw new IOException("Lỗi gửi vị trí bắn: " + e.getMessage());
            }
        }
    }