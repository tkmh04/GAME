/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.view;

/**
 *
 * @author Admin
 */
import java.io.Serializable;
import java.util.Map;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L; 
    private int currentTurnPlayerId; //ID của người chơi đang đến lượt
    private String roomId; // mã phòng
    private Player playerOne;
    private Player playerTwo;
    private String winner;
    private String loser;
    private Map<Integer, Board> boards; // Map playerId -> Board của người chơi
    private transient GameTimer gameTimer; // ko gửi
//một thời điểm, mỗi phòng chỉ có một GameTimer đang chạy 
    
    public Room(String roomId, Player playerOne, Player playerTwo, Map<Integer, Board> boards) {
        this.roomId = roomId;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.boards = boards;
        this.currentTurnPlayerId = playerOne.getPlayerId();
        this.gameTimer = null; 
    }
//Nếu bạn gọi lại room.setGameTimer(new GameTimer(...)), giá trị cũ của gameTimer sẽ bị ghi đè bằng một đối tượng mới.    
    public GameTimer getGameTimer() {
        return gameTimer;
    }

    public void setGameTimer(GameTimer gameTimer) {
        this.gameTimer = gameTimer;
    }

    public String getRoomId() {
        return roomId;
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }
    
    public Map<Integer, Board> getBoards() {
        return boards;
    }     
    // Setter cập nhật cho currentTurnPlayerId
    public void setCurrentTurnPlayerId(int currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }
    
    public int getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void switchTurn() {
        this.currentTurnPlayerId = (this.currentTurnPlayerId == playerOne.getPlayerId())
                                   ? playerTwo.getPlayerId()
                                   : playerOne.getPlayerId();
    }    
    // Lấy đối tượng người chơi bằng playerId
    public Player getPlayerById(int playerId) {
        if (playerOne.getPlayerId() == playerId) {
            return playerOne;
        } else if (playerTwo.getPlayerId() == playerId) {
            return playerTwo;
        }
        return null;
    }
    // Lấy Player object theo tên
    public Player getPlayerByName(String name) {
        if (playerOne.getPlayerName().equals(name)) {
            return playerOne;
        } else if (playerTwo.getPlayerName().equals(name)) {
            return playerTwo;
        }
        return null;
    }
    // Lấy đối tượng đối thủ bằng playerId
    public String getOpponentNameById(int playerId) {
        if (playerOne.getPlayerId() == playerId) {
            return playerTwo.getPlayerName();
        } else if (playerTwo.getPlayerId() == playerId) {
            return playerOne.getPlayerName();
        }
        return null;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getLoser() {
        return loser;
    }

    public void setLoser(String loser) {
        this.loser = loser;
    }
    // Lấy board của người chơi theo playerId
    public Board getBoardByPlayerId(int playerId) {
        return boards.get(playerId);
    }
    // Lưu board của người chơi
    public void setBoardForPlayer(int playerId, Board board) {
        boards.put(playerId, board);
    }

    public int getPlayerIdByName(String name) {
        if (playerOne.getPlayerName().equals(name)) {
            return playerOne.getPlayerId(); // Trả về playerId của playerOne nếu tên khớp
        } else if (playerTwo.getPlayerName().equals(name)) {
            return playerTwo.getPlayerId(); // Trả về playerId của playerTwo nếu tên khớp
        }
        return -1; // Nếu không tìm thấy người chơi
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Room ID: ").append(roomId).append("\n");
        sb.append("Player One: ").append(playerOne != null ? playerOne.getPlayerName() : "N/A").append("\n");
        sb.append("Player Two: ").append(playerTwo != null ? playerTwo.getPlayerName() : "N/A").append("\n");

        // Kiểm tra null cho Board trước khi gọi toString()
        Board boardOne = boards.get(playerOne != null ? playerOne.getPlayerId() : -1);
        Board boardTwo = boards.get(playerTwo != null ? playerTwo.getPlayerId() : -1);

        sb.append("Board of Player One: ").append(boardOne != null ? boardOne.toString() : "No Board").append("\n");
        sb.append("Board of Player Two: ").append(boardTwo != null ? boardTwo.toString() : "No Board").append("\n");

        return sb.toString();
    }

}