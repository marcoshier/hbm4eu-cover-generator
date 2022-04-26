import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

class PeerScraper(file: String) {

    private val rows = csvReader().readAll(File(file)).drop(1)
    private val rows2 = csvReader().readAll(File("data/peer2.csv"))

    val rowsNumber = rows.size

    val titles = rows.map { it[0] }
    val authors = rows.map { it[1] }
    val date = rows.map { listOf(it[2]) }
    val keywords = rows.map { listOf(it[3]) }
    val urls = rows.map { it[6] }


    val titles2 = rows2.map { it[3] }

    fun checkDuplicates(): MutableSet<String>{
/*

        val seen: MutableSet<String> = mutableSetOf()
        val duplicates: MutableSet<String> = mutableSetOf()

        for (i in titles + titles2) {
            if (!seen.add(i)) {
                duplicates.add(i)
            }
        }
*/


        return (titles + titles2).distinct().toMutableSet()
    }

}


// SORTERS

fun MutableList<List<Pair<String, String>>>.byStart(): List<Pair<String, String>> {
    return map {
        val pair = it[0]
        pair
    }.sortedBy { it.second }
}
