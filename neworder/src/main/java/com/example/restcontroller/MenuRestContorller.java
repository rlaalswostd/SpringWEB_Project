package com.example.restcontroller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Category;
import com.example.entity.Menu;
import com.example.entity.Menuimage;
import com.example.entity.OrderItem;
import com.example.entity.Orders;
import com.example.entity.Store;
import com.example.repository.CategoryRepository;
import com.example.repository.MenuImageRepository;
import com.example.repository.MenuRepository;
import com.example.repository.OrderItemRepository;
import com.example.repository.StoreRepository;
import com.example.token.JWTUtil;

import lombok.ToString;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@ToString
@RequestMapping(value = "/api/menu")
public class MenuRestContorller {

    @Value("${upload.dir}") // 업로드 디렉토리 경로를 설정 파일로부터 읽어올 수 있습니다.
    private String uploadDir;

    @Autowired
    private MenuRepository menuRepository; // 메뉴를 관리할 Repository

    @Autowired
    private MenuImageRepository menuImageRepository; // 메뉴 이미지 Repository

    @Autowired
    private StoreRepository storeRepository; // 매장 Repository

    @Autowired
    private CategoryRepository categoryRepository; // 카테고리 Repository

    @Autowired
    private OrderItemRepository orderItemRepository; // 주문(장바구니) Repository

    @Autowired
    private JWTUtil jwtUtil; // 토큰

