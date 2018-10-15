package logbook.plugin.kanmusulist.gui;

import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import logbook.internal.LoggerHolder;
import logbook.internal.gui.Tools;
import logbook.internal.gui.WindowController;
import logbook.plugin.PluginContainer;
import logbook.plugin.gui.MainExtMenu;

public class KanmusuListGeneratorMenu implements MainExtMenu {
    private static Stage stage = null;

    @Override
    public MenuItem getContent() {
        MenuItem menu = new MenuItem("艦隊晒しフォーマット");
        menu.setOnAction(e -> {
            if (this.stage != null) {
                this.stage.toFront();
                return;
            }

            try {
                Stage stage = new Stage();
                URL url = this.getClass().getClassLoader()
                        .getResource("kanmusulist/gui/KanmusuListGenerator.fxml");
                FXMLLoader loader = new FXMLLoader(url);
                loader.setClassLoader(PluginContainer.getInstance().getClassLoader());
                Parent root = loader.load();
                stage.setScene(new Scene(root));
                WindowController controller = loader.getController();
                controller.setWindow(stage);

                stage.initOwner(menu.getParentPopup().getOwnerWindow());
                stage.setTitle("艦隊晒しフォーマット");
                Tools.Windows.setIcon(stage);
                Tools.Windows.defaultCloseAction(controller);
                Tools.Windows.defaultOpenAction(controller);
                stage.setOnCloseRequest(event -> this.stage = null);
                this.stage = stage;
                this.stage.show();
            } catch (Exception ex) {
                LoggerHolder.get().warn("艦隊晒しフォーマットを開けませんでした", ex);
            }
        });
        return menu;
    }

}