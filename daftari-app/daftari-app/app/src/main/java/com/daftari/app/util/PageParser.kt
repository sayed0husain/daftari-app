package com.daftari.app.util

/**
 * Parses a user-entered pages string and returns the set of page numbers it represents.
 * Supports:
 *  - Ranges: "10-25" or "10 الى 25" style dashes
 *  - Separate pages: "3,7,12"
 *  - Mixed: "3,10-15,20"
 * Any non-numeric segment is ignored so free text doesn't crash the parser.
 */
object PageParser {

    fun parsePages(input: String): Set<Int> {
        if (input.isBlank()) return emptySet()
        val result = mutableSetOf<Int>()
        val segments = input.split(",", "،")
        for (rawSegment in segments) {
            val segment = rawSegment.trim()
            if (segment.isEmpty()) continue
            val rangeParts = segment.split("-", "–", "إلى").map { it.trim() }
            if (rangeParts.size == 2) {
                val start = rangeParts[0].toIntOrNull()
                val end = rangeParts[1].toIntOrNull()
                if (start != null && end != null && start <= end) {
                    result.addAll(start..end)
                    continue
                }
            }
            segment.toIntOrNull()?.let { result.add(it) }
        }
        return result
    }

    fun countMissingPages(input: String): Int = parsePages(input).size
}
