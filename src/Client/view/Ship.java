/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.view;
/**
 *
 * @author Admin
 */

/*
 * 
 * OOP
 */
//enum hashmap cho player
import java.io.Serializable;
import java.util.ArrayList;

public class Ship implements Serializable{
   private static final long serialVersionUID = 1L;
    private int size; // kích thước 5 4 3 3 2
    private char symbol; // ký tự B S C D P
    private ArrayList<String> positions; // vị trí ô tương ứng A1..

    public Ship(int size, char symbol) {
        this.size = size;
        this.symbol = symbol;
        this.positions = new ArrayList<>();
    }

    public int getSize() {
        return size;
    }

    public char getSymbol() {
        return symbol;
    }

    public ArrayList<String> getPositions() {
        return positions;
    }

    public void addPosition(String position) {
        positions.add(position);
    }
    @Override
    public String toString() {
    return "Ship{" +
           "size=" + size +
           ", symbol='" + symbol + '\'' +
           ", positions=" + positions +
           '}';
    }
}