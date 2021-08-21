package scripts.core;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;
import scripts.core.paint.PaintInfo;
import scripts.core.paint.Painter;
import scripts.dax_api.api_lib.WebWalkerServerApi;
import scripts.dax_api.api_lib.models.DaxCredentials;
import scripts.dax_api.api_lib.models.DaxCredentialsProvider;

import java.awt.*;
import java.io.IOException;

@ScriptManifest(category = "Tools", authors = {"Whipz"}, name = "ShowOfScript")
public class ShowOffScript extends Script implements PaintInfo, Painting {

    final Painter corePaint = new Painter(this, Painter.PaintLocations.BOTTOM_LEFT_CHATBOX,
            new Color[] { new Color(255, 251, 255) }, "Trebuchet MS", new Color[] { new Color(93, 156, 236, 255) },
            new Color[] { new Color(39, 95, 175) }, 1, false, 5, 3, 0);

    @Override
    public void run() {
        WebWalkerServerApi.getInstance().setDaxCredentialsProvider(new DaxCredentialsProvider() {
            @Override
            public DaxCredentials getDaxCredentials() {
                return new DaxCredentials("sub_DPjXXzL5DeSiPf", "PUBLIC-KEY");
            }
        });
        while(true) {
            try {
                new ShowOffScriptEvent(this).execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String[] getPaintInfo() {
        return new String[] {"Nothing to see here"};
    }

    @Override
    public void onPaint(Graphics g) {
        corePaint.paint(g);
    }

    /*@Override
	public void passArguments(HashMap<String, String> arg) {
		/*HashMap<String, String> parsedArgs = get(arg);
		if (parsedArgs.containsKey("BankEvent")) {
			General.println("BankEvent is: " + parsedArgs.get("BankEvent"));
		}
		if (parsedArgs.containsKey("LoadoutEvent")) {
			General.println("LoadoutEvent is: " + parsedArgs.get("LoadoutEvent"));
		}
}

    public static HashMap<String, String> get(HashMap<String, String> userInput) {
        if (userInput.containsKey("custom_input")) {
            return getFromString(userInput.get("custom_input"));
        }
        return new HashMap<>();
    }

    public static HashMap<String, String> getFromString(String argumentsString) {
        HashMap<String, String> arguments = new HashMap<>();
        if (argumentsString == null) {
            return arguments;
        }

        String[] argList = argumentsString.split(";(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split everything by ;
        for (String currentArgument : argList) {
            if (!currentArgument.contains(":")) {
                break;
            }
            String[] argumentSplit = currentArgument.split(":");
            arguments.put(argumentSplit[0].replaceAll("^\"|\"$", ""), argumentSplit[1].replaceAll("^\"|\"$", ""));
        }
        return arguments;
    }*/

}
