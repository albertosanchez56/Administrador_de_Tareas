package com.tareas.taskboard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tareas.taskboard.dto.InvitationResponse;
import com.tareas.taskboard.entity.User;
import com.tareas.taskboard.entity.BoardInvitation;
import com.tareas.taskboard.entity.BoardMembers;
import com.tareas.taskboard.exception.AccessDeniedException;
import com.tareas.taskboard.exception.InvitationNotFoundException;
import com.tareas.taskboard.exception.MemberAlreadyExistsException;
import com.tareas.taskboard.exception.UserNotFoundException;
import com.tareas.taskboard.repository.BoardInvitationRepository;
import com.tareas.taskboard.repository.BoardMemberRepository;
import com.tareas.taskboard.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class InvitationService {

    private final BoardInvitationRepository boardInvitationRepository;
    private final UserRepository userRepository;
    private final BoardMemberRepository boardMemberRepository;

    public InvitationService(BoardInvitationRepository boardInvitationRepository, UserRepository userRepository,
            BoardMemberRepository boardMemberRepository) {
        this.boardInvitationRepository = boardInvitationRepository;
        this.userRepository = userRepository;
        this.boardMemberRepository = boardMemberRepository;
    }

    @Transactional
    public List<InvitationResponse> listPending(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        List<BoardInvitation> invitations = boardInvitationRepository.findByInviteeAndStatus(user,
                BoardInvitation.Status.PENDING);

        return invitations.stream().map(InvitationResponse::from).toList();

    }

    @Transactional
    public InvitationResponse accept(Long invitationId, Long userId) {
        BoardInvitation invitation = boardInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation not found"));

        if (!invitation.getInvitee().getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to accept this invitation");
        }

        if (invitation.getStatus() != BoardInvitation.Status.PENDING) {
            throw new MemberAlreadyExistsException("Invitation is not pending");
        }

        if (boardMemberRepository.existsByBoardAndUser(invitation.getBoard(), invitation.getInvitee())) {
            throw new MemberAlreadyExistsException("User is already a member of the board");
        }

        BoardMembers member = new BoardMembers(
                invitation.getBoard(),
                invitation.getInvitee(),
                BoardMembers.Role.MEMBER);

        boardMemberRepository.save(member);

        invitation.setStatus(BoardInvitation.Status.ACCEPTED);
        boardInvitationRepository.save(invitation);

        return InvitationResponse.from(invitation);

    }

    @Transactional
    public InvitationResponse reject(Long invitationId, Long userId) {
        BoardInvitation invitation = boardInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationNotFoundException("Invitation not found"));

        if (!invitation.getInvitee().getId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to accept this invitation");
        }

        if (invitation.getStatus() != BoardInvitation.Status.PENDING) {
            throw new MemberAlreadyExistsException("Invitation is not pending");
        }

        invitation.setStatus(BoardInvitation.Status.REJECTED);
        boardInvitationRepository.save(invitation);

        return InvitationResponse.from(invitation);
    }
}
