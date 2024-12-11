// package com.example.restcontroller;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestHeader;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// import com.example.entity.Notice;
// import com.example.token.JWTUtil;

// import io.jsonwebtoken.Claims;
// import lombok.RequiredArgsConstructor;



// @RestController
// @RequestMapping(value = "/api")
// @RequiredArgsConstructor  
// public class SseRestController {

//      final JWTUtil jwtUtil;
//      final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // @GetMapping(value = "/sse/connect")
    // public SseEmitter connect(@RequestHeader("Authorization") String authHeader) {
    //     try {
    //         String token = authHeader.replace("Bearer ", "");
    //         /// 토큰에서 사용자 정보 추출
    //         Claims claims = jwtUtil.validate(token);
    //         String email = claims.get("email", String.class);
    //     }
    // }

            
//             SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
//             emitters.put(email, emitter);
            
//             // 연결 성공 이벤트 전송
//             emitter.send(SseEmitter.event()
//                 .name("connect")
//                 .data("Connected successfully"));
            
//             // 연결 종료 시 정리
//             emitter.onCompletion(() -> emitters.remove(email));
//             emitter.onTimeout(() -> emitters.remove(email));
//             emitter.onError((e) -> emitters.remove(email));
            
//             return emitter;
//         } catch (Exception e) {
//             e.printStackTrace();
//             throw new RuntimeException("SSE 연결 실패");
//         }
//     }

//     // 공지사항 이벤트 전송 메서드
//     public void sendNoticeEvent(Notice notice) {
//         Map<String, Object> eventData = new HashMap<>();
//         eventData.put("type", "NOTICE");
//         eventData.put("data", notice);

//         emitters.forEach((email, emitter) -> {
//             try {
//                 emitter.send(SseEmitter.event()
//                     .name("notice")
//                     .data(eventData));
//             } catch (Exception e) {
//                 emitters.remove(email);
//             }
//         });
//     }
// }
        