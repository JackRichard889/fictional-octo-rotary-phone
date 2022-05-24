package us.techhigh.maps

import kotlin.test.Test

internal class NavTests {
    @Test
    fun manyNavigationTests() {
        repeat(500) {
            val s = BuildingB.floors.flatten().filterIsInstance<MetadataNode>().random()
            val e = BuildingB.floors.flatten().filterIsInstance<MetadataNode>().random()

            println("Testing navigation from $s to $e.")

            if (s != e) {
                assert(BuildingB.paths(s.metadata.getIdentifier(), e.metadata.getIdentifier()).last().id == e.id)
                println("Successful navigation!")
            }
        }

        repeat(500) {
            val s = BuildingC.floors.flatten().filterIsInstance<MetadataNode>().random()
            val e = BuildingC.floors.flatten().filterIsInstance<MetadataNode>().random()

            if (s != e) {
                assert(BuildingC.paths(s.metadata.getIdentifier(), e.metadata.getIdentifier()).last().id == e.id)
            }
        }
    }

}