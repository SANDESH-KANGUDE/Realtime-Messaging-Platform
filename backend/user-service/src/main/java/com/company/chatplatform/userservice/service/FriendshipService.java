package com.company.chatplatform.userservice.service;

import com.company.chatplatform.common.core.exception.ConflictException;
import com.company.chatplatform.common.core.exception.ResourceNotFoundException;
import com.company.chatplatform.common.core.util.UUIDv7Utils;
import com.company.chatplatform.common.kafka.topics.EventTopics;
import com.company.chatplatform.userservice.domain.entity.Friendship;
import com.company.chatplatform.userservice.domain.entity.OutboxEventEntity;
import com.company.chatplatform.userservice.domain.entity.UserProfile;
import com.company.chatplatform.userservice.domain.repository.FriendshipRepository;
import com.company.chatplatform.userservice.domain.repository.OutboxEventRepository;
import com.company.chatplatform.userservice.domain.repository.UserProfileRepository;
import com.company.chatplatform.userservice.dto.FriendshipDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserProfileRepository userProfileRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public FriendshipService(
            FriendshipRepository friendshipRepository,
            UserProfileRepository userProfileRepository,
            OutboxEventRepository outboxEventRepository,
            UserService userService,
            ObjectMapper objectMapper
    ) {
        this.friendshipRepository = friendshipRepository;
        this.userProfileRepository = userProfileRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public FriendshipDto sendFriendRequest(String requesterId, String addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new ConflictException("Cannot send friend request to yourself", "SELF_FRIEND_REQUEST");
        }

        userProfileRepository.findById(addresseeId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found", "USER_NOT_FOUND"));

        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requesterId, addresseeId);
        if (existing.isPresent()) {
            throw new ConflictException("Friendship or request already exists", "FRIENDSHIP_ALREADY_EXISTS");
        }

        String friendshipId = UUIDv7Utils.generateString();
        Friendship friendship = new Friendship(friendshipId, requesterId, addresseeId, "PENDING");
        friendshipRepository.save(friendship);

        // Outbox event for friend request
        try {
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "friendshipId", friendshipId,
                    "requesterId", requesterId,
                    "addresseeId", addresseeId
            ));
            OutboxEventEntity outbox = new OutboxEventEntity(
                    UUIDv7Utils.generateString(),
                    "FRIENDSHIP",
                    friendshipId,
                    EventTopics.FRIEND_REQUEST_SENT,
                    payloadJson
            );
            outboxEventRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }

        UserProfile addresseeProfile = userProfileRepository.findById(addresseeId).orElse(null);
        return new FriendshipDto(
                friendship.getId(),
                friendship.getRequesterId(),
                friendship.getAddresseeId(),
                friendship.getStatus(),
                addresseeProfile != null ? userService.toDto(addresseeProfile) : null,
                friendship.getCreatedAt().toString()
        );
    }

    @Transactional
    public FriendshipDto acceptFriendRequest(String currentUserId, String friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found", "FRIEND_REQUEST_NOT_FOUND"));

        if (!friendship.getAddresseeId().equals(currentUserId)) {
            throw new ConflictException("Only addressee can accept this request", "UNAUTHORIZED_FRIEND_ACCEPT");
        }

        friendship.setStatus("ACCEPTED");
        friendship.setUpdatedAt(Instant.now());
        friendshipRepository.save(friendship);

        // Outbox event for friend request accepted
        try {
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "friendshipId", friendship.getId(),
                    "requesterId", friendship.getRequesterId(),
                    "addresseeId", friendship.getAddresseeId()
            ));
            OutboxEventEntity outbox = new OutboxEventEntity(
                    UUIDv7Utils.generateString(),
                    "FRIENDSHIP",
                    friendship.getId(),
                    "friend.request.accepted.v1",
                    payloadJson
            );
            outboxEventRepository.save(outbox);
        } catch (Exception e) {
            // Log warning but do not roll back transaction
        }

        UserProfile friendProfile = userProfileRepository.findById(friendship.getRequesterId()).orElse(null);
        return new FriendshipDto(
                friendship.getId(),
                friendship.getRequesterId(),
                friendship.getAddresseeId(),
                friendship.getStatus(),
                friendProfile != null ? userService.toDto(friendProfile) : null,
                friendship.getCreatedAt().toString()
        );
    }

    public List<FriendshipDto> getUserFriends(String userId) {
        List<Friendship> friendships = friendshipRepository.findAllByUserIdAndStatus(userId, "ACCEPTED");
        return friendships.stream().map(f -> {
            String friendId = f.getRequesterId().equals(userId) ? f.getAddresseeId() : f.getRequesterId();
            UserProfile friendProfile = userProfileRepository.findById(friendId).orElse(null);
            return new FriendshipDto(
                    f.getId(),
                    f.getRequesterId(),
                    f.getAddresseeId(),
                    f.getStatus(),
                    friendProfile != null ? userService.toDto(friendProfile) : null,
                    f.getCreatedAt().toString()
            );
        }).toList();
    }

    public List<FriendshipDto> getPendingRequests(String userId) {
        List<Friendship> requests = friendshipRepository.findByAddresseeIdAndStatus(userId, "PENDING");
        return requests.stream().map(f -> {
            UserProfile requester = userProfileRepository.findById(f.getRequesterId()).orElse(null);
            return new FriendshipDto(
                    f.getId(),
                    f.getRequesterId(),
                    f.getAddresseeId(),
                    f.getStatus(),
                    requester != null ? userService.toDto(requester) : null,
                    f.getCreatedAt().toString()
            );
        }).toList();
    }
}
