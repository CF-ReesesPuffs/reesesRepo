package com.cfreesespuffs.github.giftswapper.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.cfreesespuffs.github.giftswapper.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class LoginTest {

    @Test
    fun isLoginActivityInView() {
        val activityScenario = ActivityScenario.launch(Login::class.java)
        onView(withId(R.id.login)).check(matches(isDisplayed()))
    }

    @Test
    fun canInputUserNameField() {
        val theString = "name"
        val activityScenario = ActivityScenario.launch(Login::class.java)
        val appCompatEditText = onView(withId(R.id.usernameLogin))
        appCompatEditText.perform(replaceText(theString))
        onView(withId(R.id.usernameLogin)).check(matches(withText("name")))
        appCompatEditText.check(matches(withText("name")))
        appCompatEditText.check(matches(withText(theString)))
    }

    @Test
    fun canInputPasswordField() {
        val activityScenario = ActivityScenario.launch(Login::class.java)
        val passwordTextField = onView(withId(R.id.passwordLogin))
        passwordTextField.perform(replaceText("password"))
        passwordTextField.check(matches(withText("password")))
    }

    @Test
    fun loginExists() {
        val activityScenario = ActivityScenario.launch(Login::class.java)
        onView(withId(R.id.loginButton)).check(matches(withText("Login")))
    }

    @Test
    fun toSignUpExists() {
        val activityScenario = ActivityScenario.launch(Login::class.java)
        onView(withId(R.id.toSignUp)).check(matches(withText("Sign Up")))
    }

    @Test
    fun loginClickToast() {
        val activityScenario = ActivityScenario.launch(Login::class.java)

        onView(withText("Login")).perform(click())
//        onView(withText("Please check your email and username are correct.")).inRoot(withDecor)
//
//        onView(withText("Please check your email and username are correct."))
//
//       "Please check your email and username are correct."
//        onView(withText("Please check your email and username are correct.")).inRoot(ToastMatcher2().apply{ matches(
//            isDisplayed())})
    }
}