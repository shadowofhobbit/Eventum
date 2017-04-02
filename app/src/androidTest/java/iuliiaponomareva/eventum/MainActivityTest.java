package iuliiaponomareva.eventum;


import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.view.inputmethod.EditorInfo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import iuliiaponomareva.eventum.activities.MainActivity;
import iuliiaponomareva.eventum.data.DbHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasImeAction;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

public class MainActivityTest {
    @Before
    public void deleteDB() {
        InstrumentationRegistry.getTargetContext().deleteDatabase(DbHelper.DATABASE_NAME);
    }

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(
            MainActivity.class);

    @Test
    public void testEmptyView() {
        onView(withId(R.id.empty_textview)).check(matches(isDisplayed()));
        onView(withId(R.id.empty_textview)).check(
                matches(withText(R.string.no_feeds_added_yet)));
        onView(withId(R.id.all_news)).check(matches(not(isDisplayed())));

    }

    @Test
    public void testAddingFeed() {
        onView(withId(R.id.addFeedItem)).perform(click());
        onView(withId(R.id.new_feed_url)).check(matches(isDisplayed()));
        onView(withId(R.id.new_feed_url)).check(matches(hasImeAction(EditorInfo.IME_ACTION_DONE)));
    }

    @Test
    public void testRemovingFeed() {
        onView(withId(R.id.removeFeedItem)).perform(click());
        onView(withId(R.id.no_feeds_view)).check(matches(isDisplayed()));
        onView(withId(R.id.feeds_to_remove)).check(matches(not(isDisplayed())));

    }

}