    // 메뉴 등록
    // 127.0.0.1:8080/ROOT/api/menu/store/{storeId}/register
    @PostMapping("/store/{storeId}/register")
    public ResponseEntity<Map<String, Object>> registerMenu(
            @PathVariable String storeId,
            @RequestParam String name,
            @RequestParam String cname,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam Boolean isAvailable) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 매장 존재 여부 확인
            if (!storeRepository.existsById(storeId)) {
                response.put("status", 0);
                response.put("message", "매장 정보가 존재하지 않습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 메뉴 이름, 가격, 카테고리명 검증
            if (name == null || name.trim().isEmpty()) {
                response.put("status", 0);
                response.put("message", "메뉴 이름은 필수입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                response.put("status", 0);
                response.put("message", "가격은 0부터 입력가능합니다");
                return ResponseEntity.badRequest().body(response);
            }
            if (cname == null || cname.trim().isEmpty()) {
                response.put("status", 0);
                response.put("message", "카테고리명은 필수입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 메뉴가 이미 존재하는지 확인
            Menu existingMenu = menuRepository.findByNameAndStoreId(name, storeId);
            if (existingMenu != null) {
                response.put("status", 0);
                response.put("message", "이미 존재하는 메뉴입니다");
                return ResponseEntity.badRequest().body(response);
            }

            // 카테고리명으로 카테고리 정보 찾기
            Category category = categoryRepository.findByCnameAndStoreId(cname, storeId);
            if (category == null) {
                response.put("status", 0);
                response.put("message", "유효하지 않은 카테고리명입니다");
                return ResponseEntity.badRequest().body(response);
            }

            /// 메뉴 객체 생성 및 카테고리, 가격, 가용성 설정
            Menu menu = new Menu();
            menu.setName(name);
            menu.setPrice(price);
            menu.setCategory(category);
            menu.setIsAvailable(isAvailable);
            menu.setStoreId(storeId); // storeId는 경로 변수에서 받아온 값

            // 이미지 파일 처리
            byte[] imageData = null;
            String filename = null;
            String filetype = null;
            Long filesize = null;

            if (imageFile != null && !imageFile.isEmpty()) {
                imageData = imageFile.getBytes(); // 이미지 파일을 바이너리로 변환
                filename = imageFile.getOriginalFilename(); // 파일 이름
                filetype = imageFile.getContentType(); // 파일 타입
                filesize = imageFile.getSize(); // 파일 사이즈
            } else {
                // 기본 이미지 설정
                filename = "default_image.png"; // 기본 이미지 파일명
                filetype = "image/png"; // 기본 이미지 타입
                filesize = 0L; // 기본 파일 크기
                imageData = new byte[0]; // 빈 데이터
            }

            // Menuimage 객체 생성 및 저장
            Menuimage menuimage = new Menuimage();
            menuimage.setFilename(filename);
            menuimage.setFiletype(filetype);
            menuimage.setFilesize(filesize);
            menuimage.setFiledata(imageData); // 바이너리 이미지 데이터 저장
            menuimage.setMenu(menu); // 메뉴와 이미지 연결

            // 메뉴와 메뉴 이미지를 각각 저장
            menuRepository.save(menu);
            menuimage.setMenu(menu);
            menuImageRepository.save(menuimage);

            response.put("status", 200);
            response.put("message", "Menu가 성공적으로 등록되었습니다.");
            response.put("data", menu);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "에러가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /// 메뉴 수정
    // 127.0.0.1:8080/ROOT/api/menu/store/5/menu/118/update.do
    @PutMapping("/store/{storeId}/menu/{menuId}/update.do")
    public Map<String, Object> updateMenu(
            @PathVariable String storeId, // storeId를 PathVariable로 받기
            @PathVariable Long menuId, // menuId를 PathVariable로 받기
            @RequestParam String name,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) String cname,
            @RequestParam Boolean isAvailable,
            @RequestParam(required = false) MultipartFile imageFile) {

        Map<String, Object> map = new HashMap<>();
        try {
            // storeId로 매장 정보 찾기
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store == null) {
                map.put("status", 0);
                map.put("message", "해당 매장이 존재하지 않습니다.");
                return map;
            }

            // 카테고리 id 찾기
            Optional<Category> categoryOptional = menuRepository.findCategoryByMenuId(menuId);

            // 카테고리가 설정되지 않은 경우 처리
            if (categoryOptional.isEmpty()) {
                map.put("status", 0);
                map.put("message", "해당 메뉴는 카테고리가 설정되지 않았습니다.");
                return map;
            }
            // 메뉴 ID로 메뉴 찾기
            Menu existingMenu = menuRepository.findById(menuId).orElse(null);
            if (existingMenu == null) {
                map.put("status", 0);
                map.put("message", "해당 메뉴가 존재하지 않습니다.");
                return map;
            }

            System.out.println("수정 전 메뉴 정보:");
            System.out.println("메뉴 ID: " + existingMenu.getId());
            System.out.println("메뉴 이름: " + existingMenu.getName());
            System.out.println("카테고리 ID: "
                    + (existingMenu.getCategory() != null ? existingMenu.getCategory().getCategoryId() : "null"));
            System.out.println("카테고리 이름: "
                    + (existingMenu.getCategory() != null ? existingMenu.getCategory().getCname() : "null"));

            // 메뉴가 해당 매장에 속하는지 확인
            if (!existingMenu.getStoreId().equals(storeId)) {
                map.put("status", 0);
                map.put("message", "해당 메뉴는 이 매장에 속하지 않습니다.");
                return map;
            }

            // 메뉴명 수정
            if (name != null && !name.isEmpty()) {
                existingMenu.setName(name);
            }

            // isAvailable 수정
            if (isAvailable != null) {
                existingMenu.setIsAvailable(isAvailable);
            } else {
                map.put("status", 0);
                map.put("message", "isAvailable 값을 입력해 주세요.");
                return map;
            }

            // 가격 수정
            if (price != null) {
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    map.put("status", 0);
                    map.put("message", "가격은 0원부터 가능합니다");
                    return map;
                }
                existingMenu.setPrice(price);
            } else {
                map.put("status", 0);
                map.put("message", "가격을 입력해 주세요.");
                return map;
            }

            // 카테고리 수정
            if (cname != null && !cname.isEmpty()) {
                // findFirstByCname(cname)에서 findByCnameAndStoreId(cname, storeId)로 변경
                Category category = categoryRepository.findByCnameAndStoreId(cname, storeId);
                if (category == null) {
                    map.put("status", 0);
                    map.put("message", "해당 매장에 존재하지 않는 카테고리입니다.");
                    return map;
                }
                existingMenu.setCategory(category);
            }

            // 이미지 수정 처리
            if (imageFile != null && !imageFile.isEmpty()) {
                byte[] imageData = imageFile.getBytes();
                String filename = imageFile.getOriginalFilename();
                String filetype = imageFile.getContentType();
                Long filesize = imageFile.getSize();

                // 기존 이미지 찾기
                List<Menuimage> existingImages = menuImageRepository.findByMenu_Id(menuId);
                if (!existingImages.isEmpty()) {
                    Menuimage existingImage = existingImages.get(0); // 첫 번째 이미지만 수정한다고 가정
                    existingImage.setFilename(filename);
                    existingImage.setFiletype(filetype);
                    existingImage.setFilesize(filesize);
                    existingImage.setFiledata(imageData);

                    // 수정된 이미지 저장
                    menuImageRepository.save(existingImage);
                } else {
                    map.put("status", 0);
                    map.put("message", "기존 이미지가 존재하지 않습니다. 이미지를 등록 해주세요");
                    return map;
                }
            } else {
                // 이미지가 없다면 기존 이미지는 그대로 두기
                List<Menuimage> existingImages = menuImageRepository.findByMenu_Id(menuId);
                if (existingImages.isEmpty()) {
                    map.put("status", 0);
                    map.put("message", "기존 이미지가 존재하지 않습니다. 이미지를 등록 해주세요");
                    return map;
                }
            }

            // 수정 전 카테고리 ID 출력
            System.out.println("수정 전 카테고리 ID: " + existingMenu.getCategory().getCategoryId());
            // 메뉴 저장
            menuRepository.save(existingMenu);
            // 수정 후 카테고리 ID 출력
            System.out.println("수정 후 카테고리 ID: " + existingMenu.getCategory().getCategoryId());

            System.out.println("수정 후 메뉴 정보:");
            System.out.println("메뉴 ID: " + existingMenu.getId());
            System.out.println("메뉴 이름: " + existingMenu.getName());
            System.out.println("카테고리 ID: "
                    + (existingMenu.getCategory() != null ? existingMenu.getCategory().getCategoryId() : "null"));
            System.out.println("카테고리 이름: "
                    + (existingMenu.getCategory() != null ? existingMenu.getCategory().getCname() : "null"));

            map.put("status", 200);
            map.put("message", "메뉴가 성공적으로 수정되었습니다.");
        } catch (IOException e) {
            map.put("status", -1);
            map.put("message", "이미지 파일 처리 오류: " + e.getMessage());
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "에러가 발생했습니다: " + e.getMessage());
        }
        return map;
    }

    // 메뉴 삭제
    @DeleteMapping("/store/{storeId}/menu/{id}/delete.do")
    public Map<String, Object> deleteMenu(@PathVariable String storeId, @PathVariable Long id) {
        Map<String, Object> map = new HashMap<>();
        try {
            // storeId로 메뉴 조회
            Menu menuToDelete = menuRepository.findById(id).orElse(null);
            if (menuToDelete == null) {
                map.put("status", 0);
                map.put("message", "해당 메뉴가 존재하지 않습니다.");
                return map;
            }

            // storeId와 일치하는 메뉴인지 확인
            if (!menuToDelete.getStoreId().equals(storeId)) {
                map.put("status", 0);
                map.put("message", "이 메뉴는 해당 매장의 메뉴가 아닙니다.");
                return map;
            }

            // OrderItem 테이블에서 해당 메뉴가 존재하는지 확인
            List<OrderItem> orderItems = orderItemRepository.findByMenu(menuToDelete);
            for (OrderItem orderItem : orderItems) {
                // 해당 메뉴와 연관된 주문이 'ORDERED' 상태인 경우 삭제 불가
                if (orderItem.getOrder().getStatus() == Orders.OrderStatus.ORDERED) {
                    map.put("status", 0);
                    map.put("message", "해당 메뉴는 현재 주문에 포함되어 있어 삭제할 수 없습니다.");
                    return map;
                }
            }
            // 메뉴 이미지 삭제 (이미지 파일이 있다면 삭제)
            List<Menuimage> existingImages = menuImageRepository.findByMenu_Id(id);
            if (!existingImages.isEmpty()) {
                menuImageRepository.deleteAll(existingImages);
            }

            // 메뉴 삭제
            menuRepository.delete(menuToDelete);

            map.put("status", 200);
            map.put("message", "메뉴가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "에러가 발생했습니다: " + e.getMessage());
        }
        return map;
    }

    // 이미지 조회(menuId로 조회)
    // 127.0.0.1:8080/ROOT/api/menu/menuimage/??
    @GetMapping(value = "/menuimage/{menuId}")
    public ResponseEntity<?> getMenuImage(@PathVariable Long menuId) {
        Map<String, Object> map = new HashMap<>();

        try {
            // menuId로 이미지 목록 조회
            List<Menuimage> menuImages = menuImageRepository.findByMenu_Id(menuId);

            if (menuImages == null || menuImages.isEmpty()) {
                map.put("status", 0);
                map.put("message", "해당 메뉴의 이미지를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
            }

            // 여러 이미지가 있는 경우 첫 번째 이미지만 반환
            Menuimage menuImage = menuImages.get(0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(menuImage.getFiletype()));
            return new ResponseEntity<>(menuImage.getFiledata(), headers, HttpStatus.OK);

        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "이미지 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
        }
    }

    // 메뉴 목록 조회
    // 127.0.0.1:8080/ROOT/api/menu/store/5/menulist.do
    @GetMapping("/store/{storeId}/menulist.do")
    public Map<String, Object> getMenuList(@PathVariable String storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sort, // 정렬 기준 (기본값: 가격)
            @RequestParam(defaultValue = "asc") String order) { // 정렬 순서 (기본값: 오름차순)

        Map<String, Object> map = new HashMap<>();
        try {
            // Pageable 객체 생성 (page 번호, size 크기, 가격 기준으로 정렬)
            Pageable pageable = PageRequest.of(page, size,
                    "asc".equals(order) ? Sort.by(sort).ascending() : Sort.by(sort).descending());

            // 페이지네이션 적용된 매장별 메뉴 목록 조회
            Page<Menu> menuPage = menuRepository.findByStoreId(storeId, pageable);

            // 메뉴 목록이 있을 경우
            if (menuPage != null && menuPage.hasContent()) {
                map.put("status", 200);
                map.put("message", "메뉴 목록 조회 성공");

                // 페이지네이션 데이터
                map.put("totalPages", menuPage.getTotalPages()); // 총 페이지 수
                map.put("totalElements", menuPage.getTotalElements()); // 총 메뉴 수
                map.put("currentPage", menuPage.getNumber()); // 현재 페이지 번호

                // 메뉴 데이터 리스트 생성
                List<Map<String, Object>> menuDataList = new ArrayList<>();
                for (Menu menu : menuPage.getContent()) {
                    Map<String, Object> menuData = new HashMap<>();
                    menuData.put("id", menu.getId());
                    menuData.put("name", menu.getName());
                    menuData.put("price", menu.getPrice());
                    menuData.put("isAvailable", menu.getIsAvailable());
                    if (menu.getCategory() != null) {
                        menuData.put("cname", menu.getCategory().getCname());
                        menuData.put("categoryId", menu.getCategory().getCategoryId()); // 카테고리 ID 추가
                    }

                    // 메뉴 이미지 URL 처리
                    List<Menuimage> menuImages = menuImageRepository.findByMenu_Id(menu.getId());
                    if (menuImages != null && !menuImages.isEmpty()) {
                        String imageUrl = "/api/menu/menuimage/" + menu.getId();
                        menuData.put("imageUrl", imageUrl);
                    } else {
                        menuData.put("imageUrl", null);
                    }
                    menuDataList.add(menuData);
                }

                map.put("data", menuDataList);
            } else {
                map.put("status", 0);
                map.put("message", "메뉴가 없습니다.");
            }
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "에러가 발생했습니다: " + e.getMessage());
        }
        return map;
    }

    // 메뉴 검색
    // 127.0.0.1:8080/ROOT/api/menu/5/search.do
    @GetMapping("/{storeId}/search.do")
    public Map<String, Object> searchMenu(
            @PathVariable String storeId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 정렬 순서에 따른 분기 처리
            Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

            Page<Menu> menuPage;
            if (name != null && !name.isEmpty() && category != null && !category.isEmpty()) {
                menuPage = menuRepository.findByStoreIdAndNameContainingAndCategory_CnameContaining(storeId, name,
                        category, pageable);
            } else if (name != null && !name.isEmpty()) {
                menuPage = menuRepository.findByStoreIdAndNameContaining(storeId, name, pageable);
            } else if (category != null && !category.isEmpty()) {
                menuPage = menuRepository.findByStoreIdAndCategory_CnameContaining(storeId, category, pageable);
            } else {
                menuPage = menuRepository.findByStoreId(storeId, pageable); // storeId로 메뉴 조회
            }

            List<Map<String, Object>> menuDataList = new ArrayList<>();
            for (Menu menu : menuPage.getContent()) {
                Map<String, Object> menuData = new HashMap<>();
                menuData.put("id", menu.getId());
                menuData.put("name", menu.getName());
                menuData.put("price", menu.getPrice());
                if (menu.getCategory() != null) {
                    menuData.put("cname", menu.getCategory().getCname());
                }
                List<Menuimage> menuImages = menuImageRepository.findByMenu_Id(menu.getId());
                if (menuImages != null && !menuImages.isEmpty()) {
                    String imageUrl = "/api/menu/menuimage/" + menu.getId();
                    menuData.put("imageUrl", imageUrl);
                } else {
                    menuData.put("imageUrl", null);
                }
                menuDataList.add(menuData);
            }

            map.put("status", 200);
            map.put("message", "검색 결과");
            map.put("data", menuDataList);
            map.put("totalPages", menuPage.getTotalPages());
            map.put("totalElements", menuPage.getTotalElements());
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "에러가 발생했습니다: " + e.getMessage());
        }
        return map;
    }

