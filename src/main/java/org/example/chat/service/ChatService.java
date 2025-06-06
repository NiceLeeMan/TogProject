package org.example.chat.service;

import org.example.chat.dao.ChatDAO;
import org.example.chat.dto.CreateGroupChatReqDto;
import org.example.chat.dto.CreateOneToOneChatReqDto;
import org.example.chat.dto.JoinChatReqDto;
import org.example.chat.dto.JoinChatResDto;
import org.example.chat.dto.MemberInfo;
import org.example.chat.dto.OutChatRoomReqDto;
import org.example.chat.dto.OutChatRoomResDto;
import org.example.message.dto.MessageInfo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatService
 *
 * • ChatDAO를 호출하여 비즈니스 로직을 처리하고, DTO로 결과를 반환합니다.
 */
public class ChatService {

    private final ChatDAO chatDAO;

    public ChatService(ChatDAO chatDAO) {
        this.chatDAO = chatDAO;
    }

    /**
     * 1:1 채팅방 생성 + 바로 입장 정보 반환
     */
    public JoinChatResDto createAndJoinOneToOne(CreateOneToOneChatReqDto req) {
        try {
            // 1) 방 생성 (DB 상으로 생성자+대상 모두 MEMBER 테이블에 INSERT됨)
            Long newRoomId = chatDAO.insertOneToOneRoom(
                    req.getUsername(),
                    req.getFriendUsername()
            );

            // 2) 방 이름 조회
            String roomName = chatDAO.selectRoomName(newRoomId);

            // 3) 멤버 정보 조회 (joinedAt 포함, 생성자는 이미 참여되어 있음)
            List<MemberInfo> members = chatDAO.selectActiveMembers(newRoomId);

            // 4) 메시지 내역 조회 (생성 직후라 메시지는 없겠지만, API 형태 통일)
            List<MessageInfo> messages = chatDAO.selectChatHistorySinceJoin(newRoomId, req.getUsername());

            // 5) JoinChatResDto 에 joinAt 필드를 설정
            JoinChatResDto dto = new JoinChatResDto(newRoomId, roomName, members, messages);
            dto.setJoinAt(LocalDateTime.now());
            return dto;

        } catch (SQLException e) {
            e.printStackTrace();
            return new JoinChatResDto(); // 빈 DTO 리턴
        }
    }

    /**
     * 그룹 채팅방 생성 + 바로 입장 정보 반환
     */
    public JoinChatResDto createAndJoinGroup(CreateGroupChatReqDto req) {
        try {
            // 1) 방 생성 (DB 상으로 creator 포함 모든 멤버 삽입됨)
            Long newRoomId = chatDAO.insertGroupRoom(
                    req.getUsername(),
                    req.getChatRoomName(),
                    req.getMembers().stream()
                            .map(MemberInfo::getUsername)
                            .collect(Collectors.toList())
            );

            // 2) 방 이름 조회
            String roomName = chatDAO.selectRoomName(newRoomId);

            // 3) 멤버 정보 조회
            List<MemberInfo> members = chatDAO.selectActiveMembers(newRoomId);

            // 4) 메시지 내역 조회 (생성 직후이므로 빈 리스트)
            List<MessageInfo> messages = chatDAO.selectChatHistorySinceJoin(newRoomId, req.getUsername());

            // 5) JoinChatResDto 에 joinAt 필드를 설정
            JoinChatResDto dto = new JoinChatResDto(newRoomId, roomName, members, messages);
            dto.setJoinAt(LocalDateTime.now());
            return dto;

        } catch (SQLException e) {
            e.printStackTrace();
            return new JoinChatResDto();
        }
    }

    /**
     * 채팅방 입장 (클릭해서 방 들어가기) — 단순히 조회만 수행
     */
    public JoinChatResDto joinChat(JoinChatReqDto req) {
        try {
            Long chatRoomId = req.getChatRoomId();
            String username = req.getUsername();

            // 1) 채팅방 이름 조회
            String roomName = chatDAO.selectRoomName(chatRoomId);

            // 2) 멤버 목록 조회 (joinedAt 포함)
            List<MemberInfo> members = chatDAO.selectActiveMembers(chatRoomId);

            // 3) 메시지 내역 조회 (입장 시점 이후)
            List<MessageInfo> messages = chatDAO.selectChatHistorySinceJoin(chatRoomId, username);

            // 4) JoinChatResDto 에 joinAt 필드를 설정
            JoinChatResDto dto = new JoinChatResDto(chatRoomId, roomName, members, messages);
            dto.setJoinAt(LocalDateTime.now());
            return dto;

        } catch (SQLException e) {
            e.printStackTrace();
            return new JoinChatResDto();
        }
    }

    /**
     * 채팅방 나가기 처리 (1:1/그룹 구분 → 삭제 or soft-delete,
     *                마지막 멤버일 경우 방 전체 삭제)
     */
    public OutChatRoomResDto leaveChatRoom(OutChatRoomReqDto reqDto) throws SQLException {
        Long chatRoomId = reqDto.getChatRoomId();
        String username = reqDto.getUsername();

        // 1) username → userId 조회
        Long userId = chatDAO.getUserIdByUsername(username);

        // 2) 방 유형 조회 (ONE_TO_ONE vs GROUP)
        String roomType = chatDAO.getRoomType(chatRoomId);
        boolean isOneToOne = "ONE_TO_ONE".equals(roomType);

        // 3) 멤버 나가기 처리 (ONE_TO_ONE: soft-delete / GROUP: delete row)
        chatDAO.markMemberLeft(chatRoomId, userId, isOneToOne);

        // 4) 나간 후 남아 있는 활성 멤버 여부 확인
        boolean anyActive = chatDAO.hasActiveMembers(chatRoomId);

        // 5) 응답 DTO 생성
        OutChatRoomResDto res = new OutChatRoomResDto();
        res.setChatRoomId(chatRoomId);
        res.setUsername(username);
        res.setLeftAt(LocalDateTime.now());

        if (!anyActive) {
            // 5-A) 마지막 멤버가 떠난 경우 → 방 전체 삭제
            chatDAO.deleteEntireChatRoom(chatRoomId);
            res.setMembers(new ArrayList<>());
            res.setDeleted(true);
        } else {
            // 5-B) 남은 멤버가 있는 경우 → 남은 멤버 목록 조회
            List<MemberInfo> remaining = chatDAO.selectActiveMembers(chatRoomId);
            res.setMembers(remaining);
            res.setDeleted(false);
        }

        return res;
    }
}
