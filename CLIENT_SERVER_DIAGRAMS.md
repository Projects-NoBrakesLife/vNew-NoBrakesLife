# ไดอะแกรม Client-Server Architecture
## โครงสร้างระบบ Client-Server ของเกม No Brakes Life

---

## 1. โครงสร้างโดยรวม (System Architecture)

```
┌─────────────────────────────────────────────────────────────────────┐
│                        GAME SYSTEM OVERVIEW                         │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   Client 1   │         │   Client 2   │         │   Client 3   │
│  (Player 1)  │         │  (Player 2)  │         │  (Player 3)  │
└──────┬───────┘         └──────┬───────┘         └──────┬───────┘
       │                        │                        │
       │  TCP/IP Connection     │                        │
       │  (Port: 12345)         │                        │
       │                        │                        │
       └────────────────────────┼────────────────────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │   GAME SERVER         │
                    │   (GameServer.java)   │
                    │                       │
                    │  - จัดการ 4 Players  │
                    │  - Broadcast Messages │
                    │  - Sync Game State    │
                    └───────────────────────┘
```

---

## 2. Component Details (รายละเอียดส่วนประกอบ)

```
CLIENT SIDE                                  SERVER SIDE
═══════════════════════════════════════════════════════════════════

┌─────────────────────────┐                ┌──────────────────────┐
│   GameLobbyMenu.java    │                │   GameServer.java    │
│   - แสดง UI Lobby       │                │   - รับ Connections  │
│   - รอ Players          │                │   - จัดการ Players   │
└──────────┬──────────────┘                └──────────┬───────────┘
           │                                          │
           │  เชื่อมต่อผ่าน                          │
           ▼                                          ▼
┌─────────────────────────┐                ┌──────────────────────┐
│  NetworkManager.java    │◄──────────────►│  ClientHandler       │
│  - connect()            │   TCP Socket   │  - รับ/ส่งข้อมูล    │
│  - sendMessage()        │                │  - จัดการ 1 Client   │
│  - listenToServer()     │                └──────────┬───────────┘
└──────────┬──────────────┘                           │
           │                                          │
           ▼                                          ▼
┌─────────────────────────┐                ┌──────────────────────┐
│   GameScene.java        │                │   PlayerInfo.java    │
│   - จัดการเกม           │                │   - เก็บข้อมูล       │
│   - อัพเดทสถานะ         │                │   - Player State     │
│   - แสดงผล UI           │                └──────────────────────┘
└─────────────────────────┘
```

---

## 3. Connection Flow (ขั้นตอนการเชื่อมต่อ)

```
CLIENT                                                SERVER
══════                                                ══════

1. เปิด GameLobbyMenu
   │
   │  NetworkManager.getInstance()
   │  .connect("localhost", 12345)
   │
   ├──────────────[TCP SYN]────────────────────────►  ServerSocket.accept()
   │                                                   │
   │◄─────────────[TCP SYN-ACK]──────────────────────┤
   │                                                   │
   ├──────────────[TCP ACK]─────────────────────────► new ClientHandler(socket)
   │                                                   │
   │                                                   │ เริ่ม Thread ใหม่
   │                                                   │
2. ส่งข้อมูล Player                                   │
   │                                                   │
   ├────────["PLAYER_JOIN:Player_X"]────────────────► handleClientJoin()
   │                                                   │
   │                                                   │ เพิ่ม Player ลง List
   │                                                   │
   │◄───────["PLAYER_JOINED:1:Player_X"]─────────────┤
   │                                                   │
   │◄───────["PLAYER_UPDATE:..."]────────────────────┤ broadcast ให้ทุก Client
   │                                                   │
3. รับรายชื่อ Players ทั้งหมด                         │
   │                                                   │
   │◄───────["PLAYERS_LIST:P1,P2,P3"]────────────────┤
   │                                                   │
   │ แสดงใน Lobby UI                                  │
   │                                                   │
4. รอจนครบ 4 Players                                  │
   │                                                   │
   │◄───────["ALL_PLAYERS_READY"]────────────────────┤ เมื่อครบ 4 คน
   │                                                   │
   │ เปิด GameWindow                                  │
   │                                                   │
```

---

## 4. Game Flow (การทำงานของเกม)

