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

import static java.util.stream.Collectors.joining;

public class KanmusuListGeneratorController extends WindowController {

    private String format = "";

    @FXML
    private Label result;

    @FXML
    private TextField kanmusuList;

    @FXML
    private CheckBox isExclusionDuplicateLv1;

    @FXML
    void copyToClipboard(ActionEvent event) {
        this.create(event);

        ClipboardContent content = new ClipboardContent();
        content.putString(this.format);
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
        Map<Integer, List<SimpleEntry<Integer, Integer>>> ships = new HashMap<>();
        for (Ship ship : ShipCollection.get().getShipMap().values()) {
            int lv = ship.getLv();
            int shipId = ship.getShipId();

            // 初期のShip IDと改造数を取得する
            SimpleEntry<Integer, Integer> charIdAndLvSuffix = this.getCharIdAndLvSuffix(shipId);
            int charId = charIdAndLvSuffix.getKey();
            int lvSuffix = charIdAndLvSuffix.getValue();

            if (! ships.containsKey(charId)) {
                ships.put(charId, new ArrayList<>());
            }
            ships.get(charId).add(new SimpleEntry<>(lv, lvSuffix));
        }

        this.format = ships.entrySet().stream()
            .map(shipEntry -> shipEntry.getValue().stream()
                .filter(level -> shipEntry.getValue().size() == 1 || !(level.getKey() == 1 && this.isExclusionDuplicateLv1.isSelected()))
                .map(level -> level.getKey() + "." + level.getValue())
                .collect(joining(",", shipEntry.getKey() + ":", "")))
            .collect(joining("|", ".2|", ""));

        this.kanmusuList.setText(this.format);
    }

    private Map<Integer, Integer> getShipIdAndBeforeId() {
        Map<Integer, Integer> shipIdAndBeforeId = new HashMap<>();
        // マスターデータから改造前と後のMapを作成する
        for (ShipMst ship : ShipMstCollection.get().getShipMap().values()) {
            // 改造先がない、もしくは既に追加されていてShipIDが追加されているものより大きい場合（コンバート改装）はスキップ
            Integer afterShipId = ship.getAftershipid();
            int shipId = ship.getId();
            if (afterShipId == null || shipIdAndBeforeId.containsKey(afterShipId) && shipIdAndBeforeId.get(afterShipId) < shipId) {
                continue;
            }
            // 改造前と後を追加
            shipIdAndBeforeId.put(afterShipId, shipId);
        }

        return shipIdAndBeforeId;
    }

    private SimpleEntry<Integer, Integer> charIdAndLvSuffix(Map<Integer, Integer> shipIdAndBeforeId, int shipId, int count) {
        count++;
        if (shipIdAndBeforeId.containsKey(shipId)) {
            // 改造前の値がある場合は再帰処理
            int beforeShipId = shipIdAndBeforeId.get(shipId);
            return this.charIdAndLvSuffix(shipIdAndBeforeId, beforeShipId, count);
        }

        // 改造前の値がない場合はSimpleEntryを返す
        return new SimpleEntry<>(shipId, count);
    }

    private SimpleEntry<Integer, Integer> getCharIdAndLvSuffix(int shipId) {
        Map<Integer, Integer> shipIdAndBeforeId = this.getShipIdAndBeforeId();
        return this.charIdAndLvSuffix(shipIdAndBeforeId, shipId, 0);
    }

}
