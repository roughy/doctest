package com.devbliss.doctest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import org.apache.http.entity.mime.content.FileBody;
import org.junit.AfterClass;
import org.junit.Before;

import com.devbliss.doctest.httpfactory.PostUploadWithoutRedirectImpl;
import com.devbliss.doctest.machine.DocTestMachine;
import com.devbliss.doctest.utils.FileHelper;
import com.devbliss.doctest.utils.JSONHelper;

import de.devbliss.apitester.ApiRequest;
import de.devbliss.apitester.ApiResponse;
import de.devbliss.apitester.ApiTest;
import de.devbliss.apitester.Context;
import de.devbliss.apitester.Cookie;

public abstract class LogicDocTest {

    /**
     * Constant used to show all the elements of a list e.g {@link #cookiesToShow} or
     * {@link #headersToShow}. Just set this list equals to this {@link #ALL_ELEMENTS}
     */
    public static final List<String> ALL_ELEMENTS = Arrays.asList("*");

    protected static DocTestMachine docTestMachine;

    private final ApiTest apiTest;
    private final JSONHelper jsonHelper;
    private final FileHelper fileHelper;

    /**
     * defines the cookies that we want to render for the documentation
     * for this case: its the default declaration and no cookies are declared
     * if you want to define cookies to display for a request,
     * you HAVE TO override this list in your test
     * <p>
     * If you want to see all the cookies, you have to set
     * <code>cookiesToShow = {@link #ALL_ELEMENTS}</code>
     * </p>
     * <strong>IMPORTANT</strong>: you don't have to care about the case of the cookies name
     * 
     * 
     * @return
     */
    protected List<String> cookiesToShow = new ArrayList<String>();

    /**
     * defines the headers that we want to render for the documentation
     * for this case: its the default declaration and no headers are declared
     * if you want to define headers to display for a request or response,
     * you HAVE TO override this list in your test
     * <p>
     * If you want to see all the headers, you have to set
     * <code>headersToShow = {@link #ALL_ELEMENTS}</code>
     * </p>
     * <strong>IMPORTANT</strong>: you don't have to care about the case of the headers name
     * 
     * 
     * @return
     */
    protected List<String> headersToShow = new ArrayList<String>();

    @Before
    public void ensureDocTestClassSet() {
        fileHelper.validateFileName(getFileName());
        docTestMachine.beginDoctest(getFileName(), getIntroduction());
    }

    @AfterClass
    public static void finishDocTest() throws Exception {
        LogicDocTest.docTestMachine.endDocTest();
        LogicDocTest.docTestMachine.prepareDocTest();
    }

    public LogicDocTest(
            DocTestMachine docTest,
            ApiTest apiTest,
            JSONHelper jsonHelper,
            FileHelper fileHelper) {
        LogicDocTest.docTestMachine = docTest;
        this.apiTest = apiTest;
        this.jsonHelper = jsonHelper;
        this.fileHelper = fileHelper;
    }

    /**
     * This method must be overridden if you want to set an introduction
     * The default value of the introduction is: empty.
     * 
     * @return
     */
    protected String getIntroduction() {
        return new String();
    }

    /**
     * Returns the name of the report file
     * 
     * @return name of the file without extension
     */
    protected abstract String getFileName();

    protected void say(String say) {
        docTestMachine.say(say);
    }

