import java.io.File

class Dictionary(mainFileName: String) {
    val mainFile: File = File("src/main/kotlin/$mainFileName")
    val backUp: File = File("src/main/kotlin/back-up.txt")
    val emptyFile: File = File("src/main/kotlin/empty-file.txt")
    val impossibleLettersList: MutableList<String> = mutableListOf()
    val impossibleStartLettersList: MutableList<String> = mutableListOf()
    val impossibleEndLettersList: MutableList<String> = mutableListOf()

    /**
     * Creates new File with [fileName] located in [pathName].
     */
    fun createFile(fileName: String, pathName: String = "src/main/kotlin/"): File = File(pathName + fileName)

    /**
     * Counts the number of words in [file] and returns that value.
     */
    fun size(file: File): Int {
        var list: MutableList<String> = mutableListOf()
        file.forEachLine { list.add(it) }
        return list.size
    }
    /**
     * Updates [backUp] file with [mainFile].
     */
    fun updateBackUp() {
        mainFile.copyTo(backUp, true)
    }

    /**
     * Updates [mainFile] back to its previous version [backUp].
     */
    fun restoreBackUp() {
        backUp.copyTo(mainFile, true)
    }

    /**
     * Removes all content in [file].
     */
    fun emptyFile(file: File) {
        emptyFile.copyTo(file, true)
    }

    /**
     * Adds new contents from [newFile] to [file].
     */
    fun updateFile(file: File, newFile: File) {
        updateBackUp()
        var list: MutableList<String> = mutableListOf()
        copyFileToList(file, list)
        newFile.forEachLine { if (!list.contains(it) && onlyLetters(it)) list.add(it.lowercase()) }
        val newList: List<String> = list.sortedBy { it }.distinct()
        emptyFile(file)
        copyListToFile(file, listToMutable(newList))
    }

    /**
     * Adds all values from [file] to [list].
     */
    fun copyFileToList(file: File, list: MutableList<String>) {
        file.forEachLine { list.add(it) }
    }

    /**
     * Adds all values from [list] to [file].
     */
    fun copyListToFile(file: File, list: MutableList<String>) {
        for (item in list) file.appendText(if (item == list[0]) item else "\n$item")
    }

    fun listToMutable(list: List<String>): MutableList<String> {
        val mutableList: MutableList<String> = mutableListOf()
        for (index in list.indices) mutableList.add(list[index])
        return mutableList
    }

    /**
     * Returns whether [word] contains only letters.
     */
    fun onlyLetters(word: String): Boolean {
        for (char in word) if (!char.isLetter()) return false
        return true
    }

    /**
     * Prints all elements of [file].
     */
    fun printFile(file: File) {
        var list: MutableList<String> = mutableListOf()
        copyFileToList(file, list)
        println(list)
    }

    /**
     * Strings together the values of [list] and returns it as a string.
     */
    fun toString(list: MutableList<Char>): String {
        var str: String = ""
        for (char in list) str += char
        return str
    }

    /**
     * Takes [word] and returns an updated version of [list] of possible permutations [word] can be rearranged into
     * from [minLettersPermutated] to [wordSize] characters.
     * Constant [k] is used to keep track of which character in [word] is being permutated.
     */
    fun findPermutations(list: MutableList<Char> = mutableListOf(), word: String, wordSize: Int = word.length,
                         minLettersPermutated: Int, k: Int = 1): MutableList<String> {
        var newList: MutableList<String> = mutableListOf()

        if (k > minLettersPermutated) newList.add(toString(list))
        if (k > wordSize) {
            list.remove(list[list.size - 1])
            return newList
        }

        for (char in word) {
            list.add(char)
            for (word in findPermutations(list,
                word.substring(if (word.indexOf(char) == word.length - 1) 0
                else 0, word.indexOf(char)) + word.substring(word.indexOf(char) + 1),
                wordSize, minLettersPermutated, k + 1)) {
                newList.add(word)
            }
        }

        if (list.isNotEmpty())
            list.remove(list[list.size - 1])
        return listToMutable(newList.sortedBy { it.length }.sortedBy { it }.distinct())
    }

    /**
     * Removes words from [list] that likely aren't real words.
     */
    fun filterNonWords(list: MutableList<String>) {
        var removeList: MutableList<String> = mutableListOf()

        for (word in list) {
            for (item in impossibleLettersList)
                if (word.contains(item)
                    || (!word.contains("a", true)
                            && !word.contains("e", true)
                            && !word.contains("i", true)
                            && !word.contains("o", true)
                            && !word.contains("u", true)
                            && !word.contains("y", true))) {
                    removeList.add(word)
                    break
                }

            if (!removeList.contains(word))
                for (item in impossibleStartLettersList)
                    if (word.startsWith(item)) {
                        removeList.add(word)
                        break
                    }

            if (!removeList.contains(word))
                for (item in impossibleEndLettersList)
                    if (word.endsWith(item)) {
                        removeList.add(word)
                        break
                    }
        }

        println(removeList)

        for (word in removeList)
            if (list.contains(word))
                list.remove(word)
    }

    /**
     * Finds all possible words with at least [minLetters] in [word] and adds them to [file].
     */
    fun findPossibleRealWords(file: File, word: String, minLetters: Int = 3) {
        val impossibleLetterCombos = File("src/main/kotlin/impossible-letter-combos.txt")
        val impossibleStartLetters = File("src/main/kotlin/impossible-start-letters.txt")
        val impossibleEndLetters = File("src/main/kotlin/impossible-end-letters.txt")
        copyFileToList(impossibleLetterCombos, impossibleLettersList)
        copyFileToList(impossibleStartLetters, impossibleStartLettersList)
        copyFileToList(impossibleEndLetters, impossibleEndLettersList)
        var list: MutableList<String> = findPermutations(word = word, minLettersPermutated = minLetters)
        copyFileToList(file, list)
        filterNonWords(list)
        emptyFile(file)
        copyListToFile(file, list)
    }
}

fun main() {
    val dict: Dictionary = Dictionary("dictionary-large.txt")
    var dictionary = File("src/main/kotlin/dictionary.txt")
    println("Size Before: ${dict.size(dict.mainFile)}")
    dict.updateFile(dict.mainFile, dictionary)
    println("Size After: ${dict.size(dict.mainFile)}")
}