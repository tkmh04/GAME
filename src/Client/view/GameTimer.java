/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client.view;

/**
 *
 * @author Admin
 */

import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {
    private Timer timer; //  lập lịch thực hiện các tác vụ
    private int timeLeft; // Số giây còn lại
    private Runnable onTimeOut; // thực hiện một hành động khi hết thời gian

    public GameTimer(int duration, Runnable onTimeOut) { // thời gian bắt đầu đếm ngược
        this.timeLeft = duration;
        this.onTimeOut = onTimeOut;
    }

    public void start() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    timer.cancel();
                    onTimeOut.run(); // Hết thời gian, gọi hàm timeout
                } else {
                    timeLeft--;
//                    System.out.println("Thời gian còn lại: " + timeLeft + " giây");
                }
            }
        }, 0, 1000); //s
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public int getTimeLeft() {
        return timeLeft;
    }
    
    
}
