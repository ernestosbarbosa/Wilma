package com.epam.wilma.webapp.config.servlet.admin;
/*==========================================================================
Copyright 2013-2016 EPAM Systems

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

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.epam.wilma.webapp.security.HostValidatorService;

/**
 * Servlet that answers back if you have admin privileges or not, in json format.
 * @author Adam_Csaba_Kiraly
 */
@Component
public class AdminStatusServlet extends HttpServlet {

    @Autowired
    private HostValidatorService adminHosts;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean adminStatus = adminHosts.isRequestFromAdmin(req);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        PrintWriter printWriter = resp.getWriter();
        printWriter.write(String.format("{\"adminStatus\": %s}", adminStatus));
        printWriter.flush();
        printWriter.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

}
