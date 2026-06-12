package com.mayur.distributed_promptforge.intelligence_service;

import com.mayur.distributed_promptforge.common_lib.enums.ProjectPermission;
import com.mayur.distributed_promptforge.intelligence_service.client.WorkspaceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IntelligenceServiceApplicationTests {

    @Autowired
    private WorkspaceClient workspaceClient;

    @Test
    void testCheckPermissionFeignClient() {
    }

}
