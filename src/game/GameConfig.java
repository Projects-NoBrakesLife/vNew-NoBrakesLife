package game;

public class GameConfig {
    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    
    public static final String BACKGROUND_IMAGE = "assets" + java.io.File.separator + "background" + java.io.File.separator + "NewTimesMapMock4a.png";
    public static final String HOVER_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Button Select.wav";
    public static final String TURN_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Bubble Pop.wav";
    
    public static final long LOADING_SCREEN_DELAY_MS = 5000;

    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8888;
    public static final int MAX_PLAYERS = 4;
    public static final int MIN_PLAYERS_TO_START = 2;
    public static final int MAX_TURNS = 4;
    
    public static final double TIME_BASE_COST = 5.0;
    public static final double TIME_DISTANCE_MULTIPLIER = 100.0;
    public static final double TIME_MAX_COST = 7.0;

    public static final HoverObject[] HOVER_OBJECTS = {
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "gym.png", "ยิม", 484.0, 201.0, 279.0, 243.0, 0.0, 697.0, 435.0, "BACK"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "laundry.png", "ร้านซักรีด", 371.0, 274.0, 217.0, 244.0, 0.0, 544.0, 484.0, "BACK"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "kfb.png", "ร้านไก่ทอดฟาสต์ฟู้ด", 202.0, 298.0, 234.0, 234.0, 0.0,366.0, 576.0, "BACK"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "shop.png", "ร้านขายของชำ", 388.0, 470.0, 311.0, 343.0, 0.0, 527.0, 791.0, "FRONT"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "dormitory.png", "หอพัก", 848.0, 147.0, 218.0, 287.0, 0.0, 980.0, 435.0, "FRONT"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "bank.png", "ธนาคาร", 1464.0, 287.0, 269.0, 323.0, 0.0, 1508.0, 585.0, "BACK"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "collectibles.png", "ร้านของสะสม", 1010.0, 319.0, 268.0, 284.0, 0.0, 1193.0, 569.0, "BACK"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "garden.png", "สวนจก", 605.0, 431.0, 525.0, 283.0, 0.0, 874.0, 567.0, "FRONT"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "university.png", "มหาวิทยาลัย NSU", 967.0, 626.0, 266.0, 302.0, 0.0, 1026.0, 899.01, "FRONT"),
            new HoverObject("assets" + java.io.File.separator + "obj" + java.io.File.separator + "job.png", "ศูนย์จัดหางาน", 671.0, 600.0, 240.0, 314.0, 0.0, 720.0, 899.0, "FRONT"),



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

