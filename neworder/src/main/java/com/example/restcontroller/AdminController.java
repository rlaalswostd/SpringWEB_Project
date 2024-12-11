package com.example.restcontroller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.Token;
import com.example.entity.Admin;
import com.example.entity.Notice;
import com.example.entity.Store;
import com.example.mapper.TokenMapper;
import com.example.repository.AdminRepository;
import com.example.repository.NoticeRepository;
import com.example.repository.StoreRepository;
import com.example.service.NoticeService;
import com.example.token.JWTUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
//
@RestController
@RequestMapping(value = "/api/admin")
@RequiredArgsConstructor
public class AdminController {

    final AdminRepository adminRepository;
    final JWTUtil jwtUtil;
    final BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
    final TokenMapper tokenMapper;
    final NoticeRepository noticeRepository;
    final NoticeService noticeService;
    final StoreRepository storeRepository;

    private boolean checkAdminAuth(String token) {
        try {
            System.out.println("============================== 관리자 확인 ==============================");

            var claims = jwtUtil.validate(token);
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            System.out.println("Token email: " + email);
            System.out.println("Token role: " + role);

            Admin adminData = (Admin) adminRepository.findByEmail(email);
            if (adminData == null) {
                System.out.println("Admin not found for email: " + email);
                return false;
            }

            System.out.println("Found admin role: " + adminData.getRole());
            return Admin.Role.SUPER_ADMIN.equals(adminData.getRole()) ||
                    Admin.Role.STORE_ADMIN.equals(adminData.getRole());
        } catch (Exception e) {
            System.out.println("Error in checkAdminAuth: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logoutPOST(@RequestHeader("Authorization") String authorization,
            HttpServletResponse responseMap) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = authorization.replace("Bearer ", "");

            var claims = jwtUtil.validate(token);
            String adminName = claims.get("admin_name", String.class);
            String role = claims.get("role", String.class);
            String email = claims.get("email", String.class);

            int result = tokenMapper.deleteByAdminEmail(email);
            if (result > 0) {
                System.out.println("토큰 삭제 완료: " + email);
            } else {
                System.out.println("토큰을 찾을 수 없습니다.: " + email);
            }

            response.put("status", "success");
            response.put("message", "로그아웃 성공");
            response.put("logoutTime", new Date());

            System.out.println("Logout successful for " + role + ": " + adminName);

            Cookie cookie = new Cookie("token", token);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            responseMap.addCookie(cookie);

        } catch (Exception e) {
            System.err.println("Logout failed: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "로그아웃 실패: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> loginPOST(@RequestBody Admin admin, HttpServletResponse responseMap) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("============================== 관리자 로그인 필터 ==============================");
            System.out.println("Email: " + admin.getEmail());

            Admin adminData = (Admin) adminRepository.findByEmail(admin.getEmail());
            if (adminData == null) {
                throw new IllegalArgumentException("존재하지 않는 계정입니다.");
            }

            if (!bcpe.matches(admin.getPassword(), adminData.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }

            if (!Admin.Role.SUPER_ADMIN.equals(adminData.getRole())) {
                throw new IllegalArgumentException("관리자 권한이 없습니다.");
            }

            tokenMapper.deleteByAdminEmail(admin.getEmail());

            Map<String, Object> map = new HashMap<>();
            map.put("email", adminData.getEmail());
            map.put("admin_name", adminData.getAdminName());
            map.put("role", adminData.getRole().toString());
            String token = jwtUtil.createToken(map); // JWT 생성

            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 4); // 4시간 유효
            responseMap.addCookie(cookie);

            Token tokenObj = new Token();
            tokenObj.setAdminEmail(adminData.getEmail());
            tokenObj.setToken(token);
            tokenObj.setCreateAt(new Date());
            tokenObj.setExpiretime(new Date(System.currentTimeMillis() + (60 * 4 * 1000)));

            int result = tokenMapper.insertToken(tokenObj);
            if (result > 0) {
                System.out.println("Token saved successfully for email: " + adminData.getEmail());
            } else {
                throw new RuntimeException("토큰 저장 실패");
            }

            // 응답 데이터 구성
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("adminId", adminData.getAdminId());
            adminInfo.put("email", adminData.getEmail());
            adminInfo.put("adminName", adminData.getAdminName());
            adminInfo.put("role", adminData.getRole().toString());
            adminInfo.put("createdAt", adminData.getCreatedAt());

            response.put("status", "success");
            response.put("message", "관리자 로그인 성공");
            response.put("data", adminInfo);
            response.put("token", token); // 생성된 JWT 토큰 포함

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }

    //// 판매자 로그인
    @PostMapping("/manager-login")
    public Map<String, Object> managerLoginPOST(@RequestBody Admin admin, HttpServletResponse responseMap) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println(
                    "============================== Store Manager Login Filter ==============================");
            System.out.println("Requested Email: " + admin.getEmail());

            // 1. 이메일로 사장님 찾기
            Admin adminData = (Admin) adminRepository.findByEmail(admin.getEmail());
            if (adminData == null) {
                throw new IllegalArgumentException("존재하지 않는 계정입니다.");
            }
            System.out.println("Found Admin ID: " + adminData.getAdminId());

            // 2. 비밀번호 확인
            if (!bcpe.matches(admin.getPassword(), adminData.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
            }

            // 3. STORE_ADMIN 권한 확인
            if (!Admin.Role.STORE_ADMIN.equals(adminData.getRole())) {
                throw new IllegalArgumentException("사장님 계정이 아닙니다.");
            }

            // 4. 해당 관리자의 모든 매장 조회
            List<Store> stores = storeRepository.findByAdminId(adminData.getAdminId());
            List<Map<String, Object>> storeList = stores.stream()
                    .map(store -> {
                        Map<String, Object> storeMap = new HashMap<>();
                        storeMap.put("storeId", store.getStoreId());
                        storeMap.put("storeName", store.getStoreName());
                        storeMap.put("address", store.getAddress());
                        storeMap.put("phone", store.getPhone());
                        storeMap.put("isActive", store.getIsActive());
                        return storeMap;
                    })
                    .collect(Collectors.toList());

            // 5. 기존 토큰 삭제
            tokenMapper.deleteByAdminEmail(admin.getEmail());

            // 6. JWT 토큰 생성
            Map<String, Object> map = new HashMap<>();
            map.put("email", adminData.getEmail());
            map.put("admin_name", adminData.getAdminName());
            map.put("role", adminData.getRole().toString());
            map.put("adminId", adminData.getAdminId());  // adminId만 포함
            String token = jwtUtil.createToken(map); // JWT 생성

            // 7. 토큰 저장
            Token tokenObj = new Token();
            tokenObj.setAdminEmail(adminData.getEmail());
            tokenObj.setToken(token);
            tokenObj.setCreateAt(new Date());
            tokenObj.setExpiretime(new Date(System.currentTimeMillis() + (60 * 4 * 1000)));

            int result = tokenMapper.insertToken(tokenObj);
            if (result > 0) {
                System.out.println("토큰 저장 완료: " + adminData.getEmail());
            } else {
                throw new RuntimeException("토큰 저장 실패");
            }

            // 8. 응답 데이터 구성
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("adminId", adminData.getAdminId());
            adminInfo.put("email", adminData.getEmail());
            adminInfo.put("adminName", adminData.getAdminName());
            adminInfo.put("role", adminData.getRole().toString());
            adminInfo.put("createdAt", adminData.getCreatedAt());
            adminInfo.put("stores", storeList); // 모든 매장 정보를 포함

            response.put("status", "success");
            response.put("message", "사장님 로그인 성공");
            response.put("data", adminInfo);
            response.put("token", token); // 생성된 JWT 토큰 포함

            // 9. 쿠키 설정
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 4); // 4시간 유효
            responseMap.addCookie(cookie);

        } catch (Exception e) {
            System.err.println("Login Error: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/adminregister")
    public Map<String, Object> registerSellerPOST(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Admin seller) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("============================== 판매자 등록 ==============================");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("유효하지 않은 토큰 형식입니다.");
            }

            String token = authHeader.replace("Bearer ", "");
            System.out.println("Token: " + token);

            if (!checkAdminAuth(token)) {
                throw new IllegalArgumentException("관리자 권한이 없습니다.");
            }

            if (adminRepository.findByEmail(seller.getEmail()) != null) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            }

