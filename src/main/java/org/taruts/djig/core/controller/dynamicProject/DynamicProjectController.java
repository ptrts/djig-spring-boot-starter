package org.taruts.djig.core.controller.dynamicProject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.taruts.djig.core.DynamicProject;
import org.taruts.djig.core.DynamicProjectManager;

@ResponseBody
public class DynamicProjectController {

    @Autowired
    private DynamicProjectManager dynamicProjectManager;

    @PutMapping("{projectName}")
    public void put(@PathVariable("projectName") String projectName, @RequestBody DynamicProjectDto dto) {
        DynamicProject dynamicProject = DynamicProjectDtoMapper.map(projectName, dto);
        dynamicProjectManager.addProject(dynamicProject);
    }
}
