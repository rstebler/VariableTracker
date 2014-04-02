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
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient("localhost");
		} catch (UnknownHostException e) {
			System.out.println("Host not found.");
		}

		if (mongoClient != null) {
			DB db = mongoClient.getDB("test");
			DBCollection coll = db.getCollection("variableInformation");

			List<String> classNames = coll.distinct("variableClass");

			for (String className : classNames) {
				// Go through all class names
				//System.out.println(className);
				BasicDBObject methodQuery = new BasicDBObject("variableClass", className);
				List<String> methodNames = coll.distinct("variableMethod", methodQuery);

				for (String methodName : methodNames) {
					// Go through all method names
					//System.out.println("   "+methodName);
					BasicDBObject variableNameQuery = new BasicDBObject("variableClass", className).append("variableMethod", methodName);
					List<String> variableNames = coll.distinct("variableName", variableNameQuery);

					for (String variableName : variableNames) {
						// Go through all variable names
						//System.out.println("     "+variableName);
						BasicDBObject variableTypeQuery = new BasicDBObject("variableClass", className).append("variableMethod", methodName).append("variableName", variableName);
						List<String> variableTypes = coll.distinct("variableType", variableTypeQuery);

						if(variableTypes.size() > 1) {
							// This variable has multiple types!
							System.out.println("!!! Multiple variable types in "+ className + ">>" + methodName +">>"+variableName+" !!!");
						}
						for (String variableType : variableTypes) {
							// Go through all types names
							// Count how many times the variable has this type
							BasicDBObject variableTypeCountQuery = new BasicDBObject("variableClass", className).append("variableMethod", methodName).append("variableName", variableName).append("variableType", variableType);
							List<String> variableTypesCount = coll.distinct("variableType", variableTypeCountQuery);
							
							if( variableTypes.size() > 1) {
								System.out.println("      "+variableType+" : "+ variableTypesCount.size()+"x");
							}
						}
					}
				}
				//System.out.println();
			}
		}
	}

}
