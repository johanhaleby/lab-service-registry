package com.jayway.serviceregistry.interfaces.rest;

import org.springframework.hateoas.Links;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
public class EntryPointController {

    @RequestMapping(value = "/", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public HttpEntity<Links> entryPoint() {
////        Link selfLink = linkTo(GameEntryPointController.class).withSelfRel();
////        Link createGameLink = linkTo(CreateGameController.class).withRel("create");
////        Link findGameLink = linkTo(FindGameByIdController.class).withRel("find");
////        Link findOngoingGames = linkTo(methodOn(FindOngoingGamesController.class).findOngoingGames()).withRel("ongoing");
//        return new ResponseEntity<>(new Links(selfLink, createGameLink, findGameLink, findOngoingGames), HttpStatus.OK);
        return null;
    }
}
