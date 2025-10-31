# No Brakes Life - เอกสารสรุปโปรเจค

## ภาพรวมโปรเจค

**No Brakes Life** เป็นเกมจำลองการใช้ชีวิตนักศึกษาแบบเทิร์นเบส (Turn-Based Simulation Game) ที่พัฒนาด้วย Java Swing พร้อมระบบ Multiplayer Online ผ่าน TCP Socket

### จุดประสงค์หลัก
- จำลองการใช้ชีวิตนักศึกษาที่ต้องจัดการทรัพยากร (เวลา, เงิน, สุขภาพ, ทักษะ, การศึกษา)
- ฝึกทักษะการวางแผนและตัดสินใจ
- รองรับการเล่นหลายคนผ่านระบบ Online Multiplayer

---

## โครงสร้างโปรเจค

```
vNew-NoBrakesLife/
├── src/
│   ├── Main.java                    # Entry Point หลักของเกม
│   ├── game/                        # ไฟล์เกมหลัก
│   ├── network/                     # ระบบ Multiplayer
│   ├── editor/                      # เครื่องมือ Editor สำหรับสร้าง UI
│   └── data/                        # ข้อมูล GameObject
├── assets/                          # รูปภาพ, เสียง, ฟอนต์
│   ├── background/                  # พื้นหลังเกม
│   ├── ui/                          # UI Elements
│   ├── player/                      # ภาพตัวละคร
│   ├── obj/                         # ไอคอนสถานที่
│   ├── sfx/                         # เสียงเอฟเฟกต์
│   └── font/                        # ฟอนต์ภาษาไทย
└── audio_config.txt                 # ค่าเริ่มต้นเสียง

```

---

## ส่วนประกอบหลัก

### 📂 1. Package: `game` - ระบบเกมหลัก

#### **Main.java**
- **หน้าที่**: จุดเริ่มต้นของโปรแกรม
- **การทำงาน**: เรียก `MainMenu` เพื่อแสดงหน้าเมนูหลัก

#### **GameConfig.java**
- **หน้าที่**: เก็บค่าคงที่และการตั้งค่าเกมทั้งหมด
- **ข้อมูลสำคัญ**:
  - ขนาดหน้าต่างเกม (1920x1080)
  - จำนวนผู้เล่นสูงสุด (4 คน)
  - จำนวนเทิร์นทั้งหมด (10 สัปดาห์)
  - ค่าใช้จ่ายเวลาของแต่ละกิจกรรม
  - ค่าพลังงาน, สุขภาพ, ค่าใช้จ่าย
  - ตำแหน่งสถานที่ต่างๆ (หอพัก, ธนาคาร, โรงอาหาร, ฯลฯ)
  - คำถามสำหรับเรียน (University)
  - รายการปลาและโอกาสได้ (Fishing)

#### **MainMenu.java**
- **หน้าที่**: หน้าเมนูหลักของเกม
- **ฟีเจอร์**:
  - ปุ่มเริ่มเกม (โหมดเดี่ยว/ออนไลน์)
  - ปุ่มตั้งค่า (เสียง, ความละเอียด)
  - ปุ่ม About (ข้อมูลทีมผู้พัฒนา)
  - ระบบ Hover Effects และเสียง
  - รองรับภาษาไทย

#### **GameModeMenu.java**
- **หน้าที่**: เลือกโหมดการเล่น
- **ตัวเลือก**:
  - Single Player (เล่นคนเดียว)
  - Host Game (สร้างห้องเป็น Server)
  - Join Game (เข้าร่วมเกมที่มีอยู่)

#### **GameLobbyMenu.java**
- **หน้าที่**: ห้องรอก่อนเริ่มเกม (สำหรับโหมดออนไลน์)
- **ฟีเจอร์**:
  - แสดงรายชื่อผู้เล่นที่เข้าร่วม
  - รอผู้เล่นครบ (ขั้นต่ำ 1 คน, สูงสุด 4 คน)
  - ปุ่ม Start Game (สำหรับ Host)
  - อัปเดต lobby แบบ real-time

