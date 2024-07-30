package com.theoflu.Document.Management.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportReq {
    private String filename;
    private String report_message;
}
