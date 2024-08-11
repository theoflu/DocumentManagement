package com.theoflu.Document.Management.user.FileSigner;

import com.theoflu.Document.Management.user.request.SignReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url ="http://127.0.0.1:5000/", name = "ApiRequests")

public interface ApiReqs {
    @PostMapping("signfile")
     ResponseEntity<?> signfile(@RequestHeader("Authorization") String token, @RequestBody SignReq signReq);
}
