/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.view;

import java.io.Serializable;

/**
 *
 * @author Admin
 */

public class Player implements Serializable{
    private static final long serialVersionUID = 1L; 
    private int playerId; // ID người chơi
    private String playerName; // Tên người chơi
    private boolean status; // Trạng thái (true: online, false: đang đâu)

    public Player(int playerId, String playerName, boolean status) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.status = status;
    }

    // Getter và Setter
    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
        public String toString() {
        return playerId + "-" + playerName + "-" + status;
    }
  
}


