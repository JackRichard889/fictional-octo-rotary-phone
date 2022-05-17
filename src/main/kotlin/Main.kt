import kotlin.math.pow
import kotlin.math.sqrt

/*
interface NodeBuilder

abstract class Node {
    abstract val id: String
    abstract val position: Pair<Int, Int>
    var children: List<Node> = listOf()
    var parentNode: Node? = null
    val metadata: NodeMetadata? = null

    class BasicNodeBuilder : NodeBuilder {
        internal val newChildren = mutableListOf<Node>()
        operator fun Node.unaryPlus() = newChildren.add(this)
    }

    open fun build(init: BasicNodeBuilder.() -> Unit) : Node {
        val builder = BasicNodeBuilder().also(init)
        children = builder.newChildren
        children.forEach { it.parentNode = this }
        return this
    }
}

data class NodeMetadata(val building: Char, val floor: Int, val room: Int) {
    fun getIdentifier() : String = "$building$floor${(if (room < 10) "0" else "") + room.toString()}"
}

class FloorNode(override val id: String, override val position: Pair<Int, Int>) : Node()

class DirectionalNode(override val id: String, override val position: Pair<Int, Int>) : Node() {
    class DirectionalNodeBuilder : NodeBuilder {
        val newChildren = mutableListOf<Node>()

    }

    override fun build(init: DirectionalNodeBuilder.() -> Unit): Node {

    }
}

class RoomNode(override val id: String, override val position: Pair<Int, Int>) : Node()
class ElevatorNode(override val id: String, override val position: Pair<Int, Int>) : Node()
class StairNode(override val id: String, override val position: Pair<Int, Int>) : Node()

fun main() {
    val room1 = RoomNode("r1", 0 to 0)
    val room2 = RoomNode("r2", 0 to 0)
    val room3 = RoomNode("r3", 0 to 0)
    val room4 = RoomNode("r4", 0 to 0)

    val hallway1 = DirectionalNode("h1", 0 to 0)
    val hallway2 = DirectionalNode("h2", 0 to 0)
    val hallway3 = DirectionalNode("h3", 0 to 0)

    val elevator1 = ElevatorNode("e1", 0 to 0)
    val elevator2 = ElevatorNode("e2", 0 to 0)

    val stairs1 = StairNode("s1", 0 to 0)
    val stairs2 = StairNode("s2", 0 to 0)

    val floor1 = FloorNode("f1", 0 to 0).build {
        +hallway1.build {
            +room1
            +elevator1.build { +elevator2 }
            +stairs1.build { +stairs2 }
        }
        +hallway2.build {
            +room2
            +room3
        }
    }

    val floor2 = FloorNode("f2", 0 to 0).build {
        +hallway3.build {
            +room4
            +elevator2.build { +elevator1 }
            +stairs2.build { +stairs1 }
        }
    }

    fun recursionStuff(node: Node?, destination: Node, path: List<Node> = listOf(node!!), max: Int = 0) : Node? {
        return if (max < 16 && node != null && (path.size < 2 || !path.take(path.size - 1).contains(node))) {
            if (node.id == destination.id) { println(path.joinToString { "Go to " + it.id }); return destination }
            val b = node.children.mapNotNull { recursionStuff(it, destination, path + it, max + 1) }
            return if (node.parentNode != null) { recursionStuff(node.parentNode, destination, path + node.parentNode!!,max + 1) ?: b.firstOrNull() } else b.firstOrNull()
        } else null
    }

    println(if (recursionStuff(room1, room4)?.id == room4.id) "Destination reached!" else "Destination not found.")

    embeddedServer(Netty, port = 80) {
        routing {
            get("/") {
                call.respondText("Hello, world!")
            }
        }
    }.start(wait = true)
}*/

data class Metadata(val building: Char, val floor: Int, val room: Int) {
    fun getIdentifier() : String = "$building$floor${(if (room < 10) "0" else "") + room.toString()}"
}

enum class Direction { NORTH, EAST, WEST, SOUTH }

data class DirectionNode(
    val north: List<DirectionNode> = listOf(),
    val east: List<DirectionNode> = listOf(),
    val west: List<DirectionNode> = listOf(),
    val south: List<DirectionNode> = listOf(),
    val position: Pair<Double, Double>
) {
    fun distanceTo(other: DirectionNode) : Double = sqrt((other.position.first - this.position.first).pow(2) + (other.position.second - this.position.second).pow(2))
    fun directionTo(other: DirectionNode) : Direction = when {
            this.position.first > other.position.first -> Direction.WEST
            this.position.first < other.position.first -> Direction.EAST
            this.position.second > other.position.second -> Direction.SOUTH
            this.position.second < other.position.second -> Direction.NORTH
            else -> throw Exception("Nodes are at the same position!")
        }
}

fun main() {
    val n1 = DirectionNode(position = -1.0 to 0.0)
    val n2 = DirectionNode(position = -1.0 to 1.0)
    println(n1.distanceTo(n2))
    println(n1.directionTo(n2))
}