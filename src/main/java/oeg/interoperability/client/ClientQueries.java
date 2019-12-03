package oeg.interoperability.client;

public class ClientQueries {

	// -- Constructor
	
		/**
		 * Private constructor prevents Java to add an implicit public one for this utility class which is not meant to be instantiated
		 */
		private ClientQueries() {
			// empty
		}
		
	// -- Attributes
	
	public static final String SELECT_QUERY_FIND_WOT_THINGS = "prefix wot: <http://iot.linkeddata.es/def/wot#>\n" + 
				"SELECT DISTINCT ?thing WHERE { \n" + 
				"	?thing a wot:Thing .\n" + 
				"}";

	public static final String ASK_QUERY = "ASK { $$ }";
	 
	
	// -- Methods
	
	
	 public static String instanciateAskQuery(String triplets) {
		 return ASK_QUERY.replace("$$", triplets);
	 }
	
	 public static String instanciateAskQueryWithSubject(String subject) {
		 StringBuilder str = new StringBuilder();
		 str.append("<").append(subject).append("> ?p ?o .");
		 return ASK_QUERY.replace("$$", str);
	 }
	   
}
