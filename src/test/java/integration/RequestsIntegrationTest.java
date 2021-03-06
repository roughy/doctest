/*
 * Copyright 2013, devbliss GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package integration;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.devbliss.doctest.DocTest;
import com.devbliss.doctest.LogicDocTest;
import com.devbliss.doctest.httpfactory.PostUploadWithoutRedirectImpl;

import de.devbliss.apitester.ApiRequest;
import de.devbliss.apitester.ApiResponse;
import de.devbliss.apitester.ApiTest;
import de.devbliss.apitester.Context;

/**
 * Example implementation of a unit test class extending the {@link DocTest}.
 * 
 * This class is used in the {@link ReportCreatedIntegrationTest} class and describes how to use the
 * doctest library to make some http requests.
 * 
 * @author bmary
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestsIntegrationTest extends DocTest {

    private static final String JSON_TEXT = "The response contains a JSON payload";
    private static final String HTML_TEXT = "The response contains a HTML payload";
    private static final String HTTP_TEXT = "The response contains the HTTP_STATUS of the request";
    private static final String PAYLOAD_OBJECT =
            "{'abc':'123', 'cde': {'start': 'today', 'end':'tomorrow'}}";
    private static final String PAYLOAD_ARRAY =
            "[{'objectId': 123},{'objectId':4567}]";
    private static final String REASON_PHRASE = "This is not a normal response code";
    private static final String HEADER_VALUE11 = "application/json";
    private static final String HEADER_VALUE12 = "text/html";
    private static final String HEADER_VALUE2 = "value2";
    private static final String HEADER_NAME1 = "content-type";
    private static final String HEADER_NAME2 = "name2";
    private static final String COOKIE_VALUE_1 = "123456(-)654321";
    private static final String COOKIE_NAME_1 = "login";
    private static ApiTest API;

    @BeforeClass
    public static void beforeClass() {
        API = mock(ApiTest.class);
        DocTest.setApi(API);
    }

    @Override
    protected String getFileName() {
        return "HttpRequests";
    }

    @Override
    public String getIntroduction() {
        return "This documentation describes the input/output of the four http methods.";
    }

    private Object obj;
    private ApiResponse apiResponse;
    private URI uri;
    private Context context;
    private ApiRequest apiRequest;
    private Map<String, String> headers;
    private Map<String, String> cookies;

    @Before
    public void setUp() throws Exception {
        headersToShow = Arrays.asList(HEADER_NAME1, "Cookie");
        cookiesToShow = Arrays.asList(COOKIE_NAME_1);
        obj = new TestObject();
        uri =
                new URIBuilder().setScheme("http").setHost("www.hostname.com").setPort(8080)
                        .setPath("/resource/id:12345").build();

        headers = new HashMap<String, String>();
        headers.put(HEADER_NAME1, HEADER_VALUE11);
        headers.put(HEADER_NAME2, HEADER_VALUE2);

        cookies = new HashMap<String, String>();
        cookies.put(COOKIE_NAME_1, COOKIE_VALUE_1);
    }

    @Test
    public void getJson() throws Exception {
        apiRequest = new ApiRequest(uri, "get", headers, cookies);
        apiResponse = new ApiResponse(HttpStatus.SC_OK, REASON_PHRASE, PAYLOAD_ARRAY, headers);
        context = new Context(apiResponse, apiRequest);
        when(API.get(uri)).thenReturn(context);

        sayNextSection("Making a get request for json content");
        ApiResponse resp = makeGetRequest(uri);

        assertEqualsAndSay(HttpStatus.SC_OK, resp.httpStatus, HTTP_TEXT);
        assertEqualsAndSay(PAYLOAD_ARRAY, resp.payload, JSON_TEXT);
    }

    @Test
    public void getHtml() throws Exception {
        headers.put(HEADER_NAME1, HEADER_VALUE12);
        String html = "<p>some Html content</p>";
        apiRequest = new ApiRequest(uri, "get", headers, cookies);
        apiResponse = new ApiResponse(HttpStatus.SC_OK, REASON_PHRASE, html, headers);
        context = new Context(apiResponse, apiRequest);
        when(API.get(uri)).thenReturn(context);

        sayNextSection("Making a get request for html content");
        ApiResponse resp = makeGetRequest(uri);

        assertEqualsAndSay(HttpStatus.SC_OK, resp.httpStatus, HTTP_TEXT);
        assertEqualsAndSay(html, resp.payload, HTML_TEXT);
    }

    @Test
    public void delete() throws Exception {
        headersToShow = LogicDocTest.SHOW_ALL_ELEMENTS;
        apiRequest = new ApiRequest(uri, "delete", headers, cookies);
        apiResponse = new ApiResponse(HttpStatus.SC_NO_CONTENT, REASON_PHRASE, null, headers);
        context = new Context(apiResponse, apiRequest);
        when(API.delete(uri, null)).thenReturn(context);

        sayNextSection("Making a delete request");
        ApiResponse response = makeDeleteRequest(uri);

        assertEqualsAndSay(HttpStatus.SC_NO_CONTENT, response.httpStatus, HTTP_TEXT);
        assertNull(response.payload);
    }

    @Test
    public void post() throws Exception {
        apiRequest = new ApiRequest(uri, "post", headers, cookies);
        apiResponse = new ApiResponse(HttpStatus.SC_CREATED, REASON_PHRASE, PAYLOAD_OBJECT, headers);
        context = new Context(apiResponse, apiRequest);
        when(API.post(uri, obj)).thenReturn(context);

        sayNextSection("Making a post request");
        ApiResponse response = makePostRequest(uri, obj);

        assertEqualsAndSay(HttpStatus.SC_CREATED, response.httpStatus, HTTP_TEXT);
        assertEqualsAndSay(PAYLOAD_OBJECT, response.payload, JSON_TEXT);
    }

    @Test
    public void put() throws Exception {
        apiRequest = new ApiRequest(uri, "put", headers, cookies);
        apiResponse = new ApiResponse(HttpStatus.SC_NO_CONTENT, REASON_PHRASE, PAYLOAD_OBJECT, headers);
        context = new Context(apiResponse, apiRequest);
        when(API.put(uri, obj)).thenReturn(context);

        sayNextSection("Making a put request with encöding chäracters");
        ApiResponse response = makePutRequest(uri, obj);

        assertEqualsAndSay(HttpStatus.SC_NO_CONTENT, response.httpStatus, HTTP_TEXT);
        assertEqualsAndSay(PAYLOAD_OBJECT, response.payload, JSON_TEXT);
    }

    @Test
    public void postUploadText() throws Exception {
        apiRequest = new ApiRequest(uri, "post", headers, cookies);
        apiResponse = new ApiResponse(HttpStatus.SC_CREATED, "", null, headers);
        context = new Context(apiResponse, apiRequest);
        when(API.post(eq(uri), eq(null), isA(PostUploadWithoutRedirectImpl.class))).thenReturn(
                context);
        sayNextSection("Making an upload post request");

        ApiResponse response =
                makePostUploadRequest(uri, new File("src/test/resources/file.txt"), "paramName");
        assertEqualsAndSay(HttpStatus.SC_CREATED, response.httpStatus, HTTP_TEXT);
        assertNull(response.payload);
    }

    @Test
    public void postUploadImage() throws Exception {
        apiRequest = new ApiRequest(uri, "post", headers, cookies);
        apiResponse = new ApiResponse(HttpStatus.SC_CREATED, "", null, headers);
        context = new Context(apiResponse, apiRequest);
        when(API.post(eq(uri), eq(null), isA(PostUploadWithoutRedirectImpl.class))).thenReturn(
                context);
        sayNextSection("Making an upload post request with an image file");

        ApiResponse response =
                makePostUploadRequest(uri, new File("src/test/resources/picture.png"), "paramName");

        assertEqualsAndSay(HttpStatus.SC_CREATED, response.httpStatus, HTTP_TEXT);
        assertNull(response.payload);
    }

    @Test
    public void suiteRequests() throws Exception {
        sayNextSection("Suite of requests");

        say("All requests are independent and can be done in a sequentially way.");
        say("Let's first upload a resource:");

        apiRequest = new ApiRequest(uri, "post", headers, cookies);
        apiResponse = new ApiResponse(HttpStatus.SC_CREATED, "", null, headers);
        context = new Context(apiResponse, apiRequest);
        when(API.post(eq(uri), eq(null), isA(PostUploadWithoutRedirectImpl.class))).thenReturn(
                context);

        ApiResponse response =
                makePostUploadRequest(uri, new File("src/test/resources/picture.png"), "paramName");

        assertEqualsAndSay(HttpStatus.SC_CREATED, response.httpStatus, HTTP_TEXT);
        assertNull(response.payload);

        say("And now we would like to update another resource:");
        apiRequest = new ApiRequest(uri, "put", headers, cookies);
        apiResponse = new ApiResponse(HttpStatus.SC_OK, "", PAYLOAD_OBJECT, headers);
        context = new Context(apiResponse, apiRequest);
        when(API.put(uri, obj)).thenReturn(context);

        response = makePutRequest(uri, obj);

        assertEqualsAndSay(HttpStatus.SC_OK, response.httpStatus, HTTP_TEXT);
        assertEqualsAndSay(PAYLOAD_OBJECT, response.payload, JSON_TEXT);
    }
}