```
┌─────────────────────────────────────────────────────────────────┐
│                     GAME INITIALIZATION                         │
└─────────────────────────────────────────────────────────────────┘

CLIENT 1 (Player 1)          SERVER              CLIENT 2 (Player 2)
═══════════════════════════════════════════════════════════════════

1. GameWindow สร้าง
   │
   │  GameScene(isOnlineMode=true)
   │
   ├───────["I_AM_READY"]──────────►  เก็บสถานะ
   │                                    │
   │                                    │  รอทุกคน Ready
   │                                    │
   │                          ◄─────["I_AM_READY"]──────┤
   │                                    │
   │                                    │ ทุกคน Ready แล้ว
   │◄────["GAME_START"]────────────────┤
   │                                    │
   │                          ◄─────["GAME_START"]──────┤
   │                                    │
   │  เริ่มเกม                          │                เริ่มเกม
   │                                    │

┌─────────────────────────────────────────────────────────────────┐
│                     GAME LOOP (ตลอดเกม)                         │
└─────────────────────────────────────────────────────────────────┘

2. Player Movement
   │
   │  handleClick(x, y)
   │
   ├───["PLAYER_MOVE:1:x:y"]──────────►  broadcast
   │                                    │
   │                          ◄─────["PLAYER_MOVE:1:x:y"]─────┤
   │                                    │
   │                                    │  updatePlayerPosition()
   │                                    │

3. Player Hover Object
   │
   │  onMouseMove(obj)
   │
   ├───["PLAYER_HOVER:1:objId"]───────►  broadcast
   │                                    │
   │                          ◄─────["PLAYER_HOVER:1:objId"]──┤
   │                                    │
   │                                    │  แสดง hover state

4. Turn Management
   │
   │  currentTurnPlayerId = 1
   │  (เฉพาะ Player 1 เคลื่อนไหวได้)
   │
   ├───["TURN_COMPLETE:1:5"]──────────►  broadcast
   │                                    │
   │◄────["NEXT_TURN:2"]───────────────┤
   │                                    │
   │                          ◄─────["NEXT_TURN:2"]────────────┤
   │                                    │
   │  เปลี่ยนเป็น Turn ของ Player 2    │   เปลี่ยนเป็นเทิร์นตัวเอง
   │                                    │

5. Player Stats Update
   │
   │  handleFishing() → เพิ่ม money
   │
   ├───["UPDATE_STATS:1:skill:        broadcast
   │     edu:hp:money:bank"]──────────►│
   │                                    │
   │                          ◄─────["UPDATE_STATS:1:..."]────┤
   │                                    │
   │                                    │  updatePlayerStats()
   │                                    │  แสดงใน HUD

6. Game End
   │
   │  maxTurns reached
   │
   ├───["SYNC_PLAYER:1:stats..."]─────►  เก็บข้อมูลสุดท้าย
   │                                    │
   │◄────["SYNC_PLAYER:2:stats..."]────┤
   │                                    │
   │  แสดง GameSummaryWindow            │  แสดง GameSummaryWindow
   │                                    │
```

---

## 5. Message Protocol (โปรโตคอลข้อความ)

```
┌─────────────────────────────────────────────────────────────────┐
│               MESSAGE FORMAT & STRUCTURE                        │
└─────────────────────────────────────────────────────────────────┘

รูปแบบ: "MESSAGE_TYPE:param1:param2:param3:..."

┌──────────────────────┬─────────────────────────────────────────┐
│   MESSAGE TYPE       │   FORMAT & DESCRIPTION                  │
├──────────────────────┼─────────────────────────────────────────┤
│ PLAYER_JOIN          │ "PLAYER_JOIN:PlayerName"                │
│                      │ → เข้าร่วมเกม                           │
├──────────────────────┼─────────────────────────────────────────┤
│ PLAYER_JOINED        │ "PLAYER_JOINED:playerId:playerName"     │
│                      │ → Server ยืนยันการเข้าร่วม              │
├──────────────────────┼─────────────────────────────────────────┤
│ PLAYER_UPDATE        │ "PLAYER_UPDATE:p1Name:p2Name:p3Name:p4" │
│                      │ → รายชื่อ Players ทั้งหมด               │
├──────────────────────┼─────────────────────────────────────────┤
│ ALL_PLAYERS_READY    │ "ALL_PLAYERS_READY"                     │
│                      │ → ครบ 4 คน พร้อมเริ่มเกม               │
├──────────────────────┼─────────────────────────────────────────┤
│ GAME_START           │ "GAME_START:mapSeed"                    │
│                      │ → เริ่มเกม + seed สำหรับ random         │
├──────────────────────┼─────────────────────────────────────────┤
│ PLAYER_MOVE          │ "PLAYER_MOVE:playerId:x:y:direction"    │
│                      │ → ผู้เล่นเคลื่อนไหว                     │
├──────────────────────┼─────────────────────────────────────────┤
│ PLAYER_HOVER         │ "PLAYER_HOVER:playerId:objId:isHover"   │
│                      │ → ผู้เล่น hover วัตถุ                   │
├──────────────────────┼─────────────────────────────────────────┤
│ UPDATE_STATS         │ "UPDATE_STATS:playerId:skill:edu:hp:    │
│                      │  money:bankDeposit"                      │
│                      │ → อัพเดทสถานะผู้เล่น                    │
├──────────────────────┼─────────────────────────────────────────┤
│ SYNC_PLAYER          │ "SYNC_PLAYER:playerId:skill:edu:hp:     │
│                      │  money:bankDeposit"                      │
│                      │ → ซิงค์ข้อมูลก่อนจบเกม                  │
├──────────────────────┼─────────────────────────────────────────┤
│ TURN_COMPLETE        │ "TURN_COMPLETE:playerId:turnNumber"     │
│                      │ → จบเทิร์น                              │
├──────────────────────┼─────────────────────────────────────────┤
│ NEXT_TURN            │ "NEXT_TURN:nextPlayerId"                │
│                      │ → เทิร์นถัดไป                           │
└──────────────────────┴─────────────────────────────────────────┘
```

