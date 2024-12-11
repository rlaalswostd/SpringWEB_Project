package com.example.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.example.entity.Store;
import com.example.entity.Admin;
import com.example.repository.StoreRepository;
import com.example.repository.AdminRepository;

@RestController
@RequestMapping("/api/store")
public class StoreRestController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private AdminRepository adminRepository;

    // 페이지네이션을 통한 매장 전체 조회
    @GetMapping("/selectall.do")
    public Map<String, Object> getAllStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "storeId") String sortBy, // 정렬 기준
            @RequestParam(defaultValue = "asc") String sortDir // 정렬 방향
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 정렬 설정
            Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Store> storePage = storeRepository.findAll(pageable);

            // 매장 데이터 변환
            List<Map<String, Object>> storeDetails = storePage.getContent().stream().map(store -> {
                Map<String, Object> storeMap = new HashMap<>();
                storeMap.put("storeId", store.getStoreId());
                storeMap.put("storeName", store.getStoreName());
                storeMap.put("address", store.getAddress());
                storeMap.put("phone", store.getPhone());
                storeMap.put("isActive", store.getIsActive());
                storeMap.put("adminId", store.getAdmin().getAdminId());
                return storeMap;
            }).collect(Collectors.toList());

            response.put("status", 200);
            response.put("result", storeDetails);
            response.put("totalPages", storePage.getTotalPages());
            response.put("currentPage", storePage.getNumber());
        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "에러 발생: " + e.getMessage());
        }
        return response;
    }

    /// 모든 매장 조회
    // 127.0.0.1:8080/ROOT/api/store/select.do
    @GetMapping("/select.do")
    public Map<String, Object> getAllStores() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Store> stores = storeRepository.findAll();

            // 필요한 필드만 추출
            List<Map<String, Object>> storeDetails = stores.stream().map(store -> {
                Map<String, Object> storeMap = new HashMap<>();
                storeMap.put("storeId", store.getStoreId());
                storeMap.put("storeName", store.getStoreName());
                storeMap.put("address", store.getAddress());
                storeMap.put("phone", store.getPhone());
                storeMap.put("isActive", store.getIsActive());
                storeMap.put("adminId", store.getAdmin().getAdminId());
                return storeMap;
            }).collect(Collectors.toList());

            response.put("status", 200);
            response.put("result", storeDetails);
        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "에러 발생: " + e.getMessage());
        }
        return response;
    }

    // select store api( 사장 로그인 했을때 스토어 조회되는 주소 )

    // 매장 ID로 매장 조회 (스토어 선택하면 storeid 불러와서 해당 매장 컨트롤하는 주소)
    // 127.0.0.1:8080/ROOT/api/store/selectlist.do
    @GetMapping("/selectlist.do")
    public ResponseEntity<Map<String, Object>> getStoreById(@RequestParam String storeId) {
        Map<String, Object> response = new HashMap<>();
        Optional<Store> storeOptional = storeRepository.findById(storeId);

        if (storeOptional.isPresent()) {
            Store store = storeOptional.get();

            // 필요한 필드만 추출하여 응답
            Map<String, Object> storeInfo = new HashMap<>();
            storeInfo.put("storeId", store.getStoreId());
            storeInfo.put("storeName", store.getStoreName());
            storeInfo.put("address", store.getAddress());
            storeInfo.put("phone", store.getPhone());
            storeInfo.put("isActive", store.getIsActive());
            storeInfo.put("adminId", store.getAdmin().getAdminId());

            response.put("status", 200);
            response.put("data", storeInfo);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", 404);
            response.put("message", "매장 정보를 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // adminId로 해당 사장님 매장 조회
    // 127.0.0.1:8080/ROOT/api/store/
    @GetMapping("/stores/{adminId}")
    public ResponseEntity<Map<String, Object>> getStoresByAdminId(@PathVariable Integer adminId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // adminId로 매장 목록 조회
            List<Store> stores = storeRepository.findByAdminId(adminId);

            if (stores.isEmpty()) {
                response.put("status", "error");
                response.put("message", "매장이 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 매장 정보를 상세하게 매핑
            List<Map<String, Object>> storeList = stores.stream()
                    .map(store -> {
                        Map<String, Object> storeInfo = new HashMap<>();
                        storeInfo.put("storeId", store.getStoreId());
                        storeInfo.put("storeName", store.getStoreName());
                        storeInfo.put("address", store.getAddress());
                        storeInfo.put("phone", store.getPhone());
                        storeInfo.put("isActive", store.getIsActive());

                        // 관리자 정보 추가
                        Map<String, Object> adminInfo = new HashMap<>();
                        adminInfo.put("adminId", store.getAdmin().getAdminId());
                        adminInfo.put("adminName", store.getAdmin().getAdminName());
                        storeInfo.put("admin", adminInfo);

                        return storeInfo;
                    })
                    .collect(Collectors.toList());

            response.put("status", "success");
            response.put("data", storeList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 매장 생성
    // 127.0.0.1:8080/ROOT/api/store/insert.do
    @PostMapping("/insert.do")
    public Map<String, Object> createStore(@RequestBody Store store) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 매장 ID가 이미 존재하는지 확인
            Optional<Store> existingStore = storeRepository.findById(store.getStoreId());
            if (existingStore.isPresent()) {
                response.put("status", 409); // 409 Conflict
                response.put("message", "매장 ID가 이미 존재합니다.");
                return response;
            }

            // 관리자 ID로 관리자 확인
            Optional<Admin> adminOptional = adminRepository.findById(store.getAdmin().getAdminId());
            if (!adminOptional.isPresent()) {
                response.put("status", 404);
                response.put("message", "관리자를 찾을 수 없습니다.");
                return response;
            }

            Admin admin = adminOptional.get();
            System.out.println(admin.getRole());  // role 값 출력

            // 관리자 role이 super_admin인지 확인 (대소문자 구분)
            if (!admin.getRole().name().equals("SUPER_ADMIN")) { // Role enum의 name() 메서드를 사용
                response.put("status", 403); // 403 Forbidden
                response.put("message", "관리자는 super_admin 권한이어야 합니다.");
                return response;
            }

            store.setAdmin(admin);
            Store createdStore = storeRepository.save(store);

            // 반환할 데이터만 추출
            Map<String, Object> storeData = new HashMap<>();
            storeData.put("storeId", createdStore.getStoreId());
            storeData.put("storeName", createdStore.getStoreName());
            storeData.put("address", createdStore.getAddress());
            storeData.put("phone", createdStore.getPhone());
            storeData.put("isActive", createdStore.getIsActive());
            storeData.put("adminId", createdStore.getAdmin().getAdminId());

            response.put("status", 200);
            response.put("message", "매장이 성공적으로 생성되었습니다.");
            response.put("data", storeData);
        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "매장 생성 중 오류 발생: " + e.getMessage());
        }
        return response;
    }

    // 매장 수정
    @Transactional
    // 127.0.0.1:8080/ROOT/api/store/update.do
    @PutMapping("/update.do")
    public ResponseEntity<Map<String, Object>> updateStore(@RequestBody Store store) {
        Map<String, Object> response = new HashMap<>();
        try {
            // ID로 매장 조회
            Optional<Store> existingStoreOptional = storeRepository.findById(store.getStoreId());

            if (!existingStoreOptional.isPresent()) {
                response.put("status", 404);
                response.put("message", "매장을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Store existingStore = existingStoreOptional.get();
            // 기존 매장에서 수정할 필드만 업데이트
            existingStore.setStoreName(store.getStoreName());
            existingStore.setAddress(store.getAddress());
            existingStore.setPhone(store.getPhone());
            existingStore.setIsActive(store.getIsActive());

            // Admin 아이디가 제공되면 처리
            if (store.getAdmin() != null && store.getAdmin().getAdminId() != null) {
                Optional<Admin> adminOptional = adminRepository.findById(store.getAdmin().getAdminId());
                if (adminOptional.isPresent()) {
                    // Admin이 존재하면 Store의 Admin을 업데이트
                    existingStore.setAdmin(adminOptional.get());
                } else {
                    // Admin이 존재하지 않으면 메시지 반환
                    response.put("status", 404);
                    response.put("message", "Admin 아이디가 존재하지 않습니다.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            }

            // 수정된 매장 정보를 저장
            storeRepository.save(existingStore);

            // 수정된 데이터 반환
            Map<String, Object> storeData = new HashMap<>();
            storeData.put("storeId", existingStore.getStoreId());
            storeData.put("storeName", existingStore.getStoreName());
            storeData.put("address", existingStore.getAddress());
            storeData.put("phone", existingStore.getPhone());
            storeData.put("isActive", existingStore.getIsActive());
            storeData.put("adminId", existingStore.getAdmin() != null ? existingStore.getAdmin().getAdminId() : null);

            response.put("status", 200);
            response.put("message", "매장 정보가 성공적으로 수정되었습니다.");
            response.put("data", storeData);
        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "매장 수정 중 오류 발생: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    // 매장 삭제
    @DeleteMapping("/delete.do")
    // 127.0.0.1:8080/ROOT/api/store/delete.do?storeId= ?
    public ResponseEntity<Map<String, Object>> deleteStore(@RequestParam String storeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (storeId == null || storeId.isEmpty()) {
                response.put("status", 400);
                response.put("message", "storeId가 필요합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (storeRepository.existsById(storeId)) {
                storeRepository.deleteById(storeId);
                response.put("status", 200);
                response.put("message", "매장이 성공적으로 삭제되었습니다.");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", 404);
                response.put("message", "매장 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "매장 삭제 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
