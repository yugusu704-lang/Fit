package com.fit.tracker.domain.model

object DefaultCatalogs {

    val exercises = listOf(
        // 1. 经典有氧
        ExerciseDefinition(
            id = "jump_rope",
            name = "跳绳",
            unitType = ExerciseUnitType.REPS,
            unitLabel = "个",
            kcalPerUnit = 0.14,
            stepQuantum = 10,
            minUnits = 50,
            category = "经典有氧"
        ),
        ExerciseDefinition(
            id = "running_400m",
            name = "操场跑步(400m)",
            unitType = ExerciseUnitType.LAPS,
            unitLabel = "圈",
            kcalPerUnit = 32.0,
            stepQuantum = 1,
            minUnits = 1,
            category = "经典有氧"
        ),
        ExerciseDefinition(
            id = "swimming_50m",
            name = "自由泳/蛙泳",
            unitType = ExerciseUnitType.METERS,
            unitLabel = "米",
            kcalPerUnit = 0.5,
            stepQuantum = 50,
            minUnits = 50,
            category = "经典有氧"
        ),
        ExerciseDefinition(
            id = "cycling_km",
            name = "户外骑行",
            unitType = ExerciseUnitType.KILOMETERS,
            unitLabel = "公里",
            kcalPerUnit = 30.0,
            stepQuantum = 1,
            minUnits = 1,
            category = "经典有氧"
        ),
        ExerciseDefinition(
            id = "bodyweight_squats",
            name = "徒手深蹲",
            unitType = ExerciseUnitType.REPS,
            unitLabel = "个",
            kcalPerUnit = 0.32,
            stepQuantum = 10,
            minUnits = 10,
            category = "经典有氧"
        ),

        // 2. 校园球类
        ExerciseDefinition(
            id = "basketball_half",
            name = "半场篮球",
            unitType = ExerciseUnitType.MINUTES,
            unitLabel = "分钟",
            kcalPerUnit = 6.5,
            stepQuantum = 5,
            minUnits = 15,
            category = "校园球类"
        ),
        ExerciseDefinition(
            id = "badminton",
            name = "羽毛球",
            unitType = ExerciseUnitType.MINUTES,
            unitLabel = "分钟",
            kcalPerUnit = 5.5,
            stepQuantum = 5,
            minUnits = 15,
            category = "校园球类"
        ),
        ExerciseDefinition(
            id = "table_tennis",
            name = "乒乓球",
            unitType = ExerciseUnitType.MINUTES,
            unitLabel = "分钟",
            kcalPerUnit = 4.0,
            stepQuantum = 5,
            minUnits = 15,
            category = "校园球类"
        ),
        ExerciseDefinition(
            id = "soccer",
            name = "足球",
            unitType = ExerciseUnitType.MINUTES,
            unitLabel = "分钟",
            kcalPerUnit = 7.0,
            stepQuantum = 5,
            minUnits = 15,
            category = "校园球类"
        ),

        // 3. 体测必考
        ExerciseDefinition(
            id = "pull_ups",
            name = "引体向上",
            unitType = ExerciseUnitType.REPS,
            unitLabel = "个",
            kcalPerUnit = 1.0,
            stepQuantum = 2,
            minUnits = 4,
            category = "体测必考"
        ),
        ExerciseDefinition(
            id = "sit_ups",
            name = "仰卧起坐",
            unitType = ExerciseUnitType.REPS,
            unitLabel = "个",
            kcalPerUnit = 0.4,
            stepQuantum = 10,
            minUnits = 20,
            category = "体测必考"
        ),
        ExerciseDefinition(
            id = "push_ups",
            name = "标准俯卧撑",
            unitType = ExerciseUnitType.REPS,
            unitLabel = "个",
            kcalPerUnit = 0.5,
            stepQuantum = 5,
            minUnits = 10,
            category = "体测必考"
        ),
        ExerciseDefinition(
            id = "running_test",
            name = "1000m/800m体测跑",
            unitType = ExerciseUnitType.LAPS,
            unitLabel = "次",
            kcalPerUnit = 65.0,
            stepQuantum = 1,
            minUnits = 1,
            category = "体测必考"
        ),

        // 4. 宿舍燃脂
        ExerciseDefinition(
            id = "jumping_jacks",
            name = "开合跳",
            unitType = ExerciseUnitType.REPS,
            unitLabel = "个",
            kcalPerUnit = 0.2,
            stepQuantum = 20,
            minUnits = 50,
            category = "宿舍燃脂"
        ),
        ExerciseDefinition(
            id = "dorm_dumbbells",
            name = "宿舍哑铃/负重练习",
            unitType = ExerciseUnitType.REPS,
            unitLabel = "次",
            kcalPerUnit = 0.35,
            stepQuantum = 10,
            minUnits = 20,
            category = "宿舍燃脂"
        ),
        ExerciseDefinition(
            id = "plank",
            name = "平板支撑",
            unitType = ExerciseUnitType.MINUTES,
            unitLabel = "分钟",
            kcalPerUnit = 4.5,
            stepQuantum = 1,
            minUnits = 1,
            category = "宿舍燃脂"
        ),
        ExerciseDefinition(
            id = "aerobic_workout",
            name = "宿舍跟练燃脂操",
            unitType = ExerciseUnitType.MINUTES,
            unitLabel = "分钟",
            kcalPerUnit = 6.0,
            stepQuantum = 5,
            minUnits = 15,
            category = "宿舍燃脂"
        ),

        // 5. 校园通勤
        ExerciseDefinition(
            id = "campus_bike",
            name = "共享单车/校园骑行",
            unitType = ExerciseUnitType.KILOMETERS,
            unitLabel = "公里",
            kcalPerUnit = 28.0,
            stepQuantum = 1,
            minUnits = 1,
            category = "校园通勤"
        ),
        ExerciseDefinition(
            id = "campus_walk",
            name = "校园散步/步行",
            unitType = ExerciseUnitType.KILOMETERS,
            unitLabel = "公里",
            kcalPerUnit = 42.0,
            stepQuantum = 1,
            minUnits = 1,
            category = "校园通勤"
        ),
        ExerciseDefinition(
            id = "stair_climbing",
            name = "爬楼梯(教学楼/宿舍)",
            unitType = ExerciseUnitType.LAPS,
            unitLabel = "层",
            kcalPerUnit = 4.0,
            stepQuantum = 2,
            minUnits = 4,
            category = "校园通勤"
        )
    )

