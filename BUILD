load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "2048",
    srcs = ["GameContainer.java"],
    main_class = "com.example.pokemon2048.GameContainer",
    deps = [
      ":everything"
    ],
)

java_library(
    name = "everything",
    srcs = [
      "AnimateBlock.java",
      "GameState.java", 
      "Grid.java", 
      "ImageLoader.java", 
      "PokemonCell.java", 
      "RestartButton.java", 
      "ScorePanel.java"
    ],
)
