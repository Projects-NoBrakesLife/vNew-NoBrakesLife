package game;

import java.util.ArrayList;
import editor.Waypoint;

public class GameConfig {
    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    
    public static final String BACKGROUND_IMAGE = "assets" + java.io.File.separator + "background" + java.io.File.separator + "NewTimesMapMock4a.png";
    public static final String HOVER_SOUND = "assets" + java.io.File.separator + "sfx" + java.io.File.separator + "Button Select.wav";
    
    public static final long LOADING_SCREEN_DELAY_MS = 5000;

    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 8888;
    public static final int MAX_PLAYERS = 4;
    public static final int MIN_PLAYERS_TO_START = 2;

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
    
    public static ArrayList<ArrayList<Waypoint>> getWaypointPaths() {
        ArrayList<ArrayList<Waypoint>> allPaths = new ArrayList<>();

        ArrayList<Waypoint> path0 = new ArrayList<>();
        path0.add(new Waypoint(949.0, 461.0));
        path0.add(new Waypoint(870.0, 422.0));
        path0.add(new Waypoint(830.0, 400.0));
        path0.add(new Waypoint(791.0, 389.0));
        path0.add(new Waypoint(767.0, 401.0));
        path0.add(new Waypoint(735.0, 421.0));
        path0.add(new Waypoint(676.0, 445.0));
        path0.add(new Waypoint(644.0, 462.0));
        path0.add(new Waypoint(606.0, 474.0));
        path0.add(new Waypoint(576.0, 496.0));
        path0.add(new Waypoint(546.0, 509.0));
        path0.add(new Waypoint(462.0, 539.0));
        path0.add(new Waypoint(408.0, 575.0));
        path0.add(new Waypoint(353.0, 607.0));
        path0.add(new Waypoint(307.0, 632.0));
        path0.add(new Waypoint(273.0, 653.0));
        path0.add(new Waypoint(250.0, 662.0));
        path0.add(new Waypoint(293.0, 693.0));
        path0.add(new Waypoint(338.0, 716.0));
        path0.add(new Waypoint(382.0, 740.0));
        path0.add(new Waypoint(431.0, 761.0));
        path0.add(new Waypoint(493.0, 794.0));
        path0.add(new Waypoint(548.0, 819.0));
        path0.add(new Waypoint(625.0, 843.0));
        path0.add(new Waypoint(639.0, 871.0));
        path0.add(new Waypoint(701.0, 892.0));
        path0.add(new Waypoint(731.0, 917.0));
        path0.add(new Waypoint(763.0, 929.0));
        path0.add(new Waypoint(793.0, 943.0));
        path0.add(new Waypoint(858.0, 923.0));
        path0.add(new Waypoint(909.0, 897.0));
        path0.add(new Waypoint(942.0, 876.0));
        path0.add(new Waypoint(968.0, 872.0));
        path0.add(new Waypoint(1001.0, 893.0));
        path0.add(new Waypoint(1058.0, 919.0));
        path0.add(new Waypoint(1091.0, 935.0));
        path0.add(new Waypoint(1113.0, 942.0));
        path0.add(new Waypoint(1185.0, 915.0));
        path0.add(new Waypoint(1228.0, 893.0));
        path0.add(new Waypoint(1265.0, 869.0));
        path0.add(new Waypoint(1321.0, 848.0));
        path0.add(new Waypoint(1354.0, 821.0));
        path0.add(new Waypoint(1409.0, 798.0));
        path0.add(new Waypoint(1451.0, 778.0));
        path0.add(new Waypoint(1496.0, 752.0));
        path0.add(new Waypoint(1572.0, 716.0));
        path0.add(new Waypoint(1613.0, 693.0));
        path0.add(new Waypoint(1650.0, 674.0));
        path0.add(new Waypoint(1650.0, 654.0));
        path0.add(new Waypoint(1617.0, 626.0));
        path0.add(new Waypoint(1575.0, 613.0));
        path0.add(new Waypoint(1527.0, 585.0));
        path0.add(new Waypoint(1461.0, 554.0));
        path0.add(new Waypoint(1420.0, 533.0));
        path0.add(new Waypoint(1361.0, 507.0));
        path0.add(new Waypoint(1309.0, 483.0));
        path0.add(new Waypoint(1274.0, 461.0));
        path0.add(new Waypoint(1209.0, 418.0));
        path0.add(new Waypoint(1175.0, 402.0));
        path0.add(new Waypoint(1149.0, 384.0));
        path0.add(new Waypoint(1124.0, 387.0));
        path0.add(new Waypoint(1074.0, 406.0));
        path0.add(new Waypoint(1027.0, 426.0));
        path0.add(new Waypoint(1000.0, 441.0));
        path0.add(new Waypoint(982.0, 455.0));
        path0.add(new Waypoint(962.0, 459.0));
        path0.add(new Waypoint(953.0, 460.0));
        allPaths.add(path0);

        ArrayList<Waypoint> path1 = new ArrayList<>();
        path1.add(new Waypoint(1333.0, 515.0));
        path1.add(new Waypoint(1292.0, 541.0));
        path1.add(new Waypoint(1204.0, 577.0));
        path1.add(new Waypoint(1153.0, 603.0));
        path1.add(new Waypoint(1121.0, 620.0));
        path1.add(new Waypoint(1043.0, 653.0));
        path1.add(new Waypoint(984.0, 688.0));
        path1.add(new Waypoint(959.0, 702.0));
        path1.add(new Waypoint(891.0, 678.0));
        path1.add(new Waypoint(835.0, 637.0));
        path1.add(new Waypoint(760.0, 606.0));
        path1.add(new Waypoint(732.0, 594.0));
        path1.add(new Waypoint(668.0, 559.0));
        path1.add(new Waypoint(605.0, 532.0));
        path1.add(new Waypoint(573.0, 509.0));
        allPaths.add(path1);

        ArrayList<Waypoint> path2 = new ArrayList<>();
        allPaths.add(path2);

        return allPaths;
    }
}

