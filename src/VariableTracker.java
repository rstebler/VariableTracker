import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class VariableTracker {

	public static void main(String[] args) {
		int totalVariables = 0;
		int totalVariablesWithDifferentTypes = 0;
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient("localhost");
		} catch (UnknownHostException e) {
			System.out.println("Host not found.");
		}

		if (mongoClient != null) {
			DB db = mongoClient.getDB("test");
			DBCollection coll = db.getCollection("variableInformation");
			long totalAssignments = coll.count();
			
			System.out.println("Total entries in the database: " + totalAssignments);

			List<String> classNames = coll.distinct("variableClass");

			for (String className : classNames) {
				// Go through all class names
				BasicDBObject methodQuery = new BasicDBObject("variableClass", className);
				List<String> methodNames = coll.distinct("variableMethod", methodQuery);

				for (String methodName : methodNames) {
					// Go through all method names
					BasicDBObject variableNameQuery = new BasicDBObject("variableClass", className).append("variableMethod", methodName);
					List<String> variableNames = coll.distinct("variableName", variableNameQuery);

					for (String variableName : variableNames) {
						// Go through all variable names
						BasicDBObject variableTypeQuery = new BasicDBObject("variableClass", className).append("variableMethod", methodName).append("variableName", variableName);
						List<String> variableTypes = coll.distinct("variableType", variableTypeQuery);

						if(variableTypes.size() > 1) {
							// This variable has multiple types!
							totalVariablesWithDifferentTypes++;
							System.out.println("Multiple variable types found for "+ className + ">>" + methodName +">>"+variableName);
						}
						totalVariables++;
						for (String variableType : variableTypes) {
							// Go through all types names
							// Count how many times the variable has this type
							BasicDBObject variableTypeCountQuery = new BasicDBObject("variableClass", className).append("variableMethod", methodName).append("variableName", variableName).append("variableType", variableType);
							long variableTypesCount = coll.count(variableTypeCountQuery);
							
							if(variableTypes.size() > 1) {
								System.out.println("      " + variableType + " : " + variableTypesCount + "x");
							}
						}
					}
				}
			}
			
			System.out.println();
			System.out.println("Results");
			System.out.println("-------");
			System.out.println("Total variable assignments: " + totalAssignments);
			System.out.println("Total variables: " + totalVariables);
			System.out.println("--> every variable was assigned " + (float)Math.round((float)totalAssignments/totalVariables*100)/100  + " times");
			System.out.println("Total variables with different types: " + totalVariablesWithDifferentTypes + " (" + (float)Math.round((float)totalVariablesWithDifferentTypes/totalVariables*10000)/100 + "%)");
		}
	}

}
