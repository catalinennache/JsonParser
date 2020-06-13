/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsonparser;

import java.util.HashMap;
import java.util.Set;


public class JsonObject {
    public HashMap<String,Object> content;
    
    public JsonObject(){
        content = new HashMap();
    }
    
    private JsonObject(HashMap map){
        content = map;
    }
    
    public Object getValue(String name){
        return content.get(name);
    }
    
    public Set getKeys(){
        return content.keySet();
    }
    
    protected void addValue(String name, Object value){
        content.put(name, value);
    }   
    
   
}
