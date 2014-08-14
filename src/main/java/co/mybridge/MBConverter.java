package co.mybridge;

import org.json.JSONObject;
import com.mongodb.BasicDBObject;

public interface MBConverter {
    abstract public JSONObject convertBasicDBToJSON (BasicDBObject bobj);
    abstract public BasicDBObject convertJSONToBasicDB (JSONObject jobj);
}
