package org.example.message.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.example.message.dto.SendMessageReq;
import org.example.message.dto.SendMessageRes;
import org.example.message.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * MessageController의 onMessage(...) 브로드캐스팅 동작 단위 테스트
 */
class MessageControllerTest {

    @Mock
    private MessageService mockMessageService;

    @Mock
    private Session mockSession;

    @Mock
    private RemoteEndpoint.Async mockAsyncRemote;

    private MessageController controller;
    private ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // MessageController(MessageService) 생성자 존재 확인 :contentReference[oaicite:0]{index=0}
        controller = new MessageController(mockMessageService);
        when(mockSession.getAsyncRemote()).thenReturn(mockAsyncRemote);
    }

    @Test
    void onMessage_shouldCallServiceAndBroadcastToAllOpenSessions() throws IOException, SQLException {
        // given
        Long roomId = 56L;
        SendMessageReq req = new SendMessageReq();
        req.setRoomId(roomId);
        req.setSenderId(19L);
        req.setContents("안녕하세요");

        SendMessageRes res = new SendMessageRes();
        res.setMsgId(1001L);
        res.setChatRoomId(roomId);
        res.setSenderId(19L);
        res.setContents("안녕하세요");
        res.setCreatedAt(LocalDateTime.now());

        String reqJson = mapper.writeValueAsString(req);
        String resJson = mapper.writeValueAsString(res);

        // service.saveMessage(...) 메서드 존재 확인 :contentReference[oaicite:1]{index=1}
        when(mockMessageService.saveMessage(any(SendMessageReq.class))).thenReturn(res);

        // 방(roomId)에 하나의 세션을 미리 추가하기 위해 onOpen 호출 :contentReference[oaicite:2]{index=2}
        controller.onOpen(mockSession, roomId);

        // when
        controller.onMessage(mockSession, roomId, reqJson);

        // then
        // 1) MessageService.saveMessage 호출 검증
        verify(mockMessageService, times(1)).saveMessage(any(SendMessageReq.class));

        // 2) getAsyncRemote().sendText(resJson) 호출 검증 (브로드캐스트)
        verify(mockAsyncRemote, times(1)).sendText(resJson);
    }
}
