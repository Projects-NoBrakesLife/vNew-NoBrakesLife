package network;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int playerId;
    public String playerName;
    public boolean isConnected;
    
    public int skill = 0;
    public int education = 0;
    public int health = 100;
    public int money = 500;
    public int bankDeposit = 0;
    
    public PlayerInfo(int playerId, String playerName, boolean isConnected) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.isConnected = isConnected;
        this.skill = 0;
        this.education = 0;
        this.health = 100;
        this.money = 500;
        this.bankDeposit = 0;
    }
    
    public PlayerInfo(int playerId, String playerName, boolean isConnected, int skill, int education, int health, int money) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.isConnected = isConnected;
        this.skill = skill;
        this.education = education;
        this.health = health;
        this.money = money;
        this.bankDeposit = 0;
    }
    
    @Override
    public String toString() {
        return "Player_" + playerId + " (" + playerName + ")";
    }
}


class PlayerState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int playerId;
    public double x;
    public double y;
    public String direction; 
    public boolean isMoving;
    
    public PlayerState(int playerId, double x, double y, String direction, boolean isMoving) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.isMoving = isMoving;
    }
}

