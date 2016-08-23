package iuliiaponomareva.eventum;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.webkit.WebView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkArgument;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewPageActivityTest {

    @Rule
    public ActivityTestRule<ViewPageActivity> rule = new ActivityTestRule<>(
            ViewPageActivity.class, true, false);

    @Test
    public void testLoadedUrl() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.NEWS_LINK, "about:blank");
        rule.launchActivity(intent);
        onView(withId(R.id.full_news)).check(matches(withOriginalUrl("about:blank")));
    }

    @Test
    public void testCopyingUrl() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.NEWS_LINK, "about:blank");
        rule.launchActivity(intent);
        onView(withId(R.id.copyLink)).perform(click());

        //test Toast
        onView(withText(startsWith(rule.getActivity().getString(R.string.link_copied)))).
                inRoot(withDecorView(
                        not(is(rule.getActivity().
                                getWindow().getDecorView())))).
                check(matches(isDisplayed()));

        ClipboardManager clipboardManager = (ClipboardManager) rule.getActivity().
                getSystemService(Context.CLIPBOARD_SERVICE);
        checkArgument(clipboardManager.hasPrimaryClip(), "Nothing copied");
        ClipData clipData = clipboardManager.getPrimaryClip();
        onView(withId(R.id.full_news)).check(
                matches(withUrl(clipData.getItemAt(0).getText().toString())));
    }

    public static Matcher<View> withOriginalUrl(String url) {
        checkArgument(!(url == null));
        return withOriginalUrl(is(url));
    }

    public static Matcher<View> withOriginalUrl(final Matcher<String> matcherText) {
        checkNotNull(matcherText);
        return new BoundedMatcher<View, WebView>(WebView.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("with URL: " + matcherText);
            }

            @Override
            protected boolean matchesSafely(WebView webView) {
                return matcherText.matches(webView.getOriginalUrl());
            }
        };
    }

    public static Matcher<View> withUrl(String url) {
        checkArgument(!(url == null));
        return withUrl(is(url));
    }

    public static Matcher<View> withUrl(final Matcher<String> matcherText) {
        checkNotNull(matcherText);
        return new BoundedMatcher<View, WebView>(WebView.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("with URL: " + matcherText);
            }

            @Override
            protected boolean matchesSafely(WebView webView) {
                return matcherText.matches(webView.getUrl());
            }
        };
    }
}
