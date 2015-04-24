package com.epam.wilma.gepard.test.sequence.handling;

/*==========================================================================
Copyright 2013-2015 EPAM Systems

This file is part of Wilma.

Wilma is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Wilma is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wilma.  If not, see <http://www.gnu.org/licenses/>.
===========================================================================*/

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.epam.gepard.annotations.TestClass;
import com.epam.gepard.annotations.TestParameter;
import com.epam.wilma.gepard.testclient.RequestParameters;

/**
 * Tests that the template formatter gets the referenced sequence descriptor's sequence.
 *
 * First test:
 *  Uploads a stub config with two sequence descriptors and with the response descriptor
 *  referencing the first sequence descriptor. Sends four requests then checks if the template
 *  formatter got the first sequence descriptor's sequence.
 *
 * Second test:
 *  Uploads a stub config with two sequence descriptors and with the response descriptor
 *  referencing the second sequence descriptor. Sends four requests then checks if the template
 *  formatter got the second sequence descriptor's sequence.
 *
 * @author Adam_Csaba_Kiraly
 */
@TestClass(id = "SequenceHandling", name = "Multiple SequenceDescriptors")
public class MultipleSequenceDescriptorsTest extends SequenceHandlingTestBase {

    @TestParameter(id = "PAR0")
    private String tcNama;
    @TestParameter(id = "PAR1")
    private String tcRequestBody;
    @TestParameter(id = "PAR2")
    private String tcJarFile;
    @TestParameter(id = "PAR3")
    private String tcStubConfigFile;
    @TestParameter(id = "PAR4")
    private String tcFinalResponseFile;

    public void testSequenceHandling() throws Exception {
        clearSequences();
        uploadJarToWilma("message-sequence.jar", tcJarFile);
        uploadStubConfigToWilma(tcStubConfigFile);
        String spaceSeparatedText = tcRequestBody;
        String[] texts = spaceSeparatedText.split(" ");
        sendRequestAndAssertMessage(texts[0], "welcome");
        sendRequestAndAssertMessage(texts[1], "welcome");
        sendRequestAndAssertMessage(texts[2], "welcome");
        sendRequestAndAssertMessageFromFile(texts[3], tcFinalResponseFile);

        updateDefaultGroupOfStubConfiguration(tcStubConfigFile);
    }

    private void sendRequestAndAssertMessage(final String requestBody, final String expectedAnswer) throws Exception {
        RequestParameters requestParameters = createRequestParameters(requestBody);
        setExpectedResponseMessage(expectedAnswer);
        callWilmaWithPostMethodAndAssertResponse(requestParameters);
    }

    private void sendRequestAndAssertMessageFromFile(final String requestBody, final String expectedAnswerFileLocation) throws Exception {
        RequestParameters requestParameters = createRequestParameters(requestBody);
        setExpectedResponseMessageFromFile(expectedAnswerFileLocation);
        callWilmaWithPostMethodAndAssertResponse(requestParameters);
    }

    protected RequestParameters createRequestParameters(final String text) throws FileNotFoundException {
        InputStream requestBody = new ByteArrayInputStream(text.getBytes());
        String testServerUrl = getWilmaVersionInfoUrl();
        String wilmaHost = getClassData().getEnvironment().getProperty("wilma.host");
        Integer wilmaPort = Integer.parseInt(getClassData().getEnvironment().getProperty("wilma.port.external"));
        String contentType = "text/plain";
        String acceptHeader = "xml";
        String contentEncoding = "no";
        String acceptEncoding = "no";
        return new RequestParameters().testServerUrl(testServerUrl).useProxy(true).wilmaHost(wilmaHost).wilmaPort(wilmaPort).xmlIS(requestBody)
                .contentType(contentType).acceptHeader(acceptHeader).contentEncoding(contentEncoding).acceptEncoding(acceptEncoding);
    }
}
