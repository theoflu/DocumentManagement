package com.theoflu.Document.Management.user.request;


import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Data
@Builder
public class ApproveReq {
    private String filename;
    private boolean approve;
    private String team_name;
}
