package dev.eaji

/**
 * Calculates the visual column width of a string, taking the editor's tab size into account.
 */
fun String.visualColumnWidth(tabSize: Int): Int {
    var col = 0
    for (char in this) {
        if (char == '\t') {
            col += tabSize - (col % tabSize)
        } else {
            col++
        }
    }
    return col
}