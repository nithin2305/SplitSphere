package com.splitsphere.controller;

import com.splitsphere.dto.GroupRequest;
import com.splitsphere.dto.GroupResponse;
import com.splitsphere.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class GroupController {
    
    private final GroupService groupService;
    
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody GroupRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(groupService.createGroup(request, userId));
    }
    
    @PostMapping("/join/{joinCode}")
    public ResponseEntity<GroupResponse> joinGroup(
            @PathVariable String joinCode,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(groupService.joinGroup(joinCode, userId));
    }
    
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getUserGroups(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(groupService.getUserGroups(userId));
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId));
    }
}
