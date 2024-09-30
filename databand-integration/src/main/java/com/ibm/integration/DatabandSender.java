/*
 * Copyright IBM Corp. 2024
 *
 * The following sample of source code ("Sample") is owned by International
 * Business Machines Corporation or one of its subsidiaries ("IBM") and is
 * copyrighted and licensed, not sold. You may use, copy, modify, and
 * distribute the Sample in any form without payment to IBM, for the purpose of
 * assisting you in the development of your applications.
 *
 * The Sample code is provided to you on an "AS IS" basis, without warranty of
 * any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do
 * not allow for the exclusion or limitation of implied warranties, so the above
 * limitations or exclusions may not apply to you. IBM shall not be liable for
 * any damages you suffer as a result of using, copying, modifying or
 * distributing the Sample, even if IBM has been advised of the possibility of
 * such damages.
 */
package com.ibm.integration;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import com.ibm.integration.ikc.Config;
import com.ibm.integration.model.databand.Run;

public class DatabandSender {
    public static void sendRun(Run run) throws Exception {
        System.out.println("Sending run to Databand...");
        URL url = (new URI(Config.getInstance().getDatabandUrl())).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Content-Encoding", "gzip");
        con.setRequestProperty("Authorization", "Bearer " + Config.getInstance().getDatabandToken());

        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(con.getOutputStream());
        Writer out = new BufferedWriter(new OutputStreamWriter(gzipOutputStream));
        out.write(run.toString());
        out.close();

        InputStreamReader in = new InputStreamReader(con.getInputStream());
        while (in.ready()) {
            System.out.print((char) in.read());
        }
    }
}
