package us.techhigh.maps.nodes

@kotlinx.serialization.Serializable
actual class FlooredNode(@kotlinx.serialization.Transient private val _neighbors: MutableList<DirectionNode> = mutableListOf(), @kotlinx.serialization.Transient private val _position: Pair<Double, Double> = 0.0 to 0.0, @kotlinx.serialization.Transient private val _id: String = "", val type: String, var floor: Int = 0) : DirectionNode(_neighbors, _id, _position)
