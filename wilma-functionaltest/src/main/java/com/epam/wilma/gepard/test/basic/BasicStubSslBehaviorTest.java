package com.epam.wilma.gepard.test.basic;

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

import com.epam.gepard.annotations.TestClass;
import com.epam.gepard.annotations.TestParameter;
import com.epam.wilma.gepard.WilmaTestCase;
import com.epam.wilma.gepard.testclient.RequestParameters;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Sends example3.xml to the wilma webApplication over https.
 */
@TestClass(id = "Basic - Stub - SSL", name = "Stub Answers")
public class BasicStubSslBehaviorTest extends WilmaTestCase {
    private static final String EXAMPLE_3_XML = "resources/example3.xml";
    private static final String RESOURCE_FILE_NAME = "example3.xml";
    private static final String STUB_CONFIG = "resources/stubConfig.xml";

    @TestParameter(id = "PAR0")
    private String tcName;
    @TestParameter(id = "PAR1")
    private String tcContentType;
    @TestParameter(id = "PAR2")
    private String tcAcceptHeader;
    @TestParameter(id = "PAR3")
    private String tcContentEncoding;
    @TestParameter(id = "PAR4")
    private String tcAcceptEncoding;

    /**
     * B, send the req2-xml message to Apache (use wilma as proxy), but when the this request arrives to Wilma,
     * Wilma sends resp-xml2 back as response instead of forwarding te request to Apache.
     * (don't forget to log the messages)
     *
     * @throws Exception
     */
    public void testBasicStubBehaviorWithSsl() throws Exception {
        //given
        setOperationModeTo("wilma");
        uploadTemplateToWilma(RESOURCE_FILE_NAME, EXAMPLE_3_XML);
        uploadStubConfigToWilma(STUB_CONFIG);
        setOriginalRequestMessageFromFile(EXAMPLE_3_XML);
        setExpectedResponseMessageFromFile("resources/uc3_2TestResponse.txt");
        RequestParameters requestParameters = createRequestParameters();
        if (tcName.contains("SSL")) {
            naTestCase("SSL part testing is not prepared on test environment side.");
            //when and then
            callWilmaWithPostMethodAndAssertResponse(requestParameters); //send request, receive response and check if expected result test is in the result
        }
        naTestCase("This test is designed for test SSL connection.");
    }

    protected RequestParameters createRequestParameters() throws FileNotFoundException {
        String testServerUrl = getWilmaSSLTestServerUrl();
        String wilmaHost = getClassData().getEnvironment().getProperty("wilma.host");
        Integer wilmaPort = Integer.parseInt(getClassData().getEnvironment().getProperty("wilma.port.external"));
        String contentType = "application/" + tcContentType;
        String acceptHeader = "application/" + tcAcceptHeader;
        String contentEncoding = tcContentEncoding;
        String acceptEncoding = tcAcceptEncoding;
        return new RequestParameters().testServerUrl(testServerUrl).useProxy(true).wilmaHost(wilmaHost).wilmaPort(wilmaPort)
                .xmlIS(new FileInputStream(EXAMPLE_3_XML)).contentType(contentType).acceptHeader(acceptHeader).contentEncoding(contentEncoding)
                .acceptEncoding(acceptEncoding);
    }
}