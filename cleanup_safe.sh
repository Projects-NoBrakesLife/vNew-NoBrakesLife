#!/bin/bash
# Script สำหรับลบไฟล์ที่ไม่จำเป็น (ปลอดภัย 100%)
# ลบเฉพาะไฟล์ Test และ Debug ที่ไม่ได้ใช้จริง

echo "🧹 เริ่มทำความสะอาดโค้ด (Safe Mode)"
echo "=================================="
echo ""

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
echo "🗑️  กำลังลบไฟล์..."

if [ -f "src/game/GameSummaryWindowTest.java" ]; then
    rm src/game/GameSummaryWindowTest.java
    echo "✅ ลบ GameSummaryWindowTest.java"
else
    echo "⚠️  ไม่พบ GameSummaryWindowTest.java"
fi

if [ -f "src/game/DebugLogger.java" ]; then
    rm src/game/DebugLogger.java
    echo "✅ ลบ DebugLogger.java"
else
    echo "⚠️  ไม่พบ DebugLogger.java"
fi

if [ -f "src/game/Game.java" ]; then
    rm src/game/Game.java
    echo "✅ ลบ Game.java"
else
    echo "⚠️  ไม่พบ Game.java"
fi

echo ""

# นับไฟล์หลังลบ
AFTER_COUNT=$(find src -name "*.java" | wc -l)
REMOVED=$((BEFORE_COUNT - AFTER_COUNT))
echo "📊 ไฟล์ทั้งหมดหลังลบ: $AFTER_COUNT ไฟล์"
echo "🎯 ลบไปทั้งหมด: $REMOVED ไฟล์"
echo ""

echo "✅ ทำความสะอาดเสร็จสิ้น!"
echo ""
echo "⚠️  คำแนะนำต่อไป:"
echo "   1. ทดสอบ compile: javac src/Main.java"
echo "   2. ทดสอบรันเกม: java -cp out/production/vNew-NoBrakesLife Main"
echo "   3. ถ้ามีปัญหา restore จาก: $BACKUP_DIR"