            seller.setRole(Admin.Role.STORE_ADMIN);
            seller.setPassword(bcpe.encode(seller.getPassword()));
            adminRepository.save(seller);

            response.put("status", "success");
            response.put("message", "관리자 회원가입 성공");
            response.put("data", seller);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "관리자 회원가입 실패: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/register")
    public Map<String, Object> registerAdminPOST(@RequestBody Admin admin) {
        Map<String, Object> response = new HashMap<>();

        try {
            Object existingAdmin = adminRepository.findByEmail(admin.getEmail());
            if (existingAdmin != null) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
            }

            admin.setRole(Admin.Role.SUPER_ADMIN);
            admin.setPassword(bcpe.encode(admin.getPassword()));
            Admin savedAdmin = adminRepository.save(admin);

            response.put("status", "success");
            response.put("message", "관리자 회원가입 성공");
            response.put("data", savedAdmin);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "관리자 등록 실패: " + e.getMessage());
        }
        System.out.println("=== 관리자 등록 완료 ===");
        return response;
    }

    @PostMapping("/notice/insert.do")
    public Map<String, Object> createNotice(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Notice notice) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("status", 401);
                response.put("message", "유효하지 않은 토큰 형식");
                return response;
            }
            String token = authHeader.replace("Bearer ", "");
            if (!checkAdminAuth(token)) {
                response.put("status", 403);
                response.put("message", "관리자 권한이 없습니다.");
                return response;
            }

            Optional<Admin> adminOptional = adminRepository.findById(notice.getAdmin().getAdminId());
            if (!adminOptional.isPresent()) {
                response.put("status", 400);
                response.put("message", "관리자를 찾을 수 없습니다.");
                return response;
            }

            notice.setActive(true);
            notice.setAdmin(adminOptional.get());

            Notice createNotice = noticeService.createNotice(notice);

            response.put("status", 200);
            response.put("message", "공지 등록 완료");
            response.put("result", createNotice);

        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "공지 등록 실패" + e.getMessage());
        }
        return response;
    }

    @GetMapping("/notice/list.do")
    public Map<String, Object> getlistNotice(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("status", 401);
                response.put("message", "유효하지 않은 토큰 형식입니다.");
                return response;
            }

            String token = authHeader.replace("Bearer ", "");
            if (!checkAdminAuth(token)) {
                response.put("status", 403);
                response.put("message", "관리자 권한이 없습니다.");
                return response;
            }

            List<Notice> notice = noticeService.getActiveNotice();
            response.put("status", 200);
            response.put("result", notice);
        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "공지사항 조회 실패: " + e.getMessage());
        }
        return response;
    }

    @PutMapping(value = "/notice/update.do")
    public Map<String, Object> putUpdate(
            @RequestBody Notice notice,
            @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("status", 401);
                response.put("message", "유효하지 않은 토큰 형식입니다.");
                return response;
            }

            String token = authHeader.replace("Bearer ", "");
            if (!checkAdminAuth(token)) {
                response.put("status", 403);
                response.put("message", "관리자 권한이 없습니다.");
                return response;
            }

            Optional<Notice> ret = noticeService.getNoticeById(notice.getNoticeId());
            if (!ret.isPresent()) {
                response.put("status", 400);
                response.put("message", "수정 공지를 찾을 수 없습니다.");
                return response;
            }

            Notice origin = ret.get();
            origin.setTitle(notice.getTitle());
            origin.setContent(notice.getContent());

            Notice updateNotice = noticeService.updateNotice(origin);
            response.put("status", 200);
            response.put("message", "공지사항 수정 성공");
            response.put("result", updateNotice);
        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "공지사항 수정 실패: " + e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/notice/delete.do")
    public Map<String, Object> deleteDelete(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Integer noticeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("status", 401);
                response.put("message", "유효하지 않은 토큰 형식입니다.");
                return response;
            }

            String token = authHeader.replace("Bearer ", "");
            if (!checkAdminAuth(token)) {
                response.put("status", 403);
                response.put("message", "관리자 권한이 없습니다.");
                return response;
            }

            Optional<Notice> notice = noticeService.getNoticeById(noticeId);
            if (!notice.isPresent()) {
                response.put("status", 404);
                response.put("message", "공지사항을 찾을 수 없습니다.");
                return response;
            }

            Notice target = notice.get();
            target.setActive(false);
            noticeService.updateNotice(target);

            response.put("status", 200);
            response.put("message", "성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "삭제 실패: " + e.getMessage());
        }
        return response;
    }
}