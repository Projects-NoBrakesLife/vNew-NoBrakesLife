package game;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigPopup {

    public enum PopupType {
        CLUCKERS,
        DORM,
        JOB_CENTER,
        BANK,
        SHOP_GROCERY,
        GARDEN,
        DORMITORY,
        UNIVERSITY,
        GYM,
        CUSTOM
    }

    private static final Map<PopupType, Supplier<PopupPreset>> registry = new HashMap<>();
    private static final Map<String, PopupType> nameToType = new HashMap<>();

    public static class PopupPreset {
        public PopupWindow.PopupWindowConfig config;
        public ArrayList<MenuElement> elements;
    }

    static {
        
        registerPreset(PopupType.CLUCKERS, ConfigPopup::createCluckersPreset);
        registerPreset(PopupType.BANK, ConfigPopup::createBankPreset);
        registerPreset(PopupType.GARDEN, ConfigPopup::createGardenPreset);
        registerPreset(PopupType.DORMITORY, ConfigPopup::createDormitoryPreset);
        registerPreset(PopupType.UNIVERSITY, ConfigPopup::createUniversityPreset);
        registerPreset(PopupType.GYM, ConfigPopup::createGymPreset);
        
        nameToType.put("ร้านไก่ทอดตลาดน้อย", PopupType.CLUCKERS);
        nameToType.put("ธนาคารไทณิชย์", PopupType.BANK);
        nameToType.put("สวนจก", PopupType.GARDEN);
        nameToType.put("หอพัก", PopupType.DORMITORY);
        nameToType.put("มหาวิทยาลัย NSU", PopupType.UNIVERSITY);
        nameToType.put("ยิมหน้ามอ", PopupType.GYM);
    }

    public static void registerPreset(PopupType type, Supplier<PopupPreset> factory) {
        if (type == null || factory == null) return;
        registry.put(type, factory);
    }

    public static PopupWindow.PopupWindowConfig createConfig(PopupType type) {
        Supplier<PopupPreset> s = registry.get(type);
        if (s == null) return null;
        PopupPreset p = s.get();
        return p != null ? p.config : null;
    }

    public static ArrayList<MenuElement> createElements(PopupType type) {
        Supplier<PopupPreset> s = registry.get(type);
        if (s == null) return null;
        PopupPreset p = s.get();
        return p != null ? p.elements : null;
    }

    public static PopupType resolveTypeByName(String displayName) {
        if (displayName == null) return null;
        return nameToType.get(displayName);
    }

    public static PopupWindow.PopupWindowConfig createCluckersConfig() {
        PopupWindow.PopupWindowConfig cfg = new PopupWindow.PopupWindowConfig();
        cfg.width = 1632;
        cfg.height = 918;
        cfg.backgroundColor = new Color(255, 82, 10);
        cfg.useBackgroundImage = false;
        return cfg;
    }

    public static ArrayList<MenuElement> createCluckersElements() {
        ArrayList<MenuElement> popupElements = new ArrayList<>();

        popupElements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "CluckersClerk.png",
                761.0, -16.0, 874.8, 972.0));

        popupElements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item.png",
                419.0, 130.0, 256.0, 256.0));

        popupElements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item.png",
                67.0, 130.0, 256.0, 256.0));

        popupElements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TEMP_Exit_button.png",
                13.0, 8.0, 149.0, 95.0));

        popupElements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item.png",
                70.0, 390.0, 256.0, 256.0));

        MenuElement bucket = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Cluckers-Bucket #2969.png",
                435.0, 151.0, 176.9, 176.9);
        bucket.setUseScaleEffect(true);
        bucket.setTooltip("ไก่ทอดบักเก็ต +สุขภาพ 40");
        popupElements.add(bucket);

        MenuElement t1 = new MenuElement("30 $", 230.0, 342.0, 24);
        t1.setTextColor(new Color(255, 255, 255));
        popupElements.add(t1);

        popupElements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item.png",
                421.0, 391.0, 256.0, 256.0));

        MenuElement fries = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Cluckers-Fries #2994.png",
                96.0, 418.0, 162.5, 162.5);
        fries.setUseScaleEffect(true);
        fries.setTooltip("เฟรนช์ฟรายส์ +สุขภาพ 40");
        popupElements.add(fries);

        popupElements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Background-Border-02.png",
                736.0, -225.0, 1440.1, 1241.1));

        MenuElement burger = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Cluckers-Burger #2907.png",
                94.0, 156.0, 177.5, 177.5);
        burger.setUseScaleEffect(true);
        burger.setTooltip("เบอร์เกอร์ไก่ +สุขภาพ 50");
        popupElements.add(burger);

        MenuElement t2 = new MenuElement("20 $", 582.0, 343.0, 24);
        t2.setTextColor(new Color(255, 255, 255));
        popupElements.add(t2);

        MenuElement t3 = new MenuElement("30 $", 233.0, 602.0, 24);
        t3.setTextColor(new Color(255, 255, 255));
        popupElements.add(t3);

        MenuElement t4 = new MenuElement("20 $", 583.0, 603.0, 24);
        t4.setTextColor(new Color(255, 255, 255));
        popupElements.add(t4);

        MenuElement shake = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Cluckers-Shake.png",
                451.0, 418.0, 163.8, 163.8);
        shake.setUseScaleEffect(true);
        shake.setTooltip("เชคไก่ +สุขภาพ 30");
        popupElements.add(shake);

        return popupElements;
    }

    public static PopupPreset createCluckersPreset() {
        PopupPreset preset = new PopupPreset();
        preset.config = createCluckersConfig();
        preset.elements = createCluckersElements();
        return preset;
    }

    public static PopupWindow.PopupWindowConfig createBankConfig() {
        PopupWindow.PopupWindowConfig cfg = new PopupWindow.PopupWindowConfig();
        cfg.width = 1632;
        cfg.height = 918;
        cfg.backgroundColor = new Color(43, 127, 52);
        cfg.useBackgroundImage = false;
        return cfg;
    }

    public static ArrayList<MenuElement> createBankElements() {
        ArrayList<MenuElement> elements = new ArrayList<>();

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TEMP_Exit_button.png",
                13.0, 8.0, 149.0, 95.0));

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "BankClerk.png",
                745.0, -132.0, 1122.7, 1247.4));

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Background-Border-02.png",
                723.0, -76.0, 1198.8, 1033.2));

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item_none.png",
                390.0, 98.0, 256.0, 256.0));

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item_none.png",
                35.0, 98.0, 256.0, 256.0));

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item_none.png",
                34.0, 418.0, 256.0, 256.0));

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item_none.png",
                383.0, 416.0, 256.0, 256.0));

        MenuElement withdraw500 = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Withdraw-500 #42864.png",
                78.0, 464.0, 132.5, 132.5);
        withdraw500.setTooltip("ถอนเงิน 100 $");
        withdraw500.setUseScaleEffect(true);
        elements.add(withdraw500);

        MenuElement withdrawAll = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Withdraw-All #45109.png",
                437.0, 465.0, 132.5, 132.5);
        withdrawAll.setTooltip("ถอนเงิน ทั้งหมด");
        withdrawAll.setUseScaleEffect(true);
        elements.add(withdrawAll);

        MenuElement deposit500 = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Deposit-500.png",
                79.0, 145.0, 132.5, 132.5);
        deposit500.setUseScaleEffect(true);
        deposit500.setTooltip("ฝากเงิน 100 $");
        elements.add(deposit500);

        MenuElement depositAll = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Deposit-All.png",
                437.0, 144.0, 132.5, 132.5);
        depositAll.setUseScaleEffect(true);
        depositAll.setTooltip("ฝากเงิน ทั้งหมด");
        elements.add(depositAll);


        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "Input-Field-Small-White_0.png",
                1027.0, 746.0, 556.7, 92.7));

        MenuElement totalText = new MenuElement("ยอดรวม  ....", 1185.0, 802.0, 32);
        totalText.setTextColor(new Color(0, 0, 0));
        elements.add(totalText);

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TutorialSpeechbubble.png",
                726.0, 187.0, 449.5, 199.8));

        MenuElement t1 = new MenuElement("เก็บเงินไว้กับเฮียไนท์", 811.0, 232.0, 32);
        t1.setTextColor(new Color(0, 0, 0));
        elements.add(t1);

        MenuElement t2 = new MenuElement("รับไปเลยปันผล", 840.0, 273.0, 32);
        t2.setTextColor(new Color(0, 0, 0));
        elements.add(t2);

        MenuElement t3 = new MenuElement("ร้อยละ 10$", 862.0, 316.0, 32);
        t3.setTextColor(new Color(0, 0, 0));
        elements.add(t3);

        MenuElement t4 = new MenuElement("ทุกสัปดาห์", 868.0, 353.0, 32);
        t4.setTextColor(new Color(0, 0, 0));
        elements.add(t4);

        return elements;
    }

    public static PopupPreset createBankPreset() {
        PopupPreset preset = new PopupPreset();
        preset.config = createBankConfig();
        preset.elements = createBankElements();
        return preset;
    }

    public static PopupWindow.PopupWindowConfig createGardenConfig() {
        PopupWindow.PopupWindowConfig cfg = new PopupWindow.PopupWindowConfig();
        cfg.width = 1632;
        cfg.height = 918;
        cfg.backgroundColor = new Color(0, 153, 153);
        cfg.useBackgroundImage = false;
        return cfg;
    }

    public static ArrayList<MenuElement> createGardenElements() {
        ArrayList<MenuElement> elements = new ArrayList<>();

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "ParkFish.png",
                703.0, -10.0, 958.8, 960.8));

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Background-Border-02.png",
                679.0, -68.0, 1345.0, 1159.2));

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TEMP_Exit_button.png",
                13.0, 8.0, 149.0, 95.0));

        elements.add(new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item_none.png",
                39.0, 101.0, 256.0, 256.0));

        MenuElement fishingPole = new MenuElement(MenuElement.ElementType.IMAGE,
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Fishing-Pole #44610.png",
                63.0, 128.0, 165.0, 165.0);
        fishingPole.setUseScaleEffect(true);
        fishingPole.setTooltip("เบ็ดทั่วไป มีโอกาสตกได้ปลาราคาสูง");

        MenuElement bg_item_none = new MenuElement(MenuElement.ElementType.IMAGE, "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item_none.png", 383.0, 97.0, 256.0, 256.0);
        elements.add(bg_item_none);
        MenuElement Icon_Fishing_Pole_Max = new MenuElement(MenuElement.ElementType.IMAGE, "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Fishing-Pole Max.png", 413.0, 128.0, 171.6, 171.6);
        Icon_Fishing_Pole_Max.setUseScaleEffect(true);
        Icon_Fishing_Pole_Max.setTooltip("เบ็ดทองคำ [ยังไม่ปลดล็อค]");
        elements.add(Icon_Fishing_Pole_Max);


        elements.add(fishingPole);


        return elements;
    }

    public static PopupPreset createGardenPreset() {
        PopupPreset preset = new PopupPreset();
        preset.config = createGardenConfig();
        preset.elements = createGardenElements();
        return preset;
    }

    private static PopupWindow.PopupWindowConfig createDormitoryConfig() {
        PopupWindow.PopupWindowConfig config = new PopupWindow.PopupWindowConfig();
        config.width = 1632;
        config.height = 918;
        config.backgroundColor = new Color(102, 102, 102);
        config.useBackgroundImage = false;
        return config;
    }

    private static ArrayList<MenuElement> createDormitoryElements() {
        ArrayList<MenuElement> elements = new ArrayList<>();

        MenuElement bg_item_none = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item_none.png", 
                86.0, 151.0, 386.6, 386.6);
        elements.add(bg_item_none);

        MenuElement crappyApartmentBg = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Crappy Apartment Background.png", 
                543.0, -71.0, 1826.2, 1031.0);
        elements.add(crappyApartmentBg);

        MenuElement borderBg = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Background-Border-02.png", 
                524.0, -13.0, 1498.6, 1291.5);
        elements.add(borderBg);

        MenuElement bed = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Furniture-Bed_Mattress.png", 
                137.0, 199.0, 237.4, 237.4);
        bed.setUseScaleEffect(true);
        bed.setTooltip("นอนพักผ่อน +สุขภาพ " + GameConfig.SLEEP_HEALTH_BONUS);
        elements.add(bed);

        MenuElement exitButton = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TEMP_Exit_button.png", 
                13.0, 8.0, 149.0, 95.0);
        elements.add(exitButton);

        return elements;
    }

    public static PopupPreset createDormitoryPreset() {
        PopupPreset preset = new PopupPreset();
        preset.config = createDormitoryConfig();
        preset.elements = createDormitoryElements();
        return preset;
    }

    private static PopupWindow.PopupWindowConfig createUniversityConfig() {
        PopupWindow.PopupWindowConfig config = new PopupWindow.PopupWindowConfig();
        config.width = 1632;
        config.height = 918;
        config.backgroundColor = new Color(0, 102, 51);
        config.useBackgroundImage = false;
        return config;
    }

    private static ArrayList<MenuElement> createUniversityElements() {
        ArrayList<MenuElement> elements = new ArrayList<>();

        MenuElement universityClerk = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "UniversityClerk.png", 
                696.0, -76.0, 1030.3, 1144.8);
        elements.add(universityClerk);

        MenuElement borderBg = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Background-Border-02.png", 
                674.0, -114.0, 1425.5, 1228.5);
        elements.add(borderBg);

        MenuElement inputFieldWhite = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "Input-Field-Small-White_0.png", 
                103.0, 383.0, 442.0, 73.6);
        elements.add(inputFieldWhite);

        MenuElement buttonGray = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "button" + File.separator + "Button-Small-Gray.png", 
                201.0, 469.0, 199.6, 106.6);
        elements.add(buttonGray);

        MenuElement studyIcon = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Chamber-Study-Old.png", 
                255.0, 479.0, 89.6, 80.1);
        studyIcon.setUseScaleEffect(true);
        studyIcon.setTooltip("เรียนหนังสือ +การศึกษา");
        elements.add(studyIcon);

        MenuElement answerPlaceholder = new MenuElement("   ", 283.0, 423.0, 24);
        answerPlaceholder.setTextColor(new Color(0, 0, 0));
        elements.add(answerPlaceholder);

        MenuElement questionText = new MenuElement("คำถาม", 120.0, 255.0, 18);
        questionText.setTextColor(new Color(0, 0, 0));
        elements.add(questionText);

        MenuElement exitButton = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TEMP_Exit_button.png", 
                13.0, 8.0, 149.0, 95.0);
        elements.add(exitButton);


        MenuElement img = new MenuElement(MenuElement.ElementType.IMAGE, "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TutorialSpeechbubble.png", 637.0, 215.0, 457.4, 203.3);
        elements.add(img);
        MenuElement text = new MenuElement("This is the concept", 730.0, 328.0, 32);
        text.setTextColor(new Color(0, 0, 0));
        elements.add(text);

        return elements;
    }

    public static PopupPreset createUniversityPreset() {
        PopupPreset preset = new PopupPreset();
        preset.config = createUniversityConfig();
        preset.elements = createUniversityElements();
        return preset;
    }
    
    
    private static PopupWindow.PopupWindowConfig createGymConfig() {
        PopupWindow.PopupWindowConfig config = new PopupWindow.PopupWindowConfig();
        config.width = 1632;
        config.height = 918;
        config.backgroundColor = new Color(255, 153, 0);
        config.useBackgroundImage = false;
        return config;
    }
    
    private static ArrayList<MenuElement> createGymElements() {
        ArrayList<MenuElement> elements = new ArrayList<>();
        
        elements.add(new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Gym.png", 
                657.0, -105.0, 1071.1, 1190.2));
        
        elements.add(new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item_none.png", 
                354.0, 322.0, 256.0, 256.0));
        
        MenuElement tophand = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "LOC-Gym tophand #33276.png", 
                374.0, 380.0, 190.3, 95.5);
        tophand.setUseScaleEffect(true);
        tophand.setTooltip("ออกกำลังกายเต็มที่ สุขภาพ +" + GameConfig.GYM_HEALTH_BONUS_TOPHAND + " (ใช้เวลา " + (int)GameConfig.GYM_TIME_COST_TOPHAND + " ชม.)");
        elements.add(tophand);
        
        elements.add(new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "bg_item_none.png", 
                37.0, 326.0, 256.0, 256.0));
        
        MenuElement icon = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Icon-Location-Gym #41606.png", 
                64.0, 359.0, 177.5, 177.5);
        icon.setUseScaleEffect(true);
        icon.setTooltip("ออกกำลังกายเบาๆ สุขภาพ +" + GameConfig.GYM_HEALTH_BONUS_ICON + " (ใช้เวลา " + (int)GameConfig.GYM_TIME_COST_ICON + " ชม.)");
        elements.add(icon);
        
        elements.add(new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "Background-Border-02.png", 
                639.0, -37.0, 1368.4, 1179.4));
        
        elements.add(new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TutorialSpeechbubble.png", 
                519.0, 27.0, 534.6, 237.6));
        
        MenuElement text1 = new MenuElement("หวัดดีไอ้น้อง", 688.0, 76.0, 32);
        text1.setTextColor(new Color(0, 0, 0));
        elements.add(text1);
        
        MenuElement text2 = new MenuElement("ออกกำลังกายกับอาจารย์", 605.0, 119.0, 32);
        text2.setTextColor(new Color(0, 0, 0));
        elements.add(text2);
        
        MenuElement text3 = new MenuElement("เลน กล้ามโต", 686.0, 166.0, 32);
        text3.setTextColor(new Color(0, 0, 0));
        elements.add(text3);
        
        MenuElement text4 = new MenuElement("เพื่อสุขภาพ", 696.0, 219.0, 32);
        text4.setTextColor(new Color(0, 0, 0));
        elements.add(text4);
        
        MenuElement exitButton = new MenuElement(MenuElement.ElementType.IMAGE, 
                "assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TEMP_Exit_button.png", 
                13.0, 8.0, 149.0, 95.0);
        exitButton.setHoverImage("assets" + File.separator + "ui" + File.separator + "popup" + File.separator + "TEMP_exit_button_hover.png");
        elements.add(exitButton);
        
        return elements;
    }
    
    public static PopupPreset createGymPreset() {
        PopupPreset preset = new PopupPreset();
        preset.config = createGymConfig();
        preset.elements = createGymElements();
        return preset;
    }
}


