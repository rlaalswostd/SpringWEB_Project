package com.example.restcontroller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.entity.Category;
import com.example.entity.Store;
import com.example.repository.CategoryRepository;
import com.example.repository.StoreRepository;

@RestController
@RequestMapping(value = "/api/category")
public class CategoryRestController {
    @Autowired
    private CategoryRepository categoryRepository; // 카테고리 관련 DB 작업을 처리하는 레포지토리

    @Autowired
    private StoreRepository storeRepository;

    /// 카테고리 등록
    // 127.0.0.1:8080/ROOT/api/category/store/{storeId}/register
    // { "cname": "test용", "displayOrder": 1 }
    @PostMapping("/store/{storeId}/register")
    public Map<String, Object> registerCategory(@PathVariable String storeId,
            @RequestBody Map<String, Object> requestMap) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 매장 존재 여부 확인
            Optional<Store> storeOpt = storeRepository.findById(storeId);
            if (!storeOpt.isPresent()) {
                map.put("status", 0);
                map.put("message", "매장 정보가 존재하지 않습니다.");
                return map;
            }

            Store store = storeOpt.get();

            // Category 객체 생성 및 설정
            Category category = new Category();
            category.setCname(requestMap.get("cname").toString()); // 카테고리명
            category.setDisplayOrder(Integer.parseInt(requestMap.get("displayOrder").toString())); // 표시순서
            category.setStoreId(storeId);
            category.setStore(store);

            // 동일 매장 내 카테고리명 중복 확인
            Category existingCategory = categoryRepository.findByCnameAndStoreId(
                    category.getCname(),
                    storeId);
            if (existingCategory != null) {
                map.put("status", 0);
                map.put("message", "이미 존재하는 카테고리 이름입니다");
                return map;
            }

            // 동일 매장 내 표시순서 중복 확인
            Category existingDisplayOrder = categoryRepository.findByStoreIdAndDisplayOrder(
                    storeId,
                    category.getDisplayOrder());
            if (existingDisplayOrder != null) {
                map.put("status", 0);
                map.put("message", "이미 존재하는 순서입니다");
                return map;
            }

