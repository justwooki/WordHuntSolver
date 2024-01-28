import java.io.File
import kotlin.math.*

fun main() {
    provideWords()
}

const val NUM_LETTERS_IN_ALPHABET = 26

/**
 * The minimum size of words to include in word hunt.
 */
const val MINIMUM_WORD_SIZE = 3

/**
 * A solver for finding words built from words in [wordList].
 */
class WordHuntSolver(val wordList: List<String>, val minLength: Int = MINIMUM_WORD_SIZE, val boardSize: Int = 4) {
    /**
     * The position of each character on the board is represented by its [row] and [col].
     */
    class Position(row: Int, col: Int) {
        val row: Int = row
        val col: Int = col
    }

    // This list is cleared in getWords(), populated in findWords(),
    // and returned by getWords().
    private val words = mutableListOf<String>()

    private var board: MutableList<MutableList<Char>> = mutableListOf()

    private fun posExists(pos: Position): Boolean =
        pos.row in 0 until boardSize && pos.col in 0 until boardSize

    private fun posIsAvailable(pos: Position, currPath: MutableList<Position>): Boolean {
        for (i in currPath) {
            if (i.row == pos.row && i.col == pos.col) {
                return false
            }
        }
        return true
    }

    private fun pathTest(pos: Position, currPath: MutableList<Position>, targetedChar: Char): Boolean =
        posExists(pos) && posIsAvailable(pos, currPath) && board[pos.row][pos.col] == targetedChar

    private fun findPath(currPos: Position, currPath: MutableList<Position>, targetedChar: Char): MutableList<Position> {
        var list: MutableList<Position> = mutableListOf()
        var possiblePositions: List<Position> = listOf(Position(currPos.row, currPos.col - 1),
            Position(currPos.row, currPos.col + 1), Position(currPos.row - 1, currPos.col),
            Position(currPos.row + 1, currPos.col), Position(currPos.row - 1, currPos.col + 1),
            Position(currPos.row - 1, currPos.col - 1), Position(currPos.row + 1, currPos.col + 1),
            Position(currPos.row + 1, currPos.col - 1))

        for (pos in possiblePositions) {
            if (pathTest(pos, currPath, targetedChar)) {
                list.add(pos)
            }
        }

        return list
    }

    private fun wordExists(word: String, path: MutableList<Position> = mutableListOf()): Boolean {
        if (word.isEmpty()) {
            return true
        } else {
            outerLoop@ for (char in word) {
                var charInstances: MutableList<Position> = mutableListOf()
                if (path.isEmpty()) {
                    for (row in 0 until boardSize) {
                        for (col in 0 until boardSize) {
                            if (board[row][col] == char) {
                                charInstances.add(Position(row, col))
                            }
                        }
                    }
                } else {
                    charInstances = findPath(path[path.size - 1], path, char)
                }
                if (charInstances.isEmpty()) {
                    break@outerLoop
                } else {
                    for (instance in charInstances) {
                        path.add(instance)
                        if (wordExists(if (word.length > 1) word.substring(1) else "", path)) {
                            return true
                        }
                    }
                }
            }

            return false
        }
    }

    private fun findWords(boardLetters: String) {
        var iteration: Int = boardSize
        for (row in 1..boardSize) {
            var cols: MutableList<Char> = mutableListOf()
            for (char in boardLetters.substring(iteration - 4, iteration)) {
                cols.add(char)
            }
            board.add(cols)
            iteration += boardSize
        }

        for (word in wordList) {
            if (word.length >= minLength && wordExists(word)) {
                words.add(word)
            }
        }
    }

    /**
     * Gets all the [words] of [str] that can be made using
     * the words in [wordList].
     */
    fun getWords(str: String): List<String> {
        words.clear()
        board.clear()
        findWords(str.lowercase())
        return words
    }
}

fun provideWords() {
    val wordList: List<String> = File("src/main/kotlin/dictionary-large.txt").readLines()
    val solver: WordHuntSolver = WordHuntSolver(wordList)

    while (true) {
        print("Enter text to word hunt (or 'exit' to exit): ")
        val input = readln()
        if (input == "exit") return
        if (input.length.toDouble() == solver.boardSize.toDouble().pow(2)) {
            val results = solver.getWords(input)
            if (results.isEmpty()) {
                println("No words were found for '$input'.")
            } else {

                val text = if (results.size == 1) "One word of '$input' was found:"
                else "${results.size} words of '${input}' were found:"
                println(text)
                println(results.sortedBy { it.length}.joinToString(separator = "\n"))
            }
            println()
        }

    }
}