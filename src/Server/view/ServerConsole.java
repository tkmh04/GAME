//package Server.view;
//
//import Client.view.Player;
//import java.io.*;
//import java.net.*;
//import java.util.*;
//
//
//
//public class ServerConsole {
//    private static final int PORT = 12345;
//    private static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
//   
//    
//    public static void main(String[] args) {
//        System.out.println("Server chat đang chạy...");
//        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
//            while (true) {
//                new ClientHandler(serverSocket.accept()).start();
//            }
//        } catch (IOException e) {
//            System.err.println("Lỗi khi khởi động server: " + e.getMessage());
//        }
//    }
//
//    private static class ClientHandler extends Thread {
//        private Socket socket;
//        private PrintWriter out;
//        private BufferedReader in;
//        private String userName;
//        private static ArrayList<Player> players = new ArrayList<>();
//        public ClientHandler(Socket socket) {
//            this.socket = socket;
//        }
//
//        @Override
//public void run() {
//    try {
//        System.out.println("Client kết nối từ: " + socket.getInetAddress() + ":" + socket.getPort());
//
//        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        out = new PrintWriter(socket.getOutputStream(), true);
//                // Tạo mã ID cho người chơi (Ví dụ: player1, player2, ...)
//        int playerCount = 0;  // Đếm số lượng người chơi
//        playerCount++;
//        userName = in.readLine(); // Nhận tên người chơi
//if (userName != null && !userName.isEmpty()) {
//    Player player = new Player(players.size() + 1, userName, true);
//    players.add(player);
//
//    // Gửi danh sách người chơi cho tất cả các client
//    for (Player p : players) {
//        System.out.println(p.getPlayerName());
//    }
//} else {
//    //
//}
//
//
//// Gửi mã ID và tên người chơi về cho client
//System.out.println(playerCount + "-" +userName + " đã kết nối.");
//        synchronized (clients) {
//            clients.add(this);
//        }
//
//        String message;
//        while ((message = in.readLine()) != null) {
//            System.out.println(userName + ": " + message);
//            if (message.equalsIgnoreCase("EXIT")) {
//                System.out.println(userName + " đã thoát khỏi game.");
//                break;
//            }
//            broadcast(userName + ": " + message);
//        }
//
//        // Nếu vòng lặp kết thúc mà không do lệnh "EXIT", tức là client bị mất kết nối
//        System.out.println(userName + " đã ngắt kết nối.");
//    } catch (IOException e) {
//        System.err.println(userName + " mất kết nối: " + e.getMessage());
//    } finally {
//        disconnect();
//    }
//}
//
//
//
//        private void broadcast(String message) {
//            synchronized (clients) {
//                for (ClientHandler client : clients) {
//                    if (client != this) { // Không gửi lại tin nhắn cho chính người gửi
//                        client.sendMessage(message);
//                    }
//                }
//            }
//        }
//
//        private void sendMessage(String message) {
//            out.println(message);
//        }
//
//        private void disconnect() {
//            try {
//                if (socket != null) {
//                    socket.close();
//                }
//            } catch (IOException e) {
//                System.err.println("Lỗi khi đóng socket: " + e.getMessage());
//            }
//            synchronized (clients) {
//                clients.remove(this);
//            }
//            System.out.println(userName + " đã ngắt kết nối.");
//            
//        }
//    }
//}
