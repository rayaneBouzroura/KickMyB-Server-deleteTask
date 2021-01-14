package org.kickmyb.server.tasks;

import org.kickmyb.server.model.MUser;
import org.kickmyb.transfer.*;


import java.util.List;

public interface ServiceCore {

    // entity handling
    TaskDetailResponse detail(Long id, MUser user);
    void addOne(AddTaskRequest req, MUser user) throws Existing;
    List<HomeItemResponse> home(Long userID);

    // Potential web demo for JS injection
    String index();

    MUser userFromUsername(String username);
}