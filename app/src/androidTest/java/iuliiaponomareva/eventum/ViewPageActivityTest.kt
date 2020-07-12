package iuliiaponomareva.eventum

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.webkit.WebView
import androidx.core.util.Preconditions
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import iuliiaponomareva.eventum.activities.MainActivity
import iuliiaponomareva.eventum.activities.ViewPageActivity
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.core.Is
import org.hamcrest.core.IsNot
import org.hamcrest.core.StringStartsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class ViewPageActivityTest {
    @get:Rule
    var rule = ActivityTestRule(
        ViewPageActivity::class.java, true, false
    )

    @Test
    fun testLoadedUrl() {
        val intent = Intent()
        intent.putExtra(MainActivity.NEWS_LINK, "about:blank")
        rule.launchActivity(intent)
        Espresso.onView(ViewMatchers.withId(R.id.webView))
            .check(ViewAssertions.matches(withOriginalUrl("about:blank")))
    }

    @Test
    fun testCopyingUrl() {
        val intent = Intent()
        intent.putExtra(MainActivity.NEWS_LINK, "about:blank")
        rule.launchActivity(intent)
        Espresso.onView(ViewMatchers.withId(R.id.copyLink)).perform(ViewActions.click())

        //test Toast
        Espresso.onView(
            ViewMatchers.withText(
                StringStartsWith.startsWith(
                    rule.activity.getString(R.string.link_copied)
                )
            )
        ).inRoot(
            RootMatchers.withDecorView(
                IsNot.not(
                    Is.`is`(
                        rule.activity.window.decorView
                    )
                )
            )
        ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        val clipboardManager = rule.activity
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        Preconditions.checkArgument(
            Objects.requireNonNull(
                clipboardManager
            ).hasPrimaryClip(), "Nothing copied"
        )
        val clipData = clipboardManager.primaryClip!!
        Espresso.onView(ViewMatchers.withId(R.id.webView)).check(
            ViewAssertions.matches(
                withUrl(
                    clipData.getItemAt(0).text.toString()
                )
            )
        )
    }

    companion object {
        fun withOriginalUrl(url: String?): Matcher<View> {
            Preconditions.checkArgument(url != null)
            return withOriginalUrl(Is.`is`(url))
        }

        private fun withOriginalUrl(matcherText: Matcher<String?>): Matcher<View> {
            Preconditions.checkNotNull(
                matcherText
            )
            return object : BoundedMatcher<View, WebView>(WebView::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("with URL: $matcherText")
                }

                override fun matchesSafely(webView: WebView): Boolean {
                    return matcherText.matches(webView.originalUrl)
                }
            }
        }

        fun withUrl(url: String?): Matcher<View> {
            Preconditions.checkArgument(url != null)
            return withUrl(Is.`is`(url))
        }

        private fun withUrl(matcherText: Matcher<String?>): Matcher<View> {
            Preconditions.checkNotNull(
                matcherText
            )
            return object : BoundedMatcher<View, WebView>(WebView::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("with URL: $matcherText")
                }

                override fun matchesSafely(webView: WebView): Boolean {
                    return matcherText.matches(webView.url)
                }
            }
        }
    }
}