            // 등록한 카테고리 DB에 저장
            categoryRepository.save(category);
            map.put("status", 200);
            map.put("message", "카테고리 등록이 성공적으로 완료되었습니다");
            map.put("data", category);
        } catch (Exception e) {

            // 기타 오류 발생 시
            map.put("status", -1);
            map.put("message", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }

    // 특정 매장{storeid}의 카테고리 목록 조회
    // 127.0.0.1:8080/ROOT/api/category/store/{storeId}/list
    @GetMapping("/store/{storeId}/list")
    public Map<String, Object> getCategoryList(@PathVariable String storeId) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 매장 존재 여부 확인
            Optional<Store> store = storeRepository.findById(storeId);
            if (!store.isPresent()) {
                map.put("status", 0);
                map.put("message", "매장 정보가 존재하지 않습니다.");
                return map;
            }

            // 특정 매장{storeID}에 해당하는 모든 카테고리 목록을 순서대로 조회
            List<Category> categories = categoryRepository.findByStoreIdOrderByDisplayOrder(storeId);

            map.put("status", 200);
            map.put("data", categories);
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }

    // 카테고리 수정
    // 127.0.0.1:8080/ROOT/api/category/store/{storeId}/update/{categoryId}
    // { "cname": "새로운 카테고리 이름", "displayOrder": 3 }
    @PutMapping("/store/{storeId}/update/{categoryId}")
    public Map<String, Object> updateCategory(@PathVariable String storeId,
            @PathVariable Integer categoryId,
            @RequestBody Map<String, Object> requestMap) {
        Map<String, Object> map = new HashMap<>();
        try {
            // {storeID로} 해당 매장 존재 여부 확인
            Optional<Store> storeOpt = storeRepository.findById(storeId);
            if (!storeOpt.isPresent()) {
                map.put("status", 0);
                map.put("message", "매장 정보가 존재하지 않습니다.");
                return map;
            }

            Store store = storeOpt.get();

            // 카테고리{categoryID} 존재 여부 확인
            Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
            if (!categoryOpt.isPresent()) {
                map.put("status", 0);
                map.put("message", "존재하지 않는 카테고리입니다");
                return map;
            }

            Category category = categoryOpt.get();

            // 해당 매장의 카테고리인지 확인
            if (!category.getStoreId().equals(storeId)) {
                map.put("status", 0);
                map.put("message", "해당 카테고리를 수정할 권한이 없습니다");
                return map;
            }

            // 카테고리 이름{cname} 수정
            if (requestMap.containsKey("cname")) {
                String newCategoryName = requestMap.get("cname").toString();

                // 동일 매장 내 카테고리명 중복 확인
                Category existingCategory = categoryRepository.findByCnameAndStoreId(newCategoryName, storeId);
                if (existingCategory != null && !existingCategory.getCategoryId().equals(categoryId)) {
                    map.put("status", 0);
                    map.put("message", "이미 존재하는 카테고리 이름입니다");
                    return map;
                }

                category.setCname(newCategoryName); // 카테고리 이름 수정
            }

            // 카테고리 표시 순서 수정
            if (requestMap.containsKey("displayOrder")) {
                int newDisplayOrder = Integer.parseInt(requestMap.get("displayOrder").toString());
                category.setDisplayOrder(newDisplayOrder);

                // 동일 매장 내 표시순서 중복 확인
                Category existingDisplayOrder = categoryRepository.findByStoreIdAndDisplayOrder(storeId,
                        newDisplayOrder);
                if (existingDisplayOrder != null && !existingDisplayOrder.getCategoryId().equals(categoryId)) {
                    // 순서 중복 시, 기존 카테고리와 새 카테고리의 displayOrder 교환
                    int tempOrder = category.getDisplayOrder();
                    category.setDisplayOrder(existingDisplayOrder.getDisplayOrder());
                    existingDisplayOrder.setDisplayOrder(tempOrder);

                    // 변경된 두 카테고리 저장
                    categoryRepository.save(category);
                    categoryRepository.save(existingDisplayOrder);

                    map.put("status", 200);
                    map.put("message", "카테고리 순서가 중복되어 교환되었습니다");
                    map.put("data", category);
                    return map;
                }
            }

            // 수정된 카테고리 내용 저장
            categoryRepository.save(category);
            map.put("status", 200);
            map.put("message", "카테고리가 성공적으로 수정되었습니다");
            map.put("data", category);
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }

    // 카테고리 삭제
    // 127.0.0.1:8080/ROOT/api/category/store/{storeId}/delete/{categoryId}
    @DeleteMapping("/store/{storeId}/delete/{categoryId}")
    public Map<String, Object> deleteCategory(@PathVariable String storeId, @PathVariable Integer categoryId) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 매장 존재 여부 확인
            Optional<Store> storeOpt = storeRepository.findById(storeId);
            if (!storeOpt.isPresent()) {
                map.put("status", 0);
                map.put("message", "매장 정보가 존재하지 않습니다.");
                return map;
            }

            // 카테고리 존재 여부 확인
            Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
            if (!categoryOpt.isPresent()) {
                map.put("status", 0);
                map.put("message", "존재하지 않는 카테고리입니다");
                return map;
            }

            Category category = categoryOpt.get();

            // 해당 매장의 카테고리인지 확인
            if (!category.getStoreId().equals(storeId)) {
                map.put("status", 0);
                map.put("message", "해당 카테고리를 삭제할 권한이 없습니다");
                return map;
            }

            // 카테고리 삭제
            categoryRepository.delete(category);

            // 삭제된 카테고리의 display_order 값
            int deletedCategoryDisplayOrder = category.getDisplayOrder();

            // 삭제된 카테고리 이후에 순서가 큰 카테고리들의 순서 내리기
            List<Category> categoriesToUpdate = categoryRepository.findByStoreIdAndDisplayOrderGreaterThan(
                    storeId, deletedCategoryDisplayOrder);

            for (Category cat : categoriesToUpdate) {
                cat.setDisplayOrder(cat.getDisplayOrder() - 1);
                categoryRepository.save(cat); // 수정된 카테고리 저장
            }

            map.put("status", 200);
            map.put("message", "카테고리가 성공적으로 삭제되었습니다");

        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }

    // 특정 매장의 사용 중인 displayOrder 값 조회
    // 127.0.0.1:8080/ROOT/api/category/{storeId}/displayOrders
    @GetMapping("/{storeId}/displayOrders")
    public Map<String, Object> getDisplayOrders(@PathVariable String storeId) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 매장 존재 여부 확인
            Optional<Store> storeOpt = storeRepository.findById(storeId);
            if (!storeOpt.isPresent()) {
                map.put("status", 0);
                map.put("message", "매장 정보가 존재하지 않습니다.");
                return map;
            }

            // 특정 매장에 해당하는 모든 카테고리의 displayOrder 값만 추출
            List<Integer> displayOrders = categoryRepository.findByStoreIdOrderByDisplayOrder(storeId)
                    .stream()
                    .map(Category::getDisplayOrder)
                    .toList();

            map.put("status", 200);
            map.put("data", displayOrders);
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return map;
    }

}