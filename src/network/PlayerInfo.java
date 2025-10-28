package network;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public int playerId;
    public String playerName;
    public boolean isConnected;
    
    public PlayerInfo(int playerId, String playerName, boolean isConnected) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.isConnected = isConnected;
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

