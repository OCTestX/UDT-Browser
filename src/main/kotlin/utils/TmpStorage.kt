package utils

object TmpStorage {
    private val storage = mutableMapOf<String, Any?>()
    fun <T> store(value: T?): String {
        val key = getOnlyId()
        storage[key] = value
        return key
    }

    fun <T> retrieve(key: String, classType: Class<T>): T? {
        return storage[key] as T?
    }
}