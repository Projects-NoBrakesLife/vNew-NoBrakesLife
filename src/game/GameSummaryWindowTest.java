package game;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GameSummaryWindowTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            List<Player> mockPlayers = new ArrayList<>();
            Player p1 = new Player(0, 0, 1);
            p1.setEducation(120);
            p1.setSkill(90);
            p1.setMoney(800);
            p1.setHealth(110);
            mockPlayers.add(p1);

            Player p2 = new Player(0, 0, 2);
            p2.setEducation(80);
            p2.setSkill(150);
            p2.setMoney(600);
            p2.setHealth(95);
            mockPlayers.add(p2);

            Player p3 = new Player(0, 0, 3);
            p3.setEducation(60);
            p3.setSkill(85);
            p3.setMoney(1200);
            p3.setHealth(70);
            mockPlayers.add(p3);

            Player p4 = new Player(0, 0, 4);
            p4.setEducation(100);
            p4.setSkill(100);
            p4.setMoney(900);
            p4.setHealth(130);
            mockPlayers.add(p4);

            GameSummaryWindow summary = new GameSummaryWindow(mockPlayers);
            summary.setVisible(true);
        });
    }
}


