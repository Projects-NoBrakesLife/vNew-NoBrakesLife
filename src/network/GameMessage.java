package network;

import java.io.Serializable;

public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        PLAYER_JOINED,
        PLAYER_LEFT,
        UPDATE_LOBBY,
        START_GAME,
        PLAYER_MOVE,
        PLAYER_POSITION,
        CHAT_MESSAGE,
        HEARTBEAT
    }
    
    public MessageType type;
    public Object data;
    public int senderId;
    
    public GameMessage(MessageType type, Object data, int senderId) {
        this.type = type;
        this.data = data;
        this.senderId = senderId;
    }
    
    @Override
    public String toString() {
        return "Message{" + type + ", sender=" + senderId + ", data=" + data + "}";
    }
}