#### **GameScene.java** ⭐ (ไฟล์สำคัญที่สุด)
- **หน้าที่**: จัดการ gameplay หลัก ตรรกะเกม และการโต้ตอบ
- **ระบบหลัก**:
  1. **Turn Management**
     - จัดการลำดับการเล่นของผู้เล่นแต่ละคน
     - แต่ละเทิร์นมีเวลา 24 ชั่วโมง (สามารถใช้ทำกิจกรรมต่างๆ)
     - ลดสุขภาพ 10 ทุกเทิร์น (health penalty)
     - ถ้าสุขภาพต่ำกว่า 50% เริ่มเทิร์นด้วยเวลาน้อยลง
  
  2. **Player Movement**
     - ระบบเดินแบบ pathfinding ไปยังสถานที่ต่างๆ
     - Animation การเดิน (6 เฟรม)
     - การหมุนตัวละครตามทิศทาง (Front/Back)
  
  3. **Interactive Objects**
     - หอพัก (Dormitory): นอนพักฟื้นสุขภาพ
     - ธนาคาร (Bank): ฝาก/ถอนเงิน, รับดอกเบี้ย
     - โรงอาหาร (KFB): ซื้ออาหารเพิ่มสุขภาพ
     - ร้านซักรีด (Laundry): กิจกรรมพิเศษ
     - โรงยิม (Gym): ออกกำลังกาย เพิ่มสุขภาพและทักษะ
     - สวน (Garden): ตกปลา หาเงินและเพิ่มทักษะ
     - มหาวิทยาลัย (University): เรียนตอบคำถาม เพิ่มการศึกษา
     - ร้านค้า (Shop): ซื้อของ
  
  4. **Stats Management**
     - **Skill** (ทักษะ): เพิ่มจากการออกกำลังกาย, ตกปลา, เรียน
     - **Education** (การศึกษา): เพิ่มจากการตอบคำถามถูก
     - **Health** (สุขภาพ): ลดทุกเทิร์น, เพิ่มจากการกิน/นอน/ออกกำลังกาย
     - **Money** (เงิน): ได้จากการตกปลา, ลดจากการซื้อของ
     - **Bank Deposit** (เงินฝาก): รับดอกเบี้ย 10% ทุกสัปดาห์
  
  5. **Online Synchronization**
     - ส่งข้อมูลตำแหน่งผู้เล่นแบบ real-time
     - ซิงค์สถานะ (stats) ให้ผู้เล่นคนอื่นเห็น
     - ซิงค์ hover effects
     - จัดการเทิร์นผ่าน server

  6. **Game End & Summary**
     - เกมจบหลังจาก 10 สัปดาห์
     - คืนเงินฝากพร้อมดอกเบี้ย
     - แสดงหน้าสรุปผล (GameSummaryWindow)
     - แสดงผู้ชนะ (คนที่มีเงินมากที่สุด)

#### **Player.java**
- **หน้าที่**: จัดการตัวละครผู้เล่น
- **คุณสมบัติ**:
  - ตำแหน่ง (x, y)
  - ทิศทาง (Front/Back)
  - Animation frames
  - Stats (skill, education, health, money, bankDeposit)
  - เวลาคงเหลือในเทิร์น
  - ระบบเดินไปยังจุดหมาย (destination)
  - แยกระหว่าง Local Player และ Remote Player

#### **PopupWindow.java**
- **หน้าที่**: แสดง popup สำหรับโต้ตอบกับสถานที่ต่างๆ
- **ฟีเจอร์**:
  - แสดง UI elements แบบ dynamic
  - รองรับปุ่มกดสำหรับแต่ละกิจกรรม
  - แสดงข้อความ notification
  - Animation (เช่น animation ตกปลา)
  - รองรับการตอบคำถาม (สำหรับ University)

#### **GameSummaryWindow.java**
- **หน้าที่**: แสดงผลสรุปเมื่อจบเกม
- **ข้อมูลที่แสดง**:
  - อันดับผู้เล่นแต่ละคน
  - เงินทั้งหมดของแต่ละคน (มีการนับแบบ animation)
  - ประกาศผู้ชนะ
  - Stats ทั้งหมด (Skill, Education, Health)

