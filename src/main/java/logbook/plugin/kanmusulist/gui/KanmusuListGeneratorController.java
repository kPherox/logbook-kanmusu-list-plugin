package logbook.plugin.kanmusulist.gui;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import logbook.bean.Ship;
import logbook.bean.ShipCollection;
import logbook.bean.ShipMst;
import logbook.bean.ShipMstCollection;
import logbook.internal.gui.WindowController;

public class KanmusuListGeneratorController extends WindowController {

    @FXML
    private TextField kanmusuList;

    @FXML
    void create(ActionEvent event) {
        Map<Integer, String> format = new HashMap<Integer, String>();
        for (Ship ship : ShipCollection.get().getShipMap().values()) {
            int lv = ship.getLv();
            int shipId = ship.getShipId();

            // 初期のShip IDと改造数を取得する
            SimpleEntry<Integer, Integer> charIdAndLvSuffix = this.getCharIdAndLvSuffix(shipId);
            int charId = charIdAndLvSuffix.getKey();
            int lvSuffix = charIdAndLvSuffix.getValue();

            if (format.containsKey(charId)) {
                format.put(charId, String.format("%s,%d.%d", format.get(charId), lv, lvSuffix));
            } else {
                format.put(charId, String.format("%d.%d", lv, lvSuffix));
            }
        }
        StringJoiner result = new StringJoiner("|");
        // 艦隊晒しのprefix
        result.add(".2");
        format.forEach((id, value) -> {
            result.add(String.format("%d:%s", id, value));
        });
        this.kanmusuList.setText(result.toString());
    }

    private Map<Integer, Integer> getShipIdAndBeforeId() {
        Map<Integer, Integer> shipIdAndAfterId = new HashMap<Integer, Integer>();
        // マスターデータからShipIDと改造先のMapを作成する
        for (ShipMst ship : ShipMstCollection.get().getShipMap().values()) {
            // 改造先がないか、すでに追加されているならスキップ（コンバートが該当するはず）
            Integer afterShipId = ship.getAftershipid();
            if (afterShipId == null || shipIdAndAfterId.containsValue(afterShipId)) {
                continue;
            }
            // 改造元と改造先を追加
            shipIdAndAfterId.put(ship.getId(), afterShipId);
        }
        // 入れ替えて改造後ではなく改造前を取得するMapにする
        Map<Integer, Integer> shipIdAndBeforeId = shipIdAndAfterId.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

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
        return new SimpleEntry<Integer, Integer>(shipId, count);
    }

    private SimpleEntry<Integer, Integer> getCharIdAndLvSuffix(int shipId) {
        Map<Integer, Integer> shipIdAndBeforeId = this.getShipIdAndBeforeId();
        return this.charIdAndLvSuffix(shipIdAndBeforeId, shipId, 0);
    }

}