---

## 6. Data Synchronization (การซิงค์ข้อมูล)

```
┌─────────────────────────────────────────────────────────────────┐
│                  REAL-TIME SYNCHRONIZATION                      │
└─────────────────────────────────────────────────────────────────┘

SCENARIO 1: ผู้เล่นเดิน
═════════════════════════

Client 1                    Server                    Client 2,3,4
────────────────────────────────────────────────────────────────

คลิกพื้น (100, 200)
  │
  ├─["PLAYER_MOVE:1:100:200:down"]──►  รับข้อความ
  │                                      │
  │                                      │ พบว่าเป็น Player 1
  │                                      │
  │                                      │ broadcast ให้คนอื่น
  │                                      │
  │                          ◄──────["PLAYER_MOVE:1:100:200:down"]
  │                                      │
  player.setDestination()                │  player1.setDestination()
  แสดง animation                         │  แสดง animation ของ P1


SCENARIO 2: ผู้เล่นทำกิจกรรม (Fishing)
════════════════════════════════════════

Client 1                    Server                    Client 2,3,4
────────────────────────────────────────────────────────────────

คลิก Fishing spot
  │
  │ handleFishing()
  │ - เริ่ม animation
  │ - คำนวณรางวัล
  │ - money += 50
  │ - skill += 5
  │
  ├─["UPDATE_STATS:1:5:0:95:550:0"]─►  รับข้อความ
  │                                      │
  │                                      │ updatePlayerInfo
  │                                      │
  │                                      │ broadcast
  │                                      │
  │                          ◄──────["UPDATE_STATS:1:5:0:95:550:0"]
  │                                      │
  แสดงสถานะใหม่                          │  อัพเดท HUD ของ Player 1
                                         │  แสดง money = 550


SCENARIO 3: จบเทิร์น
═══════════════════

Client 1 (เทิร์นตัวเอง)    Server                    Clients
────────────────────────────────────────────────────────────────

คลิก "จบเทิร์น"
  │
  │ nextTurn()
  │
  ├─["TURN_COMPLETE:1:5"]──────────►  turnNumber++
  │                                      │
  │                                      │ nextTurnPlayerId = 2
  │                                      │
  │                          ◄──────["NEXT_TURN:2"]
  │◄────["NEXT_TURN:2"]────────────────┤
  │                                      │
  currentTurnPlayerId = 2                │  currentTurnPlayerId = 2
  ปิดการควบคุม                           │  (Player 2 เปิดควบคุม)
  แสดง "รอผู้เล่นคนอื่น"                 │


SCENARIO 4: จบเกม + แสดงสรุป
═══════════════════════════════

Client 1                    Server                    Client 2,3,4
────────────────────────────────────────────────────────────────

ถึง Turn สุดท้าย
  │
  │ showGameEndDisplayAndSummary()
  │
  │ syncAllPlayersBeforeSummary()
  │
  ├─["SYNC_PLAYER:1:10:20:80:1000:500"]►  เก็บข้อมูล
  │                                      │
  │                          ◄──────["SYNC_PLAYER:2:15:25:90:1200:600"]
  │◄────["SYNC_PLAYER:3:8:18:85:900:400"]┤
  │◄────["SYNC_PLAYER:4:12:22:75:1100:550"]┤
  │                                      │
  │ รอ 2 วินาที                           │  รอ 2 วินาที
  │                                      │
  │ new GameSummaryWindow(players)       │  new GameSummaryWindow(players)
  │                                      │
  │ แสดง:                                │  แสดง:
  │ - ผู้ชนะ                             │  - ผู้ชนะ (เหมือนกัน)
  │ - คะแนนทุกคน                         │  - คะแนนทุกคน (เหมือนกัน)
```