#### **MenuElement.java**
- **หน้าที่**: คลาสพื้นฐานสำหรับ UI element
- **ประเภท**:
  - IMAGE: รูปภาพ
  - TEXT: ข้อความ
  - BUTTON: ปุ่ม (รูปภาพที่กดได้)
- **ฟีเจอร์**:
  - Hover effects (เปลี่ยนภาพเมื่อเมาส์อยู่เหนือ)
  - Scale effects (ขยายตัวเมื่อ hover)
  - Click handlers (callback เมื่อกด)
  - Patrol animation (เคลื่อนที่ไปมา)

#### **GameObject.java**
- **หน้าที่**: วัตถุที่โต้ตอบได้ในเกม (สถานที่ต่างๆ)
- **คุณสมบัติ**:
  - ตำแหน่ง, ขนาด
  - รูปภาพไอคอน
  - Hover effect
  - Animation pulse (กระพริบ)

#### **AboutMenu.java**
- **หน้าที่**: หน้าแสดงข้อมูลทีมผู้พัฒนา
- **ฟีเจอร์**:
  - แสดงรูปสมาชิกทีม 4 คน
  - Hover effects บนรูปภาพ (ขยาย + เล่นเสียง)
  - ข้อความ "By Team C++"

#### **SettingsMenu.java**
- **หน้าที่**: หน้าตั้งค่าเกม
- **ตัวเลือก**:
  - ระดับเสียงเพลง (Music Volume)
  - ระดับเสียงเอฟเฟกต์ (SFX Volume)
  - ปุ่มย้อนกลับ

#### **SoundManager.java**
- **หน้าที่**: จัดการเสียงในเกม
- **ฟีเจอร์**:
  - เล่นเพลงพื้นหลัง (loop)
  - เล่นเสียงเอฟเฟกต์
  - ปรับระดับเสียงแยกกัน (Music/SFX)
  - บันทึกค่าลง audio_config.txt

#### **FontManager.java**
- **หน้าที่**: จัดการฟอนต์ภาษาไทย
- **คุณสมบัติ**:
  - โหลดฟอนต์ NotoSerifThai
  - Cache ฟอนต์เพื่อ performance

#### **CursorManager.java**
- **หน้าที่**: จัดการเคอร์เซอร์ที่กำหนดเอง
- **ฟีเจอร์**:
  - เคอร์เซอร์ปกติ (pointer)
  - เคอร์เซอร์กด (pointer-press)
  - เคอร์เซอร์ห้าม (pointer-denied)

#### **LoadingScreen.java**
- **หน้าที่**: หน้าจอ loading
- **ฟีเจอร์**:
  - Animation loading (13 เฟรม)
  - แสดงระหว่างโหลดข้อมูล

---

### 📂 2. Package: `network` - ระบบ Multiplayer Online

#### **NetworkManager.java** (Client-side)
- **หน้าที่**: จัดการการเชื่อมต่อกับ server ฝั่ง client
- **ฟีเจอร์**:
  1. **Connection Management**
     - เชื่อมต่อไปยัง server ผ่าน TCP Socket
     - Singleton pattern (instance เดียว)
  
  2. **Message Handling**
     - `PLAYER_ID`: รับ ID ผู้เล่นจาก server
     - `LOBBY_UPDATE`: อัปเดตรายชื่อผู้เล่นใน lobby
     - `START_GAME`: เริ่มเกม
     - `PLAYER_MOVE`: รับข้อมูลการเคลื่อนที่ของผู้เล่นคนอื่น
     - `TURN_UPDATE`: อัปเดตว่าเป็นเทิร์นของใคร
     - `UPDATE_STATS`: รับข้อมูล stats ที่อัปเดต
     - `SYNC_PLAYER`: ซิงค์ข้อมูลก่อนจบเกม
     - `PLAYER_HOVER`: แสดง hover ของผู้เล่นคนอื่น
  
  3. **Data Synchronization**
     - ส่งข้อมูลตำแหน่งผู้เล่นทุก 50ms
     - ส่ง stats เมื่อมีการเปลี่ยนแปลง
     - อัปเดต UI ทันทีเมื่อรับข้อมูล

