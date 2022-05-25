package us.techhigh.maps.nodes

import kotlinx.serialization.Transient

@kotlinx.serialization.Serializable
class FlooredNode(@Transient private val _neighbors: MutableList<DirectionNode> = mutableListOf(), @Transient private val _position: Pair<Double, Double> = 0.0 to 0.0, @Transient private val _id: String = "", val type: String, var floor: Int = 0) : DirectionNode(_neighbors, _id, _position)
