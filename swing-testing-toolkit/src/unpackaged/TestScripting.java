import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class TestScripting {

	
	public static void main(String[] args) throws ScriptException {
		// create a script engine manager
	    ScriptEngineManager factory = new ScriptEngineManager();

	    // create a JavaScript engine
	    ScriptEngine engine = factory.getEngineByName("JavaScript");

	    // evaluate JavaScript code from String
	    Object result = engine.eval("java.util.Arrays.asList(\"1\",\"2\")");
	    System.out.println(result);
	    
	}

}
