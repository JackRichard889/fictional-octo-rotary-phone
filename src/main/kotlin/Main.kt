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
    val nod = buildNodeTree("""{
    "elements": [
        {
            "id": "9458af77-69d4-4865-9ce3-c2476d5b8ffd",
            "x": -0.5,
            "y": 0.6,
            "neighbors": [
                "90b68ec2-3290-4ea4-a005-8129dcab5c81"
            ]
        },
        {
            "id": "90b68ec2-3290-4ea4-a005-8129dcab5c81",
            "x": -0.5,
            "y": 0.8,
            "neighbors": [
                "9458af77-69d4-4865-9ce3-c2476d5b8ffd",
                "a4c61917-599a-414a-a3b2-cd34bfaa82d4"
            ]
        },
        {
            "id": "0c0673a8-ddb6-4c77-a693-0f9b4353f910",
            "x": -0.1,
            "y": 0.8,
            "neighbors": [
                "a4c61917-599a-414a-a3b2-cd34bfaa82d4"
            ]
        },
        {
            "id": "6385a18a-4260-49e4-aa34-3ec041cbed6d",
            "x": -0.4,
            "y": 0.4,
            "neighbors": [
                "a4c61917-599a-414a-a3b2-cd34bfaa82d4"
            ]
        },
        {
            "id": "a4c61917-599a-414a-a3b2-cd34bfaa82d4",
            "x": -0.4,
            "y": 0.8,
            "neighbors": [
                "90b68ec2-3290-4ea4-a005-8129dcab5c81",
                "0c0673a8-ddb6-4c77-a693-0f9b4353f910",
                "6385a18a-4260-49e4-aa34-3ec041cbed6d"
            ]
        }
    ]
}""")

    println(mapDirections(nod.random().paths(nod.random())).joinToString(separator = "\n"))
}