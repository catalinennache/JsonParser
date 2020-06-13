/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsonparser;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 *
 * @author Enache
 */
enum STATE {

    UNINITIALISED, INITIALISED, KEYOPENED, KEYLOAD, KEYCLOSED, KVLINE, VALINIT, VALLOAD, VALCLOSED, DONE;

    private static STATE[] vals = values();

    public STATE next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }
}

public class AutomataParser {

    private STATE current_state;
    private static HashMap<STATE, String[]> dictionary_accepted_symbols;
    private AutomataParser slave;
    private boolean isMaster = false;
    private JsonObject held_json;
    private String current_key = "";
    private String current_value = "";
    private int stacklevel = 0;

    public AutomataParser(int stacklevel) {
        current_state = STATE.UNINITIALISED;
        held_json = new JsonObject();
        this.stacklevel = stacklevel;
    }

    public void processSymbol(String character) throws Exception {
        if (dictionary_accepted_symbols == null) {
            throw new Exception("Automata not inited");
        }
    

        String[] accepted_patterns = dictionary_accepted_symbols.get(this.current_state);
        int patterns_tested = 0;
        for (String pattern : accepted_patterns) {
            if (Pattern.compile(pattern).matcher(character).find()) {
                break;
            }
            patterns_tested++;
        }

        if (accepted_patterns.length - patterns_tested > 0 || this.isMaster) {
            //character accepted

            switch (this.current_state) {

                case UNINITIALISED: {
                    this.current_state = this.current_state.next();
                }
                break;
                case INITIALISED: {
                    this.current_state = this.current_state.next();
                    this.current_key = "";
                }
                break;
                case KEYOPENED: {
                    this.current_state = this.current_state.next();
                    this.current_key += character;
                }
                break;
                case KEYLOAD: {
                    this.current_key += character;
                }
                break;

                case KEYCLOSED: {
                    this.current_state = this.current_state.next();
                }
                break;
                case KVLINE: {
                    //daca suntem in KVLINE si a fost acceptat caracterul pentru a trece in urmatoarea stare,
                    //trebuie sa vedem ce fel de caracter a fost, ghilimea sau acolada
                    this.current_state = this.current_state.next();
                    if (character.equals("{")) {
                        slave = new AutomataParser(this.stacklevel + 1);
                        this.setMaster(true);
                        slave.processSymbol(character);
                    } else {
                        this.current_value = "";
                    }
                }
                break;

                case VALINIT: {
                    this.current_state = this.current_state.next();
                    if (this.isMaster) {
                        slave.processSymbol(character);
                    } else {
                        this.current_value += character;
                    }
                }
                break;

                case VALLOAD: {
                    if (this.isMaster) {
                        if (!slave.hasFinished()) {
                            slave.processSymbol(character);
                            if (slave.hasFinished()) {
                                JsonObject slave_json = slave.getJSON();
                              
                                this.held_json.addValue(current_key, slave_json);
                                slave = null;
                                this.setMaster(false);
                                System.out.println("slave dismissed " + this.stacklevel);
                                 this.current_state = this.current_state.next();
                                 
                               
                            }
                        } else {

                            this.current_state = this.current_state.next();
                           
                        }
                    } else {
                        this.current_value += character;
                    }
                    
                       
                }
                break;

                case VALCLOSED: {
                  
                 if(this.held_json.getValue(current_key) == null) {
                     this.held_json.addValue(current_key, current_value);
                 }
                    current_key = "";
                    current_value = "";
                    if (character.equals(",")) {
                        this.current_state = STATE.INITIALISED;
                    } else {
                        System.out.println("finished job "+this.stacklevel);
                        this.current_state = this.current_state.next();
                    }
                }
                break;

                case DONE: {
                    throw new Exception("Automata is done!");
                }

            }
        } else {
            if (!this.current_state.equals(STATE.KEYLOAD) && !this.current_state.equals(STATE.VALLOAD)) {
                throw new Exception("Malformed JSON detected!");
            } else {
                this.current_state = this.current_state.next();
            }
        }

    }

    public JsonObject getJSON() throws Exception {
        if (!this.current_state.equals(STATE.DONE)) {
            throw new Exception("Automata not in its final state >> " + this.current_state + " stacklevel >> " + this.stacklevel);
        }

        return held_json;
    }

    public void setMaster(boolean set) {
        this.isMaster = set;
    }

    public boolean hasFinished() {
        return this.current_state.equals(STATE.DONE);
    }

    public static void init() {
        dictionary_accepted_symbols = new HashMap<>();
        //symbolurile acceptate de fiecare stare pentru a putea trece in starea urmatoare
        dictionary_accepted_symbols.put(STATE.UNINITIALISED, new String[]{Pattern.quote("{")});
        dictionary_accepted_symbols.put(STATE.INITIALISED, new String[]{"\""});
        dictionary_accepted_symbols.put(STATE.KEYOPENED, new String[]{"[^\"]"});
        dictionary_accepted_symbols.put(STATE.KEYLOAD, new String[]{"[^\"]"});
        dictionary_accepted_symbols.put(STATE.KEYCLOSED, new String[]{":"});
        dictionary_accepted_symbols.put(STATE.KVLINE, new String[]{"\"", Pattern.quote("{")});
        dictionary_accepted_symbols.put(STATE.VALINIT, new String[]{"[^\"]"});
        dictionary_accepted_symbols.put(STATE.VALLOAD, new String[]{"[^\"]"});
        dictionary_accepted_symbols.put(STATE.VALCLOSED, new String[]{",", Pattern.quote("}")});
        dictionary_accepted_symbols.put(STATE.DONE, new String[]{""});
    }

}
