package com.splitsphere.service;

import com.splitsphere.dto.GroupRequest;
import com.splitsphere.dto.GroupResponse;
import com.splitsphere.model.Group;
import com.splitsphere.model.User;
import com.splitsphere.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final UserService userService;
    private final AuditService auditService;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    
    @Transactional
    public GroupResponse createGroup(GroupRequest request, String userId) {
        User creator = userService.getUserByUserId(userId);
        
        Group group = new Group();
        group.setName(request.getName());
        group.setJoinCode(generateUniqueJoinCode());
        group.setCreator(creator);
        group.getMembers().add(creator);
        
        group = groupRepository.save(group);
        
        auditService.log("CREATE", "Group", group.getId(), creator, "Group created: " + group.getName());
        
        return toGroupResponse(group);
    }
    
    @Transactional
    public GroupResponse joinGroup(String joinCode, String userId) {
        User user = userService.getUserByUserId(userId);
        
        Group group = groupRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid join code"));
        
        if (group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }
        
        group.getMembers().add(user);
        group = groupRepository.save(group);
        
        auditService.log("JOIN", "Group", group.getId(), user, "User joined group: " + group.getName());
        
        return toGroupResponse(group);
    }
    
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(String userId) {
        User user = userService.getUserByUserId(userId);
        return groupRepository.findByMembersContaining(user).stream()
                .map(this::toGroupResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return toGroupResponse(group);
    }
    
    private String generateUniqueJoinCode() {
        SecureRandom random = new SecureRandom();
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            code = sb.toString();
        } while (groupRepository.existsByJoinCode(code));
        
        return code;
    }
    
    private GroupResponse toGroupResponse(Group group) {
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setJoinCode(group.getJoinCode());
        response.setCreatorName(group.getCreator().getAccountName());
        response.setMemberNames(group.getMembers().stream()
                .map(User::getAccountName)
                .collect(Collectors.toList()));
        response.setCreatedAt(group.getCreatedAt());
        return response;
    }
}
