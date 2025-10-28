import os
from tkinter import Tk, Label, Button, filedialog, Scale, HORIZONTAL, Canvas, PhotoImage
from PIL import Image, ImageTk, ImageEnhance
import colorsys

# ฟังก์ชันช่วยเปลี่ยน Hue
def shift_hue(img, hue_shift):
    img = img.convert('RGBA')
    pixels = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r/255.0, g/255.0, b/255.0)
            h = (h + hue_shift) % 1.0
            r, g, b = colorsys.hsv_to_rgb(h, s, v)
            pixels[x, y] = (int(r*255), int(g*255), int(b*255), a)
    return img

# ฟังก์ชันเลือกโฟลเดอร์
def select_folder():
    folder = filedialog.askdirectory(title="เลือกโฟลเดอร์ที่มีภาพ")
    if folder:
        global input_folder, img_files, current_img_index
        input_folder = folder
        img_files = [f for f in os.listdir(folder) if f.lower().endswith((".png", ".jpg", ".jpeg"))]
        if img_files:
            current_img_index = 0
            load_preview()
        input_label.config(text=folder)

# โหลดและแสดงภาพพรีวิว
def load_preview():
    global preview_img, original_img
    if not img_files:
        return
    img_path = os.path.join(input_folder, img_files[current_img_index])
    original_img = Image.open(img_path).convert("RGBA")
    update_preview()

def update_preview(event=None):
    if not original_img:
        return

    # คัดลอกภาพต้นฉบับ
    img = original_img.copy()

    # ปรับค่าต่างๆ
    hue_shift = hue_scale.get() / 360.0
    brightness_val = brightness_scale.get() / 100
    contrast_val = contrast_scale.get() / 100
    color_val = color_scale.get() / 100

    img = shift_hue(img, hue_shift)
    img = ImageEnhance.Brightness(img).enhance(brightness_val)
    img = ImageEnhance.Contrast(img).enhance(contrast_val)
    img = ImageEnhance.Color(img).enhance(color_val)

    # Resize พรีวิว
    img_small = img.resize((150, 200))
    preview_img = ImageTk.PhotoImage(img_small)
    preview_canvas.create_image(75, 100, image=preview_img)
    preview_canvas.image = preview_img

def next_image():
    global current_img_index
    if not img_files:
        return
    current_img_index = (current_img_index + 1) % len(img_files)
    load_preview()

def prev_image():
    global current_img_index
    if not img_files:
        return
    current_img_index = (current_img_index - 1) % len(img_files)
    load_preview()

def save_all():
    if not input_folder or not img_files:
        result_label.config(text="❌ ยังไม่ได้เลือกโฟลเดอร์")
        return

    output_folder = os.path.join(input_folder, "output")
    os.makedirs(output_folder, exist_ok=True)

    hue_shift = hue_scale.get() / 360.0
    brightness_val = brightness_scale.get() / 100
    contrast_val = contrast_scale.get() / 100
    color_val = color_scale.get() / 100

    for file in img_files:
        path = os.path.join(input_folder, file)
        img = Image.open(path).convert("RGBA")
        img = shift_hue(img, hue_shift)
        img = ImageEnhance.Brightness(img).enhance(brightness_val)
        img = ImageEnhance.Contrast(img).enhance(contrast_val)
        img = ImageEnhance.Color(img).enhance(color_val)
        img.save(os.path.join(output_folder, file))

    result_label.config(text=f"✅ บันทึกแล้ว {len(img_files)} ไฟล์ → {output_folder}")

# ---------- GUI ----------
root = Tk()
root.title("🎨 ปรับสีตัวละคร + พรีวิว")
root.geometry("420x600")

input_folder = None
img_files = []
original_img = None
current_img_index = 0

Label(root, text="เลือกโฟลเดอร์ภาพ").pack(pady=5)
Button(root, text="📂 เลือกโฟลเดอร์", command=select_folder).pack(pady=5)
input_label = Label(root, text="ยังไม่ได้เลือก", fg="gray")
input_label.pack(pady=5)

# พรีวิว
preview_canvas = Canvas(root, width=150, height=200, bg="#222")
preview_canvas.pack(pady=10)

# ปุ่มเลื่อนภาพ
Button(root, text="◀ ก่อนหน้า", command=prev_image).pack(side="left", padx=20)
Button(root, text="▶ ถัดไป", command=next_image).pack(side="right", padx=20)

# สไลด์ปรับค่า
Label(root, text="Hue (หมุนโทนสี)").pack()
hue_scale = Scale(root, from_=0, to=360, orient=HORIZONTAL, command=update_preview)
hue_scale.set(0)
hue_scale.pack()

Label(root, text="Brightness (ความสว่าง)").pack()
brightness_scale = Scale(root, from_=0, to=300, orient=HORIZONTAL, command=update_preview)
brightness_scale.set(100)
brightness_scale.pack()

Label(root, text="Contrast (คอนทราสต์)").pack()
contrast_scale = Scale(root, from_=0, to=300, orient=HORIZONTAL, command=update_preview)
contrast_scale.set(100)
contrast_scale.pack()

Label(root, text="Saturation (ความสดของสี)").pack()
color_scale = Scale(root, from_=0, to=300, orient=HORIZONTAL, command=update_preview)
color_scale.set(100)
color_scale.pack()

Button(root, text="💾 บันทึกภาพทั้งหมด", bg="#4CAF50", fg="white", command=save_all).pack(pady=10)
result_label = Label(root, text="")
result_label.pack()

root.mainloop()
