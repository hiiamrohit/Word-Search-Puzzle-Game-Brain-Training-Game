package com.example.model

object LevelDefinitions {
    val levels: List<WordSearchLevel> = listOf(
        WordSearchLevel(
            levelNumber = 1,
            theme = "Animals",
            iconName = "Pets",
            words = listOf("CAT", "DOG", "LION", "TIGER", "BEAR"),
            gridSize = 6,
            allowDiagonal = false,
            allowReverse = false
        ),
        WordSearchLevel(
            levelNumber = 2,
            theme = "Fruits",
            iconName = "Apple",
            words = listOf("APPLE", "MANGO", "BANANA", "ORANGE", "GRAPE"),
            gridSize = 7,
            allowDiagonal = false,
            allowReverse = false
        ),
        WordSearchLevel(
            levelNumber = 3,
            theme = "Space",
            iconName = "Rocket",
            words = listOf("MOON", "MARS", "STAR", "EARTH", "COMET"),
            gridSize = 7,
            allowDiagonal = true,
            allowReverse = false
        ),
        WordSearchLevel(
            levelNumber = 4,
            theme = "Family",
            iconName = "Group",
            words = listOf("MOM", "DAD", "SON", "SISTER", "BROTHER"),
            gridSize = 8,
            allowDiagonal = true,
            allowReverse = false
        ),
        WordSearchLevel(
            levelNumber = 5,
            theme = "Food",
            iconName = "Restaurant",
            words = listOf("PIZZA", "BURGER", "RICE", "BREAD", "CHEESE"),
            gridSize = 8,
            allowDiagonal = true,
            allowReverse = true
        ),
        WordSearchLevel(
            levelNumber = 6,
            theme = "Nature",
            iconName = "Forest",
            words = listOf("TREE", "RIVER", "FLOWER", "CLOUD", "FOREST"),
            gridSize = 8,
            allowDiagonal = true,
            allowReverse = true
        ),
        WordSearchLevel(
            levelNumber = 7,
            theme = "Ocean",
            iconName = "Water",
            words = listOf("SHARK", "WHALE", "FISH", "CORAL", "SHELL"),
            gridSize = 8,
            allowDiagonal = true,
            allowReverse = true
        ),
        WordSearchLevel(
            levelNumber = 8,
            theme = "Sports",
            iconName = "SportsSoccer",
            words = listOf("CRICKET", "TENNIS", "FOOTBALL", "GOLF", "BOXING"),
            gridSize = 9,
            allowDiagonal = true,
            allowReverse = true
        ),
        WordSearchLevel(
            levelNumber = 9,
            theme = "Travel",
            iconName = "Flight",
            words = listOf("HOTEL", "TRAIN", "PLANE", "BEACH", "PASSPORT"),
            gridSize = 9,
            allowDiagonal = true,
            allowReverse = true
        ),
        WordSearchLevel(
            levelNumber = 10,
            theme = "School",
            iconName = "School",
            words = listOf("BOOK", "PENCIL", "TEACHER", "CLASS", "SCHOOL"),
            gridSize = 9,
            allowDiagonal = true,
            allowReverse = true
        )
    )

    fun getLevel(levelNumber: Int): WordSearchLevel {
        return levels.firstOrNull { it.levelNumber == levelNumber } ?: levels.first()
    }
}
