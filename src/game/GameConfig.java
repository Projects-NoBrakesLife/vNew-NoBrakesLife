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
    public static final String BUTTON_CLICK_2_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Button Click 2.wav";
    public static final String HOOK_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "hook.wav";
    public static final String STAMP_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Stamp.wav";
    
    public static final int SLEEP_HEALTH_BONUS = 30;
    public static final double SLEEP_TIME_COST = 1.5;
    
    public static final int STUDY_EDUCATION_BONUS = 15;
    public static final double STUDY_TIME_COST = 2.0;
    
    public static class Question {
        public String question;
        public String answer;
        
        public Question(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }
    
    public static final Question[] QUESTIONS = {
        new Question("Java System.out.______('Hello');", "println"),
        new Question("Java คำสั่งใช้เพื่อรับค่าจากผู้ใช้คือ?", "Scanner"),
        new Question("Java คีย์เวิร์ดที่ใช้สร้างวัตถุ (object) คือ?", "new"),
        new Question("Java class ใช้สำหรับสร้างอะไร?", "object"),
        new Question("Java คำสั่ง ... ใช้หยุด loop?", "break"),
        new Question("Java System.out.println(5 + 3); จะแสดงผลลัพธ์อะไร?", "8"),

        
        


        new Question("SQL คำสั่งใช้กรองข้อมูลคือ?", "WHERE"),
        new Question("SQL คำสั่งใช้จัดเรียงข้อมูลคือ?", "ORDER BY"),
        new Question("SQL คำสั่งใช้รวมข้อมูลกลุ่มคือ?", "GROUP BY"),
        new Question("SQL คำสั่งใช้ค้นหาข้อความบางส่วนคือ?", "LIKE"),
        new Question("SQL คำสั่งใช้ลบตารางคือ?", "DROP TABLE"),

        
        

        
        
     
        new Question("1 + 1 = ?", "2"),
        new Question("5 - 2 = ?", "3"),
        new Question("3 x 4 = ?", "12"),
        new Question("10 ÷ 2 = ?", "5"),
        new Question("7 + 8 = ?", "15"),
        new Question("9 - 3 = ?", "6"),
        new Question("100 - 75 = ?", "25"),
        new Question("12 + 18 = ?", "30"),
        new Question("15 ÷ 3 = ?", "5"),
        new Question("20 + 5 = ?", "25"),
        new Question("50 - 25 = ?", "25"),
        new Question("4 x 6 = ?", "24"),
        new Question("9 + 9 = ?", "18"),
        new Question("7 + 2 = ?", "9"),
        new Question("8 - 3 = ?", "5"),
   
        new Question("Subnet mask 255.255.255.0 คือ CIDR อะไร?", "/24"),
        new Question("Subnet mask 255.255.0.0 คือ CIDR อะไร?", "/16"),
        new Question("Subnet mask 255.0.0.0 คือ CIDR อะไร?", "/8"),
        new Question("Subnet mask 255.255.255.240 คือ CIDR อะไร?", "/28"),
        new Question("Subnet mask 255.255.255.128 คือ CIDR อะไร?", "/25"),

        new Question("โปรโตคอลใช้เรียกดูเว็บคือ?", "HTTP"),
        new Question("โปรโตคอลเข้ารหัสเว็บคือ?", "HTTPS"),
        new Question("โปรโตคอลส่งอีเมลคือ?", "SMTP"),
        new Question("โปรโตคอลดาวน์โหลดไฟล์คือ?", "FTP"),
        new Question("โปรโตคอลแปลงชื่อเป็น IP คือ?", "DNS"),
        new Question("พอร์ตของ HTTP คือ?", "80"),
        new Question("พอร์ตของ HTTPS คือ?", "443"),
        new Question("พอร์ตของ FTP คือ?", "21"),
        new Question("พอร์ตของ SSH คือ?", "22"),
        new Question("พอร์ตของ DNS คือ?", "53"),

        new Question("ชั้นที่ 3 ของ OSI คือ?", "Network"),
        new Question("ชั้นที่ 4 ของ OSI คือ?", "Transport"),
        
        new Question("โปรโตคอลในชั้น Transport คือ?", "TCP"),


        new Question("2^2 = ?", "4"),
        new Question("3^2 = ?", "9"),
        new Question("4^2 = ?", "16"),
        new Question("5^2 = ?", "25"),
        new Question("6^2 = ?", "36"),
        new Question("7^2 = ?", "49"),
        new Question("8^2 = ?", "64"),
        new Question("9^2 = ?", "81"),
        new Question("10^2 = ?", "100"),
        new Question("2^3 = ?", "8"),
        new Question("3^3 = ?", "27"),
        new Question("4^3 = ?", "64"),
        new Question("5^3 = ?", "125"),
        new Question("10^3 = ?", "1000"),
        new Question("2^4 = ?", "16"),
        new Question("2^5 = ?", "32"),
        new Question("3^4 = ?", "81"),
        new Question("3^5 = ?", "243"),
        new Question("4^4 = ?", "256"),
        new Question("5^4 = ?", "625"),
  


    };
    
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
    public static final int MAX_TURNS = 2;
    
    public static final double TIME_BASE_COST = 1.0;
    public static final double TIME_DISTANCE_MULTIPLIER = 250.0;
    public static final double TIME_MAX_COST = 3.0;
    public static final double TIME_AUTO_DECREASE = 0.01;
    public static final long TIME_AUTO_DECREASE_INTERVAL_MS = 1000;
    
    public static final double FISHING_TIME_COST = 1;
    
    public static final Fish[] FISHES = {
        new Fish("ปลาธรรมดา", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Fish-Basic.png", 2, 20, 30.0),
        new Fish("ปลาสวยงาม", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Fish-Nice.png", 3, 40, 25.0),
        new Fish("ปลากบ", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Fish-Frog.png", 3, 30, 20.0),
        new Fish("ปลาพิเศษ", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Fish-Special.png", 3, 60, 15.0),
        new Fish("ปลาน่ากลัว", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Fish-Scary.png", 2, 10, 35.0),
        new Fish("Blobfish", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Special-Blobfish.png", 2, 100, 5.0),
        new Fish("ปลาหมึก", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Special-Octopus.png", 2, 80, 8.0),
        new Fish("ปลาคาร์พ", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Special-Koi.png", 3, 90, 7.0),
        new Fish("ปลาดาว", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Special-Starfish.png", 3, 70, 10.0),
        new Fish("ขวด", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Trash-Bottle.png", 2, 0, 40.0),
        new Fish("รองเท้าบูท", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Trash-Boot.png", 2, 0, 35.0),
        new Fish("ถุงขยะ", "assets" + java.io.File.separator + "ui" + java.io.File.separator + "fishs" + java.io.File.separator + "Icon-Fishing-Trash-Bag.png", 2, 0, 30.0),
    };

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
    
    public static class Fish {
        public String name;
        public String imagePath;
        public int healthBonus;
        public int moneyBonus;
        public double weight; 
        
        public Fish(String name, String imagePath, int healthBonus, int moneyBonus, double weight) {
            this.name = name;
            this.imagePath = imagePath;
            this.healthBonus = healthBonus;
            this.moneyBonus = moneyBonus;
            this.weight = weight;
        }
    }
}

