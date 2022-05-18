import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

const val coordXScale = 1.0
const val coordYScale = 1.0
const val maxSteps = 16

enum class RoomType { CLASSROOM, OFFICE, SHOP, CAFETERIA, BATHROOM, OTHER }
data class Metadata(val building: Char, val floor: Int, val room: Int, val type: RoomType = RoomType.OTHER) {
    fun getIdentifier() = "$building$floor${(if (room < 10) "0" else "") + room.toString()}"
}

enum class Direction { NORTH, EAST, WEST, SOUTH }

open class DirectionNode(
    var neighbors: MutableList<DirectionNode> = mutableListOf(),
    val id: String,
    val position: Pair<Double, Double>
) {
    fun distanceTo(other: DirectionNode) : Double = sqrt((other.position.first - this.position.first).times(coordXScale).pow(2) + (other.position.second - this.position.second).times(coordYScale).pow(2))
    fun directionTo(other: DirectionNode) : Direction = when {
            this.position.first > other.position.first -> Direction.WEST
            this.position.first < other.position.first -> Direction.EAST
            this.position.second > other.position.second -> Direction.SOUTH
            this.position.second < other.position.second -> Direction.NORTH
            else -> throw Exception("Nodes are at the same position!")
        }

    fun addNeighbor(other: DirectionNode) {
        this.neighbors.add(other)
        other.neighbors.add(this)
    }

    fun paths(to: DirectionNode) : List<DirectionNode> {
        fun getAllPathsR(
            from: DirectionNode,
            to: DirectionNode,
            step: Int = 0,
            path: List<DirectionNode> = listOf()
        ): List<DirectionNode>? {
            if (from == to || step > maxSteps) {
                return path
            }
            return from.neighbors.filter { !path.contains(it) }
                .firstNotNullOfOrNull { getAllPathsR(it, to, step + 1, path + it) }
        }

        return this.neighbors.mapNotNull { getAllPathsR(it, to) }.minByOrNull { it.size } ?: emptyList()
    }

    override fun toString(): String = id
}

class MetadataNode(neighbors: MutableList<DirectionNode> = mutableListOf(), position: Pair<Double, Double>, metadata: Metadata) : DirectionNode(neighbors, metadata.getIdentifier(), position)

fun mapDirections(path: List<DirectionNode>) =
    listOf("Start at ${path.first()}.") + path.take(path.size - 1).mapIndexed { i, n ->
        "Go " + n.directionTo(path[i + 1]) + " for " + ceil(n.distanceTo(path[i + 1])).toInt() + " feet towards " + path[i + 1] + "."
    } + "Arrive at ${path.last()}!"

fun allNodes(start: DirectionNode, l: List<DirectionNode> = listOf(start)) : List<DirectionNode> {
    return (l + start.neighbors.filter { !l.contains(it) }.map { allNodes(it, l + it) }.flatten()).distinct()
}

@kotlinx.serialization.Serializable
data class NodeLoad(val elements: List<DirectionNodeLoad>)

@kotlinx.serialization.Serializable
data class DirectionNodeLoad(val x: Double, val y: Double, val neighbors: List<String>, val id: String) {
    fun toNode() : DirectionNode = DirectionNode(id = id, position = x to y)
}

fun buildNodeTree(k: String) : List<DirectionNode> {
    val elements = Json.decodeFromString<NodeLoad>(k).elements
    val nodes = elements.map { it.toNode() }

    elements.forEachIndexed { i, n -> nodes[i].neighbors = n.neighbors.map { nod -> nodes.first { no -> no.id == nod } }.toMutableList() }

    return nodes
}

fun main() {
    val n1 = MetadataNode(position = -1.0 to 0.0, metadata = Metadata('A', 1, 1, type = RoomType.CLASSROOM))
    val n2 = DirectionNode(position = 0.0 to 0.0, id = "2")
    val n3 = DirectionNode(position = 0.0 to 1.0, id = "3")
    val n4 = MetadataNode(position = 1.0 to 1.0, metadata = Metadata('A', 1, 2, type = RoomType.OTHER))
    val n5 = DirectionNode(position = 0.5 to 1.0, id = "5")
    val n6 = MetadataNode(position = 1.0 to 0.5, metadata = Metadata('A', 1, 3, type = RoomType.CLASSROOM))

    n1.addNeighbor(n2)
    n2.addNeighbor(n3)
    n3.addNeighbor(n4)
    n2.addNeighbor(n5)
    n5.addNeighbor(n6)
    n6.addNeighbor(n4)

    println(mapDirections(n1.paths(n4)).joinToString("\n"))
    println(allNodes(n1).filterIsInstance<MetadataNode>())

    val nod = buildNodeTree("""{
    "elements": [
        {
            "id": "137bc375-6b7f-40be-916b-3cb3df9f71ac",
            "x": -0.5,
            "y": -0.0,
            "neighbors": [
                "67510ce8-568a-479d-8efe-43b92c50973a"
            ]
        },
        {
            "id": "67510ce8-568a-479d-8efe-43b92c50973a",
            "x": -0.2,
            "y": 0.0,
            "neighbors": [
                "137bc375-6b7f-40be-916b-3cb3df9f71ac",
                "853257ac-7ad1-4596-b901-f7a2661745b0"
            ]
        },
        {
            "id": "853257ac-7ad1-4596-b901-f7a2661745b0",
            "x": -0.2,
            "y": 0.6,
            "neighbors": [
                "67510ce8-568a-479d-8efe-43b92c50973a",
                "7eda0750-669a-406c-a882-1f7035a7eed7"
            ]
        },
        {
            "id": "7eda0750-669a-406c-a882-1f7035a7eed7",
            "x": 0.0,
            "y": 0.6,
            "neighbors": [
                "853257ac-7ad1-4596-b901-f7a2661745b0"
            ]
        }
    ]
}""")

    println(mapDirections(nod.first().paths(nod.last())).joinToString(separator = "\n"))
}