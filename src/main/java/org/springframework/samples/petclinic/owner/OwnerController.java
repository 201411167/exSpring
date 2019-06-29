/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvyb
 */
@Controller
class OwnerController {

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";
    private final OwnerRepository owners;

    @Autowired
    public OwnerController(OwnerRepository clinicService) {
        this.owners = clinicService;
    }

    @GetMapping("/bean")
    @ResponseBody
    public String bean(){
        return "bean : " + owners;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping("/owners/new")
    public String initCreationForm(Map<String, Object> model) {
        Owner owner = new Owner();
        model.put("owner", owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/owners/new")
    public String processCreationForm(@Valid Owner owner, BindingResult result) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            this.owners.save(owner);
            return "redirect:/owners/" + owner.getId();
        }
    }

    @GetMapping("/owners/find")
    public String initFindForm(Map<String, Object> model) {
        model.put("owner", new Owner());
        return "owners/findOwners";
    }

    @GetMapping("/owners")
    public String processFindForm(Owner owner, BindingResult result, Map<String, Object> model) {


        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        if(owner.getFirstName() == null){
            owner.setFirstName("");
        }

        String firstname = owner.getFirstName();
        String lastname = owner.getLastName();

        // find owners by last name
        Collection<Owner> resultsLastName = this.owners.findByLastName(owner.getLastName());
        Collection<Owner> resultsFirstName = this.owners.findByFirstName(owner.getFirstName());
        Collection<Owner> results = this.owners.findByName(owner.getFirstName(), owner.getLastName());

        if(firstname == "" && lastname == ""){
            // show all owners
            model.put("selections", results);
            return "owners/ownersList";
        }
        if(firstname != "" && lastname != ""){
            // trying to search by firstname and lastname
            if(results.isEmpty()){
                result.rejectValue("lastName", "notFound", "not found");
                return "owners/findOwners";
            }
            else if(results.size() == 1){
                owner = results.iterator().next();
                return "redirect:/owners/" + owner.getId();
            }
            else{
                model.put("selections", results);
                return "owners/ownersList";
            }
        }
        if(firstname == "" && lastname != ""){
            // trying to search by lastname
            if(resultsLastName.isEmpty()){
                result.rejectValue("lastName", "notFound", "not found");
                return "owners/findOwners";
            }
            else if(resultsLastName.size()==1){
                owner = resultsLastName.iterator().next();
                return "redirect:/owners/" + owner.getId();
            }
            else{
                model.put("selections", resultsLastName);
                return "owners/ownersList";
            }
        }
        if(firstname != "" && lastname == ""){
            // trying to search by firstname
            if(resultsFirstName.isEmpty()){
                result.rejectValue("lastName", "notFound", "not found");
                return "owners/findOwners";
            }
            else if(resultsFirstName.size()==1){
                owner = resultsFirstName.iterator().next();
                return "redirect:/owners/" + owner.getId();
            }
            else{
                model.put("selections", resultsFirstName);
                return "owners/ownersList";
            }
        }
        else{
            return "owners/findOwners";
        }
    }

    @GetMapping("/owners/{ownerId}/edit")
    public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner = this.owners.findById(ownerId);
        model.addAttribute(owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/owners/{ownerId}/edit")
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            owner.setId(ownerId);
            this.owners.save(owner);
            return "redirect:/owners/{ownerId}";
        }
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelMap with the model attributes for the view
     */
    @GetMapping("/owners/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
        ModelAndView mav = new ModelAndView("owners/ownerDetails");
        mav.addObject(this.owners.findById(ownerId));
        return mav;
    }

}
