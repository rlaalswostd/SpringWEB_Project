package com.example.restcontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Store;
import com.example.entity.Tables;
// import com.example.repository.OrdersRepository;
import com.example.repository.TablesRepository;

@RestController
@RequestMapping("/api/tables")
public class TablesRestcontroller {

    @Autowired
    private TablesRepository tablesRepository;

    /// @Autowired
    // private OrdersRepository ordersRepository;

    // 모든 테이블 조회
    // 127.0.0.1:8080/ROOT/api/tables/selectlist.do
    @GetMapping("/select.do")
    public Map<String, Object> getTableList(@RequestParam(required = false) String storeId) {
        Map<String, Object> map = new HashMap<>();
        try {
            List<Tables> tableList;

            if (storeId != null) {
                // storeId가 있을 경우 해당 매장의 테이블만 조회
                tableList = tablesRepository.findByStore_StoreId(storeId);
            } else {
                // storeId가 없으면 모든 테이블 조회
                tableList = (List<Tables>) tablesRepository.findAll();
            }

            if (!tableList.isEmpty()) {
                map.put("status", 200);
                map.put("message", "테이블 목록 조회 성공");

                List<Map<String, Object>> tableDataList = new ArrayList<>();
                for (Tables table : tableList) {
                    Map<String, Object> tableData = new HashMap<>();
                    tableData.put("tableId", table.getTableId());
                    tableData.put("isOccupied", table.getIsOccupied());
                    tableData.put("tableNumber", table.getTableNumber());

                    if (table.getStore() != null) {
                        tableData.put("storeId", table.getStore().getStoreId());
                    }
                    tableDataList.add(tableData);
                }

                map.put("data", tableDataList);
            }
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "에러가 발생했습니다: " + e.getMessage());
        }
        return map;
    }

    // 특정 테이블 조회
    // 127.0.0.1:8080/ROOT/api/tables/selectlist.do ?tableId=table_id
    @GetMapping(value = "/selectlist.do")
    public ResponseEntity<Map<String, Object>> getTableById(@RequestParam Integer tableId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Tables> table = tablesRepository.findById(tableId);
        if (table.isPresent()) {
            Tables foundTable = table.get();

            // 테이블에서 필요한 필드만 추출
            Map<String, Object> tableData = new HashMap<>();
            tableData.put("tableId", foundTable.getTableId());
            tableData.put("isOccupied", foundTable.getIsOccupied());
            tableData.put("tableNumber", foundTable.getTableNumber());

            // storeId만 포함 (store가 있을 경우)
            if (foundTable.getStore() != null) {
                tableData.put("storeId", foundTable.getStore().getStoreId());
            }

            response.put("status", "성공");
            response.put("data", tableData);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Table not found");
            return ResponseEntity.notFound().build();
        }
    }

    // 새 테이블 생성
    // 127.0.0.1:8080/ROOT/api/tables/insert.do
    @PostMapping(value = "insert.do")
    public Map<String, Object> createTable(@RequestBody Tables table) {
        Map<String, Object> response = new HashMap<>();

        // 테이블 번호 중복 여부 확인
        Optional<Tables> existingTable = tablesRepository.findByTableNumberAndStore_StoreId(
                table.getTableNumber(), table.getStore().getStoreId());

        if (existingTable.isPresent()) {
            response.put("status", "conflict");
            response.put("message", "이미 사용 중인 테이블 번호입니다.");
            return response;
        }

        // 테이블 저장
        Tables savedTable = tablesRepository.save(table);

        // 필요한 필드만 추출하여 반환
        Map<String, Object> tableData = new HashMap<>();
        tableData.put("tableId", savedTable.getTableId());
        tableData.put("tableNumber", savedTable.getTableNumber());
        tableData.put("isOccupied", savedTable.getIsOccupied());

        // storeId가 있을 경우에만 추가
        if (savedTable.getStore() != null) {
            tableData.put("storeId", savedTable.getStore().getStoreId());
        }

        // 성공 메시지 및 데이터 반환
        response.put("status", "success");
        response.put("data", tableData);
        return response;
    }

    // 기존 테이블 업데이트
    @PutMapping(value = "/update.do")
    public ResponseEntity<Map<String, Object>> updateTable(@RequestBody Map<String, Object> tableDetails) {
        Map<String, Object> response = new HashMap<>();

        // tableId를 JSON에서 추출하여 Integer로 변환
        Integer tableId = (Integer) tableDetails.get("tableId");
        if (tableId == null) {
            response.put("status", "error");
            response.put("message", "Table ID is required");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Tables> table = tablesRepository.findById(tableId);
        if (table.isPresent()) {
            Tables updatedTable = table.get();

            // tableDetails로부터 다른 필드를 추출 및 설정
            updatedTable.setTableNumber((String) tableDetails.get("tableNumber"));
            updatedTable.setIsOccupied((Boolean) tableDetails.get("isOccupied"));

            // store 정보를 추출하여 객체로 설정
            Map<String, Object> storeDetails = (Map<String, Object>) tableDetails.get("store");
            if (storeDetails != null) {
                String storeId = (String) storeDetails.get("storeId");
                if (storeId != null) {
                    Store store = new Store();
                    store.setStoreId(storeId);
                    updatedTable.setStore(store);
                }
            }

            // 테이블 업데이트 저장
            tablesRepository.save(updatedTable);

            // 필요한 필드만 추출하여 응답
            Map<String, Object> tableData = new HashMap<>();
            tableData.put("tableId", updatedTable.getTableId());
            tableData.put("tableNumber", updatedTable.getTableNumber());
            tableData.put("isOccupied", updatedTable.getIsOccupied());

            // storeId만 포함 (store가 있을 경우)
            if (updatedTable.getStore() != null) {
                tableData.put("storeId", updatedTable.getStore().getStoreId());
            }

            response.put("status", "success");
            response.put("data", tableData);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Table not found");
            return ResponseEntity.notFound().build();
        }
    }

    // 테이블 삭제
    @DeleteMapping("/delete.do")
    public ResponseEntity<Map<String, Object>> deleteTable(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        Integer tableId = (Integer) request.get("tableId"); // tableId를 JSON 요청에서 추출
        if (tableId == null) {
            response.put("status", "error");
            response.put("message", "Table ID is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (tablesRepository.existsById(tableId)) {
            // 주문 테이블에서 해당 tableId를 참조하는 행들을 먼저 삭제
            // ordersRepository.deleteByTableId(tableId); // 이 부분 수정됨

            // 이후 tables 테이블에서 해당 행 삭제
            tablesRepository.deleteById(tableId);

            response.put("status", "success");
            response.put("message", "Table and associated orders deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Table not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    // 테이블삭제2
    @DeleteMapping("/delete/{tableId}")
    public ResponseEntity<Map<String, Object>> deleteTable(@PathVariable Integer tableId) {
        Map<String, Object> response = new HashMap<>();

        if (tableId == null) {
            response.put("status", "error");
            response.put("message", "Table ID is required");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Tables> table = tablesRepository.findById(tableId);
        if (table.isPresent()) {
            Tables tableToDelete = table.get();

            // 테이블이 사용 중인 경우 삭제할 수 없도록 처리
            if (tableToDelete.getIsOccupied()) {
                response.put("status", "error");
                response.put("message", "사용중인 테이블은 삭제할 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 테이블 삭제 (Cascade로 관련된 Orders와 OrderItem도 삭제됨)
            tablesRepository.deleteById(tableId);

            response.put("status", "success");
            response.put("message", "테이블이 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Table not found");
            return ResponseEntity.status(404).body(response);
        }
    }

}
