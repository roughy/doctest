package com.devbliss.doctest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.devbliss.doctest.templates.AssertDocItem;
import com.devbliss.doctest.templates.DocItem;
import com.devbliss.doctest.templates.RequestDocItem;
import com.devbliss.doctest.templates.ResponseDocItem;
import com.devbliss.doctest.templates.SectionDocItem;
import com.devbliss.doctest.templates.Templates;
import com.devbliss.doctest.templates.TextDocItem;
import com.devbliss.doctest.utils.JSONHelper;

import de.devbliss.apitester.ApiTest.HTTP_REQUEST;

/**
 * Unit test for {@link DocTestMachineImpl}
 * 
 * @author bmary
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class DocTestMachineImplUnitTest {

    private static final String CLASS_NAME = "className";
    private static final String TEXT = "text";
    private static final int RESPONSE_CODE = 130;
    private static final String JSON_VALID = "{'abc':'a'}";

    @Mock
    private Templates templates;
    @Mock
    private ReportRenderer renderer;
    @Mock
    private JSONHelper jsonHelper;
    @Captor
    private ArgumentCaptor<List<DocItem>> listItemCaptor;

    private DocTestMachineImpl machine;
    private final HTTP_REQUEST httpRequest = HTTP_REQUEST.GET;
    private URI uri;

    @Before
    public void setUp() throws URISyntaxException {
        when(jsonHelper.isJsonValid(JSON_VALID)).thenReturn(true);
        uri = new URI("");
        machine = new DocTestMachineImpl(templates, renderer, jsonHelper);
        machine.beginDoctest(CLASS_NAME);
    }

    @Test
    public void addTextItem() {
        machine.say(TEXT);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof TextDocItem);
        assertEquals(TEXT, ((TextDocItem) listItems.get(0)).text);
    }

    @Test
    public void addSectionItem() {
        machine.sayNextSection(TEXT);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof SectionDocItem);
        assertEquals(TEXT, ((SectionDocItem) listItems.get(0)).title);
    }

    @Test
    public void addAssertItem() {
        machine.sayVerify(TEXT);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof AssertDocItem);
        assertEquals(TEXT, ((AssertDocItem) listItems.get(0)).expected);
    }

    @Test
    public void addResponseItem() throws Exception {
        machine.sayResponse(RESPONSE_CODE, JSON_VALID);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof ResponseDocItem);
        assertEquals(JSON_VALID, ((ResponseDocItem) listItems.get(0)).payload);
        assertEquals(RESPONSE_CODE, ((ResponseDocItem) listItems.get(0)).responseCode);
    }

    @Test
    public void addRequestItem() throws Exception {
        machine.sayRequest(uri, JSON_VALID, httpRequest);
        machine.endDocTest();

        verify(renderer).render(listItemCaptor.capture(), eq(CLASS_NAME));

        List<DocItem> listItems = listItemCaptor.getValue();
        assertEquals(1, listItems.size());
        assertTrue(listItems.get(0) instanceof RequestDocItem);
        assertEquals(JSON_VALID, ((RequestDocItem) listItems.get(0)).payload);
        assertEquals(httpRequest, ((RequestDocItem) listItems.get(0)).http);
        assertEquals(uri.toString(), ((RequestDocItem) listItems.get(0)).uri);
    }

}