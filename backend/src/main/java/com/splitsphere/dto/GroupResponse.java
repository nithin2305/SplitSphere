package com.splitsphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;
    private String joinCode;
    private String creatorName;
    private List<String> memberNames;
    private List<MemberDTO> members;
    private LocalDateTime createdAt;
}
