package logbook.plugin.kanmusulist.gui;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

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
        Map<Integer, String> ships = new HashMap<Integer, String>();
        for (Ship ship : ShipCollection.get().getShipMap().values()) {
            int lv = ship.getLv();
            int shipId = ship.getShipId();

            // 初期のShip IDと改造数を取得する
            SimpleEntry<Integer, Integer> charIdAndLvSuffix = this.getCharIdAndLvSuffix(shipId);
            int charId = charIdAndLvSuffix.getKey();
            int lvSuffix = charIdAndLvSuffix.getValue();

            if (ships.containsKey(charId)) {
                ships.put(charId, String.format("%s,%d.%d", ships.get(charId), lv, lvSuffix));
            } else {
                ships.put(charId, String.format("%d.%d", lv, lvSuffix));
            }
        }
        StringJoiner format = new StringJoiner("|");
        // 艦隊晒しのprefix
        format.add(".2");
        ships.forEach((id, value) -> {
            format.add(String.format("%d:%s", id, value));
        });
        this.kanmusuList.setText(format.toString());
    }

    private Map<Integer, Integer> getShipIdAndBeforeId() {
        Map<Integer, Integer> shipIdAndBeforeId = new HashMap<Integer, Integer>();
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
        return new SimpleEntry<Integer, Integer>(shipId, count);
    }

    private SimpleEntry<Integer, Integer> getCharIdAndLvSuffix(int shipId) {
        Map<Integer, Integer> shipIdAndBeforeId = this.getShipIdAndBeforeId();
        return this.charIdAndLvSuffix(shipIdAndBeforeId, shipId, 0);
    }

}