    /**
     * say will result in a text in the resulting document with the support of JSON representations
     * for objects and highlighted text.
     * 
     * @param say the text to be said
     * @param objects multiple Objects matching the amount of placeholdern in the text
     */
    protected void say(String say, Object... objects) {
        String[] stringRepresentations = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof String) {
                stringRepresentations[i] = (String) objects[i];
            } else {
                stringRepresentations[i] = jsonHelper.toJson(objects[i], true);
            }
        }
        docTestMachine.say(say, stringRepresentations);
    }

    protected void sayNextSection(String sectionName) {
        docTestMachine.sayNextSectionTitle(sectionName);
    }

    private void sayRequest(ApiRequest apiRequest, Object obj) throws Exception {
        docTestMachine.sayRequest(apiRequest, jsonHelper.toJson(obj), headersToShow, cookiesToShow);
    }

    private void sayRequest(ApiRequest apiRequest) throws Exception {
        docTestMachine.sayRequest(apiRequest, null, headersToShow, cookiesToShow);
    }

    /**
     * 
     * The given POJO will be converted to JSON and be pretty printed.
     * 
     * @param obj
     * @throws Exception
     */
    protected void sayObject(Object obj) throws Exception {
        docTestMachine.sayPreformatted(obj == null ? "" : new JSONHelper().toJson(obj, true));
    }

    /**
     * 
     * The given String will be formatted as-is and be highlighted in a fancy box.
     * 
     * @param code
     * @throws Exception
     */
    protected void sayPreformattedCode(String code) throws Exception {
        docTestMachine.sayPreformatted(code == null ? "" : code);
    }

    protected ApiResponse makeGetRequestSilent(URI uri) throws Exception {
        return doGetRequest(uri).apiResponse;
    }

    private Context doGetRequest(URI uri) throws Exception {
        return apiTest.get(uri);
    }

    protected ApiResponse makeGetRequest(URI uri) throws Exception {
        Context context = doGetRequest(uri);
        sayRequest(context.apiRequest);
        docTestMachine.sayResponse(context.apiResponse, headersToShow);
        return context.apiResponse;
    }

    protected ApiResponse makePostRequestSilent(URI uri, Object obj) throws Exception {
        return doPostRequest(uri, obj).apiResponse;
    }

    private Context doPostRequest(URI uri, Object obj) throws Exception {
        return apiTest.post(uri, obj);
    }

    protected ApiResponse makePostRequest(URI uri) throws Exception {
        return makePostRequest(uri, null);
    }

    protected ApiResponse makePostRequest(URI uri, Object obj) throws Exception {
        Context context = doPostRequest(uri, obj);
        sayRequest(context.apiRequest, obj);
        docTestMachine.sayResponse(context.apiResponse, headersToShow);
        return context.apiResponse;
    }

    protected ApiResponse makePostUploadRequest(URI uri, File fileToUpload, String paramName)
            throws Exception {
        FileBody fileBodyToUpload = new FileBody(fileToUpload);
        String mimeType = new MimetypesFileTypeMap().getContentType(fileToUpload);

        Context context =
                apiTest.post(uri, null, new PostUploadWithoutRedirectImpl(paramName,
                        fileBodyToUpload));

        docTestMachine.sayUploadRequest(context.apiRequest, fileBodyToUpload.getFilename(),
                fileHelper.readFile(fileToUpload), fileToUpload.length(), mimeType, headersToShow,
                cookiesToShow);

        docTestMachine.sayResponse(context.apiResponse, headersToShow);

        return context.apiResponse;
    }

    protected ApiResponse makePutRequestSilent(URI uri, Object obj) throws Exception {
        return doPutRequest(uri, obj).apiResponse;
    }

    private Context doPutRequest(URI uri, Object obj) throws Exception {
        return apiTest.put(uri, obj);
    }

    protected ApiResponse makePutRequest(URI uri) throws Exception {
        return makePutRequest(uri, null);
    }

    protected ApiResponse makePutRequest(URI uri, Object obj) throws Exception {
        Context context = doPutRequest(uri, obj);
        sayRequest(context.apiRequest, obj);
        docTestMachine.sayResponse(context.apiResponse, headersToShow);
        return context.apiResponse;
    }

    protected ApiResponse makeDeleteRequestSilent(URI uri) throws Exception {
        return doDeleteRequest(uri).apiResponse;
    }

    private Context doDeleteRequest(URI uri) throws Exception {
        return apiTest.delete(uri);
    }

    protected ApiResponse makeDeleteRequestSilent(URI uri, Object obj) throws Exception {
        return doDeleteRequest(uri, obj).apiResponse;
    }

    private Context doDeleteRequest(URI uri, Object obj) throws Exception {
        return apiTest.delete(uri, obj);
    }

    protected ApiResponse makeDeleteRequest(URI uri) throws Exception {
        return makeDeleteRequest(uri, null);
    }

    protected ApiResponse makeDeleteRequest(URI uri, Object obj) throws Exception {
        Context context = doDeleteRequest(uri, obj);
        sayRequest(context.apiRequest, obj);
        docTestMachine.sayResponse(context.apiResponse, headersToShow);
        return context.apiResponse;
    }

    /**
     * 
     * @param expected
     * @param given
     * @param message
     */
    protected void assertEqualsAndSay(Object expected, Object given, String message) {
        assertEquals(expected, given);
        docTestMachine.sayVerify(message);
    }

    /**
     * 
     * At first converts both objects to Json and then asserts that they are equal.
     * The resulting doc will sport the expected Json String.
     * 
     * @param expected POJO
     * @param result POJO
     */
    protected void assertJsonEqualsAndSay(Object expected, Object result) {
        assertJsonEqualsAndSay(expected, result, "", null);
    }

    /**
     * 
     * At first converts both objects to Json and then asserts that they are equal, except on the
     * fields mentioned in exceptions.
     * The resulting doc will sport the expected Json String.
     * 
     * @param expected POJO
     * @param result POJO
     */

    protected void assertJsonEqualsAndSay(Object expected, Object result, List<String> exceptions) {
        assertJsonEqualsAndSay(expected, result, "", exceptions);
    }

    /**
     * 
     * First converts both objects to Json and then asserts that they are equal, except on the
     * fields mentioned in exceptions.
     * The resulting doc will sport the expected Json String after the given message.
     * 
     * @param expected POJO
     * @param result POJO
     * @param message Additional message to be concatenated to the expected Json
     * @param exceptions a List of fieldnames that will be omitted in comparison
     */
    protected void assertJsonEqualsAndSay(Object expected, Object result, String message,
            List<String> exceptions) {
        String expectedJson;
        String resultingJson;

        if (exceptions != null && exceptions.size() > 0) {
            expectedJson = jsonHelper.toJsonAndSkipCertainFields(expected, exceptions, true);
            resultingJson = jsonHelper.toJsonAndSkipCertainFields(result, exceptions, true);
        } else {
            expectedJson = jsonHelper.toJson(expected, true);
            resultingJson = jsonHelper.toJson(result, true);
        }

        assertEquals(expectedJson, resultingJson);
        docTestMachine.sayVerify(message + expectedJson);
    }

    protected void assertTrueAndSay(Boolean condition, String message) {
        assertTrue(condition);
        docTestMachine.sayVerify(message);
    }

    protected void assertFalseAndSay(Boolean condition, String message) {
        assertFalse(condition);
        docTestMachine.sayVerify(message);
    }

    protected void assertNullAndSay(Object object, String message) {
        assertNull(object);
        docTestMachine.sayVerify(message);
    }

    protected void assertNotNullAndSay(Object object, String message) {
        assertNotNull(object);
        docTestMachine.sayVerify(message);
    }

    /**
     * Assert that the value of the cookie with the given name equals the given value.
     * 
     * The resulting cookie name and value will be printed, separated by a colon.
     * 
     * @param name The name of the cookie
     * @param expectedValue The expected value
     */
    protected void assertCookieEqualsAndSay(String name, String expectedValue) {
        String value = apiTest.getTestState().getCookieValue(name);
        assertEquals(expectedValue, value);
        docTestMachine.sayVerify(name + ": " + value);
    }

    /**
     * Assert that the cookie with the given name is present.
     * 
     * The resulting cookie name and value will be printed, separated by a colon.
     * 
     * @param name The name of the cookie
     */
    protected void assertCookiePresentAndSay(String name) {
        String value = apiTest.getTestState().getCookieValue(name);
        assertNotNull(value);
        docTestMachine.sayVerify(name + ": " + value);
    }

    /**
     * Assert that the cookie with the given name is not.
     * 
     * The resulting cookie name and null will be printed, separated by a colon.
     * 
     * @param name The name of the cookie
     */
    protected void assertCookieNotPresentAndSay(String name) {
        String value = apiTest.getTestState().getCookieValue(name);
        assertNull(value);
        docTestMachine.sayVerify(name + ": " + value);
    }

    /**
     * Assert that a cookie is present that matches the attributes of the given
     * cookie
     * 
     * The resulting cookie will be output with its parameters, in JSON format.
     * 
     * @param expected The cookie to check
     */
    protected void assertCookieMatchesAndSay(Cookie expected) {
        Cookie cookie = apiTest.getTestState().getCookie(expected.name);
        assertNotNull(cookie);
        assertEquals(expected.value, cookie.value);
        if (expected.expires == null) {
            assertNull(cookie.expires);
        } else {
            assertNotNull(cookie.expires);
        }
        assertEquals(expected.path, cookie.path);
        assertEquals(expected.domain, cookie.domain);
        assertEquals(expected.secure, cookie.secure);
        assertEquals(expected.httpOnly, cookie.httpOnly);
        docTestMachine.sayVerify(jsonHelper.toJson(cookie));
    }

    /**
     * Get the value of the cookie with the given name
     * 
     * @param name The name of the cookie
     * @return The cookies value, or null if no cookie with that name was found
     */
    protected String getCookieValue(String name) {
        return apiTest.getTestState().getCookieValue(name);
    }

    /**
     * Add the cookie with the given name and value to the current state
     * 
     * @param name The name of the cookie
     * @param value The value of the cookie
     */
    protected void addCookie(String name, String value) {
        apiTest.getTestState().addCookie(
                new Cookie(name, value, null, "/", "localhost", false, false));
    }

    /**
     * Clear all cookies from the current state
     */
    protected void clearCookies() {
        apiTest.getTestState().clearCookies();
    }
}
