package utils

class ListItemIterable<T>(private val items: List<T>, private val initIndex: Int = 0) {
    init {
        if (initIndex < 0 || initIndex >= items.size) {
            throw IndexOutOfBoundsException("Initial index is out of bounds")
        }
    }
    private var _currentIndex = initIndex
    val currentIndex: Int get() = _currentIndex

    fun next(): T {
        if (items.isEmpty()) throw NoSuchElementException("List is empty")
        if (_currentIndex < items.size - 1) {
            _currentIndex++
        } else {
            _currentIndex = 0
        }
        return items[_currentIndex]
    }
}