package us.techhigh.maps.data

import us.techhigh.maps.json.DirectionStep
import us.techhigh.maps.nodes.DirectionNode

expect abstract class Building {
    abstract val floors: List<List<DirectionNode>>
    abstract val floorRange: List<Int>
    abstract val scale: Pair<Double, Double>

    fun paths(_from: String, _to: String) : List<DirectionStep>
    fun loadElevators()
    fun loadStairs()
    fun associateBuilding()

    object NodeSerializer {
        class NodeLoad

        class DirectionNodeLoad {
            fun toNode() : DirectionNode
        }

        fun buildNodeTree(k: String) : List<DirectionNode>
    }
}