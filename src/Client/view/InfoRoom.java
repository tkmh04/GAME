/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.view;

/**
 *
 * @author Admin
 */
public class InfoRoom {
    private String name;
    private int playerId;
    private Board board;
    private String id;

    public InfoRoom(String id, String name, int playerId, Board board) {
        this.id = id;
        this.name = name;
        this.playerId = playerId;
        this.board = board;
    }

    public String getId(){
        return id;
    }
    public String getName() {
        return name;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Board getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return "PlayerInfo{name='" + name + '\'' +
                ", Id phòng =" + id +
                ", playerId=" + playerId +
                ", board=" + (board != null ? board.toString() : "No Board") +
                '}';
    }
}

