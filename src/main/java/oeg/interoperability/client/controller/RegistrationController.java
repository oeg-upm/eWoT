package oeg.interoperability.client.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import helio.framework.objects.RDF;
import oeg.interoperability.client.service.GraphDBService;


@Controller
@RequestMapping("/register")
public class RegistrationController extends AbstractController {

	@Autowired
	private GraphDBService fusekiService;
	
	// -- POST method
	
	@RequestMapping(method = RequestMethod.POST) 
	public String sparqlEndpointPOST(@RequestHeader Map<String, String> headers, @RequestBody(required = true) String thingDescription, HttpServletResponse response) {
		prepareResponse(response);
		String code = "{\"code\":\"200\"}";
		try {
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return code;
	}
	
	
	
	
}
