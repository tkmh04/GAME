/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//    public enum Status {
//        EMPTY('-'),
//        MISS('O'),
//        HIT('X'),
//        CARRIER('C'),
//        BATTLESHIP('B'),
//        SUBMARINE('S'),
//        DESTROYER('D'),
//        PATROL_BOAT('P');
//
//        private final char symbol;
//
//        Status(char symbol) {
//            this.symbol = symbol;
//        }
//
//        public char getSymbol() {
//            return symbol;
//        }
//    }

public class Board implements Serializable{
    private static final long serialVersionUID = 1L; // tuần tự hoá qua mạng
    private String idRoom; // mã phòng
    private int idPlayer; // mã người chơi
    private ArrayList<Ship> ships; // ds tàu
    private ArrayList<String> hits; // ds ô trúng
    private ArrayList<String> misses; // ds ô trượt

    public Board(String idRoom, int idPlayer) {
        this.idRoom = idRoom;
        this.idPlayer = idPlayer;
        this.ships = new ArrayList<>();
        this.hits = new ArrayList<>();
        this.misses = new ArrayList<>();
    }

    public String getIdRoom() {
        return idRoom;
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }

    public ArrayList<String> getHits() {
        return hits;
    }

    public ArrayList<String> getMisses() {
        return misses;
    }

    public void addShip(Ship ship) {
        ships.add(ship);
    }

    public void addHit(String position) {
        hits.add(position);
    }

    public void addMiss(String position) {
        misses.add(position);
    }
        @Override
    public String toString() {
        return "Board{" +
               "idRoom='" + idRoom + '\'' +
               ", idPlayer=" + idPlayer +
               ", ships=" + ships +
               ", hits=" + hits +
               ", misses=" + misses +
               '}';
    }
}