package com.mayur.distributed_promptforge.workspace_service;

import com.mayur.distributed_promptforge.common_lib.security.JwtUserPrincipal;
import com.mayur.distributed_promptforge.common_lib.security.AuthUtil;
import com.mayur.distributed_promptforge.workspace_service.service.ProjectService;
import com.mayur.distributed_promptforge.workspace_service.dto.project.ProjectSummaryResponse;
import com.mayur.distributed_promptforge.workspace_service.repository.ProjectRepository;
import com.mayur.distributed_promptforge.workspace_service.repository.ProjectMemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@SpringBootTest
class WorkspaceServiceApplicationTests {


}
