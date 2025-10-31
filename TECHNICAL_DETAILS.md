# รายละเอียดทางเทคนิค - No Brakes Life

เอกสารนี้อธิบายการทำงานทางเทคนิคของโปรเจค แบ่งเป็น 3 หัวข้อหลัก

---

## 📋 สารบัญ
1. [การวาดโดยใช้ Paint และ PaintComponent](#1-การวาดโดยใช้-paint-และ-paintcomponent)
2. [การทำงานแบบ Multi-Tasking (Thread)](#2-การทำงานแบบ-multi-tasking-thread)
3. [การเชื่อมต่อผ่านเครือข่าย (Client-Server)](#3-การเชื่อมต่อผ่านเครือข่าย-client-server)

---

# 1. การวาดโดยใช้ Paint และ PaintComponent

## 🎨 หลักการ

ในโปรเจคนี้ใช้ `Graphics2D` ของ Java Swing ในการวาด UI, ตัวละคร, และ Animation ทั้งหมด โดยการ Override method `paintComponent()`

## 📝 โครงสร้างการทำงาน

```
JPanel
  ↓
Override paintComponent(Graphics g)
  ↓
แปลงเป็น Graphics2D
  ↓
วาด: Background → Objects → Players → UI → Effects
  ↓
repaint() เพื่อวาดเฟรมใหม่
```

## 💻 ตัวอย่างโค้ดจริง

### 1.1 การ Override paintComponent ใน GameWindow

**ไฟล์**: `src/game/GameWindow.java`

```java
public class GamePanel extends JPanel {
    private GameScene gameScene;
    private BackgroundManager backgroundManager;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // แปลง Graphics เป็น Graphics2D เพื่อใช้ฟีเจอร์ขั้นสูง
        Graphics2D g2d = (Graphics2D) g;
        
        // เปิดใช้ Anti-aliasing เพื่อภาพสวยขึ้น
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        
        // 1. วาดพื้นหลัง
        if (backgroundManager != null) {
            backgroundManager.render(g2d);
        }
        
        // 2. วาด Game Objects และ Players
        if (gameScene != null) {
            gameScene.render(g2d);
        }
    }
}
```

**การทำงาน**:
- `super.paintComponent(g)` ล้างหน้าจอก่อน
- แปลง `Graphics` เป็น `Graphics2D` เพื่อใช้ฟีเจอร์เพิ่มเติม
- เปิด Anti-aliasing เพื่อให้ขอบนุ่มนวล
- วาดตามลำดับ: พื้นหลัง → วัตถุ → ตัวละคร → UI

---

### 1.2 การวาดตัวละครพร้อม Animation

**ไฟล์**: `src/game/Player.java`

```java
public class Player {
    private double x, y;                    // ตำแหน่ง
    private String direction = "front";     // ทิศทาง
    private boolean isMoving = false;       // กำลังเดินหรือไม่
    private int animationFrame = 0;         // เฟรม animation ปัจจุบัน
    private long lastFrameTime = 0;        // เวลาเปลี่ยนเฟรมล่าสุด
    
    // โหลดภาพ Animation (6 เฟรม)
    private BufferedImage[] frontWalkFrames = new BufferedImage[6];
    private BufferedImage[] backWalkFrames = new BufferedImage[6];
    private BufferedImage frontIdle;
    private BufferedImage backIdle;
    
    public void render(Graphics2D g2d) {
        // เลือกภาพที่จะวาด
        BufferedImage currentImage = getCurrentFrame();
        
        if (currentImage != null) {
            // คำนวณขนาดที่จะวาด
            int drawWidth = 100;
            int drawHeight = 100;
            int drawX = (int) x - drawWidth / 2;
            int drawY = (int) y - drawHeight / 2;
            
            // วาดภาพตัวละคร
            g2d.drawImage(
                currentImage, 
                drawX, drawY, 
                drawWidth, drawHeight, 
                null
            );
            
            // วาด indicator (ถ้าเป็น Local Player)
            if (showIndicator) {
                drawPlayerIndicator(g2d, drawX, drawY);
            }
        }
    }
    
    private BufferedImage getCurrentFrame() {
        // ถ้ากำลังเดิน ใช้ animation
        if (isMoving) {
            // อัปเดตเฟรม animation ทุก 100ms
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameTime > 100) {
                animationFrame = (animationFrame + 1) % 6; // วนลูป 0-5
                lastFrameTime = currentTime;
            }
            
            // เลือก animation ตามทิศทาง
            if ("front".equals(direction)) {
                return frontWalkFrames[animationFrame];
            } else {
                return backWalkFrames[animationFrame];
            }
        } 
        // ถ้าอยู่นิ่ง ใช้ idle frame
        else {
            if ("front".equals(direction)) {
                return frontIdle;
            } else {
                return backIdle;
            }
        }
    }
    
    private void drawPlayerIndicator(Graphics2D g2d, int x, int y) {
        // วาดลูกศรชี้ลงมา (บอกว่าเป็นตัวเราเอง)
        g2d.setColor(new Color(255, 255, 0, 200)); // สีเหลืองโปร่งใส
        
        int[] xPoints = {x + 50, x + 40, x + 60};
        int[] yPoints = {y - 20, y - 10, y - 10};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
}
```

**การทำงาน Animation**:
1. เก็บ array ของภาพ 6 เฟรม
2. เปลี่ยนเฟรมทุก 100ms (10 FPS)
3. วนลูป 0→5 แล้วกลับไป 0
4. เลือกภาพตามสถานะ (เดิน/หยุด) และทิศทาง (หน้า/หลัง)

---

### 1.3 การวาด UI Elements พร้อม Hover Effects

**ไฟล์**: `src/game/MenuElement.java`

```java
public class MenuElement {
    private double x, y, width, height;
    private BufferedImage image;
    private BufferedImage hoverImage;  // ภาพตอน hover
    private boolean isHovered = false;
    private boolean useScaleEffect = false;
    
    public void render(Graphics2D g2d) {
        // เลือกภาพที่จะแสดง
        BufferedImage currentImage = (isHovered && hoverImage != null) 
            ? hoverImage 
            : image;
        
        if (currentImage != null) {
            // คำนวณการขยาย (Scale Effect)
            double scale = 1.0;
            if (useScaleEffect && isHovered) {
                scale = 1.1; // ขยาย 10%
            }
            
            int drawWidth = (int) (width * scale);
            int drawHeight = (int) (height * scale);
            int drawX = (int) (x - (drawWidth - width) / 2);
            int drawY = (int) (y - (drawHeight - height) / 2);
            
            // วาดภาพ
            g2d.drawImage(
                currentImage, 
                drawX, drawY, 
                drawWidth, drawHeight, 
                null
            );
        }
        
        // ถ้าเป็น TEXT ให้วาดข้อความ
        if (type == ElementType.TEXT && text != null) {
            g2d.setFont(font);
            g2d.setColor(textColor);
            g2d.drawString(text, (int) x, (int) y);
        }
    }
}
```

**Hover Effect**:
- เปลี่ยนภาพเมื่อเมาส์อยู่เหนือ
- ขยายขนาด 10% เมื่อ hover (Scale Effect)
- คำนวณตำแหน่งใหม่ให้อยู่กึ่งกลางเดิม

---

### 1.4 การวาดพร้อม Alpha (โปร่งใส)

**ไฟล์**: `src/game/GameScene.java`

```java
private void renderTurnDisplay(Graphics2D g2d) {
    // คำนวณค่า alpha จากเวลา (fade in/out)
    long elapsed = System.currentTimeMillis() - turnDisplayStartTime;
    
    float alpha = 0.0f;
    if (elapsed < FADE_IN_DURATION) {
        // Fade In
        alpha = (float) elapsed / FADE_IN_DURATION;
    } else if (elapsed > DISPLAY_DURATION) {
        // Fade Out
        float fadeProgress = (float) (elapsed - DISPLAY_DURATION) / FADE_OUT_DURATION;
        alpha = Math.max(0.0f, 1.0f - fadeProgress);
    } else {
        // แสดงเต็มที่
        alpha = 1.0f;
    }
    
    if (alpha > 0.01f) {
        // วาดพื้นหลังดำโปร่งใส
        float bgAlpha = alpha * 0.6f;
        g2d.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 
            bgAlpha
        ));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, 1920, 1080);
        
        // รีเซ็ต composite กลับ
        g2d.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 
            1.0f
        ));
        
        // วาดข้อความ
        g2d.setFont(new Font("Noto Serif Thai", Font.BOLD, 72));
        g2d.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(turnDisplayText);
        int x = (1920 - textWidth) / 2;
        int y = 1080 / 2;
        
        g2d.drawString(turnDisplayText, x, y);
    }
}
```

**Fade In/Out Effect**:
1. คำนวณ `alpha` จาก 0.0 → 1.0 → 0.0
2. ใช้ `AlphaComposite` กำหนดความโปร่งใส
3. วาดพื้นหลังดำโปร่งใส 60%
4. วาดข้อความที่ค่อยๆ ปรากฏและจางหาย

---

### 1.5 การ repaint() และ Game Loop

**ไฟล์**: `src/game/GameWindow.java`

```java
public class GameWindow extends JFrame {
    private GamePanel gamePanel;
    private Timer gameTimer;
    
    public GameWindow() {
        // สร้าง Timer สำหรับ Game Loop
        gameTimer = new Timer(16, e -> {
            // 16ms = ~60 FPS (1000ms / 60 = 16.67ms)
            
            // อัปเดตตรรกะเกม
            if (gamePanel != null && gamePanel.getGameScene() != null) {
                // Update logic (ไม่มีการวาดที่นี่)
                gamePanel.getGameScene().update();
            }
            
            // วาดใหม่ทั้งหมด
            gamePanel.repaint();
        });
        
        gameTimer.start();
    }
}
```

**Game Loop**:
- ใช้ `javax.swing.Timer` ทำงานทุก 16ms (60 FPS)
- แยก Update (ตรรกะ) กับ Render (การวาด)
- เรียก `repaint()` เพื่อวาดเฟรมใหม่
- `paintComponent()` จะถูกเรียกอัตโนมัติ

---

## 🎯 สรุปการวาด

```
Game Loop (60 FPS)
    ↓
repaint()
    ↓
paintComponent(Graphics g)
    ↓
super.paintComponent(g) ← ล้างหน้าจอ
    ↓
Graphics2D g2d = (Graphics2D) g
    ↓
วาดตามลำดับ:
  1. Background (พื้นหลัง)
  2. GameObject (สถานที่ต่างๆ)
  3. Player Animation (ตัวละครเคลื่อนไหว)
  4. UI Elements (ปุ่ม, ข้อความ)
  5. Effects (Fade, Alpha)
  6. HUD (แสดงสถานะ)
    ↓
แสดงผลบนหน้าจอ
```

### ข้อดี
✅ วาดทุกอย่างได้ตามต้องการ (Full Control)
✅ Performance ดี (Hardware Accelerated)
✅ รองรับ Effects ต่างๆ (Alpha, Rotation, Scale)

### ข้อควรระวัง
⚠️ ห้ามทำงานหนักใน `paintComponent()` (ใช้เวลานาน = FPS ต่ำ)
⚠️ ต้องเรียก `super.paintComponent(g)` ก่อนเสมอ
⚠️ อย่าสร้างวัตถุใหม่ใน `paintComponent()` (ใช้ Memory มาก)

---

# 2. การทำงานแบบ Multi-Tasking (Thread)

## 🔄 หลักการ

ในเกมนี้ใช้ Thread หลายตัวทำงานพร้อมกัน:
1. **Main Thread** (Event Dispatch Thread) - จัดการ UI
2. **Game Loop Thread** - อัปเดตเกมทุก 16ms
3. **Network Thread** - รับส่งข้อมูลกับ server
4. **Server Threads** - แต่ละ client มี thread แยก

## 📝 โครงสร้าง Thread

```
Main Thread (UI)
    ├── Game Timer Thread (16ms loop)
    ├── Network Listener Thread (รับข้อมูล)
    ├── Sound Thread (เล่นเสียง)
    └── Animation Threads (effects)

Server
    ├── Accept Thread (รอ client เข้า)
    └── ClientHandler Threads (แยกคนละ thread)
        ├── Thread 1 (Client 1)
        ├── Thread 2 (Client 2)
        ├── Thread 3 (Client 3)
        └── Thread 4 (Client 4)
```

---

## 💻 ตัวอย่างโค้ดจริง

### 2.1 Network Listener Thread (Client)

**ไฟล์**: `src/network/NetworkManager.java`

```java
public class NetworkManager {
    private Thread listenerThread;
    private boolean connected = false;
    private BufferedReader in;
    
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            
            connected = true;
            
            // สร้าง Thread สำหรับรับข้อมูล
            listenerThread = new Thread(() -> listenToServer());
            listenerThread.setName("NetworkListener");
            listenerThread.setDaemon(true); // จบเมื่อโปรแกรมปิด
            listenerThread.start();
            
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    private void listenToServer() {
        // Thread นี้วนลูปตลอดเวลา รับข้อมูลจาก server
        while (connected) {
            try {
                String line = in.readLine(); // blocking - รอรับข้อมูล
                if (line == null) break;
                
                // ประมวลผลข้อความที่ได้รับ
                handleMessage(line);
                
            } catch (IOException e) {
                if (connected) {
                    // มี error แสดงว่าขาดการเชื่อมต่อ
                    connected = false;
                    showConnectionError();
                }
                break;
            }
        }
    }
    
    private void handleMessage(String message) {
        // ใช้ SwingUtilities.invokeLater() เพื่ออัปเดต UI
        // เพราะ Thread นี้ไม่ใช่ Event Dispatch Thread
        
        if (message.startsWith("PLAYER_MOVE:")) {
            String[] parts = message.split(":");
            int playerId = Integer.parseInt(parts[1]);
            double x = Double.parseDouble(parts[2]);
            double y = Double.parseDouble(parts[3]);
            
            // อัปเดต UI ต้องทำใน EDT
            SwingUtilities.invokeLater(() -> {
                if (gameScene != null) {
                    gameScene.updateRemotePlayer(playerId, x, y);
                    gamePanel.repaint(); // วาดใหม่
                }
            });
        }
    }
}
```

**การทำงาน**:
1. สร้าง Thread แยก (`listenerThread`)
2. ทำ daemon thread เพื่อปิดตามโปรแกรม
3. วนลูปรอรับข้อมูลด้วย `readLine()` (blocking)
4. ใช้ `SwingUtilities.invokeLater()` เมื่ออัปเดต UI

---

### 2.2 Server ClientHandler Threads

**ไฟล์**: `src/network/GameServer.java`

```java
public class GameServer {
    private List<ClientHandler> clients = new ArrayList<>();
    private boolean running = false;
    
    public void start() {
        running = true;
        
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            
            // Thread หลัก - รอรับ client ใหม่
            while (running) {
                Socket clientSocket = serverSocket.accept();
                logWindow.addLog("Client connected: " + 
                    clientSocket.getInetAddress());
                
                // สร้าง ClientHandler (Runnable)
                ClientHandler handler = new ClientHandler(
                    clientSocket, 
                    this
                );
                clients.add(handler);
                
                // สร้าง Thread ใหม่สำหรับ client นี้
                Thread handlerThread = new Thread(
                    handler, 
                    "Handler-" + clientSocket.getInetAddress()
                );
                handlerThread.setDaemon(true);
                handlerThread.start();
            }
        } catch (IOException e) {
            logWindow.addLog("Server error: " + e.getMessage());
        }
    }
    
    // Inner class - จัดการ client 1 คน
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private GameServer server;
        private boolean connected = true;
        
        public ClientHandler(Socket socket, GameServer server) {
            this.socket = socket;
            this.server = server;
        }
        
        @Override
        public void run() {
            // แต่ละ client มี thread ของตัวเอง
            try {
                in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
                );
                out = new PrintWriter(
                    socket.getOutputStream(), 
                    true
                );
                
                // รับข้อมูลจาก client
                while (connected && !socket.isClosed()) {
                    String line = in.readLine();
                    if (line == null) break;
                    
                    // ประมวลผลและ broadcast ไปยัง client อื่น
                    handleClientMessage(line);
                }
            } catch (IOException e) {
                // Client disconnect
            } finally {
                cleanup();
            }
        }
        
        private void handleClientMessage(String message) {
            // กระจายข้อมูลไปยัง client ทุกคน
            if (message.startsWith("PLAYER_MOVE:")) {
                // ส่งต่อไปยัง client อื่นๆ (ยกเว้นตัวเอง)
                for (ClientHandler client : server.clients) {
                    if (client != this) {
                        client.sendText(message);
                    }
                }
            }
        }
        
        public synchronized void sendText(String message) {
            // synchronized เพื่อป้องกันการส่งพร้อมกัน
            if (out != null && connected) {
                out.println(message);
                out.flush();
            }
        }
    }
}
```

**การทำงาน Multi-Client**:
```
Server Accept Loop
    ↓
Client 1 เชื่อมต่อ → สร้าง Thread 1
    ↓
Client 2 เชื่อมต่อ → สร้าง Thread 2
    ↓
Client 3 เชื่อมต่อ → สร้าง Thread 3
    ↓
Client 4 เชื่อมต่อ → สร้าง Thread 4

แต่ละ Thread:
  - รับข้อมูลจาก client ตัวเอง
  - ประมวลผล
  - ส่งต่อไปยัง client อื่น
```

---

### 2.3 Thread Safety และ Synchronization

**ปัญหา**: หลาย Thread อาจอ่าน/เขียนข้อมูลเดียวกันพร้อมกัน (Race Condition)

**วิธีแก้**: ใช้ `synchronized`

```java
public class GameScene {
    private ArrayList<Player> players = new ArrayList<>();
    
    // Method นี้อาจถูกเรียกจากหลาย Thread
    public synchronized void updatePlayerStats(
        int playerId, 
        int skill, 
        int education, 
        int health, 
        int money, 
        int bankDeposit
    ) {
        // synchronized ทำให้มีแค่ 1 thread ทำงานได้ในแต่ละเวลา
        int playerIndex = playerId - 1;
        if (playerIndex >= 0 && playerIndex < players.size()) {
            Player player = players.get(playerIndex);
            player.setSkill(skill);
            player.setEducation(education);
            player.setHealth(health);
            player.setMoney(money);
            player.setBankDeposit(bankDeposit);
            
            // อัปเดต UI ใน Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                updateHUDStats();
            });
        }
    }
}
```

**การใช้ `synchronized`**:
- ป้องกัน Race Condition
- ทำให้มีแค่ 1 thread ทำงานในเวลาเดียวกัน
- Thread อื่นต้องรอจนกว่าจะเสร็จ

---

### 2.4 SwingUtilities.invokeLater()

**ปัญหา**: การอัปเดต UI ต้องทำใน Event Dispatch Thread (EDT) เท่านั้น

**วิธีแก้**: ใช้ `SwingUtilities.invokeLater()`

```java
// ❌ ผิด - อัปเดต UI จาก Network Thread
private void handleMessage(String message) {
    if (message.startsWith("UPDATE_STATS:")) {
        // Thread นี้ไม่ใช่ EDT
        gameScene.updatePlayerStats(...);
        gamePanel.repaint(); // อันตราย!
    }
}

// ✅ ถูก - ใช้ invokeLater
private void handleMessage(String message) {
    if (message.startsWith("UPDATE_STATS:")) {
        String[] parts = message.split(":");
        int playerId = Integer.parseInt(parts[1]);
        int skill = Integer.parseInt(parts[2]);
        // ... parse ข้อมูล
        
        // ส่งงานไปทำใน EDT
        SwingUtilities.invokeLater(() -> {
            gameScene.updatePlayerStats(
                playerId, skill, education, health, money, bankDeposit
            );
            gamePanel.repaint();
        });
    }
}
```

**หลักการ**:
- UI Components ไม่ Thread-safe
- ต้องอัปเดตใน EDT เท่านั้น
- `invokeLater()` เอางานเข้าคิว EDT

---

### 2.5 Timer Thread สำหรับ Animation

```java
public class PopupWindow extends JDialog {
    private Timer fishingTimer;
    
    public void startFishingAnimation() {
        final int[] frame = {0};
        
        // สร้าง Timer (รันใน EDT)
        fishingTimer = new Timer(100, e -> {
            // เปลี่ยนเฟรม animation
            frame[0] = (frame[0] + 1) % 12;
            fishingAnimationElement.setImagePath(
                "assets/fishing/frame_" + frame[0] + ".png"
            );
            repaint();
        });
        
        fishingTimer.start();
        
        // หยุดอัตโนมัติหลัง 3 วินาที
        Timer stopTimer = new Timer(3000, e -> {
            fishingTimer.stop();
            showResult();
        });
        stopTimer.setRepeats(false);
        stopTimer.start();
    }
}
```

---

## 🎯 สรุป Threading

### Thread ที่ใช้ในโปรเจค

| Thread | หน้าที่ | Blocking? |
|--------|---------|-----------|
| EDT | วาด UI, จัดการ Event | ❌ |
| Game Timer | อัปเดตเกมทุก 16ms | ❌ |
| Network Listener | รับข้อมูลจาก server | ✅ (readLine) |
| ClientHandler (x4) | จัดการแต่ละ client | ✅ (readLine) |
| Sound Thread | เล่นเสียง | ❌ |

### หลักการสำคัญ

✅ **DO**:
- ใช้ `synchronized` เมื่อหลาย thread เข้าถึงข้อมูลเดียวกัน
- ใช้ `SwingUtilities.invokeLater()` เมื่ออัปเดต UI จาก thread อื่น
- ตั้งชื่อ thread เพื่อ debug ง่าย
- ใช้ daemon thread สำหรับ background task

❌ **DON'T**:
- อัปเดต UI จาก thread ที่ไม่ใช่ EDT
- ทำงานหนักใน EDT (จะค้าง)
- ลืม handle exception ใน thread
- สร้าง thread มากเกินไป (resource leak)

---

# 3. การเชื่อมต่อผ่านเครือข่าย (Client-Server)

## 🌐 สถาปัตยกรรมระบบ

```
┌─────────────┐         ┌─────────────┐
│  Client 1   │◄───────►│             │
│ (Player 1)  │         │             │
└─────────────┘         │             │
                        │   Server    │
┌─────────────┐         │  (Host PC)  │
│  Client 2   │◄───────►│             │
│ (Player 2)  │         │             │
└─────────────┘         │             │
                        │             │
┌─────────────┐         │             │
│  Client 3   │◄───────►│             │
│ (Player 3)  │         └─────────────┘
└─────────────┘

Protocol: TCP/IP Socket
Port: 12345
Format: Text (String ที่ส่งด้วย println/readLine)
```

---

## 💻 การทำงานโดยละเอียด

### 3.1 Server - การเริ่มต้นและรับ Connection

**ไฟล์**: `src/network/GameServer.java`

```java
public class GameServer {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private List<PlayerInfo> players = new ArrayList<>();
    private boolean gameStarted = false;
    private static final int PORT = 12345;
    
    public void start() {
        running = true;
        
        try {
            // 1. สร้าง ServerSocket รอรับ connection ที่ port 12345
            serverSocket = new ServerSocket(PORT);
            logWindow.addLog("Server started on port " + PORT);
            
            // 2. วนลูปรอรับ client
            while (running) {
                // accept() จะ block รอจนมี client เชื่อมต่อ
                Socket clientSocket = serverSocket.accept();
                logWindow.addLog("Client connected: " + 
                    clientSocket.getInetAddress());
                
                // 3. สร้าง ClientHandler สำหรับ client นี้
                ClientHandler handler = new ClientHandler(
                    clientSocket, 
                    this
                );
                clients.add(handler);
                
                // 4. สร้าง Thread ใหม่สำหรับ client นี้
                Thread handlerThread = new Thread(
                    handler, 
                    "Handler-" + clientSocket.getInetAddress()
                );
                handlerThread.setDaemon(true);
                handlerThread.start();
            }
        } catch (IOException e) {
            logWindow.addLog("Server error: " + e.getMessage());
        }
    }
    
    public void handleClientJoin(ClientHandler client) {
        // เช็คว่าเกมเริ่มแล้วหรือยัง
        if (gameStarted) {
            client.sendText("GAME_STARTED");
            return;
        }
        
        // นับจำนวนผู้เล่นที่เชื่อมต่ออยู่
        int connectedCount = 0;
        for (PlayerInfo p : players) {
            if (p.isConnected) connectedCount++;
        }
        
        // ตรวจสอบว่าเต็มหรือยัง (สูงสุด 4 คน)
        if (connectedCount < GameConfig.MAX_PLAYERS) {
            // หา Player ID ว่าง
            int playerId = getNextAvailablePlayerId();
            
            if (playerId > 0) {
                // กำหนด Player ID ให้ client
                client.setPlayerId(playerId);
                players.get(playerId - 1).isConnected = true;
                logWindow.addLog("Player " + playerId + " joined");
                
                // อัปเดตจำนวนผู้เล่นใหม่
                int newConnectedCount = 0;
                for (PlayerInfo p : players) {
                    if (p.isConnected) newConnectedCount++;
                }
                
                // ส่งข้อมูล lobby ไปยัง client ทุกคน
                List<PlayerInfo> lobbyData = new ArrayList<>(players);
                GameMessage message = new GameMessage(
                    GameMessage.MessageType.UPDATE_LOBBY, 
                    lobbyData, 
                    -1
                );
                broadcastToAll(message);
                
                // ส่ง Player ID ให้ client ที่เพิ่งเข้ามา
                client.sendText("PLAYER_ID:" + playerId);
                
                // ถ้าครบจำนวนขั้นต่ำแล้ว เริ่มเกม
                if (newConnectedCount >= GameConfig.MIN_PLAYERS_TO_START) {
                    gameStarted = true;
                    
                    // รอ 3 วินาที แล้วส่งสัญญาณเริ่มเกม
                    new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                            for (ClientHandler c : new ArrayList<>(clients)) {
                                c.sendText("START_GAME");
                            }
                            logWindow.addLog("Game started!");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            }
        }
    }
    
    private int getNextAvailablePlayerId() {
        // หา Player ID ว่าง (1-4)
        for (int i = 0; i < players.size(); i++) {
            if (!players.get(i).isConnected) {
                return i + 1;
            }
        }
        return -1;
    }
}
```

---

### 3.2 Client - การเชื่อมต่อและรับ-ส่งข้อมูล

**ไฟล์**: `src/network/NetworkManager.java`

```java
public class NetworkManager {
    private static NetworkManager instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    private int playerId = -1;
    
    // Singleton Pattern
    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }
    
    public boolean connect(String host, int port) {
        try {
            // 1. สร้าง Socket เชื่อมต่อไปยัง server
            socket = new Socket(host, port);
            
            // 2. สร้าง Input/Output Stream
            out = new PrintWriter(
                socket.getOutputStream(), 
                true  // auto-flush
            );
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            
            connected = true;
            
            // 3. สร้าง Thread สำหรับรับข้อมูล
            listenerThread = new Thread(() -> listenToServer());
            listenerThread.setName("NetworkListener");
            listenerThread.setDaemon(true);
            listenerThread.start();
            
            return true;
        } catch (IOException e) {
            connected = false;
            return false;
        }
    }
    
    private void listenToServer() {
        // Thread นี้รับข้อมูลจาก server ตลอดเวลา
        while (connected) {
            try {
                // อ่านข้อความทีละบรรทัด (blocking)
                String line = in.readLine();
                if (line == null) break;
                
                // ประมวลผลตามประเภทข้อความ
                handleServerMessage(line);
                
            } catch (IOException e) {
                if (connected) {
                    connected = false;
                    SwingUtilities.invokeLater(() -> {
                        showConnectionError();
                    });
                }
                break;
            }
        }
    }
    
    private void handleServerMessage(String message) {
        // === รับ Player ID ===
        if (message.startsWith("PLAYER_ID:")) {
            String[] parts = message.split(":");
            playerId = Integer.parseInt(parts[1]);
        }
        
        // === รับข้อมูล Lobby ===
        else if (message.equals("LOBBY_UPDATE")) {
            try {
                String countLine = in.readLine();
                if (countLine.startsWith("PLAYER_COUNT:")) {
                    int count = Integer.parseInt(countLine.split(":")[1]);
                    
                    List<PlayerInfo> newPlayers = new ArrayList<>();
                    for (int i = 1; i <= count; i++) {
                        newPlayers.add(
                            new PlayerInfo(i, "Player_" + i, true)
                        );
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        if (lobbyMenu != null) {
                            lobbyMenu.updateLobbyInfo(newPlayers);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // === เริ่มเกม ===
        else if (message.equals("START_GAME")) {
            SwingUtilities.invokeLater(() -> {
                if (lobbyMenu != null) {
                    lobbyMenu.startGame();
                }
            });
        }
        
        // === รับการเคลื่อนที่ของผู้เล่นคนอื่น ===
        else if (message.startsWith("PLAYER_MOVE:")) {
            // Format: PLAYER_MOVE:id:x:y:direction:isMoving:time:destX:destY
            String[] parts = message.split(":");
            if (parts.length >= 6) {
                int remotePlayerId = Integer.parseInt(parts[1]);
                double x = Double.parseDouble(parts[2]);
                double y = Double.parseDouble(parts[3]);
                String direction = parts[4];
                boolean isMoving = "true".equals(parts[5]);
                double remainingTime = parts.length >= 7 
                    ? Double.parseDouble(parts[6]) 
                    : 24.0;
                double destX = parts.length >= 8 
                    ? Double.parseDouble(parts[7]) 
                    : x;
                double destY = parts.length >= 9 
                    ? Double.parseDouble(parts[8]) 
                    : y;
                
                SwingUtilities.invokeLater(() -> {
                    if (gameScene != null) {
                        gameScene.updateRemotePlayer(
                            remotePlayerId, 
                            x, y, 
                            direction, 
                            isMoving, 
                            remainingTime, 
                            destX, destY
                        );
                    }
                });
            }
        }
        
        // === รับการอัปเดต Stats ===
        else if (message.startsWith("UPDATE_STATS:")) {
            // Format: UPDATE_STATS:id:skill:edu:health:money:bank
            String[] parts = message.split(":");
            if (parts.length >= 7) {
                int targetPlayerId = Integer.parseInt(parts[1]);
                int skill = Integer.parseInt(parts[2]);
                int education = Integer.parseInt(parts[3]);
                int health = Integer.parseInt(parts[4]);
                int money = Integer.parseInt(parts[5]);
                int bankDeposit = Integer.parseInt(parts[6]);
                
                SwingUtilities.invokeLater(() -> {
                    if (gameScene != null) {
                        gameScene.updatePlayerStats(
                            targetPlayerId, 
                            skill, 
                            education, 
                            health, 
                            money, 
                            bankDeposit
                        );
                        gamePanel.repaint();
                    }
                });
            }
        }
        
        // === รับข้อมูลเทิร์น ===
        else if (message.startsWith("TURN_UPDATE:")) {
            // Format: TURN_UPDATE:playerId:turnNumber:type
            String[] parts = message.split(":");
            if (parts.length >= 3) {
                int turnPlayerId = Integer.parseInt(parts[1]);
                int turnNumber = Integer.parseInt(parts[2]);
                String updateType = parts.length >= 4 
                    ? parts[3] 
                    : "TURN";
                
                SwingUtilities.invokeLater(() -> {
                    if (gameScene != null) {
                        gameScene.setTurn(
                            turnPlayerId, 
                            turnNumber, 
                            updateType
                        );
                    }
                });
            }
        }
    }
    
    // ========== ส่งข้อมูลไปยัง Server ==========
    
    public void sendPlayerMove(Player player) {
        if (connected && out != null && playerId > 0) {
            // สร้างข้อความ
            String moveData = String.format(
                "PLAYER_MOVE:%d:%.2f:%.2f:%s:%s:%.2f:%.2f:%.2f",
                playerId,
                player.getX(),
                player.getY(),
                player.getDirection(),
                player.isMoving() ? "true" : "false",
                player.getRemainingTime(),
                player.getDestinationX(),
                player.getDestinationY()
            );
            
            // ส่งไปยัง server
            out.println(moveData);
            out.flush();
        }
    }
    
    public void sendPlayerStats(
        int playerId, 
        int skill, 
        int education, 
        int health, 
        int money, 
        int bankDeposit
    ) {
        if (connected && out != null && playerId > 0) {
            String statsData = String.format(
                "UPDATE_STATS:%d:%d:%d:%d:%d:%d", 
                playerId, 
                skill, 
                education, 
                health, 
                money, 
                bankDeposit
            );
            out.println(statsData);
            out.flush();
        }
    }
    
    public void sendMessage(String message) {
        if (connected && out != null) {
            out.println(message);
            out.flush();
        }
    }
}
```

---

### 3.3 Protocol - รูปแบบข้อความที่ส่งกัน

| ข้อความ | ทิศทาง | Format | ตัวอย่าง |
|---------|--------|--------|----------|
| **PLAYER_ID** | Server→Client | `PLAYER_ID:id` | `PLAYER_ID:1` |
| **LOBBY_UPDATE** | Server→Client | `LOBBY_UPDATE\nPLAYER_COUNT:n` | `LOBBY_UPDATE\nPLAYER_COUNT:3` |
| **START_GAME** | Server→Client | `START_GAME` | `START_GAME` |
| **PLAYER_MOVE** | Client↔Server | `PLAYER_MOVE:id:x:y:dir:moving:time:dx:dy` | `PLAYER_MOVE:1:100.5:200.3:front:true:22.5:150.0:250.0` |
| **UPDATE_STATS** | Client→Server | `UPDATE_STATS:id:skill:edu:hp:$$:bank` | `UPDATE_STATS:1:50:30:80:1500:200` |
| **TURN_UPDATE** | Server→Client | `TURN_UPDATE:playerId:turn:type` | `TURN_UPDATE:2:5:TURN` |
| **TURN_COMPLETE** | Client→Server | `TURN_COMPLETE:playerId:turn` | `TURN_COMPLETE:1:4` |
| **PLAYER_HOVER** | Client↔Server | `PLAYER_HOVER:id:index` | `PLAYER_HOVER:1:3` |
| **SYNC_PLAYER** | Client↔Server | `SYNC_PLAYER:id:skill:edu:hp:$$:bank` | `SYNC_PLAYER:1:50:30:80:1500:200` |
| **PLAYER_DISCONNECT** | Server→Client | `PLAYER_DISCONNECT:id` | `PLAYER_DISCONNECT:3` |

---

### 3.4 การ Broadcast ข้อมูล (Server)

```java
public class GameServer {
    
    // Broadcast ไปยัง Client ทุกคน
    public void broadcastToAll(GameMessage message) {
        synchronized (clients) {
            List<ClientHandler> clientsCopy = new ArrayList<>(clients);
            for (ClientHandler client : clientsCopy) {
                try {
                    client.sendMessage(message);
                } catch (Exception e) {
                    // Client disconnect
                }
            }
        }
    }
    
    // Broadcast Stats Update
    public void broadcastPlayerStatsUpdate(PlayerInfo playerInfo) {
        String message = String.format(
            "UPDATE_STATS:%d:%d:%d:%d:%d:%d",
            playerInfo.playerId,
            playerInfo.skill,
            playerInfo.education,
            playerInfo.health,
            playerInfo.money,
            playerInfo.bankDeposit
        );
        
        // อัปเดตข้อมูล server-side
        synchronized (players) {
            for (PlayerInfo p : players) {
                if (p.playerId == playerInfo.playerId) {
                    p.skill = playerInfo.skill;
                    p.education = playerInfo.education;
                    p.health = playerInfo.health;
                    p.money = playerInfo.money;
                    p.bankDeposit = playerInfo.bankDeposit;
                    break;
                }
            }
        }
        
        // ส่งไปยัง client ทุกคน
        synchronized (clients) {
            for (ClientHandler client : new ArrayList<>(clients)) {
                try {
                    client.sendText(message);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
```

---

### 3.5 ClientHandler - จัดการข้อความจาก Client

```java
public static class ClientHandler implements Runnable {
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(
                socket.getOutputStream(), 
                true
            );
            
            // แจ้ง server ว่า client นี้เข้ามา
            server.handleClientJoin(this);
            
            // วนลูปรับข้อความจาก client
            while (connected && !socket.isClosed()) {
                try {
                    String line = in.readLine();
                    if (line == null) break;
                    
                    // ประมวลผลข้อความ
                    if (line.startsWith("PLAYER_MOVE:")) {
                        // Forward ไปยัง client อื่น (ยกเว้นตัวเอง)
                        for (ClientHandler client : server.clients) {
                            if (client != this) {
                                client.sendText(line);
                            }
                        }
                    }
                    
                    else if (line.startsWith("PLAYER_HOVER:")) {
                        // Forward ไปยัง client อื่น
                        for (ClientHandler client : server.clients) {
                            if (client != this) {
                                client.sendText(line);
                            }
                        }
                    }
                    
                    else if (line.startsWith("UPDATE_STATS:")) {
                        // Parse และ broadcast
                        String[] parts = line.split(":");
                        if (parts.length >= 7) {
                            int targetPlayerId = Integer.parseInt(parts[1]);
                            int skill = Integer.parseInt(parts[2]);
                            int education = Integer.parseInt(parts[3]);
                            int health = Integer.parseInt(parts[4]);
                            int money = Integer.parseInt(parts[5]);
                            int bankDeposit = Integer.parseInt(parts[6]);
                            
                            PlayerInfo pInfo = new PlayerInfo(
                                targetPlayerId, 
                                "Player_" + targetPlayerId, 
                                true
                            );
                            pInfo.skill = skill;
                            pInfo.education = education;
                            pInfo.health = health;
                            pInfo.money = money;
                            pInfo.bankDeposit = bankDeposit;
                            
                            server.broadcastPlayerStatsUpdate(pInfo);
                        }
                    }
                    
                    else if (line.startsWith("TURN_COMPLETE:")) {
                        // คำนวณเทิร์นถัดไปและ broadcast
                        String[] parts = line.split(":");
                        if (parts.length >= 3) {
                            int turnPlayerId = Integer.parseInt(parts[1]);
                            int turnNumber = Integer.parseInt(parts[2]);
                            
                            // นับจำนวนผู้เล่น
                            int connectedCount = 0;
                            for (PlayerInfo p : server.players) {
                                if (p.isConnected) connectedCount++;
                            }
                            
                            // คำนวณเทิร์นถัดไป
                            int nextTurnPlayerId = turnPlayerId + 1;
                            boolean isNewWeek = false;
                            
                            if (nextTurnPlayerId > connectedCount) {
                                nextTurnPlayerId = 1;
                                turnNumber++;
                                isNewWeek = true;
                            }
                            
                            // ส่งข้อมูลเทิร์นใหม่
                            String turnUpdate = "TURN_UPDATE:" + 
                                nextTurnPlayerId + ":" + 
                                turnNumber + ":" + 
                                (isNewWeek ? "WEEK" : "TURN");
                            
                            for (ClientHandler client : server.clients) {
                                client.sendText(turnUpdate);
                            }
                        }
                    }
                    
                } catch (IOException e) {
                    break;
                }
            }
        } catch (IOException e) {
            server.logWindow.addLog("Client error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    public synchronized void sendText(String message) {
        if (out != null && connected) {
            try {
                out.println(message);
                out.flush();
            } catch (Exception e) {
                connected = false;
            }
        }
    }
}
```

---

### 3.6 ตัวอย่างการส่งข้อมูลในเกม

**การส่งข้อมูลตำแหน่งผู้เล่น** (ทุก 50ms)

```java
// ใน GameScene.java
public void render(Graphics2D g2d) {
    // ... วาด graphics ...
    
    // ส่งข้อมูลตำแหน่งทุก 50ms (20 times/sec)
    if (isOnlineMode && networkManager.isConnected()) {
        int localPlayerIndex = networkManager.getPlayerId() - 1;
        if (localPlayerIndex >= 0 && localPlayerIndex < players.size()) {
            Player localPlayer = players.get(localPlayerIndex);
            
            long networkUpdateTime = System.currentTimeMillis();
            if (!networkManager.hasLastUpdate() || 
                (networkUpdateTime - networkManager.getLastUpdateTime()) > 50) {
                
                // ส่งตำแหน่งไปยัง server
                networkManager.sendPlayerMove(localPlayer);
                networkManager.setLastUpdateTime(networkUpdateTime);
            }
        }
    }
}
```

**การส่งข้อมูล Stats เมื่อมีการเปลี่ยนแปลง**

```java
// เมื่อกินอาหาร
p.setMoney(p.getMoney() - price);
p.setHealth(p.getHealth() + healthAdd);

// ส่งข้อมูล stats ใหม่ทันที
if (isOnlineMode && networkManager != null) {
    networkManager.sendPlayerStats(
        p.getPlayerId(), 
        p.getSkill(), 
        p.getEducation(), 
        p.getHealth(), 
        p.getMoney(), 
        p.getBankDeposit()
    );
}

SwingUtilities.invokeLater(this::updateHUDStats);
```

---

## 🎯 Flow การทำงานแบบ End-to-End

### การเคลื่อนที่ของผู้เล่น

```
Client 1 (Player เดิน)
    ↓
1. Player.setDestination(x, y)
    ↓
2. ทุก 50ms ส่ง PLAYER_MOVE → Server
    ↓
Server รับ PLAYER_MOVE
    ↓
3. Forward PLAYER_MOVE → Client 2, 3, 4
    ↓
Client 2, 3, 4 รับ PLAYER_MOVE
    ↓
4. Parse ข้อมูล
    ↓
5. SwingUtilities.invokeLater(() -> {
       gameScene.updateRemotePlayer(...)
       repaint()
   })
    ↓
6. paintComponent() วาดผู้เล่นที่ตำแหน่งใหม่
```

### การอัปเดต Stats

```
Client 1 (กินอาหาร)
    ↓
1. p.setHealth(health + 50)
   p.setMoney(money - 30)
    ↓
2. sendPlayerStats() → Server
    ↓
Server รับ UPDATE_STATS
    ↓
3. อัปเดตข้อมูลใน PlayerInfo
    ↓
4. broadcastPlayerStatsUpdate() → All Clients
    ↓
Client 1, 2, 3, 4 รับ UPDATE_STATS
    ↓
5. Parse ข้อมูล
    ↓
6. SwingUtilities.invokeLater(() -> {
       gameScene.updatePlayerStats(...)
       updateHUDStats()
       repaint()
   })
    ↓
7. HUD แสดงค่าใหม่
```

---

## 🔒 การจัดการ Connection และ Error

### Disconnect Detection

```java
private void listenToServer() {
    while (connected) {
        try {
            String line = in.readLine();
            
            // readLine() คืน null เมื่อ connection ปิด
            if (line == null) {
                connected = false;
                showDisconnectMessage();
                break;
            }
            
            handleMessage(line);
            
        } catch (IOException e) {
            // IOException = connection มีปัญหา
            if (connected) {
                connected = false;
                showConnectionError();
            }
            break;
        }
    }
}
```

### Cleanup เมื่อ Disconnect

```java
private void cleanup() {
    connected = false;
    
    // แจ้งผู้เล่นคนอื่นว่า disconnect
    int disconnectedPlayerId = getPlayerId();
    if (disconnectedPlayerId > 0) {
        for (ClientHandler client : server.clients) {
            if (client != this && client.connected) {
                client.sendText("PLAYER_DISCONNECT:" + disconnectedPlayerId);
            }
        }
    }
    
    // ปิด Socket
    server.handleClientDisconnect(this);
    try {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    } catch (IOException ignored) {
    }
}
```

---

## 📊 สรุปการทำงานเครือข่าย

### จุดแข็ง ✅

1. **Real-time Updates** - อัปเดตทุก 50ms
2. **TCP Socket** - การส่งข้อมูลแน่นอน (reliable)
3. **Multi-threaded** - รองรับหลายคนพร้อมกัน
4. **Text-based Protocol** - Debug ง่าย
5. **Broadcast Pattern** - ทุกคนเห็นข้อมูลเดียวกัน

### ข้อจำกัด ⚠️

1. **No encryption** - ข้อมูลเป็น plain text
2. **No authentication** - ไม่มีระบบ login
3. **Local network** - ใช้ได้แค่ LAN (ต้องรู้ IP)
4. **Fixed port** - Port 12345 เท่านั้น
5. **No reconnection** - Disconnect แล้วต้องเข้าใหม่

### การปรับปรุงเพิ่มเติม 🔜

1. เพิ่ม heartbeat เช็ค connection
2. ใช้ JSON หรือ Protocol Buffers แทน plain text
3. เพิ่ม compression สำหรับข้อมูลขนาดใหญ่
4. รองรับ NAT traversal สำหรับ Internet
5. เพิ่ม authentication และ encryption

---

## 📚 ตัวอย่างการใช้งาน

### เริ่ม Server

```java
// ใน ServerLogWindow.java
ServerLogWindow serverWindow = new ServerLogWindow();
serverWindow.setVisible(true);

// กดปุ่ม "Start Server"
// → GameServer.start()
// → รอรับ client ที่ port 12345
```

### Client เชื่อมต่อ

```java
// ใน GameLobbyMenu.java
NetworkManager nm = NetworkManager.getInstance();
boolean success = nm.connect("localhost", 12345);

if (success) {
    // Connected!
    // รอรับ PLAYER_ID และ LOBBY_UPDATE
} else {
    // Connection failed
    showError("Cannot connect to server");
}
```

### ส่งข้อมูลในเกม

```java
// เมื่อผู้เล่นเดิน
networkManager.sendPlayerMove(player);

// เมื่อ stats เปลี่ยน
networkManager.sendPlayerStats(
    playerId, skill, education, health, money, bankDeposit
);

// เมื่อจบเทิร์น
networkManager.sendMessage("TURN_COMPLETE:" + playerId + ":" + turn);
```

---

**สิ้นสุดเอกสารรายละเอียดทางเทคนิค**

*เอกสารนี้ครอบคลุม 3 หัวข้อหลัก: Paint/Graphics, Threading, และ Networking*
*พร้อมตัวอย่างโค้ดจริงจากโปรเจค No Brakes Life*

