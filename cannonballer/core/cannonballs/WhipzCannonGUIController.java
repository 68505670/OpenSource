package scripts.core.cannonballs;

import com.allatori.annotations.DoNotRename;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import scripts.core.gui.AbstractGUIController;

import java.net.URL;
import java.util.ResourceBundle;

@DoNotRename
public class WhipzCannonGUIController extends AbstractGUIController {

    @DoNotRename
    @FXML
    TextField restockAmount;

    @DoNotRename
    @FXML
    CheckBox doricsCheckBox;

    @DoNotRename
    @FXML
    CheckBox knightsCheckBox;

    @DoNotRename
    @FXML
    CheckBox cannonCheckBox;

    @DoNotRename
    @FXML
    CheckBox levelsCheckBox;

    @DoNotRename
    @FXML
    TextField sellPercentField;

    @DoNotRename
    @FXML
    TextField buyPercentField;

    @DoNotRename
    @FXML
    public void startButtonPressed() {
        WhipzCannonBaller.dwarfCannonQuest = cannonCheckBox.isSelected();
        WhipzCannonBaller.doricQuest = doricsCheckBox.isSelected();
        WhipzCannonBaller.knightSwordQuest = knightsCheckBox.isSelected();
        WhipzCannonBaller.levelTask = levelsCheckBox.isSelected();
        WhipzCannonBaller.restockAmount = Integer.parseInt(restockAmount.getText());
        WhipzCannonBaller.buyPrice = Double.parseDouble(buyPercentField.getText());
        WhipzCannonBaller.sellPrice = Double.parseDouble(sellPercentField.getText());
        this.getGUI().close();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