---

## 7. Threading Model (การทำงานแบบ Multi-Thread)

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT THREADS                               │
└─────────────────────────────────────────────────────────────────┘

┌────────────────────────┐
│   MAIN THREAD          │  (EDT - Event Dispatch Thread)
│   - Swing UI           │
│   - paintComponent()   │
│   - Mouse/Key Events   │
└───────────┬────────────┘
            │
            │ สร้าง
            ▼
┌────────────────────────┐
│  NETWORK LISTENER      │  (Background Thread)
│  listenToServer()      │
│  - รอรับข้อความ        │
│  - parse message       │
│  - SwingUtilities.     │
│    invokeLater()       │
└────────────────────────┘

Flow:
1. Network Listener รับข้อความ (Background Thread)
2. Parse ข้อมูล
3. SwingUtilities.invokeLater(() -> {
      // อัพเดท UI ใน EDT
      updatePlayerStats();
      repaint();
   });


┌─────────────────────────────────────────────────────────────────┐
│                    SERVER THREADS                               │
└─────────────────────────────────────────────────────────────────┘

┌────────────────────────┐
│   MAIN THREAD          │
│   start()              │
│   - สร้าง ServerSocket │
│   - accept() loop      │
└───────────┬────────────┘
            │
            │ สร้าง (ต่อ Client)
            ▼
┌────────────────────────┐
│  CLIENT HANDLER 1      │  (Thread 1)
│  - รับ/ส่งข้อความ      │
│  - จัดการ Player 1     │
└────────────────────────┘
            │
            │ สร้าง (ต่อ Client)
            ▼
┌────────────────────────┐
│  CLIENT HANDLER 2      │  (Thread 2)
│  - รับ/ส่งข้อความ      │
│  - จัดการ Player 2     │
└────────────────────────┘
            │
            │ ... (ถึง 4 Clients)

หมายเหตุ:
- แต่ละ ClientHandler ทำงานใน Thread แยกกัน
- ใช้ synchronized {} เมื่อเข้าถึง shared data
  (เช่น players list, game state)
```

---

## 8. Code Examples (ตัวอย่างโค้ดสำคัญ)

### 8.1 Client: การเชื่อมต่อ Server

```java
// NetworkManager.java

public boolean connect(String host, int port) {
    try {
        // 1. สร้าง Socket เชื่อมต่อ Server
        socket = new Socket(host, port);
        
        // 2. สร้าง Input/Output Streams
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        connected = true;
        
        // 3. เริ่ม Thread ฟังข้อความจาก Server
        Thread listenerThread = new Thread(this::listenToServer);
        listenerThread.setDaemon(true);
        listenerThread.start();
        
        return true;
    } catch (IOException e) {
        return false;
    }
}

// ฟังข้อความจาก Server (ทำงานใน Background Thread)
private void listenToServer() {
    try {
        String line;
        while (connected && (line = in.readLine()) != null) {
            final String message = line;
            
            // Parse และประมวลผลข้อความ
            if (message.startsWith("PLAYER_MOVE:")) {
                String[] parts = message.split(":");
                int playerId = Integer.parseInt(parts[1]);
                double x = Double.parseDouble(parts[2]);
                double y = Double.parseDouble(parts[3]);
                
                // อัพเดท UI ใน EDT
                SwingUtilities.invokeLater(() -> {
                    if (gameScene != null) {
                        gameScene.updatePlayerPosition(playerId, x, y);
                    }
                });
            }
            // ... handle ข้อความอื่นๆ
        }
    } catch (IOException e) {
        connected = false;
    }
}
```

### 8.2 Server: รับ Connection และจัดการ Clients

```java
// GameServer.java

