package us.techhigh.maps.buildings

import us.techhigh.maps.Building
import us.techhigh.maps.Building.NodeSerializer.buildNodeTree

object BuildingC : Building('C') {
    override val floorRange: List<Int> = (1 ..4).filter { it != 2 }.toList()
    override val floors = listOf(
        buildNodeTree(this::class.java.classLoader.getResource("floors/c1.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/c3.json")?.readText() ?: ""),
        buildNodeTree(this::class.java.classLoader.getResource("floors/c4.json")?.readText() ?: "")
    )

    init {
        loadStairs()
    }
}