#### **GameServer.java** (Server-side)
- **หน้าที่**: Server สำหรับ multiplayer
- **ฟีเจอร์**:
  1. **Server Management**
     - รับ connection จาก client (Port: 12345)
     - จัดการ client หลายคนพร้อมกัน (Multi-threaded)
     - แต่ละ client มี ClientHandler แยกกัน
  
  2. **Player Management**
     - จัดสรร Player ID (1-4)
     - ตรวจสอบจำนวนผู้เล่น
     - เริ่มเกมอัตโนมัติเมื่อผู้เล่นครบ
  
  3. **Message Broadcasting**
     - กระจายข้อมูลไปยัง client ทุกคน
     - จัดการ TURN_COMPLETE และคำนวณเทิร์นถัดไป
     - Broadcasting stats updates
  
  4. **Game State**
     - ติดตามสถานะเกม (รอ/เริ่มแล้ว)
     - จัดการการ disconnect
     - Reset game

#### **ServerLogWindow.java**
- **หน้าที่**: UI สำหรับ server
- **ฟีเจอร์**:
  - แสดง log การทำงาน
  - ปุ่ม Start/Stop Server
  - ปุ่ม Reset Game
  - แสดงจำนวนผู้เล่นที่เชื่อมต่อ

#### **PlayerInfo.java**
- **หน้าที่**: เก็บข้อมูลผู้เล่นฝั่ง server
- **ข้อมูล**:
  - Player ID
  - ชื่อผู้เล่น
  - สถานะการเชื่อมต่อ
  - Stats ทั้งหมด (skill, education, health, money, bankDeposit)

#### **GameMessage.java**
- **หน้าที่**: คลาสสำหรับส่งข้อความผ่าน network
- **ประเภทข้อความ**:
  - UPDATE_LOBBY: อัปเดต lobby
  - START_GAME: เริ่มเกม

#### **NetworkLogger.java**
- **หน้าที่**: บันทึก log การทำงานของ network
- **คุณสมบัติ**: Singleton pattern, thread-safe

---

### 📂 3. Package: `editor` - เครื่องมือช่วยพัฒนา

#### **MainMenuEditorMain.java**
- **หน้าที่**: Editor สำหรับออกแบบ MainMenu
- **ฟีเจอร์**:
  - เพิ่ม/ลบ/แก้ไข UI elements
  - ลาก-วาง elements
  - ปรับขนาด, สี, ฟอนต์
  - Import/Export โค้ด
  - บันทึกเป็น Java code

#### **PopupWindowEditorMain.java**
- **หน้าที่**: Editor สำหรับออกแบบ Popup
- **ฟีเจอร์**: เหมือน MainMenuEditorMain แต่สำหรับ Popup

#### **GameEditorMain.java**
- **หน้าที่**: Editor สำหรับจัดวางสถานที่ในเกม
- **ฟีเจอร์**:
  - วาง GameObject บนแผนที่
  - กำหนดตำแหน่งผู้เล่น
  - Export ข้อมูลไปยัง GameConfig

#### **SceneExporter/SceneLoader.java**
- **หน้าที่**: บันทึก/โหลดข้อมูล scene

---

## การทำงานของเกม (Game Flow)

### 1. เริ่มเกม
```
Main → MainMenu → GameModeMenu
         ↓
    [Single Player] → GameWindow → GameScene (เล่นคนเดียว)
         ↓
    [Host Game] → ServerLogWindow + GameLobbyMenu → GameWindow
         ↓
    [Join Game] → GameLobbyMenu → GameWindow
```

### 2. ระบบ Multiplayer
```
Client                          Server
  |                               |
  |------ Connect -------------->|
  |<----- PLAYER_ID -------------|
  |<----- LOBBY_UPDATE -----------|
  |                               |
  (รอผู้เล่นครบ)                  |
  |                               |
  |<----- START_GAME ------------|
  |                               |
  (เริ่มเกม)                      |
  |                               |
  |------ PLAYER_MOVE ---------->|
  |<----- PLAYER_MOVE -----------| (broadcast)
  |------ UPDATE_STATS --------->|
  |<----- UPDATE_STATS ----------| (broadcast)
  |------ TURN_COMPLETE -------->|
  |<----- TURN_UPDATE -----------| (broadcast)
```

