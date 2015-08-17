/*
 * Copyright (c) 2015, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of Salesforce.com, Inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.desk.android.sdk.widget;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.TextView;

import com.desk.android.sdk.Desk;
import com.desk.android.sdk.config.BaseContactUsConfig;
import com.desk.android.sdk.error.IncompleteFormException;
import com.desk.android.sdk.identity.UserIdentity;
import com.desk.android.sdk.model.CreateCaseRequest;
import com.desk.android.sdk.test.R;
import com.desk.android.sdk.util.DeskDefaultsRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.desk.android.sdk.util.TestUtils.getString;
import static com.desk.android.sdk.util.TestUtils.inflateView;
import static org.assertj.android.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ContactUsView}
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ContactUsViewTest {

    private ContactUsView.FormListener mockFormListener;
    private ContactUsView contactUsView;

    @ClassRule
    public static DeskDefaultsRule resetRule = new DeskDefaultsRule();

    @Before
    public void setUp() throws Exception {
        mockFormListener = mock(ContactUsView.FormListener.class);
        contactUsView = getNewContactUsView();
    }

    @Test
    public void viewHasVerticalOrientation() throws Exception {
        assertThat(contactUsView).isVertical();
    }

    // region FormListener Tests

    @Test
    public void onFormValidCalledWhenFormValid() throws Exception {

        // this sets up ContactUsView to have textChangedListeners on email and subject
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public String getSubject() {
                        // don't want a subject so the text changed listener works
                        return null;
                    }

                    @Override
                    public boolean isSubjectEnabled() {
                        return true;
                    }

                    @Override
                    public boolean isUserNameEnabled() {
                        return true;
                    }
                });

        ContactUsView contactUsView = getNewContactUsView();
        contactUsView.setFormListener(mockFormListener);
        getEmail(contactUsView).setText("valid@email.com");
        getSubject(contactUsView).setText("Valid subject");
        getFeedback(contactUsView).setText("Valid feedback");
        verify(mockFormListener, atLeast(1)).onFormValid();
    }

    @Test
    public void onFormValidCalledWhenFormInvalid() throws Exception {

        // this sets up ContactUsView to have textChangedListeners on email and subject
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public String getSubject() {
                        // don't want a subject so the text changed listener works
                        return null;
                    }

                    @Override
                    public boolean isSubjectEnabled() {
                        return true;
                    }

                    @Override
                    public boolean isUserNameEnabled() {
                        return true;
                    }
                });

        ContactUsView contactUsView = getNewContactUsView();
        contactUsView.setFormListener(mockFormListener);
        getEmail(contactUsView).setText("valid@email.com");
        getSubject(contactUsView).setText("Valid subject");
        getFeedback(contactUsView).setText(""); // empty feedback
        verify(mockFormListener, atLeast(1)).onFormInvalid();
    }

    @Test
    public void clearFormListenerClearsListener() throws Exception {
        ContactUsView contactUsView = getNewContactUsView();
        contactUsView.setFormListener(mockFormListener);
        assertNotNull(contactUsView.getFormListener());
        contactUsView.clearFormListener();
        assertNull(contactUsView.getFormListener());
    }

    // endregion

    //region Hint Tests
    @Test
    public void nameHintMatchesDefault() throws Exception {
        assertThat(getName(contactUsView)).hasHint(getString(R.string.def_user_name_hint));
    }

    @Test
    public void emailHintMatchesDefault() throws Exception {
        assertThat(getEmail(contactUsView)).hasHint(getString(R.string.def_user_email_hint));
    }

    @Test
    public void subjectHintMatchesDefault() throws Exception {
        assertThat(getSubject(contactUsView)).hasHint(getString(R.string.def_subject_hint));
    }

    @Test
    public void feedbackHintMatchesDefault() throws Exception {
        assertThat(getFeedback(contactUsView)).hasHint(getString(R.string.def_user_feedback_hint));
    }

    @Test
    public void nameHintMatchesValueFromLayoutAttrs() throws Exception {
        ContactUsView contactUsView = inflateView(R.layout.contact_us_view_with_attributes);
        assertThat(getName(contactUsView)).hasHint(getString(R.string.test_attr_name_hint));
    }

    @Test
    public void emailHintMatchesValueFromLayoutAttrs() throws Exception {
        ContactUsView contactUsView = inflateView(R.layout.contact_us_view_with_attributes);
        assertThat(getEmail(contactUsView)).hasHint(getString(R.string.test_attr_email_hint));
    }

    @Test
    public void subjectHintMatchesValueFromLayoutAttrs() throws Exception {
        ContactUsView contactUsView = inflateView(R.layout.contact_us_view_with_attributes);
        assertThat(getSubject(contactUsView)).hasHint(getString(R.string.test_attr_subject_hint));
    }

    @Test
    public void feedbackHintMatchesValueFromLayoutAttrs() throws Exception {
        ContactUsView contactUsView = inflateView(R.layout.contact_us_view_with_attributes);
        assertThat(getFeedback(contactUsView)).hasHint(getString(R.string.test_attr_feedback_hint));
    }

    @Test
    public void nameHintMatchesValueFromStyle() throws Exception {
        ContactUsView contactUsView = inflateView(R.layout.contact_us_view_with_style);
        assertThat(getName(contactUsView)).hasHint(getString(R.string.test_style_name_hint));
    }

    @Test
    public void emailHintMatchesValueFromStyle() throws Exception {
        ContactUsView contactUsView = inflateView(R.layout.contact_us_view_with_style);
        assertThat(getEmail(contactUsView)).hasHint(getString(R.string.test_style_email_hint));
    }

    @Test
    public void subjectHintMatchesValueFromStyle() throws Exception {
        ContactUsView contactUsView = inflateView(R.layout.contact_us_view_with_style);
        assertThat(getSubject(contactUsView)).hasHint(getString(R.string.test_style_subject_hint));
    }

    @Test
    public void feedbackHintMatchesValueFromStyle() throws Exception {
        ContactUsView contactUsView = inflateView(R.layout.contact_us_view_with_style);
        assertThat(getFeedback(contactUsView)).hasHint(getString(R.string.test_style_feedback_hint));
    }
    //endregion

    // region View Visibility Tests

    @Test
    public void userNameIsVisibleWhenEnabled() throws Exception {

        // enable user name
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public boolean isUserNameEnabled() {
                        return true;
                    }
                });

        ContactUsView view = getNewContactUsView();
        assertThat(getName(view)).isVisible();
    }

    @Test
    public void userNameIsGoneWhenDisabled() throws Exception {

        // disable user name
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public boolean isUserNameEnabled() {
                        return false;
                    }
                });

        ContactUsView view = getNewContactUsView();
        assertThat(getName(view)).isGone();
    }

    @Test
    public void emailIsGoneWhenPopulated() throws Exception {

        // populate email
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setIdentity(new UserIdentity.Builder("test@test.com").create());

        ContactUsView view = getNewContactUsView();
        assertThat(getEmail(view)).isGone();
    }

    @Test
    public void subjectIsVisibleWhenEnabled() throws Exception {

        // enable subject
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public boolean isSubjectEnabled() {
                        return true;
                    }
                });

        ContactUsView view = getNewContactUsView();
        assertThat(getSubject(view)).isVisible();
    }

    @Test
    public void subjectIsGoneWhenDisabled() throws Exception {

        // disable subject
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public boolean isSubjectEnabled() {
                        return false;
                    }
                });

        ContactUsView view = getNewContactUsView();
        assertThat(getSubject(view)).isGone();
    }

    // endregion

    // region Validation Tests

    @Test
    public void isFormValidReturnsFalseWhenEmailEmpty() throws Exception {

        // need to clear the identity
        clearIdentity();

        ContactUsView contactUsView = getNewContactUsView();
        getSubject(contactUsView).setText("Subject");
        getFeedback(contactUsView).setText("Feedback");
        getEmail(contactUsView).setText("");
        assertFalse(contactUsView.isFormValid());
    }

    @Test
    public void isFormValidReturnsFalseWhenEmailInvalid() throws Exception {

        // need to clear the identity
        clearIdentity();

        ContactUsView contactUsView = getNewContactUsView();
        getSubject(contactUsView).setText("Subject");
        getFeedback(contactUsView).setText("Feedback");
        getEmail(contactUsView).setText("asdfasfsdafa");
        assertFalse(contactUsView.isFormValid());
    }

    @Test
    public void isFormValidReturnsFalseWhenSubjectEmpty() throws Exception {
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public String getSubject() {
                        return null;
                    }
                });

        ContactUsView contactUsView = getNewContactUsView();
        getFeedback(contactUsView).setText("Feedback");
        getEmail(contactUsView).setText("valid@email.com");
        assertFalse(contactUsView.isFormValid());
    }

    @Test
    public void isFormValidReturnsFalseWhenFeedbackEmpty() throws Exception {
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public boolean isSubjectEnabled() {
                        return true;
                    }
                });

        ContactUsView contactUsView = getNewContactUsView();
        getSubject(contactUsView).setText("Subject");
        getEmail(contactUsView).setText("valid@email.com");
        getFeedback(contactUsView).setText("");
        assertFalse(contactUsView.isFormValid());
    }

    @Test
    public void isFormValidReturnsTrueWhenValid() throws Exception {

        // need to clear the identity
        clearIdentity();

        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public boolean isSubjectEnabled() {
                        return true;
                    }
                });

        ContactUsView contactUsView = getNewContactUsView();
        getSubject(contactUsView).setText("Subject");
        getFeedback(contactUsView).setText("Feedback");
        getEmail(contactUsView).setText("valid@email.com");
        assertTrue(contactUsView.isFormValid());
    }

    // endregion

    // region getRequest Tests

    @Test
    public void getRequestReturnsValidRequest() throws Exception {
        final String subject = "Valid Subject";
        final String name = "Valid Name";
        final String email = "valid@email.com";
        final String feedback = "Valid Feedback";
        final String to = "to@email.com";
        Desk.with(InstrumentationRegistry.getTargetContext())
                .setContactUsConfig(new BaseContactUsConfig(InstrumentationRegistry.getTargetContext()) {
                    @Override
                    public String getSubject() {
                        return subject;
                    }
                })
                .setIdentity(new UserIdentity.Builder(email).name(name).create());
        ContactUsView contactUsView = getNewContactUsView();
        getFeedback(contactUsView).setText(feedback);
        CreateCaseRequest request = contactUsView.getRequest(to);
        assertEquals(request.getSubject(), subject);
        assertEquals(request.getName(), name);
        assertEquals(request.getFrom(), email);
        assertEquals(request.getTo(), to);
        assertEquals(request.getBody(), feedback);
    }

    @Test(expected = IncompleteFormException.class)
    public void getRequestThrowsIncompleteFormException() {
        ContactUsView contactUsView = getNewContactUsView();
        contactUsView.getRequest("to@email.com");
    }

    // endregion

    private TextView getName(ContactUsView contactUsView) {
        return (TextView) contactUsView.findViewById(R.id.user_name);
    }

    private TextView getEmail(ContactUsView contactUsView) {
        return (TextView) contactUsView.findViewById(R.id.user_email);
    }

    private TextView getSubject(ContactUsView contactUsView) {
        return (TextView) contactUsView.findViewById(R.id.user_subject);
    }

    private TextView getFeedback(ContactUsView contactUsView) {
        return (TextView) contactUsView.findViewById(R.id.user_feedback);
    }

    private void clearIdentity() {
        Desk.with(InstrumentationRegistry.getTargetContext()).setIdentity(null);
    }

    private ContactUsView getNewContactUsView() {
        ContactUsView contactUsView = new ContactUsView(InstrumentationRegistry.getTargetContext());
        contactUsView.onFinishInflate();
        return contactUsView;
    }
}