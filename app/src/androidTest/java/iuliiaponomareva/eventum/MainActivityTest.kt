package iuliiaponomareva.eventum

import android.view.inputmethod.EditorInfo
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import iuliiaponomareva.eventum.activities.MainActivity
import iuliiaponomareva.eventum.data.ReaderDatabase
import org.hamcrest.core.IsNot
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @Before
    fun deleteDB() {
        InstrumentationRegistry.getInstrumentation().targetContext
            .deleteDatabase(ReaderDatabase.DATABASE_NAME)
    }

    @get:Rule
    var rule = ActivityTestRule(
        MainActivity::class.java
    )

    @Test
    fun testEmptyView() {
        Espresso.onView(ViewMatchers.withId(R.id.emptyView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.emptyView)).check(
            ViewAssertions.matches(ViewMatchers.withText(R.string.no_feeds_added_yet))
        )
        Espresso.onView(ViewMatchers.withId(R.id.newsRecyclerView))
            .check(ViewAssertions.matches(IsNot.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun testAddingFeed() {
        Espresso.onView(ViewMatchers.withId(R.id.addFeedItem)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.newFeedEditText))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.newFeedEditText))
            .check(ViewAssertions.matches(ViewMatchers.hasImeAction(EditorInfo.IME_ACTION_DONE)))
    }

    @Test
    fun testRemovingFeed() {
        Espresso.onView(ViewMatchers.withId(R.id.removeFeedItem)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.noFeedsView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.feedsListView))
            .check(ViewAssertions.matches(IsNot.not(ViewMatchers.isDisplayed())))
    }
}