package us.techhigh.maps

import us.techhigh.maps.buildings.*
import us.techhigh.maps.nodes.MetadataNode

object School {
    private val buildings = listOf(
        BuildingB,
        BuildingC,
        BuildingD
    )


    init {
        // TODO: connect all buildings and floors
    }

    fun findNode(node: String) = buildings.map { it.floors }.flatten().flatten().first { it.id == node || if (it is MetadataNode) it.metadata.getIdentifier() == node else false }
}