### 3. ระบบเทิร์น (Turn-Based)
```
1. แสดงข้อความ "ถึงตาคุณใช้ชีวิต"
2. ผู้เล่นมีเวลา 24 ชั่วโมง
3. คลิกไปยังสถานที่ต่างๆ
4. ทำกิจกรรม (ใช้เวลา)
5. เมื่อหมดเวลาหรือจบเทิร์น → nextTurn()
6. เปลี่ยนเป็นเทิร์นผู้เล่นคนถัดไป
7. วนไปจนครบ 10 สัปดาห์
8. แสดงหน้าสรุปผล
```

### 4. การคำนวณคะแนน
```
คะแนนรวม = Money (เงินในกระเป๋า + เงินฝากธนาคาร)

ผู้ชนะ = ผู้เล่นที่มีเงินมากที่สุด

ปัจจัยเสริม:
- Skill: ช่วยในการทำกิจกรรม
- Education: เป็นตัวชี้วัดความรู้
- Health: ต้องดูแลไม่ให้ต่ำเกินไป (มีผลกับเวลาเริ่มเทิร์น)
```

---

## ฟีเจอร์พิเศษ

### ✨ 1. ระบบธนาคาร
- ฝากเงินได้ไม่จำกัด
- รับดอกเบี้ย 10% ทุกสัปดาห์
- คืนเงินฝากพร้อมดอกเบี้ยเมื่อจบเกม

### ✨ 2. ระบบตกปลา
- ใช้เวลา 4 ชั่วโมง
- มีปลาหลายชนิดด้วยโอกาสต่างกัน
- ได้เงิน + ทักษะ
- มี animation ตกปลา

### ✨ 3. ระบบเรียน
- ตอบคำถามภาษาไทย
- ตอบถูกได้ Education +10
- ตอบผิดได้ Skill +5
- ใช้เวลา 2 ชั่วโมง

### ✨ 4. Real-time Synchronization
- ตำแหน่งผู้เล่นอัปเดตทุก 50ms
- Stats อัปเดตทันทีเมื่อเปลี่ยนแปลง
- เห็น hover effects ของผู้เล่นคนอื่น

### ✨ 5. Thai Font Support
- รองรับภาษาไทยทุกหน้าจอ
- ใช้ฟอนต์ Noto Serif Thai

---

## เทคโนโลยีที่ใช้

### 1. Java Swing
- GUI Framework หลัก
- Custom painting (Graphics2D)
- Event handling (MouseListener, ActionListener)

### 2. Networking
- TCP Socket (java.net.Socket)
- Client-Server Architecture
- Multi-threaded (แต่ละ client มี thread แยกกัน)

### 3. File I/O
- ImageIO: โหลดรูปภาพ
- AudioInputStream: เล่นเสียง
- Properties: บันทึกการตั้งค่า

### 4. Design Patterns
- **Singleton**: NetworkManager, SoundManager, FontManager
- **Observer**: Event listeners
- **MVC**: แยก Model (Player, GameObject) / View (UI) / Controller (GameScene)

---

## สถิติโปรเจค

- **จำนวนไฟล์**: 40+ ไฟล์
- **บรรทัดโค้ด**: ~15,000+ บรรทัด
- **ไฟล์หลัก**: GameScene.java (~1,450 บรรทัด)
- **รูปภาพ**: 100+ รูป
- **เสียง**: 3 ไฟล์เสียง

---

## วิธีรันโปรเจค

### Single Player
```bash
java -cp out/production/vNew-NoBrakesLife Main
```

### Multiplayer (Host)
1. รัน Server:
```bash
java -cp out/production/vNew-NoBrakesLife network.ServerLogWindow
```
2. เลือก "Host Game" ในเกม

### Multiplayer (Join)
1. เลือก "Join Game"
2. ใส่ IP address ของ server (localhost หรือ IP จริง)
3. กด Connect

---


