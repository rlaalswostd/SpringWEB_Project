package com.example.restcontroller;

import com.example.entity.Admin;
import com.example.entity.Contract;
import com.example.entity.Store;
import com.example.repository.AdminRepository;
import com.example.repository.ContractRepository;
import com.example.repository.StoreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/contract")
public class ContractRestController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ContractRepository contractRepository;

    // 전체 각 매장 계약 현황 조회(페이지네이션 및 정렬)
    @GetMapping("/dashboard.do")
    public Map<String, Object> getStoresWithContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy, // 정렬 기준 (기본: 시작일)
            @RequestParam(defaultValue = "asc") String sortDir // 정렬 방향
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 만료된 계약 상태를 업데이트
            contractRepository.updateExpiredContracts(LocalDate.now());

            // Contract의 정렬 기준 설정
            Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by("status").ascending().and(Sort.by(sortBy).ascending())
                    : Sort.by("status").ascending().and(Sort.by(sortBy).descending());

            Pageable pageable = PageRequest.of(page, size, sort);

            // Contract를 페이지네이션으로 조회
            Page<Contract> contractPage = contractRepository.findAll(pageable);

            // 결과 매핑
            List<Map<String, Object>> result = contractPage.getContent().stream().map(contract -> {
                Store store = contract.getStore(); // Contract에 연결된 Store 가져오기

                // 결과 구조에 맞는 데이터 매핑
                Map<String, Object> contractMap = new HashMap<>();
                contractMap.put("storeId", store.getStoreId());
                contractMap.put("storeName", store.getStoreName());
                contractMap.put("address", store.getAddress());
                contractMap.put("phone", store.getPhone());
                // contractMap.put("adminId", store.getAdmin().getAdminId());
                contractMap.put("isActive", store.getIsActive());
                contractMap.put("contractId", contract.getContractId());
                contractMap.put("startDate", contract.getStartDate());
                contractMap.put("endDate", contract.getEndDate());
                contractMap.put("monthlyFee", contract.getMonthlyFee());
                contractMap.put("status", contract.getStatus().toString());
                return contractMap;
            }).collect(Collectors.toList());

            // 응답 데이터 구성
            response.put("status", 200);
            response.put("result", result);
            response.put("totalPages", contractPage.getTotalPages());
            response.put("currentPage", contractPage.getNumber());
        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "에러 발생: " + e.getMessage());
        }
        return response;
    }

    // DashBoard/update
    @PutMapping("/{contractId}/update")
    @Transactional
    public ResponseEntity<?> updateContract(
            @PathVariable Integer contractId,
            @RequestBody Map<String, Object> updates) {
        try {
            // Contract 조회
            Optional<Contract> contractOpt = contractRepository.findById(contractId);
            if (contractOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Contract not found.");
            }

            Contract contract = contractOpt.get();
            Store store = contract.getStore();

            // Store 필드 업데이트
            store.setStoreName((String) updates.get("storeName"));
            store.setAddress((String) updates.get("address"));
            store.setPhone((String) updates.get("phone"));

            // Contract 필드 업데이트
            contract.setStartDate(LocalDate.parse((String) updates.get("startDate")));
            contract.setEndDate(LocalDate.parse((String) updates.get("endDate")));
            contract.setMonthlyFee(new BigDecimal((String) updates.get("monthlyFee")));
            contract.setStatus(Contract.Status.valueOf((String) updates.get("status")));

            // 저장
            storeRepository.save(store);
            contractRepository.save(contract);

            return ResponseEntity.ok("Contract updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating contract: " + e.getMessage());
        }
    }

    // 전체 계약 조회
    // 127.0.0.1:8080/ROOT/api/contract/select.do
    @GetMapping(value = "/select.do")
    public Map<String, Object> getAllContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(defaultValue = "contractId") String sortBy, // 정렬 기준
            @RequestParam(defaultValue = "asc") String sortDir // 정렬 방향
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 정렬 설정
            Sort sort = sortDir.equalsIgnoreCase("asc")
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Contract> contractPage = contractRepository.findAll(pageable);

            if (!contractPage.isEmpty()) {
                response.put("status", "success");
                response.put("message", "계약 목록 조회 성공");

                List<Map<String, Object>> contractDataList = contractPage.getContent().stream().map(contract -> {
                    Map<String, Object> contractData = new HashMap<>();
                    contractData.put("contractId", contract.getContractId());
                    contractData.put("storeId", contract.getStore().getStoreId());
                    contractData.put("startDate", contract.getStartDate());
                    contractData.put("endDate", contract.getEndDate());
                    contractData.put("monthlyFee", contract.getMonthlyFee());
                    contractData.put("status", contract.getStatus().toString());
                    return contractData;
                }).collect(Collectors.toList());

                response.put("data", contractDataList);
                response.put("totalPages", contractPage.getTotalPages());
                response.put("currentPage", contractPage.getNumber());
            } else {
                response.put("status", "error");
                response.put("message", "계약이 없습니다.");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "에러가 발생했습니다: " + e.getMessage());
        }
        return response;
    }

    // 매장 등록(계약 포함)
    @PostMapping(value = "/insertWithStore.do")
    @Transactional
    public Map<String, Object> createStoreWithContract(@RequestBody Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 요청 데이터에서 Store와 Contract 정보를 추출
            Map<String, Object> data = (Map<String, Object>) requestData.get("data");
            String storeId = (String) data.get("storeId");
            String storeName = (String) data.get("storeName");
            String address = (String) data.get("address");
            String phone = (String) data.get("phone");
            Boolean isActive = (Boolean) data.get("isActive");
            Integer adminId = (Integer) data.get("adminId");

            // Admin 엔티티를 adminId를 통해 찾기
            Optional<Admin> adminOptional = adminRepository.findById(adminId);
            if (adminOptional.isEmpty()) {
                response.put("status", "error");
                response.put("message", "관리자를 찾을 수 없습니다.");
                return response;
            }
            Admin admin = adminOptional.get(); // 찾은 Admin 객체

            // 매장 ID 중복 확인
            Optional<Store> existingStore = storeRepository.findById(storeId);
            if (existingStore.isPresent()) {
                response.put("status", "error");
                response.put("message", "이미 존재하는 매장 ID입니다.");
                return response;
            }

            // 관리자 role이 STORE_ADMIN인지 확인
            if (!admin.getRole().name().equals("STORE_ADMIN")) {
                response.put("status", 403);
                response.put("message", "관리자는 STORE_ADMIN 권한이어야 합니다.");
                return response;
            }

            // Store 객체 생성
            Store store = new Store();
            store.setStoreId(storeId);
            store.setStoreName(storeName);
            store.setAddress(address);
            store.setPhone(phone);
            store.setIsActive(isActive);
            store.setAdmin(admin);

            // Contract 정보 추출
            LocalDate startDate = LocalDate.parse((String) data.get("startDate"));
            LocalDate endDate = LocalDate.parse((String) data.get("endDate"));

            // 계약 종료일이 시작일보다 하루 이상 뒤여야 함
            if (!endDate.isAfter(startDate)) {
                response.put("status", "error");
                response.put("message", "계약 종료일은 시작일보다 최소한 하루 뒤여야 합니다.");
                return response;
            }

            BigDecimal monthlyFee = new BigDecimal((Integer) data.get("monthlyFee"));

            // 상태를 요청 데이터에서 가져오기
            String statusString = (String) data.get("status");
            Contract.Status status = Contract.Status.valueOf(statusString.toUpperCase());

            // Contract 객체 생성
            Contract contract = new Contract();
            contract.setStore(store);
            contract.setStartDate(startDate);
            contract.setEndDate(endDate);
            contract.setMonthlyFee(monthlyFee);
            contract.setStatus(status); // 요청 받은 상태를 설정

            // Store와 Contract 저장
            storeRepository.save(store);
            contractRepository.save(contract);

            // 성공 응답 반환
            Map<String, Object> contractData = new HashMap<>();
            contractData.put("contractId", contract.getContractId());
            contractData.put("storeId", store.getStoreId());
            contractData.put("startDate", contract.getStartDate());
            contractData.put("endDate", contract.getEndDate());
            contractData.put("monthlyFee", contract.getMonthlyFee());
            contractData.put("status", contract.getStatus().toString());

            response.put("status", "success");
            response.put("message", "매장과 계약이 성공적으로 생성되었습니다.");
            response.put("data", contractData);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "에러가 발생했습니다: " + e.getMessage());
        }
        return response;
    }

    // 특정 계약 조회
    // 127.0.0.1:8080/ROOT/api/contract/selectlist.do?contractId= ?
    @GetMapping(value = "/selectlist.do")
    public ResponseEntity<Map<String, Object>> getContractById(@RequestParam Integer contractId) {
        Map<String, Object> response = new HashMap<>();

        Optional<Contract> contract = contractRepository.findById(contractId);
        if (contract.isPresent()) {
            Contract foundContract = contract.get();

            // 계약에서 필요한 필드만 추출
            Map<String, Object> contractData = new HashMap<>();
            contractData.put("contractId", foundContract.getContractId());
            contractData.put("storeId", foundContract.getStore().getStoreId());
            contractData.put("startDate", foundContract.getStartDate());
            contractData.put("endDate", foundContract.getEndDate());
            contractData.put("monthlyFee", foundContract.getMonthlyFee());
            contractData.put("status", foundContract.getStatus().toString());

            response.put("status", "성공");
            response.put("data", contractData);
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "계약을 찾을 수 없습니다.");
            return ResponseEntity.status(404).body(response);
        }
    }

}