public void start() {
    try {
        // 1. สร้าง ServerSocket รอรับ Connection
        serverSocket = new ServerSocket(port);
        running = true;
        
        System.out.println("Server started on port " + port);
        
        // 2. Accept Clients (สูงสุด 4 คน)
        while (running && players.size() < MAX_PLAYERS) {
            Socket clientSocket = serverSocket.accept();
            
            // 3. สร้าง ClientHandler ใน Thread ใหม่
            ClientHandler handler = new ClientHandler(clientSocket, this);
            Thread handlerThread = new Thread(handler);
            handlerThread.start();
            
            synchronized (clients) {
                clients.add(handler);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}

// ClientHandler: จัดการ 1 Client
class ClientHandler implements Runnable {
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                
                // รับข้อความจาก Client
                if (line.startsWith("PLAYER_MOVE:")) {
                    // Broadcast ให้ Clients อื่น
                    broadcastMessage(line);
                }
                else if (line.startsWith("UPDATE_STATS:")) {
                    // Parse และอัพเดทข้อมูล
                    String[] parts = line.split(":");
                    int playerId = Integer.parseInt(parts[1]);
                    
                    // อัพเดท PlayerInfo
                    PlayerInfo pInfo = new PlayerInfo(playerId, "P" + playerId, true);
                    pInfo.skill = Integer.parseInt(parts[2]);
                    pInfo.education = Integer.parseInt(parts[3]);
                    pInfo.health = Integer.parseInt(parts[4]);
                    pInfo.money = Integer.parseInt(parts[5]);
                    pInfo.bankDeposit = Integer.parseInt(parts[6]);
                    
                    // Broadcast ให้ทุกคน
                    server.broadcastPlayerStatsUpdate(pInfo);
                }
            }
        } catch (IOException e) {
            // Client disconnect
        }
    }
    
    // ส่งข้อความให้ Client นี้
    public void sendText(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }
}

// Broadcast ข้อความให้ทุก Client
public void broadcastMessage(String message) {
    synchronized (clients) {
        for (ClientHandler client : clients) {
            client.sendText(message);
        }
    }
}
```

### 8.3 Client: ส่งข้อมูลการเคลื่อนไหว

```java
// GameScene.java

private void handleClick(Point point) {
    // 1. คำนวณตำแหน่งปลายทาง
    double targetX = point.x;
    double targetY = point.y;
    
    // 2. เคลื่อนไหว Player ในเครื่องตัวเอง
    localPlayer.setDestination(targetX, targetY);
    localPlayer.setDirection(direction);
    
    // 3. ส่งข้อความไป Server (ถ้าเป็น Online Mode)
    if (isOnlineMode && networkManager != null) {
        String moveMsg = String.format("PLAYER_MOVE:%d:%.1f:%.1f:%s",
            localPlayer.getPlayerId(),
            targetX,
            targetY,
            direction
        );
        networkManager.sendMessage(moveMsg);
    }
}

// รับข้อมูลการเคลื่อนไหวจาก Server
public void updatePlayerPosition(int playerId, double x, double y, String direction) {
    SwingUtilities.invokeLater(() -> {
        // หา Player ที่ตรงกับ playerId
        for (Player p : players) {
            if (p.getPlayerId() == playerId && p.isRemotePlayer()) {
                p.setDestination(x, y);
                p.setDirection(direction);
                break;
            }
        }
        repaint();
    });
}
```

---

## 9. Synchronization Points (จุดซิงค์ที่สำคัญ)

```
┌─────────────────────────────────────────────────────────────────┐
│          CRITICAL SYNCHRONIZATION POINTS                        │
└─────────────────────────────────────────────────────────────────┘

1. การเข้าเกม (Lobby)
   ═══════════════════
   - รอจนครบ 4 Players
   - ซิงค์รายชื่อผู้เล่น
   - กดเริ่มเกมพร้อมกัน
   
   synchronized (players) {
       if (players.size() == 4) {
           broadcastMessage("ALL_PLAYERS_READY");
       }
   }

2. การเคลื่อนไหว
   ═══════════════
   - ส่งตำแหน่งทันที
   - Broadcast ให้ทุกคน
   - อัพเดท UI ใน EDT
   
   // ป้องกัน Race Condition
   SwingUtilities.invokeLater(() -> {
       updatePlayerPosition(playerId, x, y);
   });

3. สถิติผู้เล่น
   ═════════════
   - อัพเดททันทีหลังทำกิจกรรม
   - Broadcast ให้ทุกคน
   - แสดง HUD ของทุกคน
   
   if (isOnlineMode) {
       networkManager.sendPlayerStats(
           playerId, skill, edu, hp, money, bank
       );
   }

