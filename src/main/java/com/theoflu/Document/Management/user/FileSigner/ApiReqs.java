package com.theoflu.Document.Management.user.FileSigner;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url ="http://127.0.0.1:5000/signfile", name = "ApiRequests")

public interface ApiReqs {

    @PostMapping("signfile")
     ResponseEntity<?> signfile(@RequestHeader("Authorization") String token);
}