    val foods = listOf(
        // 主食谷物
        FoodItem("food_rice", "食堂米饭(熟)", "mifan", 116.0, protein = 2.6, carbs = 25.9, fat = 0.3, category = "主食谷物"),
        FoodItem("food_steamed_bun", "馒头", "mantou", 223.0, protein = 7.0, carbs = 47.0, fat = 1.1, category = "主食谷物"),
        FoodItem("food_meat_bao", "猪肉大葱包", "zhuroubao", 227.0, protein = 7.5, carbs = 32.0, fat = 8.0, category = "主食谷物"),
        FoodItem("food_veg_bao", "香菇青菜包", "qingcaibao", 180.0, protein = 5.0, carbs = 30.0, fat = 4.5, category = "主食谷物"),
        FoodItem("food_soup_noodles", "食堂汤面/拉面", "tangmian", 110.0, protein = 3.8, carbs = 21.0, fat = 1.5, category = "主食谷物"),
        FoodItem("food_fried_noodles", "炒面/炒河粉", "chaomian", 185.0, protein = 5.2, carbs = 28.5, fat = 6.0, category = "主食谷物"),
        FoodItem("food_huntun", "小馄饨(1碗)", "huntun", 135.0, protein = 5.5, carbs = 18.0, fat = 4.8, category = "主食谷物"),
        FoodItem("food_jianbing", "鸡蛋灌饼/煎饼", "jianbing", 215.0, protein = 6.5, carbs = 31.0, fat = 7.5, category = "主食谷物"),
        FoodItem("food_instant_noodles", "方便面/泡面", "fangbianmian", 470.0, protein = 9.5, carbs = 60.0, fat = 21.0, category = "主食谷物"),
        FoodItem("food_corn", "水煮玉米", "yumi", 112.0, protein = 4.0, carbs = 22.8, fat = 1.2, category = "主食谷物"),
        FoodItem("food_sweet_potato", "蒸红薯/紫薯", "hongshu", 86.0, protein = 1.6, carbs = 20.1, fat = 0.2, category = "主食谷物"),
        FoodItem("food_oats", "纯燕麦片", "yanmaipian", 367.0, protein = 15.0, carbs = 61.8, fat = 6.7, category = "主食谷物"),
        FoodItem("food_whole_wheat_bread", "全麦面包片", "quanmaimianbao", 246.0, protein = 9.2, carbs = 46.0, fat = 3.5, category = "主食谷物"),
        FoodItem("food_potato", "水煮土豆", "tudou", 77.0, protein = 2.0, carbs = 17.2, fat = 0.1, category = "主食谷物"),
        FoodItem("food_kaolengmian", "烤冷面(加蛋)", "kaolengmian", 195.0, protein = 6.0, carbs = 27.0, fat = 7.0, category = "主食谷物"),

        // 肉蛋蛋白
        FoodItem("food_chicken_leg", "食堂大鸡腿(卤/红烧)", "jitui", 185.0, protein = 20.5, carbs = 2.0, fat = 10.5, category = "肉蛋蛋白"),
        FoodItem("food_fried_chicken", "食堂炸鸡排/炸鸡柳", "zhajipai", 280.0, protein = 18.0, carbs = 14.0, fat = 17.0, category = "肉蛋蛋白"),
        FoodItem("food_hongshaorou", "食堂红烧肉", "hongshaorou", 340.0, protein = 13.0, carbs = 6.0, fat = 30.0, category = "肉蛋蛋白"),
        FoodItem("food_huangmenji", "黄焖鸡块", "huangmenji", 160.0, protein = 16.5, carbs = 3.5, fat = 9.0, category = "肉蛋蛋白"),
        FoodItem("food_tomato_egg", "番茄炒蛋", "fanqiechaodan", 130.0, protein = 6.2, carbs = 5.5, fat = 9.2, category = "肉蛋蛋白"),
        FoodItem("food_mapo_tofu", "麻婆豆腐", "mapodoufu", 115.0, protein = 7.5, carbs = 4.5, fat = 7.5, category = "肉蛋蛋白"),
        FoodItem("food_egg_whole", "水煮整蛋(约50g)", "jidan", 143.0, protein = 12.6, carbs = 1.5, fat = 9.5, category = "肉蛋蛋白"),
        FoodItem("food_egg_marinated", "食堂卤蛋", "ludan", 148.0, protein = 13.0, carbs = 2.0, fat = 9.8, category = "肉蛋蛋白"),
        FoodItem("food_egg_fried", "荷包蛋(煎蛋)", "hebaodan", 195.0, protein = 11.5, carbs = 1.8, fat = 15.5, category = "肉蛋蛋白"),
        FoodItem("food_cooked_chicken_breast", "水煮鸡胸肉(即食)", "shuizhujixiong", 133.0, protein = 27.0, carbs = 0.5, fat = 2.5, category = "肉蛋蛋白"),
        FoodItem("food_jiang_beef", "食堂酱牛肉", "jiangniurou", 180.0, protein = 26.0, carbs = 1.5, fat = 7.5, category = "肉蛋蛋白"),
        FoodItem("food_tofu_firm", "老豆腐/家常豆腐", "laodoufu", 98.0, protein = 8.1, carbs = 4.2, fat = 3.7, category = "肉蛋蛋白"),
        FoodItem("food_milk_pure", "纯牛奶(250ml)", "chunniunai", 65.0, protein = 3.2, carbs = 4.8, fat = 3.6, category = "肉蛋蛋白"),
        FoodItem("food_soy_milk_unsweet", "食堂热豆浆", "doujiang", 35.0, protein = 3.2, carbs = 2.0, fat = 1.6, category = "肉蛋蛋白"),
        FoodItem("food_sausage", "纯肉烤肠(校门口)", "kaochang", 305.0, protein = 14.0, carbs = 5.0, fat = 26.0, category = "肉蛋蛋白"),
        FoodItem("food_yogurt", "原味酸奶", "suannai", 72.0, protein = 3.1, carbs = 11.0, fat = 2.5, category = "肉蛋蛋白"),

        // 蔬菜纤维
        FoodItem("food_potato_slices", "酸辣土豆丝", "tudousi", 105.0, protein = 1.8, carbs = 16.5, fat = 3.8, category = "蔬菜纤维"),
        FoodItem("food_cabbage", "手撕包菜", "baocai", 55.0, protein = 1.5, carbs = 5.2, fat = 3.2, category = "蔬菜纤维"),
        FoodItem("food_broccoli", "清炒西兰花", "xilanhua", 45.0, protein = 2.5, carbs = 5.0, fat = 1.8, category = "蔬菜纤维"),
        FoodItem("food_green_veg", "炒油菜/青菜", "qingcai", 40.0, protein = 1.6, carbs = 3.5, fat = 2.2, category = "蔬菜纤维"),
        FoodItem("food_disanxian", "地三鲜(土豆茄子)", "disanxian", 145.0, protein = 2.2, carbs = 15.0, fat = 8.8, category = "蔬菜纤维"),
        FoodItem("food_cucumber", "凉拌黄瓜", "huanggua", 25.0, protein = 0.8, carbs = 3.0, fat = 1.0, category = "蔬菜纤维"),
        FoodItem("food_tomato", "鲜番茄/小番茄", "fanqie", 18.0, protein = 0.9, carbs = 3.9, fat = 0.2, category = "蔬菜纤维"),
        FoodItem("food_malatang_greens", "麻辣烫素菜(混拼)", "malatang", 50.0, protein = 2.0, carbs = 5.5, fat = 2.2, category = "蔬菜纤维"),
        FoodItem("food_bean_sprouts", "清炒绿豆芽", "douya", 35.0, protein = 1.8, carbs = 3.8, fat = 1.5, category = "蔬菜纤维"),
        FoodItem("food_spinach", "菠菜/上汤菠菜", "bocai", 30.0, protein = 2.4, carbs = 3.0, fat = 1.2, category = "蔬菜纤维"),
        FoodItem("food_mushroom", "香菇/口蘑", "koumo", 28.0, protein = 3.1, carbs = 3.3, fat = 0.3, category = "蔬菜纤维"),

        // 水果坚果
        FoodItem("food_apple", "红富士苹果", "pingguo", 52.0, protein = 0.3, carbs = 13.8, fat = 0.2, category = "水果坚果"),
        FoodItem("food_banana", "香蕉", "xiangjiao", 89.0, protein = 1.1, carbs = 22.8, fat = 0.3, category = "水果坚果"),
        FoodItem("food_orange", "沃柑/砂糖橘", "wogan", 44.0, protein = 0.8, carbs = 10.5, fat = 0.1, category = "水果坚果"),
        FoodItem("food_watermelon", "麒麟西瓜", "xigua", 31.0, protein = 0.6, carbs = 7.5, fat = 0.1, category = "水果坚果"),
        FoodItem("food_blueberry", "蓝莓", "lanmei", 57.0, protein = 0.7, carbs = 14.5, fat = 0.3, category = "水果坚果"),
        FoodItem("food_almonds", "每日坚果/巴旦木", "badanmu", 579.0, protein = 21.0, carbs = 21.6, fat = 49.9, category = "水果坚果")
    )

    val foodCategoryMap: Map<String, String> = foods.associate { it.id to it.category }
}
