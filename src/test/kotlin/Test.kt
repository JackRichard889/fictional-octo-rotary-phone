import kotlin.test.Test

internal class NavTests {
    @Test
    fun manyNavigationTests() {
        repeat(500) {
            val s = BuildingB.floors.flatten().filterIsInstance<MetadataNode>().random()
            val e = BuildingB.floors.flatten().filterIsInstance<MetadataNode>().random()

            if (s != e) {
                assert(BuildingB.paths(s.metadata.getIdentifier(), e.metadata.getIdentifier()).last().id == e.id)
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