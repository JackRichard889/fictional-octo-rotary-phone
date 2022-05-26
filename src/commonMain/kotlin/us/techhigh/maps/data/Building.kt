package us.techhigh.maps.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import us.techhigh.maps.json.DirectionStep
import us.techhigh.maps.nodes.DirectionNode
import us.techhigh.maps.nodes.FlooredNode
import us.techhigh.maps.nodes.MetadataNode

abstract class Building(val identifier: Char) {
    abstract val floors: List<List<DirectionNode>>
    abstract val floorRange: List<Int>
    abstract val scale: Pair<Double, Double>

    fun paths(_from: String, _to: String) : List<DirectionStep> {
        val from = floors.flatten().first { it.id == _from || if (it is MetadataNode) it.metadata.getIdentifier() == _from else false }
        val to = floors.flatten().first { it.id == _to || if (it is MetadataNode) it.metadata.getIdentifier() == _to else false }
        return from.paths(to)
    }

    fun loadElevators() {
        val elevators = floors.flatten().filterIsInstance<FlooredNode>().filter { it.type == "elevator" }
        floors.forEachIndexed { i, it ->
            val elevator = it.filterIsInstance<FlooredNode>().first { it.type == "elevator" }
            it.filterIsInstance<FlooredNode>().first { it.type == "elevator" }.floor = floorRange[i]
            it.filterIsInstance<FlooredNode>().first { it.type == "elevator" }.neighbors.addAll(elevators.filter { it.id != elevator.id })
        }
    }

    fun loadStairs() {
        floors.first()
            .filterIsInstance<FlooredNode>()
            .filter { it.type.contains("stair") }
            .map { it.type }.forEach { stair ->
                val stairsAll = floors.flatten().filterIsInstance<FlooredNode>().filter { it.type == stair }
                floors.forEachIndexed { index, directionNodes ->
                    val stairs = directionNodes.filterIsInstance<FlooredNode>().filter { it.type == stair }
                    if (stairs.isNotEmpty()) {
                        directionNodes.filterIsInstance<FlooredNode>().first { it.type == stair }.floor = floorRange[index]
                        directionNodes.filterIsInstance<FlooredNode>().first { it.type == stair }.neighbors.addAll(stairsAll.filter { it.id != stairs.first().id })
                    }
                }
            }
    }

    fun associateBuilding() {
        floors.forEach { it.forEach { room -> room.building = this } }
    }

    object NodeSerializer {
        private val json = Json { ignoreUnknownKeys = true }

        @kotlinx.serialization.Serializable
        data class NodeLoad(val elements: List<DirectionNodeLoad>)

        @kotlinx.serialization.Serializable
        data class DirectionNodeLoad(val x: Double, val y: Double, val neighbors: List<String>, val id: String, val room: String = "", val type: String = "") {
            fun toNode() : DirectionNode {
                return if (room.isEmpty() && type.isEmpty()) {
                    DirectionNode(id = id, position = x to y)
                } else if (type == "elevator" || type.contains("stair")) {
                    FlooredNode(_position = x to y, _id = id, type = type)
                } else {
                    MetadataNode(_position = x to y, metadata = Metadata(room.first(), room.take(2).last().toString().toInt(), room.takeLast(2).toInt()), _id = id)
                }
            }
        }

        fun buildNodeTree(k: String) : List<DirectionNode> {
            val elements = json.decodeFromString<NodeLoad>(k).elements
            val nodes = elements.map { it.toNode() }

            elements.forEachIndexed { i, n -> nodes[i].neighbors = n.neighbors.map { nod -> nodes.first { no -> no.id == nod } }.toMutableList() }

            return nodes
        }
    }
}