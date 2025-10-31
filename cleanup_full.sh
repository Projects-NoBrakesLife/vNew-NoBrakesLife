#!/bin/bash
# Script สำหรับลบไฟล์ที่ไม่จำเป็นทั้งหมด
# รวมถึง Editor files (ใช้เฉพาะถ้าไม่ต้องการแก้ไข UI อีกแล้ว)

echo "🧹 เริ่มทำความสะอาดโค้ด (Full Mode)"
echo "=================================="
echo "⚠️  โหมดนี้จะลบ Editor files ทั้งหมด!"
echo ""

# ถามยืนยัน
read -p "คุณแน่ใจหรือไม่? (y/N): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ ยกเลิกการลบ"
    exit 1
fi

# สร้าง backup ก่อน
echo "📦 กำลังสร้าง backup..."
BACKUP_DIR="../vNew-NoBrakesLife-backup-$(date +%Y%m%d_%H%M%S)"
cp -r ../vNew-NoBrakesLife "$BACKUP_DIR"
echo "✅ Backup สำเร็จที่: $BACKUP_DIR"
echo ""

# นับไฟล์ก่อนลบ
BEFORE_COUNT=$(find src -name "*.java" | wc -l)
echo "📊 ไฟล์ทั้งหมดก่อนลบ: $BEFORE_COUNT ไฟล์"
echo ""

# ลบไฟล์ Test และ Debug
echo "🗑️  กำลังลบไฟล์ Test/Debug..."

if [ -f "src/game/GameSummaryWindowTest.java" ]; then
    rm src/game/GameSummaryWindowTest.java
    echo "✅ ลบ GameSummaryWindowTest.java"
fi

if [ -f "src/game/DebugLogger.java" ]; then
    rm src/game/DebugLogger.java
    echo "✅ ลบ DebugLogger.java"
fi

if [ -f "src/game/Game.java" ]; then
    rm src/game/Game.java
    echo "✅ ลบ Game.java"
fi

echo ""

# ลบไฟล์ Editor
echo "🗑️  กำลังลบ Editor files..."

if [ -f "src/game/MainMenuEditor.java" ]; then
    rm src/game/MainMenuEditor.java
    echo "✅ ลบ MainMenuEditor.java"
fi

if [ -f "src/game/PopupWindowEditor.java" ]; then
    rm src/game/PopupWindowEditor.java
    echo "✅ ลบ PopupWindowEditor.java"
fi

if [ -f "src/game/GameEditor.java" ]; then
    rm src/game/GameEditor.java
    echo "✅ ลบ GameEditor.java"
fi

echo ""

# ลบ folder editor
if [ -d "src/editor" ]; then
    echo "🗑️  กำลังลบ folder editor/..."
    rm -rf src/editor
    echo "✅ ลบ folder editor/ สำเร็จ"
fi

echo ""

# นับไฟล์หลังลบ
AFTER_COUNT=$(find src -name "*.java" | wc -l)
REMOVED=$((BEFORE_COUNT - AFTER_COUNT))
PERCENT=$((REMOVED * 100 / BEFORE_COUNT))

echo "📊 สรุปผลลัพธ์:"
echo "   ไฟล์ก่อนลบ: $BEFORE_COUNT ไฟล์"
echo "   ไฟล์หลังลบ: $AFTER_COUNT ไฟล์"
echo "   ลบไปทั้งหมด: $REMOVED ไฟล์ (~$PERCENT%)"
echo ""

echo "✅ ทำความสะอาดเสร็จสิ้น!"
echo ""
echo "⚠️  คำแนะนำต่อไป:"
echo "   1. ลบ compiled classes: rm -rf out/production/vNew-NoBrakesLife/editor"
echo "   2. ทดสอบ compile: javac src/Main.java"
echo "   3. ทดสอบรันเกม: java -cp out/production/vNew-NoBrakesLife Main"
echo "   4. ถ้ามีปัญหา restore จาก: $BACKUP_DIR"
echo ""
echo "🎉 โค้ดของคุณสะอาดขึ้น $PERCENT% แล้ว!"

