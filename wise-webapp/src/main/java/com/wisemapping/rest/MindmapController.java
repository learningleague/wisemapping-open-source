package com.wisemapping.rest;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindmapUser;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestMindmap;
import com.wisemapping.rest.model.RestMindmapList;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class MindmapController extends BaseController {
    @Autowired
    private MindmapService mindmapService;

    @RequestMapping(method = RequestMethod.GET, value = "/maps/{id}", produces = {"application/json", "text/html", "application/xml"})
    @ResponseBody
    public ModelAndView getMindmap(@PathVariable int id) throws IOException {
        final MindMap mindMap = mindmapService.getMindmapById(id);
        final RestMindmap map = new RestMindmap(mindMap);
        return new ModelAndView("mapView", "map", map);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/maps", produces = {"application/json", "text/html", "application/xml"})
    public ModelAndView getMindmaps() throws IOException {
        final User user = com.wisemapping.security.Utils.getUser();

        final List<MindmapUser> mapsByUser = mindmapService.getMindmapUserByUser(user);
        final List<MindMap> mindmaps = new ArrayList<MindMap>();
        for (MindmapUser mindmapUser : mapsByUser) {
            mindmaps.add(mindmapUser.getMindMap());
        }

        final RestMindmapList restMindmapList = new RestMindmapList(mindmaps);
        return new ModelAndView("mapsView", "list", restMindmapList);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}", consumes = {"application/xml", "application/json"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateMap(@RequestBody RestMindmap restMindmap, @PathVariable int id, @RequestParam(required = false) boolean minor) throws IOException, WiseMappingException {

        final MindMap mindMap = mindmapService.getMindmapById(id);
        final User user = Utils.getUser();

        // Validate arguments ...
        final String properties = restMindmap.getProperties();
        if (properties == null) {
            throw new IllegalArgumentException("Map properties can not be null");
        }
        mindMap.setProperties(properties);

        // Validate content ...
        final String xml = restMindmap.getXml();
        if (xml == null) {
            throw new IllegalArgumentException("Map xml can not be null");
        }
        mindMap.setXmlStr(xml);

        // Update map ...
        updateMindmap(minor, mindMap, user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/maps/{id}/xml", consumes = {"application/xml"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateMapXml(@RequestBody String xml, @PathVariable int id, @RequestParam(required = false) boolean minor) throws IOException, WiseMappingException {

        final MindMap mindMap = mindmapService.getMindmapById(id);
        final User user = Utils.getUser();

        if (xml == null || xml.isEmpty()) {
            throw new IllegalArgumentException("Map xml can not be null");
        }
        mindMap.setXmlStr(xml);

        // Update map ...
        updateMindmap(minor, mindMap, user);
    }


    private void updateMindmap(boolean minor, MindMap mindMap, User user) throws WiseMappingException {
        final Calendar now = Calendar.getInstance();
        mindMap.setLastModificationTime(now);
        mindMap.setLastModifierUser(user.getUsername());
        mindmapService.updateMindmap(mindMap, minor);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/maps", consumes = {"application/xml", "application/json"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createMap(@RequestBody RestMindmap restMindmap, @NotNull HttpServletResponse response) throws IOException, WiseMappingException {

        final String title = restMindmap.getTitle();
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Map title can not be null");
        }

        final String description = restMindmap.getDescription();
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Map details can not be null");
        }

        // Some basic validations ...
        final User user = Utils.getUser();
        final MindMap mindMap = mindmapService.getMindmapByTitle(title, user);
        if (mindMap != null) {
            throw new IllegalArgumentException("Map already exists with title '" + title + "'");
        }

        // If the user has not specified the xml content, add one ...
        final MindMap delegated = restMindmap.getDelegated();
        String xml = restMindmap.getXml();
        if (xml == null || xml.isEmpty()) {
            xml = MindMap.getDefaultMindmapXml(restMindmap.getTitle());
        }
        delegated.setXmlStr(xml);
        delegated.setOwner(user);

        // Add new mindmap ...
        mindmapService.addMindmap(delegated, user);

        // Return the new created map ...
        response.setHeader("Location", "/service/maps/" + delegated.getId());
    }

}
