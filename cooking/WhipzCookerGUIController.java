package scripts.core.skills.cooking;

import com.allatori.annotations.DoNotRename;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.tribot.api.General;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSTile;
import scripts.core.gui.AbstractGUIController;

import java.net.URL;
import java.util.ResourceBundle;

@DoNotRename
public class WhipzCookerGUIController extends AbstractGUIController {

    @DoNotRename @FXML
    public Button startButton;

    @DoNotRename @FXML
    private ComboBox comboBox;

    @DoNotRename @FXML
    private ComboBox comboLocation;


    @DoNotRename @FXML
    public void startScriptPressed() {
        General.println("Start Pressed");
        General.println(comboBox.getSelectionModel().getSelectedItem().toString() + " selected");
        General.println(comboLocation.getSelectionModel().getSelectedItem().toString() + " selected");
        if(comboBox.getSelectionModel().getSelectedItem().toString().equals("Make Wine")) {
            WhipzAIOCookerScript.setStatus("Making Wines");
            WhipzAIOCookerScript.makeWines = true;
        } else if(comboBox.getSelectionModel().getSelectedItem().toString().equals("Make Bread")) {
            WhipzAIOCookerScript.setStatus("Making Bread");
            WhipzAIOCookerScript.makeBread = true;
        } else if(comboBox.getSelectionModel().getSelectedItem().toString().equals("Make Plain Pizza")) {
            WhipzAIOCookerScript.setStatus("Making plain pizza");
            WhipzAIOCookerScript.makePlainPizza = true;
        } else if(comboBox.getSelectionModel().getSelectedItem().toString().equals("Make Potato with Butter")) {
            WhipzAIOCookerScript.setStatus("Making Potato with Butter");
            WhipzAIOCookerScript.makePotatoButter = true;
        } else if(comboBox.getSelectionModel().getSelectedItem().toString().equals("Make Potato with Cheese")) {
            WhipzAIOCookerScript.setStatus("Making Potato with Cheese");
            WhipzAIOCookerScript.makePotatoCheese = true;
        } else if(comboBox.getSelectionModel().getSelectedItem().toString().equals("Make Meat Pizza")) {
            WhipzAIOCookerScript.setStatus("Making Meat Pizza");
            WhipzAIOCookerScript.makeMeatPizza = true;
        } else if(comboBox.getSelectionModel().getSelectedItem().toString().equals("Make Pineapple Pizza")) {
            WhipzAIOCookerScript.setStatus("Making Pineapple Pizza");
            WhipzAIOCookerScript.makePineapplePizza = true;
        } else if(comboBox.getSelectionModel().getSelectedItem().toString().equals("Make Tuna Potato")) {
            WhipzAIOCookerScript.setStatus("Making Tuna Potato");
            WhipzAIOCookerScript.makeTunaPotato = true;
        } else {
            WhipzAIOCookerScript.setStatus("We are Cooking: "+comboBox.getSelectionModel().getSelectedItem().toString());
            WhipzAIOCookerScript.choosenFood = comboBox.getSelectionModel().getSelectedItem().toString();
        }
        if(comboLocation.getSelectionModel().getSelectedItem().toString().equals("Rouge's Den")) {
            WhipzAIOCookerScript.setStatus("Cooking Location: "+comboLocation.getSelectionModel().getSelectedItem().toString());
            WhipzAIOCookerScript.area = new RSArea(
                    new RSTile[] {
                            new RSTile(3039, 4969, 1),
                            new RSTile(3054, 4971, 1),
                            new RSTile(3053, 4972, 1),
                            new RSTile(3053, 4976, 1),
                            new RSTile(3046, 4980, 1),
                            new RSTile(3045, 4979, 1),
                            new RSTile(3042, 4979, 1),
                            new RSTile(3041, 4976, 1),
                            new RSTile(3040, 4976, 1),
                            new RSTile(3040, 4973, 1),
                            new RSTile(3041, 4972, 1),
                            new RSTile(3041, 4971, 1),
                            new RSTile(3040, 4971, 1),
                            new RSTile(3039, 4970, 1)
                    }
            );
        } else if(comboLocation.getSelectionModel().getSelectedItem().toString().equals("Myth's guild")) {
            WhipzAIOCookerScript.setStatus("Cooking Location: "+comboLocation.getSelectionModel().getSelectedItem().toString());
            WhipzAIOCookerScript.area = new RSArea(
                    new RSTile[] {
                            new RSTile(2466, 2845, 1),
                            new RSTile(2467, 2846, 1),
                            new RSTile(2467, 2849, 1),
                            new RSTile(2466, 2850, 1),
                            new RSTile(2464, 2849, 1),
                            new RSTile(2463, 2845, 1)
                    }
            );
        } else if(comboLocation.getSelectionModel().getSelectedItem().toString().equals("Cook's guild")) {
            WhipzAIOCookerScript.setStatus("Cooking Location: "+comboLocation.getSelectionModel().getSelectedItem().toString());
            WhipzAIOCookerScript.area = new RSArea(
                    new RSTile[] {
                            new RSTile(3144, 3454, 0),
                            new RSTile(3147, 3454, 0),
                            new RSTile(3149, 3452, 0),
                            new RSTile(3149, 3450, 0),
                            new RSTile(3147, 3450, 0),
                            new RSTile(3145, 3450, 0),
                            new RSTile(3144, 3451, 0)
                    }
            );
        } else if(comboLocation.getSelectionModel().getSelectedItem().toString().equals("Catherby")) {
            WhipzAIOCookerScript.setStatus("Cooking Location: "+comboLocation.getSelectionModel().getSelectedItem().toString());
            WhipzAIOCookerScript.area = new RSArea(new RSTile(2815, 3444, 0), new RSTile(2818, 3439, 0));
        } else if(comboLocation.getSelectionModel().getSelectedItem().toString().equals("Lumbrdige")) {
            WhipzAIOCookerScript.setStatus("Cooking Location: "+comboLocation.getSelectionModel().getSelectedItem().toString());
            WhipzAIOCookerScript.area = new RSArea(new RSTile(3212, 3212, 0), new RSTile(3208, 3217, 0));
        } else if(comboLocation.getSelectionModel().getSelectedItem().toString().equals("Al Kharid")) {
            WhipzAIOCookerScript.setStatus("Cooking Location: "+comboLocation.getSelectionModel().getSelectedItem().toString());
            WhipzAIOCookerScript.area = new RSArea(new RSTile(3272, 3188, 0), new RSTile(3279, 3184, 0));
        } else if(comboLocation.getSelectionModel().getSelectedItem().toString().equals("Edgeville")) {
            WhipzAIOCookerScript.setStatus("Cooking Location: "+comboLocation.getSelectionModel().getSelectedItem().toString());
            WhipzAIOCookerScript.area = new RSArea(new RSTile(3081, 3489, 0), new RSTile(3077, 3496, 0));
        } else if(comboLocation.getSelectionModel().getSelectedItem().toString().equals("Varrock East")) {
            WhipzAIOCookerScript.setStatus("Cooking Location: "+comboLocation.getSelectionModel().getSelectedItem().toString());
            WhipzAIOCookerScript.area = new RSArea(
                    new RSTile[] {
                            new RSTile(3241, 3409, 0),
                            new RSTile(3241, 3411, 0),
                            new RSTile(3242, 3412, 0),
                            new RSTile(3242, 3414, 0),
                            new RSTile(3241, 3415, 0),
                            new RSTile(3241, 3417, 0),
                            new RSTile(3236, 3417, 0),
                            new RSTile(3236, 3415, 0),
                            new RSTile(3235, 3414, 0),
                            new RSTile(3235, 3412, 0),
                            new RSTile(3236, 3411, 0),
                            new RSTile(3236, 3409, 0)
                    }
            );
        }
        this.getGUI().close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comboBox.getItems().removeAll(comboBox.getItems());
        comboBox.getItems().addAll("Raw shrimps", "Raw chicken", "Raw beef", "Raw sardine", "Make Bread", "Raw herring", "Raw mackerel", "Raw trout", "Raw cod", "Raw pike", "Raw salmon", "Raw tuna", "Make Wine", "Make Stew", "Raw lobster", "Raw bass", "Make Plain Pizza", "Raw swordfish", "Make Potato with Butter", "Make Potato with Cheese", "Make Meat Pizza", "Raw monkfish", "Raw Shark", "Make Pineapple Pizza", "Raw manta ray", "Make Tuna Potato", "Raw anglerfish");
        comboBox.getSelectionModel().select("Make Wine");
        comboLocation.getItems().removeAll(comboLocation.getItems());
        comboLocation.getItems().addAll("Rouge's Den", "Myth's guild", "Cook's guild", "Catherby", "Lumbridge", "Al Kharid", "Edgeville", "Varrock East");
        comboLocation.getSelectionModel().select("Rouge's Den");
    }
}
