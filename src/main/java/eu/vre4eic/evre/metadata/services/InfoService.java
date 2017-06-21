/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.vre4eic.evre.metadata.services;

import java.net.MalformedURLException;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author rousakis
 */
@Path("")
public class InfoService {

    @Context
    private ServletContext context;
    @Context
    private UriInfo uri;

    /**
     * Creates a new instance of InfoService
     */
    public InfoService() {
    }

    /**
     * Retrieves representation of an instance of
     * eu.vre4eic.evre.metadata.services.InfoService
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getHtml() throws MalformedURLException {
        System.out.println(uri.getAbsolutePath());
        System.out.println(context.getResource("/apidocs").toString());

        return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n"
                + "   \"http://www.w3.org/TR/html4/loose.dtd\">\n"
                + "\n"
                + "<!DOCTYPE HTML>\n"
                + "<html xmlns:th=\"http://www.thymeleaf.org\">\n"
                + "<head> \n"
                + "    <title>e-VRE: Meta Data services description page</title> \n"
                + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                + "    <link rel=\"stylesheet\" href=\"css/style.css\"/>\n"
                + "    \n"
                + "</head>\n"
                + "<body>\n"
                + "<div class=\"colmask fullpage\">\n"
                + "	<h2>Welcome to e-VRE Meta Data Services (release <i th:text=\"${session.release}\"></i>)</h2>\n"
                + "	<p >If you can see this page the Meta Data Services have been correctly deployed and it should be possible to use the \n"
                + "	Meta Data Web Services.\n"
                + "	</p>\n"
                + "	<p>\n"
                + "	The <a href=\"https://app.swaggerhub.com/apis/rousakis/ld-services/1.0.0\" target=\"_blank\"> Web Services documentation</a></p>\n"
                + "	The <a href=\"http://139.91.183.70/apidocs/\" target=\"_blank\">Javadocs</a>\n"
                + "	\n"
                + "	\n"
                + "</div>\n"
                + "	<div class=\"footer\">\n"
                + "	\n"
                + "	 <hr/>\n"
                + "	<p>  <i>e-VRE</i></p>\n"
                + "	\n"
                + "	 </div>\n"
                + "	 \n"
                + "</body>\n"
                + "</html>\n"
                + "";
    }

}
