package us.techhigh.maps.buildings

import us.techhigh.maps.Building
import us.techhigh.maps.Building.NodeSerializer.buildNodeTree

object BuildingB : Building('B') {
    override val floorRange: List<Int> = (0..4).toList()
    override val floors = listOf(
        buildNodeTree(this::class.java.classLoader.getResource("floors/b0.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/b1.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/b2.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/b3.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/b4.json")?.readText() ?: "")
    )

    init {
        loadElevators()
        loadStairs()
    }
}