package us.techhigh.maps.buildings

import us.techhigh.maps.Building
import us.techhigh.maps.Building.NodeSerializer.buildNodeTree

object BuildingD : Building('D') {
    override val floorRange: List<Int> = (1..4).filter { it != 2 }.toList()
    override val floors = listOf(
        buildNodeTree(this::class.java.classLoader.getResource("floors/d1.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/d3.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/d4.json")?.readText() ?: "")
    )

    init {
        loadElevators()
        loadStairs()
    }
}