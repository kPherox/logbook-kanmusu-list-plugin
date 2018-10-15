package logbook.plugin.kanmusulist.gui;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.bean.ShipMst;
import logbook.bean.ShipMstCollection;
import logbook.internal.gui.WindowController;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class KanmusuListGeneratorController extends WindowController {

    private Map<Integer, Integer> shipIdAndBeforeId = emptyMap();

    @FXML
    private Label result;

    @FXML
    private TextField kanmusuList;

    @FXML
    private CheckBox exclusionDuplicateLv1;

    @FXML
    private CheckBox inclusionDuplicateLocked;

    @FXML
    void copyToClipboard(ActionEvent event) {
        this.create(event);

        ClipboardContent content = new ClipboardContent();
        content.putString(this.kanmusuList.getText());
        boolean result = Clipboard.getSystemClipboard().setContent(content);

        this.result.setText(result ? "クリップボードにコピーしました！" : "クリップボードへのコピーに失敗しました");
    }

    @FXML
    void create(ActionEvent event) {
        this.result.setText("生成しています...");

        this.generate();

        this.result.setText("生成しました！");
    }

    private void generate() {
        this.generateShipIdAndBeforeId();

        Map<Integer, List<SimpleEntry<Integer, Ship>>> ships = new HashMap<>();
        for (Ship ship : ShipCollection.get().getShipMap().values()) {
            // 初期のShip IDと改造数を取得する
            SimpleEntry<Integer, Integer> charIdAndLvSuffix = this.getCharIdAndLvSuffix(ship.getShipId());
            int charId = charIdAndLvSuffix.getKey();
            int lvSuffix = charIdAndLvSuffix.getValue();

            if (! ships.containsKey(charId)) {
                ships.put(charId, new ArrayList<>());
            }
            ships.get(charId).add(new SimpleEntry<>(lvSuffix, ship));
        }

        String format = ships.entrySet().stream()
            .map(shipEntry -> shipEntry.getValue().stream()
                .filter(ship -> shipEntry.getValue().size() == 1
                                || !(this.exclusionDuplicateLv1.isSelected() && ship.getValue().getLv() == 1)
                                || (this.inclusionDuplicateLocked.isSelected() && ship.getValue().getLocked()))
                .map(ship -> ship.getValue().getLv() + "." + ship.getKey())
                .collect(joining(",", shipEntry.getKey() + ":", "")))
            .collect(joining("|", ".2|", ""));

        this.kanmusuList.setText(format);
    }

    private void generateShipIdAndBeforeId() {
        // マスターデータから改造前と後のMapを作成する
        this.shipIdAndBeforeId = ShipMstCollection.get().getShipMap().values().stream()
            .filter(ship -> ship.getAftershipid() != null) // 改造先がない場合は含まない
            .collect(toMap(ship -> ship.getAftershipid(), ship -> ship.getId(), (old, shipId) -> shipId > old ? old : shipId)); // 既に追加されていてShipIDが追加されているものより大きい場合（コンバート改装）は変更しない
    }

    private SimpleEntry<Integer, Integer> charIdAndLvSuffix(int shipId, int count) {
        if (shipIdAndBeforeId.containsKey(shipId)) {
            // 改造前の値がある場合は再帰処理
            int beforeShipId = this.shipIdAndBeforeId.get(shipId);
            return this.charIdAndLvSuffix(beforeShipId, count+1);
        }

        // 改造前の値がない場合はSimpleEntryを返す
        return new SimpleEntry<>(shipId, count);
    }

    private SimpleEntry<Integer, Integer> getCharIdAndLvSuffix(int shipId) {
        return this.charIdAndLvSuffix(shipId, 1);
    }

}
