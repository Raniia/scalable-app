package datastore.datastores;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import datastore.DataStoreConnection;
import org.bson.Document;
import org.bson.types.ObjectId;
import utility.ResponseCodes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

public class MongodbDataStoreConnection extends DataStoreConnection {
    private MongoClient mongoClient;


    /*
    private MongoClient mongoClient;
    private static MongoClientURI mongoClientURI;


    @Override
    public void init(Map<String, Object> parameters) {
        super.init(parameters);
        if (parameters.containsKey("mongoClientURI")) {
            mongoClientURI = (MongoClientURI) parameters.get("mongoClientURI");
        }
        mongoClient = new MongoClient(mongoClientURI);
    }

    public static MongoClientURI getMongoClientURI() {
        return mongoClientURI;
    }

    public static boolean isMongoClientURISet() {
        return mongoClientURI != null;
    }
    */

    @Override
    public StringBuffer execute(Map<String, Object> parameters) throws Exception {

        mongoClient = new MongoClient((MongoClientURI) parameters.get("mongoClientURI"));

        String action = (String) parameters.get("action");
//		switch(action){
//		case "createMessagesThread":
//			return createMessagesThread((String)parameters.get("threadName"),(String)parameters.get("userId"));
//		}
        //project jdk <7...
        if (action.equals("createMessagesThread")) {
            String threadName = (String) parameters.get("threadName");
            String userId = (String) parameters.get("userId");
            return createMessagesThread(threadName, userId);
        } else if (action.equals("searchForUser")) {
            String nameQuery = (String) parameters.get("nameQuery");
            return searchForUser(nameQuery);
        } else if (action.equals("searchForUsersAndThreads")) {
            String nameQuery = (String) parameters.get("nameQuery");
            return searchForUsersAndThreads(nameQuery);
        }
        return null;
    }

    private StringBuffer searchForUsersAndThreads(String nameQuery) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("userName", java.util.regex.Pattern.compile(nameQuery));
        FindIterable<Document> findIterable = getUsersCollection().find(basicDBObject);

        JsonObject response = new JsonObject();

        JsonArray jsonArray = new JsonArray();
        for (Document document : findIterable) {
            jsonArray.add(document.getString("userName"));
        }

        basicDBObject = new BasicDBObject();
        basicDBObject.put("threadName", java.util.regex.Pattern.compile(nameQuery));
        findIterable = getMessageThreadsCollection().find(basicDBObject);

        for (Document document : findIterable) {
            jsonArray.add(document.getString("threadName"));
        }

        response.add("UsersAndThreadsNames", jsonArray);
        response.addProperty("responseCode", ResponseCodes.STATUS_OK);

        return new StringBuffer(response.getAsString());
    }

    private StringBuffer searchForUser(String nameQuery) {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("userName", java.util.regex.Pattern.compile(nameQuery));
        FindIterable<Document> findIterable = getUsersCollection().find(basicDBObject);

        JsonObject response = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Document document : findIterable) {
            jsonArray.add(document.getString("userName"));
        }

        response.add("userNames", jsonArray);
        response.addProperty("responseCode", ResponseCodes.STATUS_OK);

        return new StringBuffer(response.getAsString());
    }

    private StringBuffer createMessagesThread(String threadName, String userId) {
        ObjectId messageThreadId = new ObjectId();
        Document messagesThread = new Document("_id", messageThreadId);
        messagesThread.append("threadName", threadName);
        messagesThread.append("users", Arrays.asList(userId));
        getMessageThreadsCollection().insertOne(messagesThread);

        Document userObject = getUsersCollection().find(eq("userId", userId)).first();
        if (userObject == null) {
            userObject = new Document("id", userId);
            userObject.append("threads", Arrays.asList(messageThreadId));
            getUsersCollection().insertOne(userObject);
        } else {
            List threadsList = (List<String>) userObject.get("threads");
            threadsList.add(messageThreadId);
            getUsersCollection().updateOne(eq("userId", userId), new Document("$set", userObject));
        }
        JsonObject response = new JsonObject();
        response.addProperty("responseCode", ResponseCodes.STATUS_OK);   //TODO need a known key to follow
        response.addProperty("threadId", messageThreadId.toString());
        return new StringBuffer(response.getAsString());
    }

    private MongoDatabase getMessagingAppDB() {
        return mongoClient.getDatabase("bleh");  // TODO needs to be changed to the database name
    }

    private MongoCollection<Document> getMessageThreadsCollection() {
        return getMessagingAppDB().getCollection("bleh"); // TODO needs to be changed to the messaging thread collection name
    }

    private MongoCollection<Document> getUsersCollection() {
        return getMessagingAppDB().getCollection("bleh1"); // TODO needs to be changed to the users collection name
    }

}