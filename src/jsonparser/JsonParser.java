/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsonparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Scanner;
import java.util.Set;


public class JsonParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new Exception("Filename not provided!");
        } else {
            System.out.println("Target file " + args[0]);
        }
        String file_name = args[0];
        File target = new File(file_name);
        FileReader fr = new FileReader(target);
        BufferedReader br = new BufferedReader(fr);
        String character = "";
        AutomataParser.init();
        AutomataParser atm = new AutomataParser(0);
        int chr;
        while ((chr = br.read()) != -1) {
            char ch = (char) chr;
            character = "" + ch;
            character = character.trim();
            if(!character.equals("\n") && !character.equals(" ") && !character.equals(""))
                atm.processSymbol(character);
            else{
                System.out.println("Skipping white-space or \\n or empty character");
            }
        }
       System.out.println("-------------------------------------\n\n");
       JsonObject jsn = atm.getJSON();
       //System.out.println(jsn.getValue("test2alfa"));
      JsonParser.present(atm.getJSON(), 0);
    }
    
    public static void present(JsonObject json,int level){
        
    Set keys =    json.getKeys();
    keys.forEach(val->{
        String indent = "";
            for(int i =0;i<level;i++)
                indent += "  ";
        if(json.getValue((String)val) instanceof JsonObject){
            System.out.println(indent+val+" : ");
            JsonParser.present((JsonObject)json.getValue((String) val), level+1);
        }else{
           
            System.out.println(indent+val+" : "+json.getValue((String)val));
        }
    });
    
        
    }

}