4. การจบเทิร์น
   ═════════════
   - แจ้ง Server ว่าจบแล้ว
   - Server คำนวณผู้เล่นคนถัดไป
   - Broadcast "NEXT_TURN" ให้ทุกคน
   
   synchronized (this) {
       currentTurnPlayerId = nextPlayerId;
       broadcastMessage("NEXT_TURN:" + nextPlayerId);
   }

5. การจบเกม
   ══════════
   - ซิงค์ข้อมูลทุกคนก่อน
   - รอให้ Server รวบรวมข้อมูล
   - แสดงผลสรุปพร้อมกัน
   
   // ส่งข้อมูลสุดท้าย
   for (Player p : players) {
       String syncMsg = "SYNC_PLAYER:" + 
           p.getId() + ":" + p.getStats();
       networkManager.sendMessage(syncMsg);
   }
   
   // รอ delay
   Timer timer = new Timer(2000, e -> {
       showGameSummaryWindow();
   });
```

---

## 10. Error Handling (การจัดการข้อผิดพลาด)

```
┌─────────────────────────────────────────────────────────────────┐
│               ERROR SCENARIOS & SOLUTIONS                       │
└─────────────────────────────────────────────────────────────────┘

SCENARIO 1: ไม่สามารถเชื่อมต่อ Server
═════════════════════════════════════

Client                          UI
──────                          ───
connect() fails
  │
  ├─ return false
  │
  └─► showConnectionError()
        │
        └─► แสดง dialog ให้กรอก IP ใหม่
            
            
SCENARIO 2: Connection หลุดระหว่างเกม
════════════════════════════════════

Client                          Server
──────                          ──────
listenToServer()
  │
  ├─ IOException
  │
  ├─ connected = false          ├─ catch IOException
  │                             │
  └─► reconnect dialog          └─► removeClient()
                                      broadcast "PLAYER_LEFT"


SCENARIO 3: ข้อมูลไม่ตรงกัน (Desync)
═════════════════════════════════════

Solution: ใช้ SYNC_PLAYER message

Client 1 เห็น Player 2 อยู่ตำแหน่ง A
Client 2 อยู่ตำแหน่ง B (ที่แท้จริง)
  │
  │ Server ส่ง SYNC_PLAYER:2:B
  │
  ├─► Client 1 อัพเดท Player 2 = B
  └─► Client 2 ยืนยัน = B


SCENARIO 4: Message สูญหาย
══════════════════════════

Solution: ใช้ TCP (guaranteed delivery)
- TCP รับรองว่าข้อความถึงปลายทาง
- ถ้า network error → IOException → reconnect
```

---

## 11. Performance Optimization

```
1. ลด Network Traffic
   ══════════════════
   - ส่งเฉพาะข้อมูลที่เปลี่ยน
   - ใช้ String format สั้น "1:100:200" แทน JSON
   - ไม่ส่ง mouse move ทุกครั้ง (throttle)

2. UI Thread Safety
   ═════════════════
   - อัพเดท UI ใน EDT เท่านั้น
   - ใช้ SwingUtilities.invokeLater()
   - ไม่ block EDT ด้วย network I/O

3. Broadcast Optimization
   ════════════════════════
   synchronized (clients) {
       List<ClientHandler> copy = new ArrayList<>(clients);
   }
   // broadcast นอก synchronized block
   for (ClientHandler c : copy) {
       c.sendText(message);
   }

4. ลด Console Output
   ══════════════════
   - ลบ debug logs ออก
   - ใช้ logger แทน System.out.println()
```

---

## สรุป

ระบบ Client-Server ของเกมใช้:

✅ **TCP/IP Socket** - การเชื่อมต่อที่เชื่อถือได้
✅ **Multi-Threading** - แยก Network I/O และ UI
✅ **Text Protocol** - ข้อความแบบ "TYPE:param:param"
✅ **Real-time Sync** - อัพเดทสถานะทันที
✅ **Broadcast Pattern** - Server ส่งให้ทุก Client
✅ **EDT Thread Safety** - อัพเดท UI ปลอดภัย

การทำงาน:
1. Client เชื่อมต่อ Server
2. Server จัดการหลาย Clients ด้วย Threads
3. Client ส่งข้อมูล → Server → Broadcast → Clients อื่น
4. ทุก Client แสดงผลเหมือนกัน (Synchronized)

