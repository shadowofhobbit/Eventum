package iuliiaponomareva.eventum.viewModels

/**
 *  A wrapper for events in LiveData
 */
class Event<out T>(private val content: T) {

    var handled = false
    private set

    fun getEventIfNotHandled(): T? {
        return if (!handled) {
            handled = true
            content
        } else {
            null
        }
    }

    fun getEvent(): T {
        handled = true
        return content
    }


}