package com.theoflu.Document.Management.user.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateTeamReq {
   String team_name;
    List<String> username;
}
