package org.example.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.message.controller.MessageController;
import org.example.message.dto.SendMessageReq;
import org.example.message.dto.SendMessageRes;
import org.example.message.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import java.io.IOException;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MessageControllerTest {

    @Mock
    private MessageService messageService;
    @Mock
    private Session session;
    @Mock
    private RemoteEndpoint.Async asyncRemote;

    private MessageController controller;
    private ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new MessageController(messageService);
        when(session.getAsyncRemote()).thenReturn(asyncRemote);
    }

    @Test
    void onMessage_shouldDeserializeRequest_callService_andBroadcastResponse() throws IOException, SQLException {
        // given
        Long roomId = 123L;
        SendMessageReq req = new SendMessageReq();
        req.setRoomId(roomId);
        req.setSenderId(42L);
        req.setContents("테스트 메시지");

        SendMessageRes res = new SendMessageRes();
        res.setMsgId(555L);
        res.setChatRoomId(roomId);
        res.setSenderId(42L);
        res.setContents("테스트 메시지");
        res.setCreatedAt(java.time.LocalDateTime.now());

        String reqJson = mapper.writeValueAsString(req);
        String resJson = mapper.writeValueAsString(res);

        // service.saveMessage(...) 호출 시 미리 준비된 DTO 리턴
        when(messageService.saveMessage(any(SendMessageReq.class))).thenReturn(res);

        // when
        controller.onMessage(session, roomId, reqJson);

        // then
        // 메시지 처리를 위해 service 호출이 1회 일어나는지 확인
        verify(messageService, times(1)).saveMessage(any(SendMessageReq.class));
        // 브로드캐스트가 asyncRemote.sendText(...)로 1회 발생했는지 확인
        verify(asyncRemote, times(1)).sendText(resJson);
    }
}
