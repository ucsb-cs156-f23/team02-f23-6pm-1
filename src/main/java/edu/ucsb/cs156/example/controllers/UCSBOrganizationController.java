package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name  = "UCSBOrganization")
@RequestMapping("/api/ucsborganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController{
    
    @Autowired
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @Operation(summary= "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganization> allOrganizations() {
        Iterable<UCSBOrganization> organizations = ucsbOrganizationRepository.findAll();
        return organizations;
    }

    @Operation(summary= "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/post")
    public UCSBOrganization getOrganization (
        @Parameter(name="orgCode") @RequestParam String orgCode,
        @Parameter(name="orgTranslationShort") @RequestParam String orgTranslationShort,
        @Parameter(name="orgTranslation") @RequestParam String orgTranslation,
        @Parameter(name="inactive") @RequestParam boolean inactive
        )
        {

        UCSBOrganization organization = new UCSBOrganization();
        organization.setOrgCode(orgCode);
        organization.setOrgTranlationShort(orgTranslationShort);
        organization.setOrgTranslation(orgTranslation);
        organization.setInactive(inactive);
        ucsbOrganizationRepository.save(organization);
        return organization;
    }

    @Operation(summary= "Get a single organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganization getById(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganization orgs = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        return orgs;
    }

    @Operation(summary= "Delete a UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteCommons(
            @Parameter(name="orgCode") @RequestParam String orgCode) {
        UCSBOrganization orgs = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        ucsbOrganizationRepository.delete(orgs);
        return genericMessage("UCSBOrganization with id %s deleted".formatted(orgCode));
    }

    @Operation(summary= "Update a single organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganization updateCommons(
            @Parameter(name="orgCode") @RequestParam String orgCode,
            @RequestBody @Valid UCSBOrganization incoming) {

        UCSBOrganization orgs = ucsbOrganizationRepository.findById(orgCode)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

        orgs.setOrgCode(incoming.getOrgCode());
        orgs.setOrgTranlationShort(incoming.getOrgTranlationShort());
        orgs.setOrgTranslation(incoming.getOrgTranslation());
        orgs.setInactive(incoming.getInactive());

        ucsbOrganizationRepository.save(orgs);

        return orgs;
    }
}
