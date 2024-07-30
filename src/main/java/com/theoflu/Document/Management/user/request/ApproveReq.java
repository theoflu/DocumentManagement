package com.theoflu.Document.Management.user.request;


import lombok.Builder;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@Builder
public class ApproveReq {
    private String filename;
    private boolean approve;
}
