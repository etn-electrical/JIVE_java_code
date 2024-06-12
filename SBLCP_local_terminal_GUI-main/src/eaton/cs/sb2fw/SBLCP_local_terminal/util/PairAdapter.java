/******************************************************************************************************
 * 
 * 		Connected Solutions @ Eaton (Moon Township, PA)
 * 	
 * 		@author Jonathan Tan (JonathanTan@eaton.com)
 * 		@date 6/5/2023
 * 
 * ****************************************************************************************************
 * 
 * 		PairAdapter.java
 * 
 * 		Purpose	:	This Class is used to turn JSON pair into java Pair object. (I think, I wrote this
 * 					like six years ago) It works, so don't touch it.
 * 
 * 		For more info, checkout this project's confluence page: https://confluence-prod.tcc.etn.com/pages/viewpage.action?pageId=299547479
 * 
 * *****************************************************************************************************/


package eaton.cs.sb2fw.SBLCP_local_terminal.util;

import com.google.gson.*;
import javafx.util.Pair;

import java.lang.reflect.Type;

public class PairAdapter implements JsonSerializer<Pair<?, ?>>, JsonDeserializer<Pair<?, ?>> {
    @Override
    public JsonElement serialize(Pair<?, ?> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("key", src.getKey().toString());
        jsonObject.addProperty("value", src.getValue().toString());
        return jsonObject;
    }

    @Override
    public Pair<?, ?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String key = jsonObject.get("key").getAsString();
        String value = jsonObject.get("value").getAsString();
        return new Pair<>(key, value);
    }
}
