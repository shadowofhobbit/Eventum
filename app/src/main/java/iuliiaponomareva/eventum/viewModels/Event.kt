package iuliiaponomareva.eventum.viewModels

/**
 *  A wrapper for events in LiveData
 */
class Event<out T>(private val content: T) {

    private var handled = false

    fun getEventIfNotHandled(): T? {
        return if (!handled) {
            handled = true
            content
        } else {
            null
        }
    }
}