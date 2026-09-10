package com.fit.tracker.domain.model

object DefaultCatalogs {

    val exercises = listOf(
        ExerciseDefinition(
            id = "jump_rope",
            name = "跳绳",
            unitType = ExerciseUnitType.REPS,
            unitLabel = "个",
            kcalPerUnit = 0.14, // 1000个约140 kcal (中高强度)
            stepQuantum = 10,
            minUnits = 50
        ),
        ExerciseDefinition(
            id = "running_400m",
            name = "跑步(400m跑道)",
            unitType = ExerciseUnitType.LAPS,
            unitLabel = "圈",
            kcalPerUnit = 32.0, // 400m操场跑道1圈约32 kcal (约80 kcal/km)
            stepQuantum = 1,
            minUnits = 1
        ),
        ExerciseDefinition(
            id = "swimming_50m",
            name = "自由泳/蛙泳",
            unitType = ExerciseUnitType.METERS,
            unitLabel = "米(趟)",
            kcalPerUnit = 0.5, // 50m一趟约25 kcal
            stepQuantum = 50,
            minUnits = 50
        ),
        ExerciseDefinition(
            id = "cycling_km",
            name = "户外骑行",
            unitType = ExerciseUnitType.KILOMETERS,
            unitLabel = "公里",
            kcalPerUnit = 30.0, // 1公里约30 kcal (中等配速)
            stepQuantum = 1,
            minUnits = 1
        ),
        ExerciseDefinition(
            id = "bodyweight_squats",
            name = "徒手深蹲",
            unitType = ExerciseUnitType.REPS,
            unitLabel = "个",
            kcalPerUnit = 0.32, // 100个深蹲约32 kcal
            stepQuantum = 5,
            minUnits = 10
        )
    )

    val foods = listOf(
        // 主食谷薯类
        FoodItem("food_rice", "米饭(熟)", "mifan", 116.0, protein = 2.6, carbs = 25.9, fat = 0.3),
        FoodItem("food_steamed_bun", "馒头", "mantou", 223.0, protein = 7.0, carbs = 47.0, fat = 1.1),
        FoodItem("food_oats", "纯燕麦片", "yanmaipian", 367.0, protein = 15.0, carbs = 61.8, fat = 6.7),
        FoodItem("food_sweet_potato", "蒸红薯", "hongshu", 86.0, protein = 1.6, carbs = 20.1, fat = 0.2),
        FoodItem("food_corn", "水煮玉米", "yumi", 112.0, protein = 4.0, carbs = 22.8, fat = 1.2),
        FoodItem("food_whole_wheat_bread", "全麦全麦面包", "quanmaimianbao", 246.0, protein = 9.2, carbs = 46.0, fat = 3.5),
        FoodItem("food_noodles", "面条(熟)", "miantiao", 138.0, protein = 4.5, carbs = 28.0, fat = 0.7),
        FoodItem("food_potato", "水煮土豆", "tudou", 77.0, protein = 2.0, carbs = 17.2, fat = 0.1),

        // 肉禽蛋奶海鲜优质蛋白
        FoodItem("food_chicken_breast", "生鸡胸肉", "jixiongrou", 133.0, protein = 24.6, carbs = 2.5, fat = 1.9),
        FoodItem("food_cooked_chicken_breast", "水煮鸡胸肉", "shuizhujixiong", 165.0, protein = 31.0, carbs = 0.0, fat = 3.6),
        FoodItem("food_egg_whole", "水煮整蛋(约50g)", "jidan", 143.0, protein = 12.6, carbs = 1.5, fat = 9.5),
        FoodItem("food_egg_white", "蛋白(水煮)", "danbai", 52.0, protein = 11.0, carbs = 0.7, fat = 0.2),
        FoodItem("food_beef_lean", "瘦牛肉(生)", "shouniurou", 106.0, protein = 20.2, carbs = 1.2, fat = 2.3),
        FoodItem("food_beef_steak", "香煎牛排(精瘦)", "niupai", 155.0, protein = 26.0, carbs = 0.0, fat = 5.0),
        FoodItem("food_shrimp", "鲜虾仁", "xiaren", 85.0, protein = 18.0, carbs = 0.5, fat = 0.8),
        FoodItem("food_salmon", "三文鱼(生)", "sanwenyu", 139.0, protein = 19.8, carbs = 0.0, fat = 6.3),
        FoodItem("food_pork_lean", "猪里脊瘦肉", "zhuliji", 143.0, protein = 20.2, carbs = 1.5, fat = 6.2),
        FoodItem("food_tofu_firm", "老豆腐", "laodoufu", 98.0, protein = 8.1, carbs = 4.2, fat = 3.7),
        FoodItem("food_milk_pure", "纯牛奶", "chunniunai", 65.0, protein = 3.2, carbs = 4.8, fat = 3.6),
        FoodItem("food_milk_skim", "脱脂牛奶", "tuozhiniunai", 35.0, protein = 3.4, carbs = 4.9, fat = 0.2),
        FoodItem("food_soy_milk_unsweet", "无糖豆浆", "wutangdoujiang", 31.0, protein = 3.0, carbs = 1.5, fat = 1.6),
        FoodItem("food_greek_yogurt", "希腊酸奶(无糖)", "xilasuannai", 59.0, protein = 10.0, carbs = 3.6, fat = 0.4),

        // 蔬菜低卡膳食纤维
        FoodItem("food_broccoli", "西兰花", "xilanhua", 34.0, protein = 2.8, carbs = 6.6, fat = 0.4),
        FoodItem("food_spinach", "菠菜", "bocai", 23.0, protein = 2.9, carbs = 3.6, fat = 0.4),
        FoodItem("food_cucumber", "黄瓜", "huanggua", 15.0, protein = 0.7, carbs = 2.9, fat = 0.2),
        FoodItem("food_tomato", "番茄", "fanqie", 18.0, protein = 0.9, carbs = 3.9, fat = 0.2),
        FoodItem("food_lettuce", "生菜", "shengcai", 15.0, protein = 1.4, carbs = 2.9, fat = 0.2),
        FoodItem("food_cabbage", "卷心菜/包菜", "baocai", 24.0, protein = 1.3, carbs = 5.4, fat = 0.2),
        FoodItem("food_mushroom", "香菇/口蘑", "koumo", 28.0, protein = 3.1, carbs = 3.3, fat = 0.3),
        FoodItem("food_asparagus", "芦笋", "lusun", 22.0, protein = 2.4, carbs = 4.1, fat = 0.2),

        // 水果坚果
        FoodItem("food_apple", "苹果", "pingguo", 52.0, protein = 0.3, carbs = 13.8, fat = 0.2),
        FoodItem("food_banana", "香蕉", "xiangjiao", 89.0, protein = 1.1, carbs = 22.8, fat = 0.3),
        FoodItem("food_blueberry", "蓝莓", "lanmei", 57.0, protein = 0.7, carbs = 14.5, fat = 0.3),
        FoodItem("food_almonds", "原味巴旦木/杏仁", "badanmu", 579.0, protein = 21.0, carbs = 21.6, fat = 49.9)
    )
}
