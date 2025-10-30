package game;

public class GameConfig {
    public static final boolean DEBUG_MODE = true;
    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    
    public static final String BACKGROUND_IMAGE = "assets" + java.io.File.separator + "background" + java.io.File.separator + "NewTimesMapMock4a.png";
    public static final String HOVER_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Button Select.wav";
    public static final String TURN_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Bubble Pop.wav";
    public static final String FOOD_EATEN_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Food Eaten.wav";
    public static final String LOCATION_OPEN_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Location Open.wav";
    public static final String LOCATION_CLOSE_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Location Close.wav";
    public static final String CELEBRATION_MUSIC = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "CRAPPY_HOME_AMB.wav";
    public static final String LAST_TURN_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Last Turn.wav";
    public static final String SCORE_FILL_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Score Fill.wav";
    public static final String SCORE_TYPE_ANNOUNCED_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Score Type Announced.wav";
    
    public static final long LOADING_SCREEN_DELAY_MS = 5000;

    private static String SERVER_HOST = "127.0.0.1";
    public static final int SERVER_PORT = 8888;
    
    public static String getServerHost() {
        return SERVER_HOST;
    }
    
    public static void setServerHost(String host) {
        SERVER_HOST = host;
    }
    public static final int MAX_PLAYERS = 4;
    public static final int MIN_PLAYERS_TO_START = 2;
    public static final int MAX_TURNS = 1;
    
    public static final double TIME_BASE_COST = 1.0;
    public static final double TIME_DISTANCE_MULTIPLIER = 250.0;
    public static final double TIME_MAX_COST = 3.0;
    public static final double TIME_AUTO_DECREASE = 0.01;
    public static final long TIME_AUTO_DECREASE_INTERVAL_MS = 1000;

    public static final HoverObject[] HOVER_OBJECTS = {
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "gym.png", "ยิมหน้ามอ", 484.0, 201.0, 279.0, 243.0, 0.0, 697.0, 435.0, "BACK"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "kfb.png", "ร้านไก่ทอดตลาดน้อย", 202.0, 298.0, 234.0, 234.0, 0.0,366.0, 576.0, "BACK"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "dormitory.png", "หอพัก", 848.0, 147.0, 218.0, 287.0, 0.0, 980.0, 435.0, "FRONT"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "bank.png", "ธนาคารไทณิชย์", 1464.0, 287.0, 269.0, 323.0, 0.0, 1508.0, 585.0, "BACK"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "garden.png", "สวนจก", 605.0, 431.0, 525.0, 283.0, 0.0, 874.0, 567.0, "FRONT"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "university.png", "มหาวิทยาลัย NSU", 967.0, 626.0, 266.0, 302.0, 0.0, 1026.0, 899.01, "FRONT"),


    };
    
    public static class HoverObject {
        public String imagePath;
        public String name;
        public double x;
        public double y;
        public double width;
        public double height;
        public double rotation;
        public double playerX;
        public double playerY;
        public String direction;
        
        public HoverObject(String imagePath, String name, double x, double y, double width, double height, double rotation, double playerX, double playerY, String direction) {
            this.imagePath = imagePath;
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.rotation = rotation;
            this.playerX = playerX;
            this.playerY = playerY;
            this.direction = direction;
        }
    }
}