       // 이미지 조회(no로 조회)
    // 127.0.0.1:8080/ROOT/api/menu/image?no=??
    @GetMapping(value = "/image")
public ResponseEntity<?> imagePreview(@RequestParam(name = "no") long no) {
    Map<String, Object> map = new HashMap<>();
    Menuimage obj = null;

    try {
        obj = menuImageRepository.findById(no).orElse(null);
    } catch (Exception e) {
        map.put("status", -1);
        map.put("message", "이미지 번호가 잘못되었거나 조회 중 오류가 발생했습니다: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
    }

    // 이미지가 없는 경우
    if (obj == null) {
        map.put("status", 0);
        map.put("message", "이미지를 찾을 수 없습니다.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
    }

    // 이미지 데이터 반환
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(obj.getFiletype()));
    return new ResponseEntity<>(obj.getFiledata(), headers, HttpStatus.OK);
}

    // 이미지 수정
    // 127.0.0.1:8080/ROOT/api/menu/imageupdate/{menuId}
    @PutMapping("/imageupdate/{menuId}")
    public Map<String, Object> updateMenuImage(@PathVariable Long menuId, @RequestParam MultipartFile imageFile) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 메뉴 확인
            Menu menu = menuRepository.findById(menuId).orElse(null);
            if (menu == null) {
                map.put("status", 0);
                map.put("message", "해당 메뉴가 존재하지 않습니다.");
                return map;
            }

            // 기존 이미지 조회
            List<Menuimage> menuImages = menuImageRepository.findByMenu_Id(menuId);
            if (menuImages.isEmpty()) {
                map.put("status", 0);
                map.put("message", "해당 메뉴의 이미지가 없습니다.");
                return map;
            }

            // 기존 이미지 삭제 (기존 이미지가 있을 경우)
            Menuimage existingImage = menuImages.get(0); // 첫 번째 이미지만 수정한다고 가정
            menuImageRepository.delete(existingImage);

            // 새로운 이미지 처리
            byte[] imageData = imageFile.getBytes(); // 새로운 이미지 파일을 바이너리 데이터로 변환
            String filename = imageFile.getOriginalFilename(); // 파일명
            String filetype = imageFile.getContentType(); // 파일 타입
            Long filesize = imageFile.getSize(); // 파일 사이즈

            // 새로운 Menuimage 객체 생성
            Menuimage menuimage = new Menuimage();
            menuimage.setFilename(filename);
            menuimage.setFiletype(filetype);
            menuimage.setFilesize(filesize);
            menuimage.setFiledata(imageData); // 새로운 이미지 데이터 설정
            menuimage.setMenu(menu); // 해당 메뉴에 이미지 연결

            // 새 이미지 저장
            menuImageRepository.save(menuimage);

            map.put("status", 200);
            map.put("message", "메뉴 이미지가 성공적으로 수정되었습니다.");
        } catch (IOException e) {
            map.put("status", -1);
            map.put("message", "이미지 파일 처리 오류: " + e.getMessage());
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "에러가 발생했습니다: " + e.getMessage());
        }
        return map;
    }
}
