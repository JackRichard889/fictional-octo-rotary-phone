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

class MetadataNode(neighbors: MutableList<DirectionNode> = mutableListOf(), position: Pair<Double, Double>, id: String, val metadata: Metadata) : DirectionNode(neighbors, id, position) {
    override fun toString() = metadata.getIdentifier()
}

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
data class DirectionNodeLoad(val x: Double, val y: Double, val neighbors: List<String>, val id: String, val room: String = "") {
    fun toNode() : DirectionNode = if (room.isEmpty()) DirectionNode(id = id, position = x to y) else MetadataNode(position = x to y, metadata = Metadata(room.first(), room.take(2).last().toString().toInt(), room.takeLast(2).toInt()), id = id)
}

fun buildNodeTree(k: String) : List<DirectionNode> {
    val elements = Json { ignoreUnknownKeys = true }.decodeFromString<NodeLoad>(k).elements
    val nodes = elements.map { it.toNode() }

    elements.forEachIndexed { i, n -> nodes[i].neighbors = n.neighbors.map { nod -> nodes.first { no -> no.id == nod } }.toMutableList() }

    return nodes
}

fun main() {
    val nod = buildNodeTree("""{
    "elements": [
        {
            "id": "43034149-6081-4129-8c19-92e346935659",
            "x": -0.18,
            "y": -0.22,
            "neighbors": [
                "6e6abe86-63ed-4b88-ac39-db980982d417"
            ],
            "detail": "stair B2"
        },
        {
            "id": "b52c43fa-470d-4b82-a21a-6fe25b12ef44",
            "x": 0.91,
            "y": -0.13,
            "neighbors": [
                "3c8dba0c-fc7f-4e29-b4d2-a7baceafcbf2"
            ],
            "detail": "exit to C"
        },
        {
            "id": "aa448357-4096-432f-bce4-7b1d3fce06f4",
            "x": -0.26,
            "y": -0.22,
            "neighbors": [
                "6e6abe86-63ed-4b88-ac39-db980982d417"
            ],
            "room": "B201"
        },
        {
            "id": "b8f2693e-32cc-413d-a9ec-d40e2ab7ae44",
            "x": -0.26,
            "y": -0.09,
            "neighbors": [
                "6e6abe86-63ed-4b88-ac39-db980982d417"
            ],
            "room": "B203"
        },
        {
            "id": "6e6abe86-63ed-4b88-ac39-db980982d417",
            "x": -0.18,
            "y": -0.14,
            "neighbors": [
                "43034149-6081-4129-8c19-92e346935659",
                "6e966322-015d-4ed6-89a9-230f8d95884e",
                "0c23017b-166f-44b7-95c6-08a9611037f1",
                "aa448357-4096-432f-bce4-7b1d3fce06f4",
                "b8f2693e-32cc-413d-a9ec-d40e2ab7ae44"
            ]
        },
        {
            "id": "a8815dd1-fe76-4bc1-93bf-b41db2e38a9c",
            "x": -0.11,
            "y": -0.17,
            "neighbors": [
                "6e966322-015d-4ed6-89a9-230f8d95884e"
            ],
            "room": "B216"
        },
        {
            "id": "6e966322-015d-4ed6-89a9-230f8d95884e",
            "x": -0.11,
            "y": -0.13,
            "neighbors": [
                "6e6abe86-63ed-4b88-ac39-db980982d417",
                "4e867e5e-c584-4b19-bfee-fa31f0b233b7",
                "a8815dd1-fe76-4bc1-93bf-b41db2e38a9c",
                "cc947641-56cb-4c3f-9c2e-dbd44c53a417"
            ]
        },
        {
            "id": "cc947641-56cb-4c3f-9c2e-dbd44c53a417",
            "x": -0.11,
            "y": -0.07,
            "neighbors": [
                "6e966322-015d-4ed6-89a9-230f8d95884e"
            ],
            "room": "B219"
        },
        {
            "id": "4e867e5e-c584-4b19-bfee-fa31f0b233b7",
            "x": 0.22,
            "y": -0.13,
            "neighbors": [
                "6e966322-015d-4ed6-89a9-230f8d95884e",
                "c1583402-f700-4520-8f9b-70c7de97b0fa",
                "fbe5cdbc-6a60-4efc-851a-e5eef57faca4"
            ]
        },
        {
            "id": "fbe5cdbc-6a60-4efc-851a-e5eef57faca4",
            "x": 0.22,
            "y": -0.08,
            "neighbors": [
                "4e867e5e-c584-4b19-bfee-fa31f0b233b7"
            ],
            "room": "B220"
        },
        {
            "id": "d0198f31-2fca-4c24-9086-2ded82cf6666",
            "x": 0.26,
            "y": -0.06,
            "neighbors": [
                "c1583402-f700-4520-8f9b-70c7de97b0fa"
            ],
            "room": "B239"
        },
        {
            "id": "c1583402-f700-4520-8f9b-70c7de97b0fa",
            "x": 0.26,
            "y": -0.13,
            "neighbors": [
                "4e867e5e-c584-4b19-bfee-fa31f0b233b7",
                "dd68135b-9e1e-45df-9a32-b3504cf80912",
                "d0198f31-2fca-4c24-9086-2ded82cf6666",
                "f5590990-42d3-4635-bc36-c74ce26c4cc4"
            ]
        },
        {
            "id": "f5590990-42d3-4635-bc36-c74ce26c4cc4",
            "x": 0.26,
            "y": -0.18,
            "neighbors": [
                "c1583402-f700-4520-8f9b-70c7de97b0fa"
            ],
            "room": "B221"
        },
        {
            "id": "dd68135b-9e1e-45df-9a32-b3504cf80912",
            "x": 0.59,
            "y": -0.13,
            "neighbors": [
                "c1583402-f700-4520-8f9b-70c7de97b0fa",
                "e88cab52-d53f-40e5-94f6-7e99f2b9ace1",
                "de1d0eaa-803a-434b-904e-4c98037fe3d2"
            ]
        },
        {
            "id": "de1d0eaa-803a-434b-904e-4c98037fe3d2",
            "x": 0.59,
            "y": -0.18,
            "neighbors": [
                "dd68135b-9e1e-45df-9a32-b3504cf80912"
            ],
            "room": "B222"
        },
        {
            "id": "e88cab52-d53f-40e5-94f6-7e99f2b9ace1",
            "x": 0.63,
            "y": -0.13,
            "neighbors": [
                "dd68135b-9e1e-45df-9a32-b3504cf80912",
                "4520b0ba-83c9-4269-be35-18915f834c72",
                "e57173fa-908f-4c7c-97a1-01f9cc3ee8dc"
            ]
        },
        {
            "id": "e57173fa-908f-4c7c-97a1-01f9cc3ee8dc",
            "x": 0.63,
            "y": -0.18,
            "neighbors": [
                "e88cab52-d53f-40e5-94f6-7e99f2b9ace1"
            ],
            "room": "B223"
        },
        {
            "id": "4520b0ba-83c9-4269-be35-18915f834c72",
            "x": 0.77,
            "y": -0.13,
            "neighbors": [
                "e88cab52-d53f-40e5-94f6-7e99f2b9ace1",
                "e2eab9ca-3193-4ae7-8364-7f6027d1d84a",
                "21e24788-5aa0-489f-90ce-cf9317c12be0"
            ]
        },
        {
            "id": "21e24788-5aa0-489f-90ce-cf9317c12be0",
            "x": 0.77,
            "y": -0.08,
            "neighbors": [
                "4520b0ba-83c9-4269-be35-18915f834c72"
            ]
        },
        {
            "id": "e2eab9ca-3193-4ae7-8364-7f6027d1d84a",
            "x": 0.81,
            "y": -0.14,
            "neighbors": [
                "4520b0ba-83c9-4269-be35-18915f834c72",
                "3c8dba0c-fc7f-4e29-b4d2-a7baceafcbf2",
                "603772e4-7d4f-4e4e-9209-a509eb0da6a6"
            ]
        },
        {
            "id": "603772e4-7d4f-4e4e-9209-a509eb0da6a6",
            "x": 0.81,
            "y": -0.18,
            "neighbors": [
                "e2eab9ca-3193-4ae7-8364-7f6027d1d84a"
            ],
            "room": "B224"
        },
        {
            "id": "cb9955b2-29b2-4248-aa6f-fa2fef01f3fd",
            "x": 0.87,
            "y": -0.09,
            "neighbors": [
                "3c8dba0c-fc7f-4e29-b4d2-a7baceafcbf2"
            ]
        },
        {
            "id": "3c8dba0c-fc7f-4e29-b4d2-a7baceafcbf2",
            "x": 0.87,
            "y": -0.14,
            "neighbors": [
                "e2eab9ca-3193-4ae7-8364-7f6027d1d84a",
                "b52c43fa-470d-4b82-a21a-6fe25b12ef44",
                "cb9955b2-29b2-4248-aa6f-fa2fef01f3fd",
                "6c4a3b8f-e2c5-4643-9e85-ae6b728f2682"
            ]
        },
        {
            "id": "6c4a3b8f-e2c5-4643-9e85-ae6b728f2682",
            "x": 0.87,
            "y": -0.18,
            "neighbors": [
                "3c8dba0c-fc7f-4e29-b4d2-a7baceafcbf2"
            ]
        },
        {
            "id": "9781ee62-04f0-4e4b-bb2c-9a4eabd35886",
            "x": -0.22,
            "y": 0.28,
            "neighbors": [
                "29af6f52-c457-47fb-90a2-bb21af62c452"
            ],
            "room": "B210"
        },
        {
            "id": "29af6f52-c457-47fb-90a2-bb21af62c452",
            "x": -0.18,
            "y": 0.27,
            "neighbors": [
                "e2e0b972-873d-4568-bf72-732abc492a40",
                "cf6ffc19-a4b7-4630-bd27-b465fc398711",
                "9781ee62-04f0-4e4b-bb2c-9a4eabd35886",
                "44ae241d-2167-4343-a486-060b5e0ba3a3"
            ]
        },
        {
            "id": "44ae241d-2167-4343-a486-060b5e0ba3a3",
            "x": -0.13,
            "y": 0.28,
            "neighbors": [
                "29af6f52-c457-47fb-90a2-bb21af62c452"
            ]
        },
        {
            "id": "cf6ffc19-a4b7-4630-bd27-b465fc398711",
            "x": -0.18,
            "y": 0.5,
            "neighbors": [
                "29af6f52-c457-47fb-90a2-bb21af62c452",
                "5da34e1e-4032-4adb-8f18-96c8e11befc4",
                "0958723f-a6b8-49f9-a8b6-ce0abe4f90c7"
            ]
        },
        {
            "id": "0958723f-a6b8-49f9-a8b6-ce0abe4f90c7",
            "x": -0.13,
            "y": 0.51,
            "neighbors": [
                "cf6ffc19-a4b7-4630-bd27-b465fc398711"
            ],
            "room": "B234"
        },
        {
            "id": "a734c565-a082-40a2-b07b-f058b5bb7b90",
            "x": -0.22,
            "y": 0.83,
            "neighbors": [
                "5da34e1e-4032-4adb-8f18-96c8e11befc4"
            ],
            "room": "B212"
        },
        {
            "id": "5da34e1e-4032-4adb-8f18-96c8e11befc4",
            "x": -0.17,
            "y": 0.82,
            "neighbors": [
                "cf6ffc19-a4b7-4630-bd27-b465fc398711",
                "7e0a73ed-d1ae-495b-a0ed-82e00dff974f",
                "a734c565-a082-40a2-b07b-f058b5bb7b90",
                "dc925947-58ab-4012-b924-f6a3b39443ec"
            ]
        },
        {
            "id": "dc925947-58ab-4012-b924-f6a3b39443ec",
            "x": -0.13,
            "y": 0.83,
            "neighbors": [
                "5da34e1e-4032-4adb-8f18-96c8e11befc4"
            ],
            "room": "B235"
        },
        {
            "id": "7e0a73ed-d1ae-495b-a0ed-82e00dff974f",
            "x": -0.17,
            "y": 0.91,
            "neighbors": [
                "5da34e1e-4032-4adb-8f18-96c8e11befc4",
                "a46aad13-e1c9-4c06-aae0-529c5e5adbfd"
            ]
        },
        {
            "id": "a46aad13-e1c9-4c06-aae0-529c5e5adbfd",
            "x": -0.23,
            "y": 0.92,
            "neighbors": [
                "7e0a73ed-d1ae-495b-a0ed-82e00dff974f"
            ],
            "detail": "stair B1"
        },
        {
            "id": "78ee7b79-211e-486f-be09-3fbaa05fa338",
            "x": -0.23,
            "y": 0.16,
            "neighbors": [
                "e2e0b972-873d-4568-bf72-732abc492a40"
            ],
            "detail": "bathroom boys"
        },
        {
            "id": "e2e0b972-873d-4568-bf72-732abc492a40",
            "x": -0.18,
            "y": 0.17,
            "neighbors": [
                "29af6f52-c457-47fb-90a2-bb21af62c452",
                "78ee7b79-211e-486f-be09-3fbaa05fa338"
            ]
        },
        {
            "id": "4ff44c1e-c994-4292-95e7-445ae5340d51",
            "x": -0.23,
            "y": 0.02,
            "neighbors": [
                "0c23017b-166f-44b7-95c6-08a9611037f1"
            ],
            "detail": "bathroom girls"
        },
        {
            "id": "0c23017b-166f-44b7-95c6-08a9611037f1",
            "x": -0.18,
            "y": 0.02,
            "neighbors": [
                "6e6abe86-63ed-4b88-ac39-db980982d417",
                "4ff44c1e-c994-4292-95e7-445ae5340d51"
            ]
        },
        {
            "id": "16766945-ceb1-42b6-8afa-403593de9cb4",
            "x": -0.18,
            "y": 0.09,
            "neighbors": [
                "91f2aa4a-9ac8-4607-83ae-05afe068ccdc",
                "e2e0b972-873d-4568-bf72-732abc492a40",
                "0c23017b-166f-44b7-95c6-08a9611037f1"
            ]
        },
        {
            "id": "91f2aa4a-9ac8-4607-83ae-05afe068ccdc",
            "x": -0.26,
            "y": 0.09,
            "neighbors": [
                "16766945-ceb1-42b6-8afa-403593de9cb4"
            ],
            "detail": "elevator"
        }
    ]
}""")

    println(mapDirections(nod.first { if (it is MetadataNode) { it.metadata.getIdentifier() == "B216" } }).joinToString(separator = "\n"))
}